package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TimerExpiredActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.CallbackService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class TimerAction extends BaseAction {

    private String mName;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SET_ALARM_TIMER;
    }

    @Override
    public String getCode() {
        return Codes.ALARM_SET_TIMER;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option042, null, false);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.AlarmTimeForwardMinutes)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            ((EditText) dialogView.findViewById(R.id.AlarmTimeForwardSeconds)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Timer";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText et = (EditText) actionView.findViewById(R.id.AlarmTimeForwardMinutes);
        EditText et2 = (EditText) actionView.findViewById(R.id.AlarmTimeForwardSeconds);
        String sfTimeMinutes = et.getText().toString();
        if (sfTimeMinutes.length() < 1) {
            sfTimeMinutes = "00";
        }
        String sfTimeSeconds = et2.getText().toString();
        if (sfTimeSeconds.length() < 1) {
            sfTimeSeconds = "00";
        } else if (sfTimeSeconds.length() < 2) {
            sfTimeSeconds = "0" + sfTimeSeconds;
        }

        return new String[] { Constants.COMMAND_SET_ALARM_TIMER + ":" + sfTimeMinutes + ":" + sfTimeSeconds, context.getString(R.string.layoutAlarmSetTimerText), sfTimeMinutes + ":" + sfTimeSeconds};
    }


    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.layoutAlarmSetTimerText);
        try {
            display += " " + args[0] + ":" + args[1];
        } catch (Exception e) {
            /* Ignore any exception here */
        }

        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "0")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, "00"))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String sfTimeMinutes = "00";
        String sfTimeSeconds = "00";
        try {
            if (args[1].length() > 0)
                sfTimeMinutes = args[1]; // Time in minutes
            if (args[2].length() > 0)
                sfTimeSeconds = args[2]; // Time in seconds
        } catch (Exception e) { }

        int seconds = 0;
        if (!sfTimeMinutes.equals("0")) {
            try {
                int minutes = Integer.parseInt(sfTimeMinutes);
                seconds = minutes * 60;
            } catch (Exception e) {
            }
        }
        if (!sfTimeSeconds.equals("0")) {
            try {
                int tSeconds = Integer.parseInt(sfTimeSeconds);
                seconds += tSeconds;
            } catch (Exception e) {
            }
        }

        Logger.d("Set Timer to " + sfTimeMinutes + ":" + sfTimeSeconds);
        operationSetTimer(context, seconds, sfTimeMinutes, sfTimeSeconds);
    }

    @SuppressLint("InlinedApi") 
    private String operationSetTimer(Context context, int timeout, String minutes, String seconds) {
        // Get default Alarm URI
        // Timeout is in seconds

        Calendar time = Calendar.getInstance();
        long currentTimeMs = System.currentTimeMillis();
        time.setTimeInMillis(currentTimeMs);

        time.add(Calendar.SECOND, timeout);

        String timerExpires = "00:00:00";

        Date expires = time.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());

        timerExpires = sdf.format(expires);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Generating random ID
        final long timeMs = Calendar.getInstance().getTimeInMillis() % 1000000;
        // Generatung notification ID for updates and final notification
        final int nid = Integer.parseInt(Long.toString(timeMs % 32000));

        // Schedule tuner to fire
        Intent receiver = new Intent(context, TimerExpiredActivity.class);
        receiver.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT > 10) {
            receiver.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        receiver.putExtra(TimerExpiredActivity.EXTRA_NID, nid);
        // If name is present then send that as an extra
        if (mName != null) {
            receiver.putExtra(TimerExpiredActivity.EXTRA_NAME, mName);
        }

        if ((seconds != null) && (minutes != null) && (!seconds.isEmpty()) && (!minutes.isEmpty())) {
            receiver.putExtra(TimerExpiredActivity.EXTRA_ELAPSED, String.format(context.getString(R.string.timer_elapsed), minutes, seconds));
        }

        receiver.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent fired = PendingIntent.getActivity(context, Integer.parseInt(Long.toString(timeMs)), receiver, PendingIntent.FLAG_ONE_SHOT);

        // Start count down for notification
        long timeOutMs = time.getTimeInMillis();
        long totalTimeMs = timeOutMs - currentTimeMs;
        final long step = (totalTimeMs > CustomCountDownTimer.mTickMinute) ? CustomCountDownTimer.mTickMinute : CustomCountDownTimer.mTickSecond;
        CustomCountDownTimer timer = new CustomCountDownTimer(totalTimeMs, step, timeOutMs, nid, timerExpires, context, fired);
        timer.start();
        
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), fired);

        return timerExpires;

    }

    private class CustomCountDownTimer extends CountDownTimer {

        public static final long     mTickMinute = 60000;
        public static final long     mTickSecond = 1000;

        private int                 mNid;
        private Context             mContext;
        private long                mStep;
        private NotificationManager mManager;
        private String              mExpires;
        private PendingIntent       mPendingIntent;
        private boolean             mHasShownNotification;
        
        private SharedPreferences   mPrefs;
        private boolean             mShowNotification;
        public CustomCountDownTimer(long millisInFuture, long countDownInterval, long alarmFiresMs, int nid, String expires, Context context, PendingIntent intent) {
            super(millisInFuture, countDownInterval);
            mStep = countDownInterval;
            mContext = context;
            mNid = nid;
            mExpires = expires;
            mPendingIntent = intent;
            mHasShownNotification = false;
            mManager = (NotificationManager)  mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }


        @Override
        public void onFinish() {
            ((NotificationManager)  mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(mNid);

        }
        @Override
        public void onTick(long millisUntilFinished) {
            if (!mShowNotification) {
                mHasShownNotification = true;
                Notification notification = buildTimerNotification(mContext, "", mExpires, mPendingIntent, mStep, mNid);
                mManager.notify(mNid, notification);
            }

        }

    }

    private static Notification buildTimerNotification(Context context, String value, String subtext, PendingIntent intent, long step, int id) {

        CharSequence tickerText = (value == null) ? "Not Set" : value; // ticker-text

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(String.format(context.getString(R.string.notification_timer_extra_info), subtext));
        builder.setContentText(context.getString(R.string.notification_timer_cancel));

        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_clock_white));
        builder.setSmallIcon(R.drawable.icon_notification_large);
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        if (step > CustomCountDownTimer.mTickSecond) {
            builder.setTicker(tickerText);
        }
        
        Intent clicked = new Intent(context, CallbackService.class);
        clicked.setAction(CallbackService.ACTION_CANCEL_TIMER + "_" + System.currentTimeMillis());
        clicked.putExtra(CallbackService.EXTRA_PENDING_INTENT, intent);
        clicked.putExtra(CallbackService.EXTRA_NOTIFICATION_ID, id);

        builder.setDeleteIntent(PendingIntent.getService(context, 0, clicked, PendingIntent.FLAG_ONE_SHOT));

        return builder.build();

    }


    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetTimer) + " " + getTimeFromArgs();
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionTimer) + " " + getTimeFromArgs();
    }

    public void setTimerName(String name) {
        mName = name;
    }
    
    /**
     * Takes a parsed int (default to 10 on exception) and returns a formatted value for display of two digits.
     * original is used in display in the case that value contains the default value instead of an actual int
     * representation due to the parse throwing an exception.
     * @param value
     * @param original
     * @return
     */
    private String padValue(int value, String original) {
        if (value >= 10) {
            return original;
        } else if ((value > 0) && (value < 10)) {
            return "0" + original;
        } else {
            return "00";
        }
    }
    
    private String getTimeFromArgs() {
       return padValue(Utils.tryParseInt(mArgs, 1, 10), mArgs[1]) + ":" + padValue(Utils.tryParseInt(mArgs, 2, 10), mArgs[2]);
    }
}
