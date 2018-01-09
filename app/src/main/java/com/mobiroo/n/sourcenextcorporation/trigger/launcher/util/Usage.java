package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;

import com.amplitude.api.Amplitude;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.plus.model.people.Person;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.Action;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.BaseAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ShopItem;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Usage {

    private static DatabaseHelper mDBT;

    private static final String PREF_COHORT_MONTH_OPEN = "com.trigger.launcher.cohort_month_open";
    private static final String PREF_COHORT_WEEK_OPEN = "com.trigger.launcher.cohort_week_open";
    private static final String PREF_COHORT_MONTH_RAN = "com.trigger.launcher.cohort_month_ran";
    private static final String PREF_COHORT_WEEK_RAN = "com.trigger.launcher.cohort_week_ran";
    private static final String PREF_COHORT_OPEN_SENT = "prefCohortOpenSent";
    private static final String PREF_COHORT_RAN_SENT = "prefCohortRunSent";
    private static final String PREF_COHORT_GENERAL_SENT = "prefCohortGeneralSent";

    private static final String AMPLITUDE_API_PROD_KEY = "00cad8d1008d4e3b41757fc3d9834038";

    public static final String TOKEN_MIXPANEL = "3e7ff966d56be7a3dfe1b4efd65a8916";

    public void register(String name) {
        // Empty for now - eventually will track/store usage or single elements
    }

    public static int getNextID(Context context) {
        int id = 1;

        try {
            id = SettingsHelper.getPrefInt(context, Constants.PREF_LAST_GROUP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (id < 30000) {
            id++;
        } else {
            id = 1;
        }
        SettingsHelper.setPrefInt(context, Constants.PREF_LAST_GROUP, id);
        Log.i(Constants.TAG, "Setting Group ID to " + id);
        return id;
    }

    public static void reportData(Context context) {
        // Unused
    }


    public static void saveActionUsage(Context context, int numActions) {
        new LogUsage(context, numActions).execute(numActions);
    }

    private static class LogUsage extends AsyncTask<Integer, Void, Void> {

        private Context mContext;
        private int numActions;

        public LogUsage(Context context, int actions) {
            this.mContext = context;
            this.numActions = actions;
        }

        @Override
        protected Void doInBackground(Integer... args) {
            mDBT = DatabaseHelper.getInstance(mContext);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            try {
                Cursor c = mContext.getContentResolver().query(TaskProvider.Contract.USAGE,
                        new String[]{DatabaseHelper.FIELD_TOTAL_ACTIONS, DatabaseHelper.FIELD_DATE},
                        DatabaseHelper.FIELD_DATE + "=?",
                        new String[]{today},
                        null);

                if (c != null && (c.moveToFirst())) {
                    int total = c.getInt(0);
                    ContentValues values = new ContentValues();
                    total += numActions;
                    values.put(DatabaseHelper.FIELD_TOTAL_ACTIONS, total);
                    mContext.getContentResolver().update(TaskProvider.Contract.USAGE, values, DatabaseHelper.FIELD_DATE + "=?", new String[]{today});
                } else {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.FIELD_TOTAL_ACTIONS, numActions);
                    values.put(DatabaseHelper.FIELD_DATE, today);
                    mContext.getContentResolver().insert(TaskProvider.Contract.USAGE, values);
                }
            } catch (Exception e) {
                Logger.e("NFCT", "Exception logging usage", e);
            }

            Usage.updateRanCount(mContext);
            Usage.logRanCount(mContext);

            return null;
        }

    }

    private static class logDataPoint extends AsyncTask<Context, Void, Void> {

        private Context mContext;
        private String mCode = "";
        private String mCommand = "";
        private String mCategory = "";
        private int mCount = 0;

        @SuppressWarnings("unused")
        private int mId = 0;

        public logDataPoint(Context context, String category, String code, String command, int id) {
            this.mCode = code;
            this.mCommand = command;
            this.mCategory = category;
            this.mContext = context;
            this.mId = id;
        }

        public logDataPoint(Context context, String category, String code, String command, int id, int count) {
            this.mCode = code;
            this.mCommand = command;
            this.mCategory = category;
            this.mContext = context;
            this.mId = id;
            this.mCount = count;
        }

        @Override
        protected Void doInBackground(Context... params) {
            try {

                // Log to Google Analytics
                @SuppressWarnings("static-access")
                Tracker myExistingTracker = EasyTracker.getInstance(mContext);
                if (myExistingTracker != null) {
                    myExistingTracker.send(MapBuilder.createEvent(mCategory, mCommand, mCode, (long) 0).build());
                }

                if (Constants.USAGE_CATEGORY_COHORT.equals(mCategory)
                        || Constants.USAGE_CATEGORY_COHORT_WEEKLY.equals(mCategory)
                        || Constants.USAGE_CATEGORY_COHORT_MONTHLY.equals(mCategory)) {

                    Amplitude.logEvent(mCode + "_" + mCategory, new JSONObject().put("date", mCommand).put("count", mCount));
                }

            } catch (Exception e) {
            }

            return null;
        }
    }

    public static String getNameFromCode(String Code) {
        String name = Code;
        Action action = BaseAction.getAction(Code);
        if (action.getName() != null) {
            return action.getName();
        } else {

            if (Codes.TAG_URI.equals(Code))
                name = "URI Tag";
            else if (Codes.TAG_URL.equals(Code))
                name = "URL Tag";
            else if (Codes.TAG_VCARD.equals(Code))
                name = "vCard Tag";
            else if (Codes.SHOP_BUYNFCTAGS.equals(Code))
                name = "BuyNFCTags";
            else if (Codes.SHOP_CHIPWAVE.equals(Code))
                name = "Chipwave";
            else if (Codes.SHOP_IDENTIVE.equals(Code))
                name = "Identive";
            else if (Codes.SHOP_NFC_TAG_SHOP.equals(Code))
                name = "NfcTagShop";
            else if (Codes.SHOP_NFCDOG.equals(Code))
                name = "NFCDog";
            else if (Codes.SHOP_NFCWIRELESS.equals(Code))
                name = "NFCWireless";
            else if (Codes.SHOP_NFCZONE.equals(Code))
                name = "NFCZone";
            else if (Codes.SHOP_RAPIDNFC.equals(Code))
                name = "RapidNFC";
            else if (Codes.SHOP_TAGAGE.equals(Code))
                name = "Tagage";
            else if (Codes.SHOP_TAGSFORDROID.equals(Code))
                name = "TagsForDroid";
            else if (Codes.SHOP_TAGSTAND.equals(Code))
                name = "Tagstand More";
            else if (Codes.SHOP_TOPSHOP.equals(Code))
                name = "Topshop";
            else if (Codes.SHOP_ANDROIDBANDS.equals(Code))
                name = "Androidbands";
            else if (Codes.SHARE_EMAIL.equals(Code))
                name = "Share Email";
            else if (Codes.SHARE_GOOGLE_PLUS.equals(Code))
                name = "Share Google+";
            else if (Codes.SHARE_SMS.equals(Code))
                name = "Share SMS";
            else if (Codes.SHARE_TWITTER.equals(Code))
                name = "Share Twitter";

            return name;
        }

    }

    public static void updateRanCount(Context context) {
        long totalTasks = getRanCount(context);
        Logger.d("Num tasks run " + totalTasks++);
        SettingsHelper.setPrefLong(context, Constants.PREF_TASK_RUN_COUNT, totalTasks++);
    }

    public static long getRanCount(Context context) {
        return SettingsHelper.getPrefLong(context, Constants.PREF_TASK_RUN_COUNT, 0);
    }

    public static void logRanCount(Context context) {
        if (canLogData(context)) {
            logUserProperties(context, new SuperProperty("total_tasks_ran", getRanCount(context)));
        }
    }
    public static boolean canLogData(Context context) {
        return SettingsHelper.getPrefBool(context, Constants.PREF_USE_ANALYTICS, true);
    }

    public static void storeTuple(Context context, String code, String command, int id) {
        if (canLogData(context)) {
            String longCode = getNameFromCode(code);
            new Usage.logDataPoint(context, Constants.USAGE_CATEGORY_ACTION, longCode, command, id).execute(context);
        }
    }

    public static void storeNamedTuple(Context context, String code, String command, int id) {
        if (canLogData(context)) {
            new Usage.logDataPoint(context, Constants.USAGE_CATEGORY_ACTION, code, command, id).execute(context);
        }
    }


    public static void storeAggregateTuple(Context context, String category, String codes, String command, int id) {
        if (canLogData(context)) {
            new Usage.logDataPoint(context, category, codes, command, id).execute(context);
        }
    }

    public static void storeShopItem(Context context, ShopItem si) {
        if (canLogData(context)) {
            String longCode = "Google Checkout " + si.getName();
            new Usage.logDataPoint(context, Constants.USAGE_CATEGORY_ACTION, longCode, Codes.COMMAND_URL, -2).execute(context);
        }
    }

    private static final String INSTALL_DATE_ACTUAL = "1";
    private static final String INSTALL_DATE_DEFAULT = "2";

    public static void logInstallDate(Context context) {
        String installDate = SettingsHelper.getPrefString(context, Constants.PREF_INSTALL_DATE, "");
        String date_flag = INSTALL_DATE_ACTUAL;

        if (installDate.isEmpty()) {
            String[] data = getInstallData(context);
            installDate = data[0];
            date_flag = data[1];
        }

        new Usage.logDataPoint(context, Constants.USAGE_CATEGORY_INSTALL_DATE, date_flag, installDate, 0).execute(context);
    }

    private static String[] getInstallData(Context context) {
        String[] data = new String[2];
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

        /* Try to pull install date from Package Manager */
        long installed = 0;
        try {
            installed = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (Exception e) {
            Logger.e("Exception getting install date " + e);
        }
        if (installed != 0) {
            /* Got install date from package manager */
            Date install = new Date(installed);
            data[0] = sdfDate.format(install);
            data[1] = INSTALL_DATE_ACTUAL;
        } else {
            /* Use today's date */
            Date now = new Date();
            data[0] = sdfDate.format(now);
            data[1] = INSTALL_DATE_DEFAULT;
        }
        SettingsHelper.setPrefString(context, Constants.PREF_INSTALL_DATE, data[0]);
        return data;
    }

    private static String getDay() {
        return String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
    }

    private static String getMonth() {
        // Month starts at 0.  Pad for visual in analytics
        return String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
    }

    private static String getWeek() {
        return String.valueOf(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
    }

    public static void logAppOpened(Context context) {

        String installDate = SettingsHelper.getPrefString(context, Constants.PREF_INSTALL_DATE, "");
        if (installDate.isEmpty()) {
            String[] data = getInstallData(context);
            installDate = data[0];
        }

        String day = getDay();
        logCohortData(context, PREF_COHORT_OPEN_SENT, day, Constants.USAGE_CATEGORY_COHORT, Constants.USAGE_APP_OPENED, installDate.replace("-", ""), Constants.PREF_OPEN_COUNT);

        String week = getWeek();
        logCohortData(context, PREF_COHORT_WEEK_OPEN, week, Constants.USAGE_CATEGORY_COHORT_WEEKLY, Constants.USAGE_APP_OPENED, installDate.replace("-", ""), Constants.PREF_OPEN_COUNT_WEEKLY);

        String month = getMonth();
        logCohortData(context, PREF_COHORT_MONTH_OPEN, month, Constants.USAGE_CATEGORY_COHORT_MONTHLY, Constants.USAGE_APP_OPENED, installDate.replace("-", ""), Constants.PREF_OPEN_COUNT_MONTHLY);
    }

    public static void logGeneralTag(Context context) {

        String installDate = SettingsHelper.getPrefString(context, Constants.PREF_INSTALL_DATE, "");
        if (installDate.isEmpty()) {
            String[] data = getInstallData(context);
            installDate = data[0];
        }

        String day = getDay();
        logCohortData(context, PREF_COHORT_GENERAL_SENT, day, Constants.USAGE_CATEGORY_GENERAL_TAG, Constants.USAGE_APP_RAN, installDate.replace("-", ""), Constants.PREF_GENERAL_COUNT);
    }

    public static void logAppRan(Context context) {
        String installDate = SettingsHelper.getPrefString(context, Constants.PREF_INSTALL_DATE, "");

        if (installDate.isEmpty()) {
            String[] data = getInstallData(context);
            installDate = data[0];
        }

        String day = getDay();
        logCohortData(context, PREF_COHORT_RAN_SENT, day, Constants.USAGE_CATEGORY_COHORT, Constants.USAGE_APP_RAN, installDate.replace("-", ""), Constants.PREF_RUN_COUNT);

        String week = getWeek();
        logCohortData(context, PREF_COHORT_WEEK_RAN, week, Constants.USAGE_CATEGORY_COHORT_WEEKLY, Constants.USAGE_APP_RAN, installDate.replace("-", ""), Constants.PREF_RUN_COUNT_WEEKLY);

        String month = getMonth();
        logCohortData(context, PREF_COHORT_MONTH_RAN, month, Constants.USAGE_CATEGORY_COHORT_MONTHLY, Constants.USAGE_APP_RAN, installDate.replace("-", ""), Constants.PREF_RUN_COUNT_MONTHLY);
    }

    private static void logCohortData(Context context, String pref, String value, String category, String code, String data, String runPref) {
        if (!SettingsHelper.getPrefString(context, pref, "").equals(value)) {
            SettingsHelper.setPrefString(context, pref, value);

            new Usage.logDataPoint(context, category, code, data, 0, getRunCount(context, runPref)).execute(context);
            // Set count to 1 for this count as we've rolled to the next day/week/month
            setRunCount(context, runPref, 1);
        } else {
            // Increment count
            incrementRunCount(context, runPref);
        }
    }


    public static void incrementRunCount(Context context, String pref) {
        int count = getRunCount(context, pref);
        setRunCount(context, pref, (count + 1));
    }

    public static void setRunCount(Context context, String pref, int value) {
        SettingsHelper.setPrefInt(context, pref, value);
    }

    public static int getRunCount(Context context, String pref) {
        return SettingsHelper.getPrefInt(context, pref, 0);
    }

    public static int getDailyRunCount(Context context) {
        return getRunCount(context, Constants.PREF_RUN_COUNT);

    }

    public static int getWeeklyRunCount(Context context) {
        return getRunCount(context, Constants.PREF_RUN_COUNT_WEEKLY);
    }

    public static int getMonthlyRunCount(Context context) {
        return getRunCount(context, Constants.PREF_RUN_COUNT_MONTHLY);
    }

    public static void logAnalyticsRemoved(Context context) {
        String id = "";
        try {
            id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        if (!id.isEmpty()) {
            Logger.i("Analytics removed");
            new Usage.logDataPoint(context, "analytics_removed", "1", id, 0).execute(context);
        }
    }

    public static final String TRIGGER_NFC = "nfc";
    public static final String TRIGGER_BLUETOOTH = "bluetooth";
    public static final String TRIGGER_WIFI = "wifi";
    public static final String TRIGGER_GEOFENCE = "geofence";
    public static final String TRIGGER_BATTERY = "battery";
    public static final String TRIGGER_TIME = "time";
    public static final String TRIGGER_CHARGER = "charger";
    public static final String TRIGGER_HEADSET = "headset";
    public static final String TRIGGER_CALENDAR = "calendar";
    public static final String TRIGGER_AGENT    = "agent";

    public static void logTrigger(Context context, String action) {
        new Usage.logDataPoint(context, "trigger", "1", action, 0).execute(context);
    }

    public static void startTracking(Context context) {
        try {
            EasyTracker tracker = EasyTracker.getInstance(context);
        } catch (Exception e) {
            Logger.e("Exception starting GA", e);
        }
    }


    public static void startTracker(Activity activity, Context context) {
        try {
            EasyTracker tracker = EasyTracker.getInstance(context);
            if (tracker != null) {
                tracker.activityStart(activity);
                return;
            }

            /* Tracker is null, set context and re-try activityStart */
            EasyTracker.getInstance(context).activityStart(activity);

        } catch (Exception e) {
            Logger.e("Exception starting GA", e);
        }
    }

    public static void stopTracker(Activity activity) {
        try {
            EasyTracker.getInstance(activity).activityStop(activity);
        } catch (Exception e) {
            Logger.e("Exception stopping GA", e);
        }
    }

    public static void dispatchGa() {
        /*try {
            GAServiceManager.getInstance().se
        } catch (Exception e) {
            Logger.e("Exception dispatching GA", e);
        }*/
    }

    public static final String[] NO_DATA = new String[0];

    public static void logPurchaseEvent(Object mixpanel, String event, ShopItem item, boolean flush) {

        JSONObject obj = new JSONObject();
        if (item != null) {
            try {
                obj.put("item_name", item.getName());
            } catch (Exception e) {
            }
        }

        Amplitude.logEvent(event, obj);

    }

    public static void logPurchaseEvent(Object mixpanel, String event, boolean flush, String[]... data) {

        JSONObject obj = new JSONObject();
        for (String[] item : data) {
            try {
                obj.put(item[0], item[1]);
            } catch (Exception e) {
            }
        }
        Amplitude.logEvent(event, obj);
    }

    public static void logEvent(Object mixpanel, String event, boolean flush) {
        logMixpanelEvent(mixpanel, event, flush, new String[0]);
    }

    public static void logMixpanelEvent(Object mixpanel, String event, boolean flush, String[]... data) {

        JSONObject obj = new JSONObject();

        for (String[] item : data) {
            try {obj.put(item[0], item[1]);}
            catch (Exception e) {}
        }
        Amplitude.logEvent(event, obj);

    }

    public static Object getAnalyticsObject(Context context) {
        return null;
    }


    public static void logProfileData(Context context, Person person, String accountName) {

        JSONObject properties = null;
        if (person != null) {

            properties = buildSuperProperties(
                    new SuperProperty("age", getAge(person)), // May be 0
                    new SuperProperty("age_min", (person.getAgeRange() != null) ? person.getAgeRange().getMin() : 0), // May be 0
                    new SuperProperty("age_max", (person.getAgeRange() != null) ? person.getAgeRange().getMax() : 0), // May be 0
                    new SuperProperty("gender", getGenderDisplay(person.getGender())),
                    new SuperProperty("language", person.getLanguage()),
                    new SuperProperty("account", accountName)
            );
        } else {
            properties = buildSuperProperties(
                    new SuperProperty("account", accountName)
            );
        }

        logUserProperties(context, properties);

        Usage.logEvent(getAnalyticsObject(context), "Logged in", true);
    }


    public static void logUserProperties(Context context, JSONObject properties) {
        Amplitude.setUserProperties(properties);
    }

    public static void logUserProperty(Context context, String key, Object data) {
        logUserProperties(context, new SuperProperty(key, data));
    }


    public static void logUserProperties(Context context, SuperProperty... properties) {
        logUserProperties(context, buildSuperProperties(properties));
    }

    public static class SuperProperty {
        public String name;
        public Object value;

        public SuperProperty(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    private static String getGenderDisplay(int input) {
        switch (input) {
            case Person.Gender.MALE:
                return "male";
            case Person.Gender.FEMALE:
                return "female";
            case Person.Gender.OTHER:
            default:
                return "other";
        }
    }

    private static int getAge(Person person) {
        String sBirthday = person.getBirthday();
        if (sBirthday != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date birthday = format.parse(sBirthday);
                Date now = Calendar.getInstance().getTime();
                int age = now.getYear() - birthday.getYear(); // This is up to 1 year off (high) as this is a really lazy calculation
                return age;
            } catch (Exception e) {
                Logger.e("Exception calculating birthday: " + e, e);
            }
        }

        return 0;
    }

    public static JSONObject buildSuperProperties(SuperProperty... input) {
        JSONObject properties = new JSONObject();
        for (int i = 0; i < input.length; i++) {
            try {
                properties.put(input[i].name, input[i].value);
            } catch (Exception e) {
                Logger.e("Exception building super properties", e);
            }


        }
        return properties;
    }

    public static void startSession(Context context) {
        if (!canLogData(context)) {return;}
        Amplitude.startSession();
    }
    public static void endSession(Context context) {
        if (!canLogData(context)) { return;}
        Amplitude.endSession();
    }

    public static void initialize(Context context) {
        if (!canLogData(context)) { return;}
        Amplitude.initialize(context, AMPLITUDE_API_PROD_KEY);
    }
}

