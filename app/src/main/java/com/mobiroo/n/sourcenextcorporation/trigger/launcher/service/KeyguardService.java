package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

@SuppressWarnings("deprecation")
public class KeyguardService extends Service {

    public static final String ACTION_KEYGUARD = "com.trigger.launcher.service.keyguard.action_keyguard";
    public static final String EXTRA_ACTION = "com.trigger.launcher.service.keyguard.action";
    
    public static final int EXTRA_ACTION_KEYGUARD_START = 1;
    public static final int EXTRA_ACTION_KEYGUARD_STOP = 0;
    
    private final int FG_ID = 8584939;
    
    private KeyguardManager mManager;
    private KeyguardLock mLock;
    private Context mContext;
    
    private String KEYGUARD_TAG = "NFC Task Launcher";
    
    private boolean mIsRunning;
    
    private void enableKeyguard() {
        mIsRunning = false;
        Logger.d("KEYGUARD : Enabling Keyguard");
        
        if (mLock != null) {
            Logger.d("KEYGUARD : Lock is valid, re-enabling");
            mLock.reenableKeyguard();
            mLock = null;
        }
        
        Logger.d("KEYGUARD : Calling stop foreground");
        stopForeground(true);
    }
    
    private void disableKeyguard() {
        mIsRunning = true;
        if (mLock == null) {
            Logger.d("KEYGUARD : Getting new keyguard lock");
            mLock = getKeyguardManager().newKeyguardLock(KEYGUARD_TAG);
        }
        
        try {
            Logger.d("KEYGUARD : Disabling keyguard");
            mLock.disableKeyguard();
        } catch (Exception e) {
            Logger.e("Exception disabling keyguard", e);
        }
    }
    
    private KeyguardManager getKeyguardManager() {
        if (mManager == null) {
            Logger.d("KEYGUARD : Getting new keyguard manager");
            mManager = (KeyguardManager) mContext.getSystemService(Activity.KEYGUARD_SERVICE);
        }
        return mManager;
    }
    @Override 
    public void onCreate() {
        mContext = getBaseContext();
        registerReceiver(ScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        Logger.d("KEYGUARD : on StartCommand Starting foreground");
        startForeground(FG_ID, buildNotification());
        
        Logger.d("KEYGUARD : on StartCommand Disabling Keyguard");
        disableKeyguard();
        
        return Service.START_STICKY;

    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsRunning = false;
        enableKeyguard();
        unregisterReceiver(ScreenReceiver);
    }
    
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentText("Tap to show lock screen");
        builder.setContentTitle("Lock screen hidden");
        builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.icon_notification_large));
        builder.setSmallIcon(R.drawable.icon_notification_large);
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, ActionService.class).putExtra(ActionService.EXTRA_PAYLOAD, Constants.COMMAND_ENABLE + ":" + Constants.MODULE_KEYGUARD), PendingIntent.FLAG_ONE_SHOT));
        builder.setTicker("Lock screen hidden");
        return builder.build();
    }

    private BroadcastReceiver ScreenReceiver = new BroadcastReceiver() {
        
        private String SCREEN_ON =      "android.intent.action.SCREEN_ON";
        private String mAction;
    
        @Override
        public void onReceive(Context context, Intent intent) {
            mAction = intent.getAction();
            Logger.d("KEYGUARD: Got action " + mAction);
            if (SCREEN_ON.equals(mAction)) {
                
                if (mIsRunning) {
                    /* We're running, so the lock screen should be hidden, 
                     * try to hide 
                     */
                    disableKeyguard();
                }
            }
        }
        
    };
    
}
