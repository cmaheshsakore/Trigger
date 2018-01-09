package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.ChargingTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.Action;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.BaseAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.AgentTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BluetoothTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.GeofenceTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.HeadsetTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.WifiTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.wear.WearMessagingService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class Utils {
    /**
     * Encodes a string so that it is suitable to be passed in to the parser
     * after being read
     * 
     * @param data
     * @return
     */
    public static String encodeData(String data) {
        String out = data;
        out = out.replace(":", "&#58");
        out = out.replace(";", "&#59");
        return out;
    }

    /**
     * Decodes a potentially encoded string after being parsed so that it is
     * suitable for use
     * 
     * @param data
     * @return
     */
    public static String decodeData(String data) {
        if (data == null) return null;
        String out = data;
        out = out.replace("&#58", ":");
        out = out.replace("&#59", ";");
        return out;
    }

    public static String encodeURL(String url) {
        return url.replace("://", "_//");
    }

    public static String decodeURL(String url) {
        return url.replace("_//", "://");
    }

    /**
     * Takes in a full Action argument from a tag payload and returns a string
     * suitable for display to the user describing the action
     * 
     * @param action
     * @param command
     * @param args
     * @param context
     * @return
     */
    public static String getDisplayTextFromAction(String action, String command, String[] args, Context context) {
        String mDisplay = "";
        String mPrefix = getActionPrefix(action, context);
        // Add prefix if necessary
        if ((mPrefix != null) && (!mPrefix.equals(""))) {
            mDisplay = mPrefix + " ";
        }

        mDisplay += getActionText(command, args, context);

        return mDisplay;
    }

    private static String getActionPrefix(String action, Context context) {
        String mPrefix = "";

        if (action.equals(Constants.COMMAND_ENABLE)) {
            mPrefix = context.getString(R.string.enableText);
        } else if (action.equals(Constants.COMMAND_DISABLE)) {
            mPrefix = context.getString(R.string.disableText);
        } else if (action.equals(Constants.COMMAND_TOGGLE)) {
            mPrefix = context.getString(R.string.toggleText);
        }

        return mPrefix;
    }

    public static String getCommandFromAction(String action) {
        String mCommand = "";
        String[] commandArgs = action.split(":");

        if ((commandArgs[0].equals(Constants.COMMAND_ENABLE)) || (commandArgs[0].equals(Constants.COMMAND_DISABLE)) || (commandArgs[0].equals(Constants.COMMAND_TOGGLE))) {
            mCommand = commandArgs[1];
        } else {
            mCommand = commandArgs[0];
        }
        return mCommand;
    }

    public static String buildCommandString(String[] commands) {
        String output = "";
        for (String command : commands) {
            if (!output.equals("")) {
                output += ";" + command;
            } else {
                output = command;
            }

        }
        return output;
    }

    public static String getNextTagName(Context context) {
        int intID = 0;
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.TASKS, new String[] { "ID" }, null, null, "ID DESC");
        try {
            if (c != null) {
                if (c.moveToFirst()) {
                    String currentID = c.getString(0);
                    intID = Integer.parseInt(currentID);
                }
            }
        } catch (Exception ignore) {

        } finally {
            if (c != null) {
                c.close();
            }
        }

        intID++;
        return context.getString(R.string.default_tag_name_format, String.valueOf(intID));
    }

    public static String getActionText(String command, String[] args, Context context) {
        String mDisplay = "";
        Action action = BaseAction.getAction(BaseAction.getCodeFromCommand(command));
        mDisplay = action.getDisplayFromMessage(command, args, context);
        if (mDisplay != null) {
            return mDisplay;
        } else {
            Logger.d("Got a null message for " + command);
            return "";
        }
    }

    public static boolean isSMSPluginInstalled(Context context) {
        final String SMS_PLUGIN_NAME = "com.trigger.nfctl.plugins.sms";
        boolean pluginPresent = false;

        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(SMS_PLUGIN_NAME, 0);
            if (info != null)
                pluginPresent = true;
        } catch (NameNotFoundException e) {
            pluginPresent = false;
        }

        Logger.d("SMS Plugin Present: " + pluginPresent);

        return pluginPresent;
    }

    public static boolean isReusePluginInstalled(Context context) {
        final String PLUGIN_NAME = "com.trigger.launcher.tagreuse";
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(PLUGIN_NAME, 0);
            return (info != null) ? true : false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static Intent getLaunchIntentForPackage(Context context, String packageName) {

        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getLaunchIntentForPackage(packageName);
        } catch (Exception e) {
            Logger.e("Exception querying for " + packageName);
        }

        return null;
    }


    public static boolean isPackageInstalled(Context context, String packageName) {

        PackageManager manager = context.getPackageManager();
        try {
            manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            Logger.e("Exception querying for " + packageName);
        }

        return false;

    }

    public static boolean isPackageInstalled(PackageManager manager, String packageName) {
        try {
            manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            Logger.e("Exception querying for " + packageName);
        }

        return false;

    }

    /**
     * Detects the presence of the NFCTL call plugin
     * 
     * @param context
     * @return
     */
    public static boolean isCallPluginInstalled(Context context) {
        final String CALL_PLUGIN_NAME = "com.trigger.nfctl.plugins.call";
        boolean pluginPresent = false;
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(CALL_PLUGIN_NAME, 0);
            if (info != null)
                pluginPresent = true;
        } catch (NameNotFoundException e) {
            pluginPresent = false;
        }

        Logger.d("Call Plugin Present: " + pluginPresent);

        return pluginPresent;
    }

    /**
     * Tries to detect the presence of the su binary or a SuperUser APK
     * 
     * @return
     */
    public static boolean isRootPresent() {
        Logger.d("Checking root");
        try {

            File file = new File("/system/app/Superuser.apk");
            File file2 = new File("/system/app/SuperSU.apk");
            if (file.exists() || file2.exists()) {
                Logger.d("Superuser APK found");
                return true;
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception thrown checking for SU APK", e);
        }

        try {
            String binaryName = "su";
            String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/" };
            for (String where : places) {
                File file = new File(where + binaryName);
                if (file.exists()) {
                    Logger.d(binaryName + " was found here: " + where);
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception locating su binary", e);
        }

        return false;
    }

    /**
     * Runs a specific command using su
     * 
     * @param commands
     * @throws IOException
     */
    public static String runCommandAsRoot(String[] commands) throws IOException, InterruptedException {
        String results = "";
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            BufferedReader STDOUT = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader STDERR = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Write all commands
            for (int i = 0; i < commands.length; i++) {
                Logger.d("Adding command " + commands[i]);
                STDIN.writeBytes(commands[i] + "\n");
                STDIN.flush();
            }
            STDIN.writeBytes("exit\n");
            STDIN.flush();
            Logger.d("Waiting");
            process.waitFor();
            Logger.d("Done");
            if (process.exitValue() == 255) {
                Logger.d("su exited with 255");
                return null; // su denied
            }

            StringBuilder output = new StringBuilder();
            while (STDOUT.ready()) {
                String read = STDOUT.readLine();
                output.append(read);
                Logger.d("Output:" + read);
            }

            while (STDERR.ready()) {
                String read = STDERR.readLine();
                output.append(read);
                Logger.d("Error:" + read);
            }
            results = output.toString();
            process.destroy();
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        }
        return results;
    }

    /**
     * @return Will request WRITE_SECURE_SETTINGS permission using root and
     *         package manager. Blocking
     * @throws IOException
     * @throws InterruptedException
     */
    public static String requestWriteSecureSettings() throws IOException, InterruptedException {
        if (Build.VERSION.SDK_INT >= 16) {
            return Utils.runCommandAsRoot(new String[] { "pm grant com.mobiroo.n.sourcenextcorporation.trigger android.permission.WRITE_SECURE_SETTINGS" });
        } else {
            // Must move app to /system and alert the user to reboot.
            File container = new File("/data/app/");

            String fileName = "com.mobiroo.n.sourcenextcorporation.trigger.apk";
            String fileBase = "com.mobiroo.n.sourcenextcorporation.trigger";
            File file;
            for (int i = 0; i < 10; i++) {
                file = new File(container, fileBase + "-" + i + ".apk");
                if (file.exists()) {
                    fileName = fileBase + "-" + i + ".apk";
                }
            }

            String[] commands = { "busybox mount -o remount,rw /system", "cat /data/app/" + fileName + " > /system/app/com.mobiroo.n.sourcenextcorporation.trigger.apk",
                    "chmod 644 /system/app/com.mobiroo.n.sourcenextcorporation.trigger.apk", "mount -o remount,ro /system" };

            String results = Utils.runCommandAsRoot(commands);
            Logger.d("Results = " + results);
            return "REBOOT:" + results;
        }

    }

    public static String requestModifyPhoneState() throws IOException, InterruptedException {
        return Utils.runCommandAsRoot(new String[] { "pm grant com.mobiroo.n.sourcenextcorporation.trigger android.permission.MODIFY_PHONE_STATE"});
    }

    /**
     * Uses DateUtils to return a properly localized "N units ago" string for
     * all languages
     * 
     * @param lastUsed
     *            GMT string in format of Constants.GMT_DATE_FORMAT
     * @param defaultValue
     *            The default string to be returned if lastUsed is null or
     *            cannot be parsed to a date
     * @return
     */
    public static String formatLastUsed(String lastUsed, String defaultValue) {

        String lastUsedText = defaultValue;

        if (lastUsed != null) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            DateFormat GMT = new SimpleDateFormat(Constants.GMT_DATE_FORMAT);
            Date lastUsedDate = null;

            try {
                lastUsedDate = GMT.parse(lastUsed);
            } catch (Exception e) {
            }

            if (lastUsedDate != null) {
                lastUsedText = (String) DateUtils.getRelativeTimeSpanString(lastUsedDate.getTime(), now.getTimeInMillis(), DateUtils.SECOND_IN_MILLIS);
            }
        }

        return lastUsedText;
    }

    public static String getTimeStringAsLocal(String lastUsed, String defaultValue) {
        return getTimeStringAsLocal(lastUsed, defaultValue, Constants.STANDARD_TIME_FORMAT);
    }

    public static String getTimeStringAsLocal(String lastUsed, String defaultValue, String format) {
        String lastUsedText = defaultValue;

        if (lastUsed != null) {
            try {
                lastUsedText = new SimpleDateFormat(format).format(new SimpleDateFormat(Constants.GMT_DATE_FORMAT).parse(lastUsed));
            } catch (Exception e) {
            }
        }

        return lastUsedText;
    }

    /**
     * Replaces deprecated toGMTString from date object. Uses simple date format
     * and current (now legacy) GMT string date format
     * 
     * @param date
     * @return
     */
    public static String toGMTString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
        dateFormat.applyPattern(Constants.GMT_DATE_FORMAT);
        return dateFormat.format(date);
    }

    public static int tryParseInt(String[] args, int position, int defaultValue) {
        return tryParseInt(args, position, defaultValue, "");
    }

    public static int tryParseInt(String[] args, int position, int defaultValue, String exceptionMessage) {
        int value = defaultValue;
        String message = (exceptionMessage.isEmpty()) ? "Exception parsing arg at position " + position : exceptionMessage;
        if (args.length > position) {
            try {
                value = Integer.parseInt(args[position]);
            } catch (Exception e) {
                Logger.e(Constants.TAG, message, e);
                value = defaultValue;
            }
        }
        return value;
    }

    public static String tryParseString(String[] args, int position, String defaultValue) {
        return tryParseString(args, position, defaultValue, "");
    }

    public static String tryParseString(String[] args, int position, String defaultValue, String exceptionMessage) {
        String value = defaultValue;
        String message = (exceptionMessage.isEmpty()) ? "Exception parsing arg at position " + position : exceptionMessage;
        if (args.length > position) {
            try {
                value = args[position];
            } catch (Exception e) {
                Logger.e(Constants.TAG, message, e);
                value = defaultValue;
            }
        }
        return value;
    }

    public static String tryParseEncodedString(String[] args, int position, String defaultValue) {
        return tryParseEncodedString(args, position, defaultValue, "");
    }

    public static String tryParseEncodedString(String[] args, int position, String defaultValue, String exceptionMessage) {
        String value = defaultValue;
        String message = (exceptionMessage.isEmpty()) ? "Exception parsing arg at position " + position : exceptionMessage;
        if (args.length > position) {
            try {
                value = args[position];
                value = Utils.decodeData(value);
            } catch (Exception e) {
                Logger.e(Constants.TAG, message, e);
                value = defaultValue;
            }
        }
        return value;
    }

    public static boolean tryParseBoolean(String[] args, int position, boolean defaultValue) {
        return tryParseBoolean(args, position, defaultValue, "");
    }

    public static boolean tryParseBoolean(String[] args, int position, boolean defaultValue, String exceptionMessage) {
        boolean value = defaultValue;
        String message = (exceptionMessage.isEmpty()) ? "Exception parsing arg at position " + position : exceptionMessage;
        if (args.length > position) {
            try {
                value = Boolean.parseBoolean(args[position]);
            } catch (Exception e) {
                Logger.e(Constants.TAG, message, e);
                value = defaultValue;
            }
        }
        return value;
    }

    public static boolean hasWriteSecureSettings(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean hasPermission = false;
        try {
            hasPermission = (pm.checkPermission(permission.WRITE_SECURE_SETTINGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED);
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception checking for WRITE_SECURE_SETTINGS", e);
        }

        Logger.d("WRITE_SECURE_SETTINGS granted: " + hasPermission);
        return hasPermission;
    }

    public static String scrubURI(String uri, String id) {
        String cleanUri = uri.replace(" || _id", ""); // Some custom roms are
                                                      // returning invalid URIs
        cleanUri = uri.replace(id + "/" + id, id); // Some CM roms are returning
                                                   // the URI with an ID in it
                                                   // already
        return cleanUri;
    }

    public static void checkReceiversAsync(Context context) {
        // DON'T USE THIS RIGHT NOW.  It is starting a service from background threads
        // In some instances
        new checkReceiversAsync().execute(context);
    }

    private static class checkReceiversAsync extends AsyncTask<Context, Void, Integer> {
        private final int ACTION_NONE = 1;
        private final int ACTION_MIGRATE = 2;
        private final int ACTION_GEO = 3;
        private Context context;

        @Override
        protected Integer doInBackground(Context... args) {
            context = args[0];

            int version = SettingsHelper.getPrefInt(context, Constants.PREF_TASKS_VERSION, 2);
            if (version == 2) {
                return ACTION_GEO;
            } else {
                doReceiverCheck(context);
            }
            return ACTION_NONE;
        }

        @Override
        public void onPostExecute(final Integer action) {
            switch (action) {
                case ACTION_GEO:
                    cleanupGeo(context, false);
                    Utils.checkReceivers(context);
                    break;
            }
        }
    }

    private static void syncWearTasks(Context context) {
        context.startService(new Intent(context, WearMessagingService.class));
    }

    private static void cleanupGeo(Context context, boolean isAsync) {
        GeofenceTrigger.cleanUpGeofences(context);
        SettingsHelper.setPrefInt(context, Constants.PREF_TASKS_VERSION, 3);
    }

    private static void doReceiverCheck(Context context) {
    /* Check if receivers should be enabled */

        Logger.d("UTIL: Checking receivers");

        ArrayList<TaskSet> sets = DatabaseHelper.getAllBluetoothTasks(context);

        if (!((sets != null) && (sets.size() > 0))) {
            BluetoothTrigger.disable(context);
        } else {
            Logger.d("BT: Found " + sets.size());
            BluetoothTrigger.enable(context);
        }

        sets = DatabaseHelper.getAllWifiTasks(context);
        if (!((sets != null) && (sets.size() > 0))) {
            WifiTrigger.disable(context);
        } else {
            Logger.d("WIFI: Found " + sets.size());
            WifiTrigger.enable(context);
        }

        sets = DatabaseHelper.getAllBatteryTasks(context);
        if (!((sets != null) && (sets.size() > 0))) {
            BatteryTrigger.disable(context);
        } else {
            Logger.d("BATTERY: Found " + sets.size());
            BatteryTrigger.enable(context);
        }

        sets = DatabaseHelper.getAllChargingTasks(context);
        if (!((sets != null) && (sets.size() > 0))) {
            ChargingTrigger.disable(context);
        } else {
            Logger.d("CHARGING: Found " + sets.size());
            ChargingTrigger.enable(context);
        }

        sets = DatabaseHelper.getAllBatteryTasks(context);
        if (!((sets != null) && (sets.size() > 0))) {
            BatteryTrigger.disable(context);
        } else {
            Logger.d("BATTERY: Found " + sets.size());
            BatteryTrigger.enable(context);
        }

        sets = DatabaseHelper.getHeadsetTasks(context);
        if (!((sets != null) && (sets.size() > 0))) {
            HeadsetTrigger.disable(context);
        } else {
            Logger.d("HEADSET: Found " + sets.size());
            HeadsetTrigger.enable(context);
        }

        sets = DatabaseHelper.getAgentTasks(context);
        if (!((sets != null) && (sets.size() > 0))) {
            AgentTrigger.disable(context);
        } else {
            Logger.d("AGENT: Found " + sets.size());
            AgentTrigger.enable(context);
        }
        if (DatabaseHelper.getGeofences(context).size() > 0) {
            GeofenceClient.cleanUpGeofences(context);
        }

        TimeTrigger.scheduleTimeTasks(context);
    }

    public static void checkReceivers(Context context) {

        SettingsHelper.setPrefInt(context, Constants.PREF_TASKS_VERSION, 3);
        doReceiverCheck(context);
    }

    private static boolean isLGHomePresent(Context context) {
        final String LAUNCHER_NAME = "com.lge.homeselector";
        boolean isPresent = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : list) {
            if (info.activityInfo != null && info.activityInfo.name.contains(LAUNCHER_NAME)) {
                isPresent = true;
                Logger.d("Found LG Home");
                break;
            }
        }
        return isPresent;

    }

    private static boolean isSensePresent(Context context) {
        final String SENSE_UI_LAUNCHER_NAME = "com.htc.launcher.Launcher";
        boolean sensePresent = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : list) {
            if (info.activityInfo != null && SENSE_UI_LAUNCHER_NAME.equals(info.activityInfo.name)) {
                sensePresent = true;
                Logger.d("Found Sense");
                break;
            }
        }
        return sensePresent;
    }

    private static boolean isTWClockPresentt(Context context) {
        final String TW_CLOCK_NAME = "com.sec.android.app.clockpackage";
        boolean twPresent = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        // intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo info : list) {
            if ((info.activityInfo != null) && (info.activityInfo.name.contains(TW_CLOCK_NAME))) {
                twPresent = true;
                Logger.d("Found TouchWiz");
                break;
            }
        }
        return twPresent;
    }

    public static boolean shouldSetAlarmTwice(Context context) {
        boolean setTwice = true;

        // Check if the user has set a non Google clock package. If they have
        // and it's not a com.google package don't set this twice
        String preferred = SettingsHelper.getPrefString(context, Constants.PREF_DESIRED_ALARM_PACKAGE, "");
        if (!preferred.isEmpty() && !preferred.startsWith("com.google")) {
            Logger.d("Found a non Google preferred alarm app");
            setTwice = false;
        }

        if (setTwice) {
            if (isSensePresent(context) || isTWClockPresentt(context) || isLGHomePresent(context)) {
                Logger.d("Found skin, not setting twice");
                setTwice = false;
            }

        }

        if (setTwice) {
            Logger.d("Setting alarm 2x to get around AOSP clock bug");
        }

        return setTwice;
    }

    public static String getForwardTimeFromArgs(Context context, String[] args) {
        String hour = "";
        try {
            hour = (args[1] == null) ? "" : args[1];
        } catch (Exception e) {
        }

        return hour + " " + context.getString(R.string.layoutAlarmForwardText);
    }

    public static String getTimeFromArgs(String[] args) {
        String hour = "";
        try {
            hour = (args[1] == null) ? "" : args[1];
        } catch (Exception e) {
        }
        String minute = "";
        try {
            minute = (args[2] == null) ? "" : args[2];
        } catch (Exception e) {
        }

        try {
            if (Integer.parseInt(minute) < 10) {
                minute = "0" + minute;
            }
        } catch (Exception e) { /* fail silently */
        }

        return hour + ":" + minute;
    }

    /**
     * Updates Settings.System
     * 
     * @param name setting
     * @param value Desired value (int)
     */
    public static void updateSetting(Context context, String name, int value) {
        try {
            Settings.System.putInt(context.getContentResolver(), name, value);
        } catch (Exception e) {
            Logger.e("Exception updating setting " + name + ": " + e);
        }
    }

    /**
     * Updates Settings.System
     *
     * @param name setting
     * @param value Desired value (int)
     */
    public static void updateSetting(Context context, String name, long value) {
        try {
            Settings.System.putLong(context.getContentResolver(), name, value);
        } catch (Exception e) {
            Logger.e("Exception updating setting " + name + ": " + e);
        }
    }

    public static int getSetting(Context context, String name, int value) {
        try {
            return Settings.System.getInt(context.getContentResolver(), name, value);
        } catch (Exception e) {
            Logger.e("Exception updating setting " + name + ": " + e);
        }
        return value;
    }

    /**
     * Taskes a general name in Settings.System and a desired operation. Will
     * query current value (expects 1/0) and update according to desired
     * operation
     * 
     * @param operation
     *            Operation (Constants.OPERATION_ENABLE,
     *            Constants.OPERATION_DISABLE or Constants.OPERATION_TOGGLE)
     * @param name
     *            name
     */
    public static void operateGeneralSetting(Context context, int operation, String name) {
        if (operation == Constants.OPERATION_ENABLE) {
            updateSetting(context, name, 1);
        } else if (operation == Constants.OPERATION_DISABLE) {
            updateSetting(context, name, 0);
        } else if (operation == Constants.OPERATION_TOGGLE) {
            int current = 0;
            try {
                current = Settings.System.getInt(context.getContentResolver(), name);
            } catch (Exception e) {
                Logger.e("Exception getting setting " + name + " " + e);
            }
            if (current == 1) {
                updateSetting(context, name, 0);
            } else {
                updateSetting(context, name, 1);
            }
        }
    }

    public static boolean isHandlerPresentForIntent(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (resolveInfo.size() > 0) ? true : false;
    }

    public static String removePlaceHolders(String input) {
        String output = input;
        /* Check for known placeholders */
        if (output.contains("%ts")) {
            output = output.replace("%ts", new SimpleDateFormat("K:mm a").format(new Date()));
        }
        
        if (output.contains("%t")) {
            output = output.replace("%t", new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }

        if (output.contains("%de")) {
            output = output.replace("%de", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        }
        
        if (output.contains("%d")) {
            output = output.replace("%d", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
        
        

        return output;

    }

    public static final int MAX_PHONE_800 = 800;

    public static boolean isPhoneLayout(Context context, int maxWidth) {
        int widthDp = getWidthInDp(context);
        return widthDp < maxWidth;
    }

    public static int getWidthInDp(Context context) {

        Display display = null;
        try {
            display = ((Activity) context).getWindowManager().getDefaultDisplay();
        } catch (Exception e) {

        }

        if (display != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float density = context.getResources().getDisplayMetrics().density;
            return (int) (outMetrics.widthPixels / density);
        } else {
            return 320;
        }

    }

    public static int getHeightInDp(Context context) {
        Display display = null;
        try {
            display = ((Activity) context).getWindowManager().getDefaultDisplay();
        } catch (Exception e) {

        }

        if (display != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float density = context.getResources().getDisplayMetrics().density;
            return (int) (outMetrics.heightPixels / density);
        } else {
            return 320;
        }

    }

    public static boolean isConnectedToNetwork(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            if (ni.getType() == ConnectivityManager.TYPE_WIFI)
                if (ni.isConnectedOrConnecting())
                    haveConnectedWifi = true;
            if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
                if (ni.isConnectedOrConnecting())
                    haveConnectedMobile = true;
        }

        return haveConnectedMobile || haveConnectedWifi;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = null;
        try {
            metrics = context.getResources().getDisplayMetrics();
        } catch (Exception e) {
            Logger.e("Exception getting display metrics", e);
        }

        return (metrics != null) ? TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics) : 0;

    }

    public static String[] getNameAndClassFromApplicationString(String arg) {

        String name = arg;
        String clazz = "";
        String[] classData = arg.split("/");
        if (classData.length > 1) {
            name = classData[1];
            clazz = classData[0];
            if (name.startsWith(".")) {
                name = clazz + name;
            }
        }

        if (name.equals("com.google.android.maps.driveabout.app.DestinationActivity") || name.equals("com.google.android.maps.MapsActivity")) {
            name = "com.google.android.apps.maps";
        } else if (name.equals("app.scm")) {
            name = "app.scm.ScmMain";
        }

        return new String[] { clazz, name };
    }

    public static boolean isLgDevice() {
        return (Build.MODEL.startsWith("LG-") || Build.MANUFACTURER.equals("LGE")) && (!Build.FINGERPRINT.startsWith("google"));
    }

    /**
     * Checks whether a device is manufactured by ASUS
     *
     * @return True if device is manufactured by ASUS, False otherwise.
     */
    public static boolean isAsusDevice() {
        return Build.MANUFACTURER.equals("asus");
    }

    public static void signalTasksChanged(Context context) {
        // Schedule a wake up to fire sync to wear
        AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        m.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000,
                PendingIntent.getService(context, 0, new Intent(context, WearMessagingService.class), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void logExtras(String prefix, Intent intent) {
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Logger.d(prefix + ": " + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void executePayload(Context context, String name, String payload, int type) {
        Intent intent = new Intent(context, ParserService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }

        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.setAction("RUN_TASK" + System.currentTimeMillis()); // Ensure a unique pending intent via the action
        intent.putExtra(ParserService.EXTRA_PAYLOAD, payload);
        intent.putExtra(ParserService.EXTRA_TAG_NAME, name);
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, String.valueOf(type));

        AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)) {
            m.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 50, PendingIntent.getService(context, 1, intent, 0));
        } else {
            m.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 50, PendingIntent.getService(context, 1, intent, 0));
        }
    }
}
