package com.mobiroo.n.sourcenextcorporation.trigger.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by krohnjw on 7/17/2014.
 */
public class WearApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String PREFS_FILE = "STATE_TRACKING";
    public static final String SETTING_ACTIVITY_ACTIVE = "state.activity_active";
    public static final String SETTING_ACTIVIY_LAST_ACTIVE = "state.activity_last_active";

    public static void setBoolean(Context context, String key, boolean value) {
        context.getSharedPreferences(PREFS_FILE, 0).edit().putBoolean(key, value).commit();
    }

    public static void setLong(Context context, String key, long value) {
        context.getSharedPreferences(PREFS_FILE, 0).edit().putLong(key, value).commit();
    }

    public static boolean isApplicationActive(Context context) {
        return context.getSharedPreferences(PREFS_FILE, 0).getBoolean(SETTING_ACTIVITY_ACTIVE, false);
    }

    public static long getLastActiveTime(Context context) {
        return context.getSharedPreferences(PREFS_FILE, 0).getLong(SETTING_ACTIVIY_LAST_ACTIVE, 0);
    }

    public static void setApplicationActive(Context context, boolean active) {
        setBoolean(context, SETTING_ACTIVITY_ACTIVE, true);

    }
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
