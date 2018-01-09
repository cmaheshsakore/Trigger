package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.AsyncTask;

@SuppressWarnings("deprecation")
public class KeyguardLocker extends AsyncTask<Integer, Void, Void> {

    private Context mContext;
    private KeyguardManager mKeyguardManager;
    private KeyguardLock mLock;
    
    public KeyguardLocker(Context context, KeyguardManager manager) {
        mContext = context;
        mKeyguardManager = manager;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        int operation = params[0];

        if (mKeyguardManager == null) {
            Logger.d("Creating new keyguardManager");
            mKeyguardManager = (KeyguardManager) mContext.getSystemService(Activity.KEYGUARD_SERVICE);
        }

        if (mLock == null) {
            Logger.d("Creating new lock");
            mLock = mKeyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE);
        }

        if (operation == Constants.OPERATION_ENABLE) {

            Logger.d("Enabling Keyguard");
            try {
                mLock.reenableKeyguard();
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception enabling keyguard", e);
            }

            mLock = null;

        } else {
            try {
                mLock.disableKeyguard();
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception disabling keyguard", e);
            }
        }
        return null;
    }

    public void disableKeyGuard() {
        try {
            mLock.disableKeyguard();
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception disabling keyguard", e);
        }
    }
    
    public void enableKeyGuard() {
        try {
            mLock.reenableKeyguard();
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception disabling keyguard", e);
        }
    }
    
    protected void onPostExecute(final Void unused) {

    }

}