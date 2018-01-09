package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.RepetitionOverrideActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActivityFeedItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import java.util.Date;
import java.util.Timer;

public class ParserService extends BaseIntentService {

    public ParserService() {
        super("ParserService");
    }

    public ParserService(String name) {
        super(name);
    }

    static final String TAG = Constants.TAG;

    public static final String EXTRA_TAG_NAME = "com.trigger.launcher.TagName";
    public static final String EXTRA_TAG_ID = "com.trigger.launcher.TagId";
    public static final String EXTRA_ALT_TAG_NAME = "com.trigger.launcher.AltTagName";
    public static final String EXTRA_ALT_TAG_ID = "com.trigger.launcher.AltTagId";
    public static final String EXTRA_PAYLOAD = "LoadedPayload";
    public static final String EXTRA_SKIP_CHECK = "skip_check";

    private final int TAG_TYPE_TASK = 1;
    private final int TAG_TYPE_SWITCH = 2;

    private String mPendingTask = "";
    private String mFullTask = "";

    private String mTagName = "";
    private String mTagId = "";

    private long mTag1ID = 0;
    private long mTag2ID = 0;

    private String mTag1Name = "";
    private String mTag2Name = "";

    private Timer mTimer;
    private AlertDialog mAlert;

    private Intent mIntent;
    private boolean mSkipCheck;
    private int mTaskType;

    DatabaseHelper mDatabaseHelper;

    private class PayloadChecker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            logd("Checking payload " + params[0]);
            return processPayload(params[0], mSkipCheck);
        }

        @Override
        protected void onPostExecute(Boolean run) {
            if (run) {
                startParser(mTagName, mPendingTask);
            }
            finish();

        }

    }

    ;

    private void checkPayload(String payload) {
        if (processPayload(payload, mSkipCheck)) {
            startParser(mTagName, mPendingTask);
        }
        finish();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        acquireWakeLock(flags);
        super.onStartCommand(intent, flags, startId);
        return (START_REDELIVER_INTENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SettingsHelper.loadPreferences(ParserService.this);
        Usage.startSession(this);
        mIntent = intent;
        logd("Started");
        if (intent != null) {
            logd("Received valid intent");
            mSkipCheck = intent.hasExtra(EXTRA_SKIP_CHECK) ? intent.getBooleanExtra(EXTRA_SKIP_CHECK, false) : false;
            mTaskType = intent.getIntExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_NFC);
            mTagName = (intent.hasExtra(EXTRA_TAG_NAME)) ? decodeTagName(intent.getStringExtra(EXTRA_TAG_NAME)) : "";

            if (intent.hasExtra(EXTRA_PAYLOAD)) {
                logd("Received name and payload: " + mTagName + " - " + intent.getStringExtra(EXTRA_PAYLOAD));
                checkPayload(intent.getStringExtra(EXTRA_PAYLOAD));
            } else if (intent.hasExtra(EXTRA_TAG_NAME) && (intent.hasExtra(EXTRA_TAG_ID))) {
                loadTagsFromDatabase(intent);
            }
        } else {
            logd("Intent was null");
        }
        Usage.endSession(this);

    }


    private String decodeTagName(String name) {
        return Utils.decodeData(name);
    }

    private void startParser(String name, String task) {
        Intent intent = new Intent(ParserService.this, ActionService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(ActionService.EXTRA_TAG_NAME, name);
        intent.putExtra(ActionService.EXTRA_PAYLOAD, task);
        startService(intent);
    }


    private void loadTagsFromDatabase(Intent intent) {
        if (intent.hasExtra(EXTRA_TAG_NAME)) {
            String tagName = decodeTagName(intent.getStringExtra(EXTRA_TAG_NAME));
            String tagId = intent.getStringExtra(EXTRA_TAG_ID);
            String secondaryId = "";
            String secondaryName = "";

            if (tagId.contains(",")) {
                String[] ids = tagId.split(",");
                tagId = ids[0];
                if (ids.length > 1) {
                    secondaryId = ids[1];
                }

                String[] names = tagName.split(",");
                tagName = names[0];
                if (names.length > 1) {
                    secondaryName = decodeTagName(names[1]);
                }
            }

            logd("Got incoming tag " + tagName);

            mTag1Name = tagName;
            mTagName = tagName;

            Task tagTwo = null;

            if (intent.hasExtra(EXTRA_ALT_TAG_ID)) {
                tagTwo = Task.loadTask(this, intent.getStringExtra(EXTRA_ALT_TAG_ID), intent.getStringExtra(EXTRA_ALT_TAG_NAME), "");
            } else if (!secondaryId.isEmpty()) {
                tagTwo = Task.loadTask(this, secondaryId, secondaryName, "");
            }

            String payload = "";
            Task task = Task.loadTask(this, tagId, tagName, "");

            // We received an incoming message (manually run)
            if (tagTwo != null) {
                payload = NFCUtil.buildSwitchPayload(ParserService.this, true, task, tagTwo);
            } else {
                payload = NFCUtil.getTagPayload(ParserService.this, tagId, tagName);
            }

            if (!payload.isEmpty()) {
                new PayloadChecker().execute(payload);
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mTimer != null) {
            try {
                mTimer.cancel();
            } catch (Exception e) {
            }
        }
        if (mAlert != null) {
            if (mAlert.isShowing()) {
                mAlert.dismiss();
            }
        }
        releaseWakeLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("NFCT", "Binding");
        return null;
    }


    private void finish() {
        this.stopSelf();
    }

    private boolean processPayload(String recordText, boolean skipCheck) {
        boolean shouldAllowOverride = SettingsHelper.getPrefBool(getBaseContext(), Constants.PREF_OVERRIDE_LIMIT, false);
        boolean checkTagTimeout = SettingsHelper.getPrefBool(ParserService.this, Constants.PREF_CHECK_REPEAT, SettingsHelper.RATE_LIMITING_DEFAULT);
        boolean checkIfLastTagExecuted = SettingsHelper.getPrefBool(ParserService.this, Constants.PREF_CHECK_SEQUENTIAL, SettingsHelper.SEQUENTIAL_CHECK_DEFAULT);
        boolean processTask = true;
        int type = TAG_TYPE_TASK;
        mFullTask = recordText;

        mTagName = Utils.decodeData(mTagName);
        // Skip processing if we don't have good data
        if ((recordText == null) || (recordText.length() < 1)) {
            logd("Invalid record text, returning");
            return false;
        }

        String[] tasks = getPendingTasks(recordText);
        if (tasks.length == 0) {
            logd("Invalid task length, returning");
            return false;
        }

        if (tasks.length > 1) {
            type = TAG_TYPE_SWITCH;
        }

        logd("Setting pending task");
        setPendingTask(tasks, type);

        if (!skipCheck) {
            processTask = shouldParsePayload(tasks, type, checkIfLastTagExecuted, checkTagTimeout);
        }

        logd("Should process: " + processTask);

        if (processTask) {
            /* Update last tag use data */
            updateLastUse(mTagId, mTagName);
            updateLastTagExecuted(mFullTask, mPendingTask);
            if ((mTagName != null) && !mTagName.isEmpty()) {
                addActivityFeedEntry(mTagId, mTagName, mTaskType);
            }

            SettingsHelper.setPrefString(ParserService.this, Constants.PREF_LAST_TAG, mFullTask);

            return processTask;
        } else if (shouldAllowOverride) {
            Intent intent = new Intent(getBaseContext(), RepetitionOverrideActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ParserService.EXTRA_PAYLOAD, mFullTask);
            startActivity(intent);
            return false;
        } else {
            return false;
        }


    }

    /**
     * `
     * Returns the portion of the payload that represents the pending tasks to be executed
     *
     * @param payload
     * @return
     */
    private String[] getPendingTasks(String payload) {

        if (payload.substring(0, 1).equals(Constants.COMMAND_TAG_ID)) {
            return new String[]{payload};
        } else if (payload.substring(0, 1).equals(Constants.COMMAND_TOGGLE_PROFILE)) {
            String firstTask = "";
            String secondTask = "";
            String[] commandArr = payload.split("__");
            if (commandArr.length >= 2) {
                // This starts with U: or U:id
                firstTask = commandArr[0];

                // Trim off identifier and prefix
                firstTask = firstTask.substring(2);

                // Check if the next entry is a name
                firstTask = checkAndRemoveName(firstTask, 1);
                logd("First command set is " + firstTask);
                secondTask = checkAndRemoveName(commandArr[1], 2);
                logd("Second command set is " + secondTask);

                return new String[]{firstTask, secondTask};
            } else {
                return new String[0];
            }
        } else if ((payload.substring(0, 1).equals(Constants.COMMAND_TOGGLE_PROFILE_LEGACY))) {
            logd("Legacy Profile");
            String[] commandArr = payload.split(":"); // Legacy profiles are CONST:ID:ID
            String firstTask = "";
            String secondTask = "";
            if (commandArr.length >= 3) {
                String id1 = commandArr[1];
                String id2 = commandArr[2];
                firstTask = retrieveTagCommandString(id1);
                logd("First command set is " + firstTask);
                secondTask = retrieveTagCommandString(id2);
                logd("Second command set is " + secondTask);

                // Get Names
                mTag1Name = getTagNameFromID_Legacy(id1);
                mTag2Name = getTagNameFromID_Legacy(id2);

                mTag1ID = Long.parseLong(id1);
                mTag2ID = Long.parseLong(id2);

                return new String[]{firstTask, secondTask};
            } else {
                return new String[0];
            }
        }

        return new String[]{payload};
    }

    /**
     * Sets pending task data for use in processing based on tag type, user preference and payload
     *
     * @param tasks
     * @param type
     */
    private void setPendingTask(String[] tasks, int type) {
        if (type == TAG_TYPE_TASK) {
            mPendingTask = tasks[0];
            int firstDelimiter = mPendingTask.indexOf(";");
            if (firstDelimiter < 3)
                firstDelimiter = 3;
            try {
                setTagNameInfo(mPendingTask.substring(2, firstDelimiter));
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception thrown getting tag ID", e);
            }
        } else {
            mPendingTask = tasks[0]; // Default to first task
            boolean doesSwitchExpire = SettingsHelper.getPrefBool(ParserService.this, Constants.PREF_EXPIRE_SWITCH, false);

            long timeout = 43200000; // 12 hours

            String lastTask = getLastTagExecuted(mFullTask);
            long lastTimeExecuted = getLastTimeExecuted(mFullTask);
            long currentTime = System.currentTimeMillis();
            /* Calculate time elapsed if switch expires, otherwise default to 0 */
            long diff = (doesSwitchExpire) ? currentTime - lastTimeExecuted : 0;

            logd("Last Known Task is " + lastTask);
            logd("Timeout: %s, Elapsed: %s", timeout, diff);

            if ((lastTask.isEmpty()) || (lastTask.equals(tasks[1]) || (diff > timeout))) {
                logd("Using first task " + tasks[0]);
                mPendingTask = tasks[0];
                if (mTag1Name == null) {
                    mTag1Name = "";
                }
                mTagName = mTag1Name;
                mTagId = String.valueOf(mTag1ID);
            } else if (lastTask.equals(tasks[0])) {
                logd("Using second task " + tasks[1]);
                mPendingTask = tasks[1];
                if (mTag2Name == null) {
                    mTag2Name = "";
                }
                mTagName = mTag2Name;
                mTagId = String.valueOf(mTag2ID);
            }
        }
    }

    /**
     * Determines whether or not to continue processing a payload based on user preferences for time out and ignoring tags
     *
     * @param tasks
     * @param type
     * @param checkSequential
     * @param checkRepeat
     * @return
     */
    private boolean shouldParsePayload(String[] tasks, int type, boolean checkSequential, boolean checkRepeat) {

        logd("Checking if task should be processed based on user settings %s %s", checkSequential, checkRepeat);
        // Check if we should process this tag based on user preferences
        if (checkSequential) {
            // Check if this was the last tag executed
            if (mPendingTask.equals(SettingsHelper.getPrefString(ParserService.this, Constants.PREF_LAST_TAG, ""))) {
                logd("Task matches last processed and sequential tasks are ignored.  Will not process.");
                return false;
            }
        } else if (checkRepeat) {
            // Check if this tag has been scanned previously and we are still in the timeout window
            if (type == TAG_TYPE_TASK) {
                mPendingTask = tasks[0];
                return checkLimitingThresholdMet(DatabaseHelper.TAG_LIMITING_SINGLE, tasks[0]);
            } else if ((type == TAG_TYPE_SWITCH) && (tasks.length >= 2)) {
                return checkLimitingThresholdMet(mFullTask, mPendingTask);

            } else {
                logd("Invalid combo parsed.  Type is " + type + " size is " + tasks.length);
            }
        }

        return true;
    }

    /**
     * Parses an incoming payload for a switch or task and returns the payload minus a tag name.  Sets Tag 1 and Tag 2 name as necessary
     *
     * @param commands
     * @param tagNum
     * @return
     */
    private String checkAndRemoveName(String commands, int tagNum) {
        String finalCommands = "";
        logd("incoming commands are " + commands);
        String[] commandArr = commands.split(":");
        if (commandArr.length > 1) {
            if (commandArr[1].equals(Constants.COMMAND_TAG_NAME)) {
                // This handles form ID:q:Name

                String localName = "";
                // Strip the first two args and set the name
                int start = 2;
                if (commandArr.length > 2) {
                    localName = commandArr[2];
                    start = 3;
                    logd("* Setting localName to " + localName + " for " + tagNum);
                    if (tagNum == 2)
                        mTag2Name = localName;
                    else
                        mTag1Name = localName;
                }
                try {
                    for (int i = start; i < commandArr.length; i++) {
                        finalCommands += commandArr[i] + ":";
                    }
                    logd("Set final commands to " + finalCommands);
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception parsing name from list ", e);
                }

                // Try to grab ID
                try {
                    if (tagNum == 2) {
                        mTag2ID = Long.parseLong(commandArr[0]);
                    } else {
                        mTag1ID = Long.parseLong(commandArr[0]);
                    }
                } catch (Exception e) {
                    /*
                     * We likely got some bad input here, likely an unexpected
                     * format - set to 0
                     */
                    if (tagNum == 2) {
                        mTag2ID = 0;
                    } else {
                        mTag1ID = 0;
                    }
                }

            } else {
                // This should be the legacy form ID:Command:Arg
                for (int i = 1; i < commandArr.length; i++) {
                    finalCommands += commandArr[i] + ":";
                }

            }
        }

        logd("Final command set = " + finalCommands);

        if (finalCommands == "")
            finalCommands = commands;

        return finalCommands;
    }


    /**
     * Checks whether or not a payload should be parsed based on a user defined time out window
     *
     * @param fullTag
     * @param tagToExecute
     * @return
     */
    private boolean checkLimitingThresholdMet(String fullTag, String tagToExecute) {
        boolean processTag = true;

        // Get the last tag and threshold values from preferences
        //String lastTag = getLastTagExecuted(fullTag);
        int thresh = SettingsHelper.getPrefInt(this, Constants.PREF_REPEAT_THRESHHOLD);
        String threshUnits = SettingsHelper.getPrefString(this, Constants.PREF_REPEAT_THRESHHOLD_UNITS);

        if (thresh == 0)
            thresh = 30;

        if (threshUnits.equals("Minutes")) {
            thresh = thresh * 60;
        } else if (threshUnits.equals("Hours")) {
            thresh = thresh * 60 * 60;
        }

        /*boolean isSwitch = !fullTag.equals(DatabaseHelper.TAG_LIMITING_SINGLE);
        boolean isSameTask = tagToExecute.equals(lastTag);

        logd("IsSameTask? %s, isSwitch? %s", isSameTask, isSwitch);*/

        // If we have a single Task in the payload and it is the last task executed or this is a switch task run our check
        // This check does not make sense.  We only care if it is the last task in a sequential check, not a repeating check.
        // We also check this on both switch and single tasks.  This looks like a remnant of conditionals prior to refactoring.  Removing this for now.

        //if (isSameTask || isSwitch) {

        boolean isSwitch = !fullTag.equals(DatabaseHelper.TAG_LIMITING_SINGLE);

        // Check the threshold to see if we have exceeded it - it we have
        // not then return without doing anything
        long lastTagExecute = getLastTimeExecuted(isSwitch ? fullTag : tagToExecute);
        Date now = new Date();
        long elapsed = now.getTime() - lastTagExecute;
        long timeout = thresh * 1000;
        logd("Timeout: %s, Elapsed: %s", timeout, elapsed);


        if (elapsed < timeout) {
            // We have not exceeded the expiration time, skip
            logd("Skipping task as it is within the timeout period");
            return false;
        }
        //}

        logd("Updating last ran for task");
        // We are past the expiration or don't need to check
        updateLastTagExecuted(fullTag, tagToExecute);


        return processTag;
    }

    /**
     * Returns the last payload parsed and executed
     *
     * @param fullContents
     * @return
     */
    private String getLastTagExecuted(String fullContents) {
        String lastExecuted = "";
        Cursor c = getContentResolver().query(TaskProvider.Contract.TIMEOUTS, new String[]{DatabaseHelper.FIELD_LAST_EXECUTED}, "Tag=?", new String[]{fullContents}, null);
        try {
            while (c != null && c.moveToNext()) {
                lastExecuted = c.getString(0);
            }
        } catch (Exception e) {
            Logger.e("Exception getting last tag executed");
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return lastExecuted;
    }


    /**
     * Returns the last time this payload was executed
     *
     * @param fullContents
     * @return
     */
    private long getLastTimeExecuted(String fullContents) {
        long time = 0;
        Cursor c = getContentResolver().query(TaskProvider.Contract.TIMEOUTS, new String[]{"TimeExecuted"}, "Tag=?", new String[]{fullContents}, null);
        try {
            while (c != null && c.moveToNext()) {
                try {
                    time = Long.parseLong(c.getString(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Logger.e("Exception getting last time executed");
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return time;
    }

    /**
     * Updates the last execited time for a payload
     *
     * @param fullContents
     * @param lastExecuted
     */
    private void updateLastTagExecuted(String fullContents, String lastExecuted) {
        Date now = new Date();
        long time = now.getTime();
        logd("Setting last executed for %s to %s at %s", fullContents, lastExecuted, time);
        ContentValues values = new ContentValues();
        values.put("Tag", fullContents);
        if (lastExecuted != null) {
            values.put(DatabaseHelper.FIELD_LAST_EXECUTED, lastExecuted);
        }
        values.put(DatabaseHelper.FIELD_TIME_EXECUTED, time);

        Cursor c = getContentResolver().query(TaskProvider.Contract.TIMEOUTS, new String[]{DatabaseHelper.FIELD_LAST_EXECUTED}, "Tag=?", new String[]{fullContents}, null);
        try {
            if (c.moveToFirst()) {
                // Update
                getContentResolver().update(TaskProvider.Contract.TIMEOUTS, values, "Tag=?", new String[]{fullContents});
            } else {
                // Insert
                getContentResolver().insert(TaskProvider.Contract.TIMEOUTS, values);
            }
        } catch (Exception e) {
            Logger.e("Exception running insert/update on tag execution", e);
        } finally {
            c.close();
        }
    }

    /**
     * Retrives a command string based on a local ID.  This is for legacy profiles only (Small switch tags option).
     *
     * @param id
     * @return
     */
    private String retrieveTagCommandString(String id) {
        String commandString = "";
        logd("Loading Tag " + id);
        Cursor c = getContentResolver().query(TaskProvider.Contract.ACTIONS, new String[]{"Activity", "Description"}, "WHERE TagId=?", new String[]{id}, null);
        try {
            if ((c != null) && (c.moveToFirst())) {
                boolean keepReading = true;
                while (keepReading) {
                    String activity = c.getString(0);
                    logd("Loading " + activity);
                    if (!commandString.equals("")) {
                        commandString += ";";
                    }
                    commandString += activity;
                    keepReading = c.moveToNext();
                }
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception getting command string" + e);
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return commandString;
    }

    /**
     * Returns a tag name based on a Tag Id.  This is used for legacy "small" profile tags that stored ONLY a numerical id
     *
     * @param id
     * @return
     */
    private String getTagNameFromID_Legacy(String id) {
        String name = "";
        Cursor c = getContentResolver().query(TaskProvider.Contract.TASKS, new String[]{"Name"}, "WHERE ID=?", new String[]{id}, null);
        try {
            if ((c != null) && (c.moveToFirst())) {
                name = c.getString(0);
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception getting legacy name" + e);
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return name;
    }

    /**
     * Parses incoming payload and sets mTagName and mTagActions
     *
     * @param tagID
     * @return
     */
    private String setTagNameInfo(String tagID) {
        String localID = tagID;

        // Check ID for ":" delimiter. If this is present it's the name, strip
        // it out and reset localID and set name for display
        try {

            if (tagID.contains(":")) {
                String[] args = tagID.split(":");
                localID = args[0];

                if (args.length > 1) {
                    String localName = "";
                    for (int i = 1; i < args.length; i++) {
                        if (!localName.isEmpty()) {
                            localName += ":";
                        }
                        localName += args[i];
                    }
                    mTagName = localName;
                }
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception getting tag name " + e);
            e.printStackTrace();
        }

        return localID;
    }

    /**
     * Used to update last use data in the background to help prevent ANRs during processing
     *
     * @author krohnjw
     */
    private static class updateLastUseTask extends AsyncTask<String, Void, Void> {
        private Context context;

        public updateLastUseTask(Context c) {
            this.context = c;
        }

        @Override
        protected Void doInBackground(String... args) {
            String tagId = args[0];
            String tagName = "";
            if (args.length > 1)
                tagName = args[1];

            logd("Updating last use for " + tagName + ", " + tagId);
            SettingsHelper.setPrefString(context, Constants.PREF_LAST_TASK, tagName);

            ContentValues values = new ContentValues();
            Date now = new Date();
            values.put(DatabaseHelper.FIELD_TASK_LAST_ACCESSED, Utils.toGMTString(now));
            try {
                if ((tagName != null) && (!tagName.equals(""))) {
                    context.getContentResolver().update(TaskProvider.Contract.TASKS, values, "name=?", new String[]{tagName});
                } else if ((tagId != null) && (!tagId.equals(""))) {
                    context.getContentResolver().update(TaskProvider.Contract.TASKS, values, "ID=?", new String[]{tagId});
                }
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception updating last use", e);
            }

            return null;
        }

    }

    /**
     * Updates the last use data for a tag
     *
     * @param tagId
     * @param tagName
     */
    private void updateLastUse(String tagId, String tagName) {
        logd("Updating last use for " + tagId + ", " + tagName);
        updateLastUseTask update = new updateLastUseTask(this);
        update.execute(tagId, tagName);
    }

    private void addActivityFeedEntry(String id, String name, int type) {
        logd("Adding activity feed info for " + id + ", " + name + ", " + type);
        ActivityFeedItem item = new ActivityFeedItem(type, mTagName, Utils.toGMTString(new Date()));
        DatabaseHelper.storeActivityFeedEntry(this, item);
    }


}
