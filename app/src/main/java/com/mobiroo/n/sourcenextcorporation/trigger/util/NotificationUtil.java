package com.mobiroo.n.sourcenextcorporation.trigger.util;

/**
 * Created by c-andorid on 1/9/2018.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.R;

import java.util.Date;

public class NotificationUtil {

    public static final String KEY_LAST_NOTIFIED_DATE = "pushmakerLastNotifiedDate";
    public static final int DUPLICATE_NOTIFY_INTERVAL = 3000;
    public static final int NOTIFICATION_REQUEST_CODE = 100;
    public static final int NOTIFICATION_REQUEST_CODE_ERR = 101;

    /**
     * Show notification
     * @param context
     * @param msg
     * @param soundName
     * @param url
     */
    public static void showNotification(Context context, String msg, String soundName, String url)
    {
        // To avoid duplicate notifications in very short interval
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(context);
        long lastNotifiedDate = pref.getLong(KEY_LAST_NOTIFIED_DATE, 0);
        long nowDate = new Date().getTime();

        if (nowDate - lastNotifiedDate < DUPLICATE_NOTIFY_INTERVAL)
        {
            return;
        }

        // Store time interval when last notification is shown
        Editor editor = pref.edit();
        editor.putLong(KEY_LAST_NOTIFIED_DATE, nowDate);
        editor.commit();

        // Setup NotificationCompat
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setTicker(msg);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(msg);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);

        // Store web landing URL to the intent
        Intent startupIntent = new Intent(context, MainActivity.class);
        startupIntent.putExtra("url", url);
        startupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Create PendingIntent
        PendingIntent intent = PendingIntent.getActivity(
                context,
                NOTIFICATION_REQUEST_CODE,
                startupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(intent);

        // Setup sound to notification
        // If sound resource is specified and that exists, ring it.
        if (soundName == null) soundName = "";
        int identifier = context.getResources().getIdentifier(soundName, "raw", context.getPackageName());
        if (identifier != 0)
        {
            builder.setSound(Uri.parse("android.resource://" + context.getPackageName() +"/" + identifier));
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        } else {
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        }

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_REQUEST_CODE, builder.build());
    }

    /**
     * Show error message as a notification
     * @param context
     * @param msg
     */
    public static void showErrorNotification(Context context, String msg)
    {
        // Setup NotificationCompat
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setTicker(msg);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(msg);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setCategory(NotificationCompat.CATEGORY_ERROR);

        // Set empty PendingIntent to notification
        PendingIntent appIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        builder.setContentIntent(appIntent);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_REQUEST_CODE_ERR, builder.build());
    }
}