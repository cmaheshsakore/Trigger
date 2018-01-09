package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.HeadsetPluggedReceiver;

public class HeadsetService extends Service {

    HeadsetPluggedReceiver mReceiver;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    
    @Override
    public void onCreate() {
        // Register a battery % receiver
        if (mReceiver == null) {
            mReceiver = new HeadsetPluggedReceiver();
        }

        //checking for the headset trigger is active or not...
        if (SettingsHelper.getPrefBool(getBaseContext(), Constants.PREF_IS_HEADSET_TRIGGER_ACTIVE, false)) {
            Logger.d("HEADSET: 1+ headset tasks active.  Starting headset monitor");
            getBaseContext().registerReceiver(mReceiver,  new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        } else {
            Logger.d("No Headset triggers found");
            this.stopSelf();
        }
    }
    
    @Override
    public void onDestroy() {
        Logger.d("Removing Headset monitor");
        try { getBaseContext().unregisterReceiver(mReceiver); }
        catch (Exception e) { }
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        Log.i("NFCT", "In onBind");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Logger.d(getClass().getSimpleName() + ": Task Removed.  Restarting service");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Context context = getApplicationContext();
            if (context != null) {
                Intent restart = new Intent(getApplicationContext(), getClass());
                restart.setPackage(getPackageName());
                PendingIntent pending = PendingIntent.getService(context, 1, restart, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                m.setExact(AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + 1000,
                        pending);
            }
        }
        super.onTaskRemoved(rootIntent);
    }
}
