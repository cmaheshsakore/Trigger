package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

/**
 * Created by krohnjw on 4/15/2014.
 */
public class BaseService extends Service {

    protected static String mClassName = "BaseIntentService";

    protected static void logd(String message) {
        Logger.d("%s: %s", mClassName, message);
    }

    protected static void logd(String message, Object...args) {
        Logger.d("%s: %s", mClassName, message, args);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClassName = getClass().getSimpleName();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
