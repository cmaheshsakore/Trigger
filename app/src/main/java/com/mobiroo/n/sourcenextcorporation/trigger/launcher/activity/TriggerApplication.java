package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

/**
 * Created by krohnjw on 9/16/2014.
 */
public class TriggerApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();

        Usage.initialize(context);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Usage.startSession(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Usage.endSession(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
