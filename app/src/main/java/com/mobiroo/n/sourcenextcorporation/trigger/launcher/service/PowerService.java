package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.PreferenceCategory;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.ChargingTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.SettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.PowerReceiver;

public class PowerService extends Service {

    PowerReceiver mReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("POWER: Command received");
        return Service.START_STICKY;
    }
    
    @Override
    public void onCreate() {
        Logger.d("POWER: OnCreate");
        // Register a battery % receiver
        if (mReceiver == null) {
            mReceiver = new PowerReceiver();
        }
        if (BatteryTrigger.isEnabled(this) || ChargingTrigger.isEnabled(this)) {
            Logger.d("BATTERY: 1+ battery tasks active.  Starting battery monitor");
            getBaseContext().registerReceiver(mReceiver, PowerReceiver.POWER_FILTER);
        }

        if (SettingsHelper.getPrefBool(this, Constants.PREF_USE_FOREGROUND_POWER_SERVICE, SettingsHelper.POWER_SERVICE_DEFAULT)) {
            Logger.d("BATTERY: Calling startForeground");
            startForeground(8701, buildNotification(this));
        }
    }
    
    @Override
    public void onDestroy() {
        Logger.d("Removing battery monitor");
        try {
            getBaseContext().unregisterReceiver(mReceiver);
        } catch (Exception ignored) { }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Logger.d(getClass().getSimpleName() + ": Task Removed.  Restarting service");
        stopForeground(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Context context = getApplicationContext();
            if (context != null) {
                Intent restart = new Intent(getApplicationContext(), getClass());
                restart.setPackage(getPackageName());
                PendingIntent pending = PendingIntent.getService(context, 1, restart, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                m.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pending);
            }

        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i("NFCT", "In onBind");
        return null;
    }

    private Notification buildNotification(Context context) {

        String message;
        if (BatteryTrigger.isEnabled(context) && ChargingTrigger.isEnabled(context)) {
            message = context.getString(R.string.power_service_monitor_both);
        } else if (BatteryTrigger.isEnabled(context)) {
            message = context.getString(R.string.power_service_monitor_battery);
        } else {
            message = context.getString(R.string.power_service_monitor_charging);
        }

        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(SettingsFragment.KEY_CATEGORY, new PreferenceCategory(R.string.general, R.drawable.ic_action_gear));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentText(context.getString(R.string.power_service_disable_notification));
        builder.setContentTitle(message);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_notification_large));
        builder.setSmallIcon(R.drawable.icon_notification_large);
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setTicker(message);
        builder.setOnlyAlertOnce(true);
        builder.setPriority(Notification.PRIORITY_MIN);
        return builder.build();
    }

}
