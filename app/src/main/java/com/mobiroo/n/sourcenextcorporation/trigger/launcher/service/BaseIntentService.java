package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import junit.framework.Assert;

/**
 * Created by krohnjw on 4/15/2014.
 */
public class BaseIntentService extends IntentService {

    protected static String mClassName = "BaseIntentService";
    private static final String WAKE_NAME = "TriggerIntentService.Lock";
    private static volatile PowerManager.WakeLock mLock = null;

    public BaseIntentService(String name) {
        super(name);
    }

    protected static void logd(String message) {
        Logger.d(String.format("%s: %s", mClassName, message));
    }

    protected static void logd(String message, Object...args) {
        Logger.d(String.format("%s: %s", mClassName, String.format(message, args)));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClassName = getClass().getSimpleName();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Assert.fail();
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (mLock == null) {
            PowerManager m = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mLock = m.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_NAME);
            mLock.setReferenceCounted(true);
        }
        return mLock;
    }

    protected void acquireWakeLock(int flags) {
        logd("Acquiring wake lock");
        PowerManager.WakeLock lock = getLock(this.getApplicationContext());
        if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
            lock.acquire();
        }
    }

    protected void releaseWakeLock() {
        logd("Releasing wake lock");
        PowerManager.WakeLock lock = getLock(this.getApplicationContext());
        if (lock.isHeld()) lock.release();
    }
}
