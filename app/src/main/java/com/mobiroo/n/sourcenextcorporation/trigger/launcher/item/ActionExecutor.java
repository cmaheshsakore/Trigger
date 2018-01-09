package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.*;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.TtsWorker;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.BluetoothDeviceDisconnectAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.ForgetWifiNetworkAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.TimerAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ActionService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.widget.WidgetLarge;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by krohnjw on 9/17/2014.
 */
public class ActionExecutor {

    public static final String EXTRA_TAG_NAME = "com.trigger.launcher.TagName";
    public static final String EXTRA_TAG_ID = "com.trigger.launcher.TagId";
    public static final String EXTRA_ALT_TAG_NAME = "com.trigger.launcher.AltTagName";
    public static final String EXTRA_ALT_TAG_ID = "com.trigger.launcher.AltTagId";
    public static final String EXTRA_PAYLOAD = "LoadedPayload";
    public static final String EXTRA_START_POSITION = "startPosition";

    public static final String ENTRY_STRING = "parser";

    private final int   REQUEST_RESTART_PROCESSING  = 1001;
    private final int   ALARM_RESUME_PROCESSING     = 9000;


    private class SessionData {
        // Variables used to store widget and message content (displayed when tasks are executed)
        public String taskName;
        public String taskActions;

        public boolean playedSound;
        public int lastKnownWifiState = WifiManager.WIFI_STATE_UNKNOWN;
        public int lastKnownBluetoothState = BluetoothAdapter.ERROR;
        public List<String> groupedActions;
        public List<String> notifications;
        public boolean     sentNameForDisplay;
        public int         numIterations;
        public boolean     handleWhenReady;
        public boolean     resumeIndexIsCurrentAction = false;
        public PendingIntent resumeIntent;
        private Context context;
        public boolean     needManualReset = false;  // Used to control reset when waiting for async process to complete
        public boolean     needReset = false;             // Used to control reset when waiting for async process to complete
        public String[]    pendingArgs = null;   // Stores command set to be executed on resume
        public int         pendingStartPos = -1;      // stores position to start at when resuming
        public String      pendingTask = "";  // Stores payload to be processed
        public int         contextGroup = 0;  // Used to track grouping of actionsl
        public boolean bluetoothReceiverRegistered = false;
        public boolean wifiReceiverRegistered = false;
        public SessionData() {
            needReset = false;
            needManualReset = false;
            notifications = new ArrayList<String>();
            groupedActions = new ArrayList<String>();

        }

    }

    private SessionData mSession;

    // Variables used to manage Notification bar messages
    private NotificationManagerCompat   mNotificationManager;
    private Notification                mNotification;
    private final int                   NID = 598632158;

    private void logd(String message) {
        Logger.d("ActionExecutor: " + message);
    }

    public ActionExecutor(Context context) {
        mSession = new SessionData();
        mSession.context = context;
        mNotificationManager = NotificationManagerCompat.from(context);
    }

    public void process(Intent intent) {

        if (mSession == null) {
            logd("Session data is empty - returning");
        }

        // Restarts happen here!
        SettingsHelper.setPrefString(mSession.context, Constants.PREF_LAST_ENTRY, ENTRY_STRING);

        /* Will Receive a payload and name from ParserService */
        processIntent(intent);
    }

    private void unregisterWifiReceiver() {
        if (mSession.wifiReceiverRegistered)
            try {
                mSession.context.unregisterReceiver(WifiStateChangedReceiver);
            } catch (Exception e) {
                /* ignore exception */
            }
    }

    private void unregisterBluetoothReceiver() {
        if (mSession.bluetoothReceiverRegistered)
            try {
                mSession.context.unregisterReceiver(BluetoothStateChangedReceiver);
            } catch (Exception e) {
                /* ignore exception */
            }
    }

    public void unregisterReceivers() {
        logd("Unregistering receivers");
        unregisterWifiReceiver();
        unregisterBluetoothReceiver();
    }

    private void registerWifiReceiver() {
        logd("Registering Wifi receiver");

        try {
            mSession.context.registerReceiver(WifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            mSession.wifiReceiverRegistered= true;
        } catch (Exception e) {
            Logger.e("Exception registering wifi receiver", e);
        }
    }

    private void registerBluetoothReceiver() {
        try {
            logd("Registering Bluetooth Receiver");
            mSession.context.registerReceiver(BluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            mSession.bluetoothReceiverRegistered = true;
        } catch (Exception e) {
            Logger.e("Exception registering bluetooth receiver");
        }
    }

    private BroadcastReceiver BluetoothStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (mSession.handleWhenReady) {
                mSession.numIterations++;

                int extraBTState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                logd("Got Bluetooth State " + extraBTState + ", Last known state is " + mSession.lastKnownBluetoothState);
                if (extraBTState == BluetoothAdapter.STATE_ON || extraBTState == BluetoothAdapter.STATE_OFF) {
                    if (extraBTState != mSession.lastKnownBluetoothState) {
                        if ((mSession.pendingStartPos >= 0) && (mSession.pendingArgs != null) && (!mSession.needManualReset)) {
                            restartProcessing();
                        }
                    }
                }

                // VERSION_HACK 4.2.2 bug leaves us infinitely looping here.  Don't exceed 10 iterations
                if (mSession.numIterations > 9) {
                    if ((mSession.pendingStartPos >= 0) && (mSession.pendingArgs != null) && (!mSession.needManualReset)) {
                        restartProcessing();
                    }
                }
            }
        }
    };

    private BroadcastReceiver AirplaneModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mSession.handleWhenReady) {

                mSession.numIterations++;
                if ((mSession.pendingStartPos >= 0) && (mSession.pendingArgs != null) && (!mSession.needManualReset)) {
                    restartProcessing();
                }

                // VERSION_HACK 4.2.2 bug leaves us infinitely looping here.  Don't exceed 10 iterations
                if (mSession.numIterations > 9) {
                    if ((mSession.pendingStartPos >= 0) && (mSession.pendingArgs != null) && (!mSession.needManualReset)) {
                        restartProcessing();
                    }
                }
            }
        }
    };

    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            logd("Got Wifi State " + extraWifiState + ", Last known state is " + mSession.lastKnownWifiState);
            if ((extraWifiState == WifiManager.WIFI_STATE_ENABLED) || (extraWifiState == WifiManager.WIFI_STATE_DISABLED)) {
                if (extraWifiState != mSession.lastKnownWifiState) {
                    if (mSession.handleWhenReady) {
                        if ((mSession.pendingStartPos >= 0) && (mSession.pendingArgs != null) && (!mSession.needManualReset)) {
                            restartProcessing();
                        }
                    }
                }
            }
        }
    };

    /**
     * Takes incoming intent, looks for NDEF messages to send to parser.
     * @param intent intent to be processed
     */
    private void processIntent(Intent intent) {

        mSession.taskActions = "";
        mSession.playedSound = false;
        mSession.pendingTask = intent.getStringExtra(EXTRA_PAYLOAD);
        mSession.taskName = Utils.decodeData(intent.getStringExtra(EXTRA_TAG_NAME));
        mSession.pendingStartPos = intent.getIntExtra(EXTRA_START_POSITION, 0);


        checkReturnErrors(intent);

        if ((mSession.pendingTask == null) || (mSession.pendingTask.isEmpty())) {
            logd("Received empty task - taking no action");
            return;
        }

        logd("Start position:" + mSession.pendingStartPos);

        if (Usage.canLogData(mSession.context) && (mSession.pendingStartPos == 0)) {
            // Don't re-log ran on a restart from within a task
            Usage.logAppRan(mSession.context);
            Usage.logInstallDate(mSession.context);
        }

        processPayload(mSession.pendingTask);
    }

    private void checkReturnErrors(Intent intent) {
        if ((intent != null) && (intent.hasExtra(TtsWorker.EXTRA_TTS_RESULT))) {
            Toast.makeText(mSession.context, intent.getStringExtra(TtsWorker.EXTRA_TTS_RESULT), Toast.LENGTH_LONG).show();
        }
    }

    private void processPayload(String recordText) {

        // Show tag name if we have it
        if ((mSession.taskName != null) && (!mSession.taskName.isEmpty())) {
            addActionToDisplay(mSession.taskName);
            mSession.sentNameForDisplay = true;
            sendUserNotification(mSession.taskName);
        } else {
            sendUserNotification(mSession.context.getString(R.string.found_tag));
        }

        // Split tasks into an array of commands
        String[] Args = mSession.pendingTask.split(";");
        logd("Processing " + Args.length + " args");
        logd("Payload: " + recordText);

        mSession.pendingArgs = null; // Initialize

        // Log number of actions run and total run count
        if (mSession.pendingStartPos == 0) {
            // Don't re-log ran on a restart from within a task
            Usage.saveActionUsage(mSession.context, Args.length - 1);
        }

        // Vibrate or play tag found sound for user.
        if (!mSession.playedSound && (mSession.pendingStartPos == 0)) {
            logd("AS: Start positon is " + mSession.pendingStartPos + " vibrating and notifying");
            doVibrate();
            doSound();
        }

        parseTagCommandSet(Args, mSession.pendingStartPos); // Parse all commands stored on tag starting at 0 element
    }

    /**
     * Parse incoming tags starting at supplied position and perform the described actions
     * @param Args arg set to be parsed
     * @param startPos position within arg set to start at (default 0)
     */
    private void parseTagCommandSet(String[] Args, int startPos) {

        if (mSession.contextGroup == 0) {
            mSession.contextGroup = Usage.getNextID(mSession.context);
        }

        // Disable any pending broadcast receivers before resuming / starting
        // processing
        unregisterWifiReceiver();
        unregisterBluetoothReceiver();

        mSession.pendingArgs = Args; // store a subset of arguments to be parsed. Start processing at passed in position
        for (int j = startPos; j < Args.length; j++) {
            String[] ArgArr = Args[j].split(":"); // Each command set is
            // delimited on ':'
            mSession.pendingStartPos = j; // Store our pending start position as current
            // by default
            logd("Got " + Args[j]);

            String command = Constants.COMMAND_NO_ACTION;

            // Verify we have at least two arguments (Operation:Target)
            if (ArgArr.length >= 2) {
                // Handle all ENABLE commands
                int operation = Constants.OPERATION_ENABLE;
                com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.Action action;

                if (ArgArr[0].equals(Constants.COMMAND_ENABLE) || ArgArr[0].equals(Constants.COMMAND_DISABLE) || ArgArr[0].equals(Constants.COMMAND_TOGGLE)) {
                    String module = ArgArr[1]; // Primary Arg is the adapter/interface to enable
                    // Try to get an action from mSession.context module
                    action = BaseAction.getAction(BaseAction.getCodeFromCommand(module));
                    if (action.getName() != null) {
                        logd("Got " + action.getName());
                        // We have a valid action
                        if (ArgArr[0].equals(Constants.COMMAND_DISABLE)) {
                            command = Constants.COMMAND_DISABLE;
                            operation = Constants.OPERATION_DISABLE;
                        } else if (ArgArr[0].equals(Constants.COMMAND_TOGGLE)){
                            command = Constants.COMMAND_TOGGLE;
                            operation = Constants.OPERATION_TOGGLE;
                        } else {
                            command = Constants.COMMAND_ENABLE;
                        }

                    }
                } else {
                    // These should all start with COMMAND:ARGS
                    action = BaseAction.getAction(BaseAction.getCodeFromCommand(ArgArr[0]));

                }

                // Ensure we've found a valid action that's been defined and not the default baseaction
                if (action.getName() != null) {

                    /* See if we have a pending watchdog timer running if so cancel it */
                    cancelWatchdog();

                    /* Check for special cases in which we need to set some variables */
                    if (action instanceof BluetoothAdapterAction) {
                        mSession.lastKnownBluetoothState = ((BluetoothAdapterAction) action).getCurrentState();
                        registerBluetoothReceiver();
                    } else if (action instanceof WifiAdapterAction) {
                        mSession.lastKnownWifiState = ((WifiAdapterAction) action).getCurrentState(mSession.context);
                        registerWifiReceiver();
                    } else if (action instanceof ConfigureSsidAction) {
                        mSession.lastKnownWifiState = ((ConfigureSsidAction) action).getCurrentState(mSession.context);
                        registerWifiReceiver();
                    }  else if (action instanceof AirplaneModeAction) {
                        ((AirplaneModeAction) action).setReceiver(AirplaneModeChangedReceiver);
                    } else if (action instanceof TimerAction) {
                        ((TimerAction) action).setTimerName(mSession.taskName);
                    } else if (action instanceof WifiHotspotAction) {
                        mSession.lastKnownWifiState = ((WifiHotspotAction) action).getCurrentState(mSession.context);
                        registerWifiReceiver();
                    } else if (action instanceof BluetoothDeviceConnectAction) {
                        mSession.lastKnownBluetoothState = ((BluetoothDeviceConnectAction) action).getCurrentState();
                        registerBluetoothReceiver();
                    } else if (action instanceof BluetoothDeviceDisconnectAction) {
                        mSession.lastKnownBluetoothState = ((BluetoothDeviceDisconnectAction) action).getCurrentState();
                        registerBluetoothReceiver();
                    } else if (action instanceof ForgetWifiNetworkAction) {
                        mSession.lastKnownWifiState = ((ForgetWifiNetworkAction) action).getCurrentState(mSession.context);
                        registerWifiReceiver();
                    }


                    /*
                     * If we haven't explicitly skipped performing mSession.context action go ahead and set
                     * widget text, toast text and call performAction on object
                     */
                    //if (true) {
                    mSession.handleWhenReady = true;
                    action.setArgs(ArgArr);
                    addActionToDisplay(action.getNotificationText(mSession.context, operation));
                    addWidgetText(action.getWidgetText(mSession.context, operation));

                    // Log actions
                    action.logUsage(mSession.context, command, mSession.contextGroup);
                    mSession.groupedActions.add(action.getCode());

                    // Actions can modify start pos internally if needed.
                    action.setResumeData(mSession.pendingTask, mSession.pendingStartPos, mSession.taskName);

                    // Perform action
                    action.performAction(mSession.context, operation, ArgArr, j);

                    /// Set up potential restart if needed
                    mSession.pendingStartPos = action.getResumeIndex();
                    mSession.needReset = action.needReset();
                    mSession.needManualReset = action.needManualRestart();

                    if (mSession.needManualReset) {
                        if (mSession.pendingStartPos < mSession.pendingArgs.length) {
                            logd("Manually restarting in " + action.getRestartDelay());
                            scheduleSpecialRestart(action.getRestartDelay(), mSession.pendingStartPos);
                            // Restart is manual, cancel any existing watchdog that has been set up
                            cancelWatchdog();
                        }

                    } else {
                            /* mSession.context is an auto restart, check if we want a watchdog running */
                        if (action.scheduleWatchdog()) {
                                /* Set up mTimer to resume processing in 5 seconds in case something in the broadcast receiver
                                 * or resume method fails
                                 */
                            logd("Scheduling watchdog to resume");
                            Calendar now = Calendar.getInstance();
                            now.add(Calendar.SECOND, 5);

                            PendingIntent pending = buildResumePendingIntent(new Intent(mSession.context, ActionService.class));
                            AlarmManager manager = (AlarmManager) mSession.context.getSystemService(Context.ALARM_SERVICE);
                            manager.set(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), pending);
                        } else {
                            // Ensure we don't have an outstanding watchdog alarm
                            cancelWatchdog();
                        }
                    }
                    //}
                } else {
                    mSession.needReset = false;
                    mSession.needManualReset = false;
                }
            }

            logd("Done: reset = " + mSession.needReset + ", Next arg = " + mSession.pendingStartPos);
            if ((mSession.needReset) && (mSession.pendingStartPos < Args.length)) {
                // Break out if we need a reset after any single operation
                // The necessary broadcast receiver or time will pick back
                // up in the appropriate location
                logd("Breaking");
                break;
            }
        }  // End For loop

        if (!((mSession.needReset) && (mSession.pendingStartPos < Args.length))) {
            if (mSession.groupedActions.size() > 0) {
                // Log mSession.context aggregate data
                Collections.sort(mSession.groupedActions);
                List<String> nameList = new ArrayList<String>();
                for (String action : mSession.groupedActions) {
                    nameList.add(Usage.getNameFromCode(action));
                }
                Usage.storeAggregateTuple(mSession.context, Constants.USAGE_CAGETORY_GROUPING, TextUtils.join(",", nameList.toArray()), Constants.COMMAND_NO_ACTION, mSession.contextGroup);
            }
            sendUserNotifications();
            // Finish out in non resumeable state otherwise
            mSession.pendingStartPos = -1;
            logd("Main: Finishing");
            unregisterReceivers();
            return;
        }
    }


    private PendingIntent buildResumePendingIntent(Intent intent) {
        if (mSession.resumeIntent == null) {
            mSession.resumeIntent = buildResumePendingIntent(intent, mSession.pendingStartPos, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mSession.resumeIntent;
    }

    private PendingIntent buildResumePendingIntent(Intent intent, int position, int flag) {
        logd("Building intent for " + mSession.taskName + " at position " + position);
        intent.putExtra(EXTRA_TAG_NAME, mSession.taskName);
        intent.putExtra(EXTRA_PAYLOAD, mSession.pendingTask);
        intent.putExtra(EXTRA_START_POSITION, position);
        intent.setAction(mSession.taskName);
        return PendingIntent.getService(mSession.context, ALARM_RESUME_PROCESSING, intent, flag);
    }

    /**
     * Restarts processing where we have left off based on class vars
     */
    private void restartProcessing() {
        mSession.handleWhenReady = false;

        // Kill Watchdog on restart
        cancelWatchdog();

        mSession.needManualReset = false;

        /* If we are being caught by the watchdog and resuming at the current action
         * that triggered the watchdog we want to increment the index to avoid a potential
         * infinite loop (current -> watchdog -> restart current -> watchdog.....
         */
        if (mSession.resumeIndexIsCurrentAction) {
            if (mSession.pendingStartPos >= 0) {
                logd("Incrementing restart position (" + mSession.pendingStartPos + " to skip current action (" + (mSession.pendingStartPos + 1) + ")");
                mSession.pendingStartPos = mSession.pendingStartPos + 1;
            }
        }


        if ((mSession.pendingStartPos >= 0) && (mSession.pendingArgs != null)) {
            logd("Calling restart at " + mSession.pendingStartPos);
            mSession.needReset = false;
            logd("Restart Procressing with pending args " + mSession.pendingArgs.length + " at position " + mSession.pendingStartPos);
            parseTagCommandSet(mSession.pendingArgs, mSession.pendingStartPos);
            logd("Done with pending args - widget text is " + mSession.taskActions);
        }
    }

    /**
     * Shows a message via toast if appropriate
     * @param message message to display
     */
    private void addActionToDisplay(String message) {
        if (!mSession.sentNameForDisplay) {
            showToast(message);
        }
    }

    /**
     * Adds a piece of text to pending notifications if it is not the last message in the list (prevent duplicates on restart)
     * @param message message to display
     */
    private void addWidgetText(String message) {
        if (message != null) {
            if (mSession.notifications.size() > 0) {
                if (!message.equals(mSession.notifications.get(mSession.notifications.size() -1))) {
                    mSession.notifications.add(message);
                }
            } else {
                mSession.notifications.add(message);
            }
        }
    }

    /**
     * Optionally shows a toast (if user preference is enabled)
     * @param message message to display
     */
    private void showToast(String message) {
        if (SettingsHelper.shouldShowToast(mSession.context)) {
            Toast.makeText(mSession.context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send updated data to widget for display using current values for name / notifications.
     */
    private void sendUserNotifications() {
        logd("Sending widget text");

        // If we pulled a name from the tag prefer that, else use built actions to mSession.context point
        if ((mSession.taskName != null) && (!mSession.taskName.isEmpty())) {
            if (!mSession.sentNameForDisplay) {
                //mSession.sentNameForDisplay = true;
                sendUserNotification(mSession.taskName);
            }
        } else {
            sendUserNotification(TextUtils.join(", ", mSession.notifications));
        }

    }

    /**
     * Sends updated data to the widget for display.  Allows explicit exclusion of widget and explicit text to be set.
     * @param text text to display
     */
    @SuppressLint("InlinedApi")
    private void sendUserNotification(String text) {
        if ((text != null) && (!text.isEmpty())) {

            // Trim leading comma if exists
            if (text.substring(0, 1).equals(",")) {
                text = text.substring(1);
            }

            // Trim any whitespace
            text = text.trim();

            // Broadcast updates to widget provider
            Intent widgetIntent = new Intent(mSession.context, WidgetLarge.class);
            SettingsHelper.setPrefString(mSession.context, Constants.PREF_WIDGET_LAST_TEXT, text);
            SettingsHelper.setPrefString(mSession.context, Constants.PREF_WIDGET_LAST_TIME, Utils.toGMTString(new Date()));
            widgetIntent.putExtra("tag_widget_update_text", text);
            mSession.context.sendBroadcast(widgetIntent);

            // Try to notify DashClock
            boolean dash_clock_present = false;
            try {
                mSession.context.getPackageManager().getPackageInfo("net.nurik.roman.dashclock", PackageManager.GET_ACTIVITIES);
                dash_clock_present = true;
            } catch (Exception ignored) { }

            if (dash_clock_present) {
                try { mSession.context.getContentResolver().notifyChange(Uri.parse("nfctl://update/dashclock"), null); }
                catch (Exception e) { Logger.e("Exception with dash clock intent", e); }
            }


            // Also update the status bar notification if enabled
            if (mSession.context == null) {
                return;
            }

            if (SettingsHelper.shouldShowNotification(mSession.context)) {
                if (mNotificationManager == null) {
                    mNotificationManager = NotificationManagerCompat.from(mSession.context);
                }
                mNotificationManager.cancel(NID);

                Intent notificationIntent = new Intent(mSession.context, MainActivity.class);

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT > 10) {
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                notificationIntent.putExtra("tag", "fragment-my-tasks");

                PendingIntent contentIntent = PendingIntent.getActivity(mSession.context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

                long when = System.currentTimeMillis(); // notification time

                try {
                    mNotificationManager.cancel(NID);

                    String notifications = TextUtils.join(",", mSession.notifications);

                    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
                    bigStyle.setSummaryText(text);
                    bigStyle.bigText(notifications);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mSession.context);
                    builder.setContentText(text);
                    builder.setContentTitle(mSession.context.getString(R.string.found_tag));
                    builder.setLargeIcon(BitmapFactory.decodeResource(mSession.context.getResources(), R.drawable.icon_notification_large));
                    builder.setSmallIcon(R.drawable.icon_notification_large);
                    builder.setAutoCancel(true);
                    builder.setWhen(when);
                    builder.setContentIntent(contentIntent);
                    builder.setTicker(text);
                    //builder.setStyle(bigStyle);


                    mNotification = builder.build();

                    mNotificationManager.notify(NID, mNotification);
                } catch (Exception e) {
                    Logger.e("Exception dispatching notification", e);
                }
            }
        }
    }

    /**
     * Used to vibrate the device when a tag is read
     */
    private void doVibrate() {
        if (SettingsHelper.shouldVibrate(mSession.context)) {
            logd("AS: Vibrating for " + Constants.VIBRATE_LENGTH + "ms");
            Vibrator v = (Vibrator) mSession.context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(Constants.VIBRATE_LENGTH);
        }
    }

    /**
     * Plays a sound when a tag is read if the preference is set and API level is Gingerbread
     */
    private void doSound() {

        if (SettingsHelper.shouldPlaySound(mSession.context)) {
            try {
                String uriString = SettingsHelper.getPrefString(mSession.context, "prefNotificationURI");
                if (uriString != null) {
                    Uri alert = Uri.parse(uriString);
                    SoundPlayer player = new SoundPlayer(mSession.context, alert);
                    player.execute();
                }
            } catch (Exception ignored) {}
        }
    }



    /**
     * Used to play a notification when a tag is read
     * @author krohnjw
     *
     */
    private class SoundPlayer extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private Uri mUri;

        public SoundPlayer(Context context, Uri uri) {
            mContext = context;
            mUri = uri;
        }

        @Override
        protected Void doInBackground(Void... args) {

            if ((SettingsHelper.shouldPlaySound(mContext) && (mUri != null))) {
                try {
                    MediaPlayer mp = MediaPlayer.create(mContext, mUri);
                    mp.start();
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception playing notification sound", e);
                }

            }
            return null;

        }

    }

    /**
     * Allows a restart to be manually schedule after N second
     * @param seconds time until restart (Seconds)
     * @param nextPosition position to restart at
     */
    private void scheduleSpecialRestart(int seconds, int nextPosition) {
        mSession.needManualReset = true;
        mSession.pendingStartPos = nextPosition;
        mSession.needReset = true;

        Intent intent = new Intent(mSession.context, ActionService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_PAYLOAD, mSession.pendingTask);
        intent.putExtra(EXTRA_START_POSITION, mSession.pendingStartPos);
        intent.putExtra(EXTRA_TAG_NAME, mSession.taskName);

        AlarmManager manager = (AlarmManager) mSession.context.getSystemService(Context.ALARM_SERVICE);
        Calendar now = Calendar.getInstance();

        now.add(Calendar.SECOND, seconds);
        logd("Restarting at " + new SimpleDateFormat("HH:mm:ss").format(now.getTime()));

        manager.set(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), PendingIntent.getService(mSession.context, REQUEST_RESTART_PROCESSING, intent, PendingIntent.FLAG_ONE_SHOT));

        cancelWatchdog();
    }

    private void cancelWatchdog() {
        logd("Cancelling watchdog");
        AlarmManager m = (AlarmManager) mSession.context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent i = buildResumePendingIntent(new Intent(), mSession.pendingStartPos, PendingIntent.FLAG_CANCEL_CURRENT);
        i = buildResumePendingIntent(new Intent());
        i.cancel();
        m.cancel(i);
    }

    /**
     * @author krohnjw
     * Used to request WriteSecureSettings asynchronously
     */
    @SuppressWarnings("unused")
    private class PermissionRequest extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                logd("Trying to grant permission");
                Utils.requestWriteSecureSettings();
            } catch (IOException e) {
                Logger.e("Could not get root access");
            } catch (InterruptedException e) {
                Logger.e("Could not get root access");
            }
            return null;
        }
        protected void onPostExecute(final Void unused) {
            mSession.needManualReset = true;
            mSession.needReset = true;
            restartProcessing();
        }
    }

}
