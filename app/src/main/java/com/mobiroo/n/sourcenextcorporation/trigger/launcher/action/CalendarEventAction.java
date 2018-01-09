package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.Calendar;

public class CalendarEventAction extends BaseAction {

    private View mView;
    private Context mContext;
    private String mWhichTextViewForDialog;
    private int mHour = 0;
    private int mMinute = 0;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_EVENT_CALENDAR_STATIC;
    }

    @Override
    public String getCode() {
        return Codes.EVENT_CALENDAR_STATIC;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option052, null, false);

        CheckBox allDayBox = (CheckBox) dialogView.findViewById(R.id.eventAllDayCheck);
        final TextView startTime = (TextView) dialogView.findViewById(R.id.eventStartTime);
        final TextView endTime = (TextView) dialogView.findViewById(R.id.eventEndTime);
        allDayBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startTime.setVisibility(View.GONE);
                    endTime.setVisibility(View.GONE);
                } else {
                    startTime.setVisibility(View.VISIBLE);
                    endTime.setVisibility(View.VISIBLE);
                }
            }

        });
        
        startTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mWhichTextViewForDialog = "Time1";
                new TimePickerDialog(mContext, mEventTimeSetListener, mHour, mMinute, false).show();
            }
        });
        
        endTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mWhichTextViewForDialog = "Time2";
                new TimePickerDialog(mContext, mEventTimeSetListener, mHour, mMinute, false).show();
            }
        });
        TextView startDate = (TextView) dialogView.findViewById(R.id.eventStartDate);
        startDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mWhichTextViewForDialog = "Date1";
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(mContext, mEventDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        TextView endDate = (TextView) dialogView.findViewById(R.id.eventEndDate);
        endDate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mWhichTextViewForDialog = "Date2";
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(mContext, mEventDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            startDate.setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            endDate.setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            startTime.setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_FOUR)) {
            endTime.setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_FOUR));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_FIVE)) {
            EditText title = (EditText) dialogView.findViewById(R.id.eventTitle);
            title.setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_FIVE));
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_SIX)) {
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_SIX);
            ((CheckBox) dialogView.findViewById(R.id.eventSetReminder)).setChecked("1".equals(value));
        }
        
        
        mView = dialogView;
        mContext = context;
        return dialogView;
        
    }

    @Override
    public String getName() {
        return "Calendar Event";
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        boolean allDay = false;
        CheckBox allDayCheck = (CheckBox) actionView.findViewById(R.id.eventAllDayCheck);
        allDay = allDayCheck.isChecked();
        String startDate = "";
        String endDate = "";
        String startTime = "";
        String endTime = "";
        String eventTitle = "";

        TextView startDateET = (TextView) actionView.findViewById(R.id.eventStartDate);
        TextView endDateET = (TextView) actionView.findViewById(R.id.eventEndDate);

        if (!startDateET.equals(context.getString(R.string.dateHint)))
            startDate = startDateET.getText().toString();

        if (!endDateET.equals(context.getString(R.string.dateHint)))
            endDate = endDateET.getText().toString();

        if (!allDay) {
            TextView startTimeET = (TextView) actionView.findViewById(R.id.eventStartTime);
            TextView endTimeET = (TextView) actionView.findViewById(R.id.eventEndTime);

            if (!startTimeET.equals(context.getString(R.string.dateHint)))
                startTime = startTimeET.getText().toString();

            if (!endTimeET.equals(context.getString(R.string.dateHint)))
                endTime = endTimeET.getText().toString();

            startTime = Utils.encodeData(startTime);
            endTime = Utils.encodeData(endTime);
        }

        EditText titleET = (EditText) actionView.findViewById(R.id.eventTitle);
        eventTitle = titleET.getText().toString();

        eventTitle = Utils.encodeData(eventTitle);

        int reminder = ((CheckBox) actionView.findViewById(R.id.eventSetReminder)).isChecked() ? 1 : 0;
        String message = Constants.COMMAND_EVENT_CALENDAR_STATIC + ":" + startDate + ":" + endDate + ":" + startTime + ":" + endTime + ":" + eventTitle + ":" + reminder;
        return new String[] { message, context.getString(R.string.listDiaplayCalendarEvent), ""};
    }


    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listDiaplayCalendarEvent);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {

        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.decodeData(Utils.tryParseString(args, 3, ""))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_FOUR, Utils.decodeData(Utils.tryParseString(args, 4, ""))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_FIVE, Utils.tryParseString(args, 5, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_SIX, Utils.tryParseString(args, 6, ""))
                );
    }
    
    private DatePickerDialog.OnDateSetListener mEventDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (mWhichTextViewForDialog.equals("Date1")) {
                TextView startDate = (TextView) mView.findViewById(R.id.eventStartDate);
                startDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                // Also check end date. If empty set to now
                TextView endDate = (TextView) mView.findViewById(R.id.eventEndDate);
                if (endDate.getText().toString().equals(mContext.getString(R.string.dateHint)))
                    endDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
            } else if (mWhichTextViewForDialog.equals("Date2")) {
                TextView endDate = (TextView) mView.findViewById(R.id.eventEndDate);
                endDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener mEventTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String sHour = "" + hourOfDay;
            String sMinute = "" + minute;
            if (minute < 10)
                sMinute = "0" + minute;
            
            if (mWhichTextViewForDialog.equals("Time1")) {
                
                TextView startTime = (TextView) mView.findViewById(R.id.eventStartTime);
                startTime.setText(sHour + ":" + sMinute);
                // Also check end time. If empty set to now
                TextView endTime = (TextView) mView.findViewById(R.id.eventEndTime);
                if (endTime.getText().toString().equals(mContext.getString(R.string.timeHint)))
                    endTime.setText(sHour + ":" + sMinute);

            } else if (mWhichTextViewForDialog.equals("Time2")) {
                TextView endTime = (TextView) mView.findViewById(R.id.eventEndTime);
                endTime.setText(sHour + ":" + sMinute);
            }
        }
    };

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String startDate = Utils.tryParseEncodedString(args, 1, "");
        String endDate = Utils.tryParseEncodedString(args, 2, "");
        String startTime = Utils.tryParseEncodedString(args, 3, "");
        String endTime = Utils.tryParseEncodedString(args, 4, "");
        String title = Utils.tryParseEncodedString(args, 5, "");
        title = Utils.removePlaceHolders(title);
        String reminder = Utils.tryParseString(args, 6, "1");
        CalendarUtils.insertCalendarEvent(context, startDate, endDate, startTime, endTime, title, reminder);
        
        setupManualRestart(currentIndex + 1);
        
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetCalendarEvent);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionCalendarEvent);
    }
    
}
