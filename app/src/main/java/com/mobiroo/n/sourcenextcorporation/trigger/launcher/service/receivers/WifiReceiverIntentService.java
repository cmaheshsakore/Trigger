package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.WifiReceiver;

import java.util.ArrayList;

/**
 * Created by krohnjw on 4/7/2014.
 */
public class WifiReceiverIntentService extends BaseIntentService {

    public static final String EXTRA_PENDING_ACTION = "com.tryagent.extra_pending_action";
    public static final String EXTRA_STATE = "com.tryagent.extra_supplicant_state";
    public static final String EXTRA_SSID = "com.tryagent.extra_ssid";

    private final String PREF_WIFI_DISCONNECT_TIME = "prefWifiDisconnectTime";
    private final String PREF_LAST_WIFI_DISCONNECT_SSID = "prefWifiDisconnectSsid";
    private final String PREF_WIFI_CONNECT_TIME = "prefWifiConnectTime";
    private final String PREF_LAST_KNOWN_STATE = "prefLastKnownState";
    private final String PREF_DISCONNECT_CHECK_IN_PROGRESS = "prefDisconnectCheckInProgress";
    private final String PREF_DISCONNECT_TIME = "prefDisconnectTime";

    private final long mDisconnectTimeout = 15 * 1000; // 15s

    private final String COMPLETED = SupplicantState.COMPLETED.toString();
    private final String DISCONNECTED = SupplicantState.DISCONNECTED.toString();

    private final int mDisconnectWaitPeriod = 5 * 1000; // 5s
    private final int mConnectSameSsidTimeout = 90 * 60 * 1000; // 90 minutes

    private final String mActionConnect = "connect";
    private final String mActionDisconnect = "disconnect";



    private String mLastKnownState = "";

    private String mCurrentSsid;
    private String mLastKnownSsid;

    @Override
    protected void onHandleIntent(Intent intent) {

        SupplicantState state = null;

        try {
            state = (SupplicantState) intent.getParcelableExtra(EXTRA_STATE);
        } catch (Exception e) {
            Logger.e("Exception getting supplicant state: " + e);
        }

        if (state == null) { return; }

        int pendingAction = WifiReceiver.STATE_NONE;
        try {
            pendingAction = intent.getIntExtra(EXTRA_PENDING_ACTION, WifiReceiver.STATE_NONE);
        } catch (Exception e) {
            Logger.e("Exception getting state: " + e);
        }

        mLastKnownState = getLastKnownState(WifiReceiverIntentService.this);
        logd(String.format("Pending: %s, State %s, Last %s", pendingAction, state, mLastKnownState));

        if (pendingAction == WifiReceiver.STATE_CONNECTED) {
            // Check to see if we have any matching tasks for this SSID
            mLastKnownSsid = getLastKnownSsid(WifiReceiverIntentService.this);
            mCurrentSsid = intent.hasExtra(EXTRA_SSID) ? intent.getStringExtra(EXTRA_SSID) : getCurrentSsid(this);
            /* Verify we have a valid SSID and continue */
            if (mCurrentSsid == null || mCurrentSsid.isEmpty()) {
                logd("Count not obtain a valid SSID for this connection");
                return;
            }

            checkConnectTasks();
        } else if (pendingAction == WifiReceiver.STATE_DISCONNECTED) {
            // Check to see if we have any disconnect tasks here.
            checkDisconnectTasks(intent);
        }  else {
            logd("Taking no action for state");
            return;
        }

        logd("Service completed.  Removing wake");
        WifiReceiver.completeWakefulIntent(intent);
    }

    private void checkConnectTasks() {
        if (shouldCheckForConnect(WifiReceiverIntentService.this)) {
                /* We have a valid reason for checking, initiate check sequence */
            setDisconnectInProgress(WifiReceiverIntentService.this, false);
            logd("WIFI-C: Checking for connect tasks for " + mCurrentSsid);
            resetTasksRan();
            ArrayList<TaskSet> sets = DatabaseHelper.getWifiTasksForSSID(WifiReceiverIntentService.this, mCurrentSsid, getString(R.string.any_network));
            if ((sets != null) && (sets.size() > 0)) {
                logd("Found " + sets.size() + " matching tasks");
                for (TaskSet set : sets) {
                    Trigger trigger = set.getTrigger(0);
                    Task task = set.getTask(0);
                    if (!set.shouldUse()) {
                        logd("Skipping task");
                        continue;
                    }

                    logd("Ensuring valid restrictions for for " + task.getFullName());

                    try {
                        String condition = trigger.getCondition();
                        if (condition.equals(DatabaseHelper.TRIGGER_ON_CONNECT)) {

                            if (trigger.constraintsSatisfied(WifiReceiverIntentService.this)) {
                                Usage.logTrigger(WifiReceiverIntentService.this, Usage.TRIGGER_WIFI);

                                String id = task.getId();
                                String name = task.getName();

                                String payload = Task.getPayload(WifiReceiverIntentService.this, id, name);

                                logd("WIFI-C: Found matching connect task " + name);
                                logd("WIFI-C: Payload is " + payload);
                                executePayload(WifiReceiverIntentService.this, name, payload, TaskTypeItem.TASK_TYPE_WIFI);
                            }
                        }
                    } catch (Exception e) {
                        Logger.e("WIFI-C: Exception starting scheduled WIFI activity for " + mCurrentSsid, e);
                    }

                }
            } else {
                logd("Found no valid task sets for this ssid");
            }
                /* Log our connection and update timeout */
            logConnect(WifiReceiverIntentService.this, mCurrentSsid, true);
        } else {
            // Make sure we log a connection to save the SSID even if we fail the timeout
            logConnect(WifiReceiverIntentService.this, mCurrentSsid, false);
        }
    }

    private void checkDisconnectTasks(Intent intent) {
        Logger.d("WIFI-D: Intent has SSID of " + intent.getStringExtra(EXTRA_SSID));
        mLastKnownSsid = intent.hasExtra(EXTRA_SSID) ? intent.getStringExtra(EXTRA_SSID) : getLastKnownSsid(WifiReceiverIntentService.this);
        if (mLastKnownSsid == null) {
            mLastKnownSsid = "";
        }
            /* We received another state other than connected.  This could be disconnected, scanning, handshake, etc */
        if (shouldCheckForDisconnect(WifiReceiverIntentService.this)) {
            if (!isDisconnectInProgress(WifiReceiverIntentService.this)) {
                    /* Save current state as "disconnect" in case we get multiple broadcasts */
                logd("WIFI-D: Checking for disconnect from " + mLastKnownSsid);
                setDisconnectInProgress(WifiReceiverIntentService.this, true);
                checkForDisconnectTask(WifiReceiverIntentService.this, mLastKnownSsid);
                logDisconnect(WifiReceiverIntentService.this, mLastKnownSsid, true);
            } else {
                logd("WIFI-D: Skipping this state as another check is already in progress");
            }
        } else {
            logDisconnect(WifiReceiverIntentService.this, mLastKnownSsid, false);
        }
    }

    private String getLastKnownState(Context context) {
        return SettingsHelper.getPrefString(context, PREF_LAST_KNOWN_STATE, "");
    }

    private boolean isDisconnectInProgress(Context context) {
        boolean inProgress = SettingsHelper.getPrefBool(context, PREF_DISCONNECT_CHECK_IN_PROGRESS, false);
        if (inProgress) {
            long start = SettingsHelper.getPrefLong(context, PREF_DISCONNECT_TIME, 0);
            inProgress = ((System.currentTimeMillis() - start) > mDisconnectTimeout) ? false : true;
        }
        return inProgress;
    }

    private void setDisconnectInProgress(Context context, boolean inProgress) {
        SettingsHelper.setPrefBool(context, PREF_DISCONNECT_CHECK_IN_PROGRESS, inProgress);
        if (inProgress) {
            SettingsHelper.setPrefLong(context, PREF_DISCONNECT_TIME, System.currentTimeMillis());
        }
    }

    private boolean checkConnectDiff(long last_time, int timeout) {
        long diff = System.currentTimeMillis() - last_time;
        logd("WIFI-C: Current Time: " + System.currentTimeMillis());
        logd("WIFI-C: Last Time " + last_time);
        logd("WIFI-C: Diff is " + diff);
        logd("WIFI-C: Timeout " + timeout);
        if (diff < timeout) {
            logd("WIFI-DIFF: Ignoring this connection as it is within the timeout period: " + diff);
            return false;
        }
        return true;
    }

    private boolean shouldCheckForConnect(Context context) {
        logd("WIFI-C: Last Known State = " + mLastKnownState + " to " + mLastKnownSsid);

        if (!mLastKnownState.equals(COMPLETED)) {
            /* Our last known state was not completed, this is a valid connect
             * unless we've recently disconnected from this network and are now
             * reconnecting*/

            if (!mCurrentSsid.equals(mLastKnownSsid)) {
                logd("WIFI-C: state mismatch CHECKING");
                return true;
            } else {
                long disconnect_time = getLastDisconnectTime(context);
                long elapsed = System.currentTimeMillis() - disconnect_time;

                if (elapsed > 3000) {
                    logd("WIFI-C: Reconnected to same SSID but timeout has expired.  Last connect was " + elapsed + " ago - CHECKING");
                    return true;
                } else {
                    logd("WIFI-C: Reconnected to same SSID within the timeout period - NOT CHECKING");
                    return false;
                }
            }

        } else if (!mLastKnownSsid.equals(mCurrentSsid)) {
            /* Our last known state was connected, but it was to a different SSID */
            logd("WIFI-C: New SSID connected - CHECKING");
            return true;
        } else if (mLastKnownSsid.equals(mCurrentSsid)) {
            if (checkConnectDiff(getLastConnectTime(context), mConnectSameSsidTimeout)) {
                /* Check time since last connect, it's possible we didn't get a disconnect AND
                 * haven't connected to a new network since then
                 */
                logd("WIFI-C: Last connect time was " + getLastConnectTime(context));
                logd("WIFI-C: Current ssid matches last ssid but connection timeout exceeded CHECKING");
                return true;
            } else {
                /* Timeout has not been exceeded and we are still connected to the same network (but re-receiving a broadcast) timer will be reset in onReceive */
                logd("WIFI-C: Current ssid matches last ssid but we are within the timeout period NOT CHECKING");
                setWifiConnectTime(context);
                return false;
            }
        }

        logd("WIFI-C: Missed all valid checks- NOT CHECKING CONNECT");
        return false;
    }

    private boolean shouldCheckForDisconnect(Context context) {
        logd("WIFI-D: Last Known State = " + mLastKnownState);
        long time = getTimeSinceAction(getLastDisconnectTime(WifiReceiverIntentService.this));
        logd("WIFI-D: Run " + time + " ago");

        if (!mLastKnownState.equals(DISCONNECTED)) {
            logd("WIFI-D: CHECKING DISCONNECT");
            return true;
        } else if (!mLastKnownSsid.equals(getLastDisconnectSsid(context))) {
            /* Our last action was a disconnect, but it may have been from a different network
             * so we need to check the last ssid
             */
            logd("WIFI-D: Last known SSID was " + mLastKnownSsid + " the last ssid disconnected was " + getLastDisconnectSsid(context) + " CHECKING DISCONNECT");
            return true;
        } else if (mLastKnownState.equals(DISCONNECTED) && (time > 30 * 1000)) {
            logd("WIFI-D: Last action was disconnect, but time out exceeded");
            return true;
        }

        logd("WIFI-D: NOT CHECKING DISCONNECT");
        return false;
    }

    public static String getLastKnownSsid(Context context) {
        return SettingsHelper.getPrefString(context, Constants.PREF_LAST_SSID, "");
    }

    public static void clearLastKnownSsid(Context context) {
        Logger.d("Clearing last known ssid");
        SettingsHelper.setPrefString(context, Constants.PREF_LAST_SSID, "");
    }

    private String getLastDisconnectSsid(Context context) {
        return SettingsHelper.getPrefString(context, PREF_LAST_WIFI_DISCONNECT_SSID, "");
    }

    private long getLastConnectTime(Context context) {
        return SettingsHelper.getPrefLong(context, PREF_WIFI_CONNECT_TIME, 0);
    }

    private long getLastDisconnectTime(Context context) {
        return SettingsHelper.getPrefLong(context, PREF_WIFI_DISCONNECT_TIME, 0);
    }

    private long getTimeSinceAction(long time) {
        return System.currentTimeMillis() - time;
    }

    private void setWifiConnectTime(Context context) {
        SettingsHelper.setPrefLong(context, PREF_WIFI_CONNECT_TIME, System.currentTimeMillis());
    }

    private void logConnect(Context context, String ssid, boolean logTime) {
        logd("Saving connect for " + ssid);
        if (!TextUtils.equals(ssid, "<unknown ssid>")) {
            SettingsHelper.setPrefString(context, Constants.PREF_LAST_SSID, ssid);
        }
        SettingsHelper.setPrefString(context, Constants.PREF_LAST_WIFI_ACTION, mActionConnect);
        if (logTime) {
            logd("Setting state to completed");
            SettingsHelper.setPrefString(context, PREF_LAST_KNOWN_STATE, COMPLETED);
            SettingsHelper.setPrefLong(context, PREF_WIFI_CONNECT_TIME, System.currentTimeMillis());
        }
    }

    private void logDisconnect(Context context, String last_ssid, boolean logTime) {
        SettingsHelper.setPrefString(context, Constants.PREF_LAST_WIFI_ACTION, mActionDisconnect);
        SettingsHelper.setPrefString(context, PREF_LAST_WIFI_DISCONNECT_SSID, last_ssid);
        if (logTime) {
            logd("Setting state to disconnected");
            SettingsHelper.setPrefString(context, PREF_LAST_KNOWN_STATE, DISCONNECTED);
            SettingsHelper.setPrefLong(context, PREF_WIFI_DISCONNECT_TIME, System.currentTimeMillis());
        }

    }

    private void checkForDisconnectTask(Context context, String ssid) {
        checkForDisconnectTask(context, ssid, false);
    }

    private boolean checkDisconnectAgainstCurrentConnection(Context context, boolean skipCheck, String ssid) {
        boolean check = true;
        String current = getCurrentSsid(context);

        if (current == null) {
            check = true;
        } else if (ssid.equals(current)) {
            logd("WIFI-D: SSID's match");
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            SupplicantState supplicant_state = wifiManager.getConnectionInfo().getSupplicantState();
            logd("WIFI-D: Current supplicant state is " + supplicant_state);
            check = !(supplicant_state.equals(SupplicantState.COMPLETED));
        }

        return skipCheck || check;
    }

    private void checkForDisconnectTask(Context context, String ssid, boolean skipCheck) {
        logd("WIFI-D: Checking for disconnect task for " + ssid);

        if (context != null) {
            String current = getCurrentSsid(context);

            logd("WIFI-D: Currently connected to " + current);

            if (checkDisconnectAgainstCurrentConnection(context, skipCheck, ssid)) {
                resetTasksRan();
                ArrayList<TaskSet> sets = DatabaseHelper.getWifiTasksForSSID(context, ssid, context.getString(R.string.any_network));
                if ((sets != null) && (sets.size() > 0)) {
                    logd("WIFI-D: Found " + sets.size() + " wifi tasks for " + ssid);
                    for (TaskSet set : sets) {
                        Trigger trigger = set.getTrigger(0);
                        Task task = set.getTask(0);

                        if (!set.shouldUse()) {
                            logd("Skipping task");
                            continue;
                        }

                        logd("Ensuring valid restrictions for for " + task.getFullName());
                        try {
                            String condition = trigger.getCondition();
                            if (condition.equals(DatabaseHelper.TRIGGER_ON_DISCONNECT)) {
                                if (trigger.constraintsSatisfied(context)) {
                                    Usage.logTrigger(context, Usage.TRIGGER_WIFI);
                                    logd("WIFI-D: Found matching disconnect task for " + ssid);
                                    String id = task.getId();
                                    String name = task.getName();
                                    String payload = Task.getPayload(context, id, name);
                                    logd("WIFI-D: Found matching disconnect task " + name);
                                    logd("WIFI-D: Payload is " + payload);
                                    executePayload(context, name, payload, TaskTypeItem.TASK_TYPE_WIFI);
                                }
                            }
                        } catch (Exception e) {
                            Logger.e(Constants.TAG, "WIFI-D: Exception starting scheduled WIFI activity for " + ssid, e);
                        }

                    }
                    clearLastKnownSsid(context);
                }
            }
        }
        setDisconnectInProgress(WifiReceiverIntentService.this, false);
    }

    public static String getCurrentSsid(Context context) {
        return getCurrentSsid((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
    }

    public static String getCurrentSsid(WifiManager manager) {
        String ssid = "";
        try {
            ssid = manager.getConnectionInfo().getSSID().replace("\"", "");
            if (ssid != null && ssid.contains("unknown ssid")) {
                ssid = "";
            }
        } catch (Exception e) {
            Logger.e("Exception getting currently connected SSID", e);
        }

        return (ssid == null) ? "" : ssid;
    }
}
