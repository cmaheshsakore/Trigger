package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

/**
 * Created by krohnjw on 1/29/14.
 */
public class WakeUtils {

    private static WakeUtils    instance;
    private static Context      mContext;

    public static synchronized WakeUtils getInstance(Context context) {
        if (instance == null) {
            instance = new WakeUtils(context);
        }
        return instance;
    }

    public WakeUtils(Context context) {
        mContext = context;
    }

    private static PowerManager mPowerManager;
    private static WakeLock     mWakeLock;

    public void acquireWakeLock(Context context, String tag) {
        Logger.d("WAKE: Wake lock acquired");
        if ((mWakeLock != null) && (mWakeLock.isHeld())) return;

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        mWakeLock.acquire();
    }

    public void releaseWakeLock() {
        Logger.d("WAKE: Wake lock released");
        if (mWakeLock == null || !mWakeLock.isHeld()) return;
        mWakeLock.release();
    }
}
