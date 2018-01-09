package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.TimeTaskService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeTask implements Parcelable {

    private String mId;
    private String mTaskId;
    private String mTime;
    private String mDays;

    public static final SimpleDateFormat time_12 = new SimpleDateFormat("hh:mm a");
    public static final SimpleDateFormat time_24 = new SimpleDateFormat("HH:mm");

    public TimeTask(String id, String time, String days, String taskId) {
        mId = id;
        mTime = time;
        mDays = days;
        mTaskId = taskId;
    }
    
    public String getTime() {
        return mTime;
    }
    
    public String[] getDays() {
        return mDays.split(",");
    }
    
    public String getTaskId() {
        return mTaskId;
    }
    
    public int[] getDaysOfWeek(Context context) {
        String[] days = getDays();
        int[] daysOfWeek = new int[days.length];
        
        for (int i=0; i< days.length; i++) {
            daysOfWeek[i] = getCalendarDayFromDay(context, days[i]);
        }
        
        return daysOfWeek;
    }
    
    public String getId() {
        return mId;
    }
    
    public int getCalendarDayFromDay(Context context, String day) {

        int numeric_day = -1;
        try { numeric_day = Integer.parseInt(day); }
        catch (Exception ignored) { }

        if (numeric_day > -1) {
            return numeric_day;
        } else {
            // Fall back on legacy String check
            if (getString(context, R.string.day_sunday).equals(day)) {
                return Calendar.SUNDAY;
            } else if (getString(context, R.string.day_monday).equals(day)) {
                return Calendar.MONDAY;
            } else if (getString(context, R.string.day_tuesday).equals(day)) {
                return Calendar.TUESDAY;
            } else if (getString(context, R.string.day_wednesday).equals(day)) {
                return Calendar.WEDNESDAY;
            } else if (getString(context, R.string.day_thursday).equals(day)) {
                return Calendar.THURSDAY;
            } else if (getString(context, R.string.day_friday).equals(day)) {
                return Calendar.FRIDAY;
            } else if (getString(context, R.string.day_saturday).equals(day)) {
                return Calendar.SATURDAY;
            }
        }

        return -1;
    }
    
    private String getString(Context context, int resId) {
        return context.getString(resId);
    }
    
    public PendingIntent getIntent(Context context) {
        Intent intent = new Intent(context, TimeTaskService.class);
        intent.setAction("TASK_" + getId());
        intent.putExtra(TimeTaskService.EXTRA_ID, getId());
        intent.putExtra(TimeTaskService.EXTRA_TASK, this);
        Logger.d("Building intent with request Id " + getId());
        Utils.logExtras("TimeTask", intent);
        // TODO: Evaluate using FLAG_UPDATE_CURRENT over FLAG_ONE_SHOT?
        return PendingIntent.getService(context, Integer.parseInt(getId()), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static final String getDayStringFromStoredValue(Context context, String key2) {
        String list = new String();
        String[] days = key2.split(",");
        for (String day: days) {
            if (!list.isEmpty()) {
                list += ",";
            }
            try {
                int numeric_day = Integer.parseInt(day);
                switch(numeric_day) {
                    case Calendar.MONDAY:
                        list += context.getString(R.string.day_monday);
                        break;
                    case Calendar.TUESDAY:
                        list += context.getString(R.string.day_tuesday);
                        break;
                    case Calendar.WEDNESDAY:
                        list += context.getString(R.string.day_wednesday);
                        break;
                    case Calendar.THURSDAY:
                        list += context.getString(R.string.day_thursday);
                        break;
                    case Calendar.FRIDAY:
                        list += context.getString(R.string.day_friday);
                        break;
                    case Calendar.SATURDAY:
                        list += context.getString(R.string.day_saturday);
                        break;
                    case Calendar.SUNDAY:
                        list += context.getString(R.string.day_sunday);
                        break;
                }
            } catch (Exception e) {
                list += day;
            }
        }
        return list;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    
    public static final Creator<TimeTask> CREATOR = new Creator<TimeTask>() {
        @Override
        public TimeTask createFromParcel(Parcel source) {
            return new TimeTask(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString()
                    );
        }

        @Override
        public TimeTask[] newArray(int size) {
            return new TimeTask[size];
        }
    };
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTime);
        dest.writeString(mDays);
        dest.writeString(mTaskId);
    }
}
