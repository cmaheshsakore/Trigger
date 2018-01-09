package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

public class SettingsHelper {

    public static final String PREFS_NAME = "NFCTaskLauncherPrefs";
    
    final int REQUEST_RATE_LIMITING = 1;
    final int ACTIVITY_PICK_NOTIFICATION = 2;
    final int ACTIVITY_PICK_TIMER = 3;

    public static final boolean RATE_LIMITING_DEFAULT = false;
    public static final boolean LEGACY_PROFILES_DEFAULT = false;
    public static final boolean EXPIRE_SWITCH_DEFAULT = false;
    public static final boolean SEQUENTIAL_CHECK_DEFAULT = false;

    public static final boolean POWER_SERVICE_DEFAULT = (Build.VERSION.SDK_INT >= 19) ? true : false;
    
    /* Use singleton instance of this class 
     * to reference debug setting where context is not available
     */
    private static SettingsHelper instance;
    
    public SettingsHelper() {
    }
    
    public static synchronized SettingsHelper getInstance() {
        if (instance == null) {
            instance = new SettingsHelper();
        }
        return instance;
    }
    
    private boolean mDebugEnabled = false;
    private boolean mExperimentalEnabled = false;
    
    public boolean experimentalEnabled() {
        return mExperimentalEnabled;
    }
    
    public void setExperimentalActions(boolean enabled) {
        mExperimentalEnabled = enabled;
    }
    
    public boolean debuggingEnabled() {
        return mDebugEnabled;
    }
    
    public void setDebugging(boolean enabled) {
        mDebugEnabled = enabled;
    }
    
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, 0);
    }
    
    public static void loadPreferences(Context context) {
        if (context != null) {
            SettingsHelper.getInstance().setDebugging(SettingsHelper.isDebuggingEnabled(context));
            SettingsHelper.getInstance().setExperimentalActions(SettingsHelper.isExperimentalActionsEnabled(context));
        }
    }

    public static String getPrefString(Context context, String prefName) {
        return getPrefString(context, prefName, "");
    }

    public static String getPrefString(Context context, String prefName, String defaultValue) {

        return getSharedPreferences(context).getString(prefName,
                    !prefName.equals(Constants.PREF_NOTIFICATION_URI)
                        ? defaultValue
                        : null
                     );

    }

    public static void setPrefString(Context context, String prefName, String prefValue) {
        getSharedPreferences(context)
        .edit()
        .putString(prefName, prefValue)
        .commit();
    }

    public static boolean getPrefBool(Context context, String prefName) {
        return getSharedPreferences(context).getBoolean(prefName, false);
    }

    public static boolean getPrefBool(Context context, String prefName, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean(prefName, defaultValue);
    }

    public static void setPrefBool(Context context, String prefName, boolean prefValue) {
        getSharedPreferences(context)
        .edit()
        .putBoolean(prefName, prefValue)
        .commit();

    }

    public static long getPrefLong(Context context, String prefName) {
        return getSharedPreferences(context).getLong(prefName, 0);
    }

    public static long getPrefLong(Context context, String prefName, long prefValue) {
        return getSharedPreferences(context).getLong(prefName, prefValue);
    }
    
    public static void setPrefLong(Context context, String prefName, long prefValue) {
        getSharedPreferences(context)
        .edit()
        .putLong(prefName, prefValue)
        .commit();
    }

    public static int getPrefInt(Context context, String prefName) {
        return getPrefInt(context, prefName, 0);

    }

    public static int getPrefInt(Context context, String prefName, int defaultValue) {
        return getSharedPreferences(context).getInt(prefName, defaultValue);
    }

    public static void setPrefInt(Context context, String prefName, int prefValue) {
        getSharedPreferences(context)
        .edit()
        .putInt(prefName, prefValue)
        .commit();
    }

    public static float getPrefFloat(Context context, String prefName) {
        return getPrefFloat(context, prefName, 0f);
    }

    public static float getPrefFloat(Context context, String prefName, float defaultValue) {
        return getSharedPreferences(context).getFloat(prefName, defaultValue);

    }

    public static void setPrefFloat(Context context, String prefName, float prefValue) {
        getSharedPreferences(context)
        .edit()
        .putFloat(prefName, prefValue)
        .commit();
    }
    
    /* Convenience methods for querying settings */

    public static Boolean shouldVibrate(Context context) {
        return getPrefBool(context, Constants.PREF_VIBRATE, false);
    }

    public static Boolean shouldShowNotification(Context context) {
        return getPrefBool(context, Constants.PREF_SHOW_NOTIFICATION, true);
    }

    public static Boolean shouldShowToast(Context context) {
        return getPrefBool(context, Constants.PREF_SHOW_TOAST, false);
    }

    public static Boolean shouldPlaySound(Context context) {
        /* Don't play a sound in ICS+, use User pref in Gingerbread */
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? false : getPrefBool(context, Constants.PREF_PLAY_AUDIO, false);
    }

    public static Boolean isDebuggingEnabled(Context context) {
        return getPrefBool(context, Constants.PREF_DEBUGGING, false);
    }
    
    public static Boolean isExperimentalActionsEnabled(Context context) {
        return true; //getPrefBool(context, Constants.PREF_EXPERIMENTAL, false);
    }
    
    public static boolean useAARInMessage(Context context) {
        // Default AAR to true for all devices in 4.1+
        return Build.VERSION.SDK_INT >= 14;
    }
    
    public static boolean allowMetrics(Context context) {
        return getSharedPreferences(context).getBoolean(Constants.PREF_ALLOW_METRICS, true);
    }
    
}
