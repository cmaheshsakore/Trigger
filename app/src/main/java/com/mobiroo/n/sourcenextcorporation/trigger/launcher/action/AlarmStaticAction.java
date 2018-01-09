package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.AlarmClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmStaticAction extends BaseAction {

    private int mHour = 12;
    private int mMinute = 0;
    private View mView;
    private Context mContext;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SET_ALARM;
    }

    @Override
    public String getCode() {
        return Codes.ALARM_SET_STATIC;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option026, null, false);
        
        LinearLayout addAlarmLL = (LinearLayout) dialogView.findViewById(R.id.alarmStaticPrimary);
        addAlarmLL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(mContext, mTimeSetListener, mHour, mMinute, false).show();
            }
            
        });
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_ONE) && hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            ((TextView) dialogView.findViewById(R.id.AlarmTime)).setText(
                    arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE) + 
                    ":" +
                    arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO)
                    );
        }
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            ((EditText) dialogView.findViewById(R.id.alarmMessage)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE));   
        }
        
        mView = dialogView;
        mContext = context;
        return dialogView;
    }

    @Override
    public String getName() {
        return "Static Alarm";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        TextView tv = (TextView) actionView.findViewById(R.id.AlarmTime);
        String curText = tv.getText().toString();

        EditText messagebox = (EditText) actionView.findViewById(R.id.alarmMessage);
        String messageText = "";
        if (!messagebox.getText().toString().equals(""))
            messageText = messagebox.getText().toString();

        if (!curText.equals(context.getString(R.string.defaultAlarmText))) {
            String outMinute = "" + mMinute + "";
            if (mMinute < 10)
                outMinute = "0" + mMinute;

            String action = Constants.COMMAND_SET_ALARM + ":" + mHour + ":" + mMinute + ":" + messageText;

            CheckBox showUI = (CheckBox) actionView.findViewById(R.id.checkShowUI);
            if ((showUI != null) && (showUI.isChecked())) {
                action += ":1";
            }
            return new String[] {action, context.getString(R.string.layoutAlarmSetText), mHour + ":" + outMinute };
            
        } else
            return new String[] { "" };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.layoutAlarmSetText);
        try {
            display += " " + args[0] + ":" + args[1];
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Command:h:min:Message
        String[] args = action.split(":");
        String hour = Utils.tryParseString(args, 1, "");
        String minutes = Utils.tryParseString(args, 2, "");
        String message = Utils.tryParseString(args, 3, "");
        
        if (!minutes.isEmpty()) {
            if (Utils.tryParseInt(new String[] { minutes }, 0, 10) < 10) {
                minutes = "0" + minutes;
            }
        }
        
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, hour),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, minutes),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, message)
                );
    }
    
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;

            String outMinute = "" + mMinute + "";
            if (mMinute < 10) {
                outMinute = "0" + mMinute;
            }

            TextView tv = (TextView) mView.findViewById(R.id.AlarmTime);
            tv.setText(mHour + ":" + outMinute);
        }
    };

    @Override
    public void performAction(final Context context, int operation, String[] args, int currentIndex) {
        final int hour = Integer.parseInt(args[1]);
        final int minute = Integer.parseInt(args[2]);

        String outMinute = "" + minute + "";
        if (minute < 10)
            outMinute = "0" + minute;

        final String message = Utils.tryParseString(args, 3, "");

        String showUI = Utils.tryParseString(args, 4, "");
        
        final String uiParam = showUI;

        Logger.d("Set Alarm to " + hour + ":" + outMinute);

        operationSetAlarm(context, hour, minute, message, uiParam);

        // Start the activity a second time after we give everything
        // a second to propogate as the handler DOES NOT enable the
        // alarm unless
        // / it ALREADY exists (this is stupid) and it doesn't
        // accept a flag to override this
        // This works GREAT for AOSP devices. It creates two alarms
        // in Sense devices (although only one fires, it's a UI
        // annoyance)

        boolean setTwice = Utils.shouldSetAlarmTwice(context);

        if (setTwice) {
            final Handler handler = new Handler();
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            operationSetAlarm(context, hour, minute, message, uiParam);
                        }
                    });
                }
            }, 2000);
        }
        
        setupManualRestart(currentIndex +1, 4);
    }
    
    @SuppressLint("InlinedApi") private void operationSetAlarm(Context context, int hour, int minute, String message, String showUI) {
        Intent intent = new Intent();
        intent.setAction(AlarmClock.ACTION_SET_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);

        if (!showUI.equals("1"))
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        else
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);

        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
        if (Utils.isHandlerPresentForIntent(context, intent)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Logger.d("No handler found for alarm intent");
        }

    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetAlarm) + " " + Utils.getTimeFromArgs(mArgs);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionAlarm) + " " + Utils.getTimeFromArgs(mArgs);
    }
    
}
