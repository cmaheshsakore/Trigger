package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.AlarmClock;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmForwardAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_SET_ALARM_FORWARD;
    }

    @Override
    public String getCode() {
        return Codes.ALARM_SET_FORWARD;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option027, null, false);
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.AlarmTimeForward)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            ((EditText) dialogView.findViewById(R.id.alarmMessage)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));   
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Dynamic Alarm";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String messageText = "";
        String sfTime = "";
        String action = "";

        EditText et = (EditText) actionView.findViewById(R.id.AlarmTimeForward);
        EditText messagebox = (EditText) actionView.findViewById(R.id.alarmMessage);
        if ((et != null) && (messagebox != null)) {
            if (!messagebox.getText().toString().equals(""))
                messageText = messagebox.getText().toString();

            sfTime = et.getText().toString();
            if (sfTime.isEmpty()) {
                sfTime = "30";
            }

            action = Constants.COMMAND_SET_ALARM_FORWARD + ":" + sfTime + ":" + messageText;

            CheckBox showUI = (CheckBox) actionView.findViewById(R.id.checkShowUI);
            if ((showUI != null) && (showUI.isChecked())) {
                action += ":1";
            }
        }

        return new String[] { action, context.getString(R.string.layoutAlarmSetText), sfTime + " " + context.getString(R.string.layoutAlarmForwardText)};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.layoutAlarmSetText);
        try {
            display += " " + args[0] + context.getString(R.string.layoutAlarmForwardText);
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        String minutes = Utils.tryParseString(args, 1, "");
        String message = Utils.tryParseString(args, 2, "");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, minutes),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, message)
                );
    }
    
    @Override
    public void performAction(final Context context, int operation, String[] args, int currentIndex) {
        try {
            String sfTime = args[1];
            
            final Time time = new Time();
            time.setToNow();
            
            try {
                time.minute += Integer.parseInt(sfTime);
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Error parsing time " + e);
            }
            time.normalize(true);

            String mMinute = "" + time.minute + "";
            if (time.minute < 10)
                mMinute = "0" + time.minute;

            final String message = Utils.tryParseString(args, 2, "");
            String showUI = Utils.tryParseString(args, 3, "");
            final String uiParam = showUI;

            Logger.d("Set Alarm to " + time.hour + ":" + mMinute + ", " + message);

            operationSetAlarm(context, time.hour, time.minute, message, uiParam);

            // Start the activity a second time after we give
            // everything a second to propogate as the handler DOES
            // NOT enable the alarm unless
            // it ALREADY exists (this is stupid) and it doesn't
            // accept a flag to override this
            // This works GREAT for AOSP devices. It creates two
            // alarms in Sense devices (although only one fires,
            // it's a UI annoyance)

            boolean setTwice = Utils.shouldSetAlarmTwice(context);

            if (setTwice) {
                final Handler handler = new Handler();
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                operationSetAlarm(context, time.hour, time.minute, message, uiParam);
                            }
                        });
                    }
                }, 2000);
            }

            setupManualRestart(currentIndex + 1, 4);

        } catch (Exception e) {
            Logger.e("Exception setting alarm", e);
        }
        
    }

    @SuppressLint("InlinedApi") 
    private void operationSetAlarm(Context context, int hour, int minute, String message, String showUI) {
        Intent intent = new Intent();
        intent.setAction(AlarmClock.ACTION_SET_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!showUI.equals("1"))
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        else
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);

        intent.putExtra(AlarmClock.EXTRA_MESSAGE, message);
        if (Utils.isHandlerPresentForIntent(context, intent)) {
            context.startActivity(intent);
        } else {
            Logger.d("No handler found for alarm intent");
        }

    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetAlarm) + " " + Utils.getForwardTimeFromArgs(context, mArgs);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionAlarm) + " " + Utils.getForwardTimeFromArgs(context, mArgs);
    }
    
    

}
