package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by krohnjw on 12/19/13.
 */
public class TimeConstraint extends Constraint implements TimePickerDialog.OnTimeSetListener {

    public TimeConstraint() { }

    public TimeConstraint(String id, String trigger_id, String type, String key1, String key2) {
        super(id, trigger_id, type, key1, key2);
    }

    public TimeConstraint(int type, String key1, String key2) {
        super(type, key1, key2);
    }

    @Override
    public String getType() {
        return String.valueOf(Constraint.TYPE_TIME);
    }

    @Override
    public boolean isConstraintSatisfied(Context context) {
        // Key 1 is the start hours and minutes
        // Key 2 is the end hours and minutes

        logd("Checking constraint");
        String[] start = getExtra(1).split(":");
        int startHour = Integer.parseInt(start[0]);
        int startMinute = Integer.parseInt(start[1]);
        logd("Start: " + startHour + ":" + startMinute);

        Calendar s = Calendar.getInstance();
        s.set(Calendar.HOUR_OF_DAY, startHour);
        s.set(Calendar.MINUTE, startMinute);



        String[] end = getExtra(2).split(":");
        int endHour = Integer.parseInt(end[0]);
        int endMinute = Integer.parseInt(end[1]);
        logd("End: " + endHour + ":" + endMinute);
        Calendar e = Calendar.getInstance();
        e.set(Calendar.HOUR_OF_DAY, endHour);
        e.set(Calendar.MINUTE, endMinute);

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        // There are a few instances in which we need to modify the day that the start / end occurs in

        // If end hour is after start hour then we don't need to modify the day at all

        // if start hour is greater than end hour we have one of two scenarios
        // Start occurred today and end occurs tomorrow (22:00 - 10:00) - current time 23:00
        // Start occurred yesterday and end occurs today (22:00 - 10:00) - current time 08:00


        if ((endHour < startHour) || ((startHour == endHour) && (endMinute <= startMinute))) {
            if ((currentHour > startHour) || ((currentHour == startHour) &&  (currentMinute >= startMinute))) {
                // Our current time is AFTER the start 0-23
                // Add a day to end as it occurs tomorrow
                logd("Moving end day ahead");
                e.add(Calendar.DATE, 1);
            } else {
                // Current time is BEFORE start 0-23
                // Roll start back one day, preserve current day on end
                logd("Setting start day back");
                s.add(Calendar.DATE, -1);
            }
        }


        // Roll the day forward if end time is before or the same time as start time


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        logd("START: " + sdf.format(s.getTime()) + " - " + s.getTimeInMillis());
        logd("NOW  : " + sdf.format(now.getTime()) + " - " + now.getTimeInMillis());
        logd("END  : " + sdf.format(e.getTime()) + " - " + e.getTimeInMillis());


        if ((s.getTimeInMillis() <= now.getTimeInMillis()) && (e.getTimeInMillis() >= now.getTimeInMillis())) {
            logd("Time constraint is OK");
            return true;
        }

        logd("Time constraint is false");
        return false;
    }

    public static String getFormattedTime(String time) {
        String[] start = time.split(":");

        Calendar s = Calendar.getInstance();
        s.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start[0]));
        s.set(Calendar.MINUTE, Integer.parseInt(start[1]));

        return mSdf.format(s.getTime());
    }

    private TextView mStartTimeView;
    private TextView mEndTimeView;

    private final int               TIME_REQUEST_START = 1;
    private final int               TIME_REQUEST_END   = 2;
    private int                     mTimeRequest;

    private int                     mStartHour = 8;
    private int                     mStartMinutes = 0;
    private int                     mEndHour = 20;
    private int                     mEndMinutes = 0;

    @Override
    public View getView(final Context context) {
        View base = super.getView(context);

        View child = View.inflate(context, R.layout.constraint_time, null);

        mStartTimeView = (TextView) child.findViewById(R.id.time_start);
        mStartTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeRequest = TIME_REQUEST_START;
                new TimePickerDialog(context, TimeConstraint.this, mStartHour, mStartMinutes, false).show();
            }

        });

        mEndTimeView = (TextView) child.findViewById(R.id.time_end);
        mEndTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeRequest = TIME_REQUEST_END;
                new TimePickerDialog(context, TimeConstraint.this, mEndHour, mEndMinutes, false).show();
            }

        });

        updateTimeUi();

        addChildToContainer(base, child);

        return base;
    }

    @Override
    public int getText() {
        return R.string.time_task;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_action_clock;
    }

    @Override
    public TimeConstraint buildConstraint(Context context) {
        return new TimeConstraint(Constraint.TYPE_TIME, mStartHour + ":" + mStartMinutes, mEndHour + ":" + mEndMinutes);
    }

    @Override
    public void loadData(Context context, Constraint c) {
        super.loadData(context, c);
        String[] time = c.getExtra(1).split(":");
        try {
            mStartHour = Integer.parseInt(time[0]);
            mStartMinutes = Integer.parseInt(time[1]);
        } catch (Exception e) {

        }

        time = c.getExtra(2).split(":");
        try {
            mEndHour = Integer.parseInt(time[0]);
            mEndMinutes = Integer.parseInt(time[1]);
        } catch (Exception e) {

        }
    }

    @Override
    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        String output = String.format(context.getString(R.string.display_between_times),
                TimeConstraint.getFormattedTime(getExtra(1)),
                TimeConstraint.getFormattedTime(getExtra(2)));

        holder.constraint_time_text.setVisibility(View.VISIBLE);
        holder.constraint_time_text.setText(output);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        switch(mTimeRequest) {
            case TIME_REQUEST_START:
                mStartHour = hourOfDay;
                mStartMinutes = minute;
                setTimeDisplay(mStartTimeView, hourOfDay, minute);
                break;
            case TIME_REQUEST_END:
                mEndHour = hourOfDay;
                mEndMinutes = minute;
                setTimeDisplay(mEndTimeView, hourOfDay, minute);
                break;
        }
    }

    private void setTimeDisplay(TextView view, int hour, int minutes) {
        view.setText(hour + ":" + ((minutes < 10) ? "0" + minutes : minutes));
    }

    private void updateTimeUi() {
        mStartTimeView.setText(TimeConstraint.getFormattedTime(mStartHour + ":" + mStartMinutes));
        mEndTimeView.setText(TimeConstraint.getFormattedTime(mEndHour + ":" + mEndMinutes));
    }

}
