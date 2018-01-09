package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.ChargingTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PowerReceiver extends TriggerReceiver {

    protected long      DEFAULT_DELAY   = 3000;
    protected boolean   USE_ALARM       = true;

    private static final String PREF_LAST_BAT_PERCENTAGE_SEEN = "prefLastBatteryPercentageSeen";

    public static final IntentFilter POWER_FILTER = new IntentFilter() {
        {
            addAction(Intent.ACTION_BATTERY_CHANGED);
            addAction(Intent.ACTION_POWER_CONNECTED);
            addAction(Intent.ACTION_POWER_DISCONNECTED);
        }
    };

    private static final int TREND_DOWN = 0;
    private static final int TREND_UP = 1;

    private int mChargingStatus;
    private int mChargingType;

    private float mScaledLevel;
    private float mLastLevelSeen;

    private int mTrend;


    private final String PREF_CHARGING_STATE = "com.gettrigger.chargingstate";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!IabClient.checkLocalUnlockOrTrial(context)) {
            Logger.d("User is not authorized for this feature");
            return;
        }

        String action = intent.getAction();
        Logger.d("POWER: Action is " + action);
        resetTasksRan();
        if (BatteryTrigger.isEnabled(context) &&
                Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            getScaledLevelFromIntent(context, intent);

            mLastLevelSeen = getLastLevelSeen(context);

            if ((mScaledLevel > 0) && (mScaledLevel != mLastLevelSeen)) {

                setTrend();
                setLevel(context, mScaledLevel);

                Logger.d("BATTERY: Current scaled level = " + mScaledLevel);
                Logger.d("BATTERY: Trend = " + mTrend);

                checkForBatteryTasks(context);
            }
        }
        if ((Intent.ACTION_POWER_CONNECTED.equals(action)
            || Intent.ACTION_POWER_DISCONNECTED.equals(action))) {
            String previous_state = SettingsHelper.getPrefString(context, PREF_CHARGING_STATE, "");

            SettingsHelper.setPrefString(context, PREF_CHARGING_STATE, action);

            if ((ChargingTrigger.isEnabled(context)) &&
                ((previous_state.isEmpty()) || (!previous_state.equals(action)))) {

                getChargingDataFromIntent(intent);

                if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                    if (mChargingType == -1) {
                        final Handler handler = new Handler();
                        Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            public void run() {
                                handler.post(new Runnable() {
                                    public void run() {
                                        checkConnectStats(context);
                                    }
                                });
                            }
                        }, 3000);


                    }
                    // Sleep for 3s and then query state
                } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                    checkForCharingTasksForType(context, intent.getAction(), -1);
                }

            }
        }
    }

    private void checkConnectStats(Context context) {
        Logger.d("CHARGING: Re-checking power data");
        Intent stats = context.registerReceiver(null, POWER_FILTER);
        if (stats != null) {
            getChargingDataFromIntent(stats);

            if (mChargingType != -1) {
              checkForCharingTasksForType(context, Intent.ACTION_POWER_CONNECTED, mChargingType);
            }
        }
    }

    private void checkForCharingTasksForType(Context context, String action, int type) {
        String desired_condition = getConditionFromType(type);
        String alt_condition = desired_condition;

        // Allow USB and AC plugs to also trigger basic charger_on
        if (desired_condition.equals(DatabaseHelper.TRIGGER_CHARGER_ON_USB) || desired_condition.equals(DatabaseHelper.TRIGGER_CHARGER_ON_AC) || desired_condition.equals(DatabaseHelper.TRIGGER_WIRELESS_CHARGER)) {
            alt_condition = DatabaseHelper.TRIGGER_CHARGER_ON;
        }

        ArrayList<TaskSet> sets = DatabaseHelper.getChargerTasks(context);
        if ((sets != null) && (sets.size() > 0)) {
            for (TaskSet set:  sets) {
                Trigger trigger = set.getTrigger(0);
                Task task = set.getTask(0);
                if (set.shouldUse()) {
                    String trigger_condition = trigger.getCondition();
                    Logger.d("CHARGING: checking " + trigger_condition + " against " + desired_condition + " and " + alt_condition);
                    if (trigger_condition.equals(desired_condition) || trigger_condition.equals(alt_condition)) {
                        if (trigger.constraintsSatisfied(context)) {
                            Usage.logTrigger(context, Usage.TRIGGER_CHARGER);
                            String id = task.getId();
                            String name = task.getName();

                            Logger.d("CHARGING: Got " + id + ", " + name);

                            String payload = Task.getPayload(context, id, name);

                            Logger.d("CHARGING: Running task " + name);
                            Logger.d("CHARGING: Payload is " + payload);

                            executePayload(context, name, payload, TaskTypeItem.TASK_TYPE_CHARGER);
                        }
                    }
                }
            }
        } else {
            Logger.d("CHARGING: No matching tasks found");
        }
    }

    private String getConditionFromType(int type) {
        switch(type) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return DatabaseHelper.TRIGGER_CHARGER_ON_AC;
            case BatteryManager.BATTERY_PLUGGED_USB:
                return DatabaseHelper.TRIGGER_CHARGER_ON_USB;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return DatabaseHelper.TRIGGER_WIRELESS_CHARGER;
            default:
                return DatabaseHelper.TRIGGER_CHARGER_OFF;
        }
    }

    private void getChargingDataFromIntent(Intent intent) {
        mChargingStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        mChargingType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        Logger.d("CHARGING: Status is " + mChargingStatus);
        Logger.d("CHARGING: Type is " + mChargingType);

    }

    private void getScaledLevelFromIntent(Context context, Intent intent) {
        int level = intent.getIntExtra("level", -1);
        double scale = intent.getIntExtra("scale", -1);

        if ((level == -1) || (scale == -1)) {
            Intent batt = context.getApplicationContext().registerReceiver(null, POWER_FILTER);
            if (batt != null) {
                level = batt.getIntExtra("level", 0);
                scale = batt.getIntExtra("scale", -1);
            }
        }

        mScaledLevel = level;

        if (scale > 0) {
            mScaledLevel = (level / (float) scale) * 100;
        }

    }

    private void checkForBatteryTasks(Context context) {
        // Check for any tasks that match this percentage

        ArrayList<TaskSet> sets = DatabaseHelper.getBatteryTasksForLevel(context, (int) mScaledLevel);
        if ((sets != null) && (sets.size() > 0)) {
            for (TaskSet set : sets) {
                Trigger trigger = set.getTrigger(0);
                Task task = set.getTask(0);
                if (set.shouldUse()) {
                    String condition = trigger.getCondition();
                    String desired_condition = (mTrend == TREND_DOWN) ? DatabaseHelper.TRIGGER_BATTERY_GOES_BELOW : DatabaseHelper.TRIGGER_BATTERY_GOES_ABOVE;

                    if (desired_condition.equals(condition)) {
                        if (trigger.constraintsSatisfied(context)) {
                            // We have a matching battery level entry.  Build task and dispatch
                            Usage.logTrigger(context, Usage.TRIGGER_BATTERY);
                            String id = task.getId();
                            String name = task.getName();

                            Logger.d("BATTERY: Got " + id + ", " + name);

                            String payload = Task.getPayload(context, id, name);

                            Logger.d("BATTERY: Running task " + name);
                            Logger.d("BATTERY: Payload is " + payload);


                            executePayload(context, name, payload, TaskTypeItem.TASK_TYPE_BATTERY);
                        }
                    }
                }
            }
        }
    }

    private float getLastLevelSeen(Context context) {
        return SettingsHelper.getPrefFloat(context, PREF_LAST_BAT_PERCENTAGE_SEEN, 0);
    }

    private void setLevel(Context context, float value) {
        SettingsHelper.setPrefFloat(context, PREF_LAST_BAT_PERCENTAGE_SEEN, value);
    }

    private void setTrend() {
        mTrend = (mScaledLevel < mLastLevelSeen) ? TREND_DOWN : TREND_UP;
    }


}
