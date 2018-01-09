package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class CallbackService extends Service {

    public static final String ACTION_CANCEL_TIMER =        "CallbackService.action_cancel_timer";
    public static final String EXTRA_PENDING_INTENT =       "CallbackService.pending_intent";
    public static final String EXTRA_NOTIFICATION_ID =      "CallbackService.notification_id";
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Logger.d("CALLBACK: received action " + action);
            if (action.startsWith(ACTION_CANCEL_TIMER)) {
                /* Grab the pending intent from the bundle */
                PendingIntent cancel = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
                int id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                Logger.d("CALLBACK: Cancelling ID " + id);
                cancelTimer(cancel, id);
            }
        }

        this.stopSelf();
        return Service.START_NOT_STICKY;
    }
    
    private void cancelTimer(PendingIntent intent, int id) {
        
        Logger.d("Cancelling timer with ID " + id);
        
        /* Hide updates from countdown */
        SharedPreferences prefs = getBaseContext().getSharedPreferences("timer_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("hide_" + id, true);
        editor.commit();
        
        /* Cancel pending intent */
        AlarmManager manager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(intent);
        
        /* Clear existing notification */
        if (id != -1) {
            NotificationManager nManager = (NotificationManager)  getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nManager.cancel(id);
        }
        
    }

}
