package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.Date;

public class CalendarUtils {

    @SuppressWarnings("deprecation")
    public static void insertCalendarEvent(Context context, String startDate, String endDate, String startTime, String endTime, String title, String reminder) {
        // We have a static start date and start time. Use this to get time in
        // millis
        boolean allDay = true;
        String finalStartDate = startDate;
        if (!startTime.equals("")) {
            allDay = false;
            finalStartDate += " " + startTime;
        }

        String finalEndDate = endDate;
        if (!endTime.equals("")) {
            allDay = false;
            finalEndDate += " " + endTime;
        }

        long startTimeLong = 0;
        try {
            startTimeLong = Date.parse(finalStartDate);
        } catch (Exception e) { /* Do nothing - we have a sane default */
        }
        long endTimeLong = 0;
        try {
            endTimeLong = Date.parse(finalEndDate);
        } catch (Exception e) { /* Do nothing - we have a sane default */
        }


        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Events.CONTENT_URI);
        if (startTimeLong != 0)
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeLong);
        intent.putExtra(Events.ALL_DAY, allDay);
        if (endTimeLong != 0)
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeLong);

        Logger.d("Setting hasAlarm to " + reminder.equals("0"));
        intent.putExtra(CalendarContract.Events.HAS_ALARM, reminder.equals("0") ? false : true);

        intent.putExtra(Events.TITLE, title);
        try {
            context.startActivity(intent);
        } catch (Exception e) { /* no calendar handler*/
            Logger.e("Exception adding calendar event " + e);
        }

    }

    public static void insertCalendarEvent(Context context, long startTime, long endTime, String title) {


        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Events.CONTENT_URI);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
        intent.putExtra(Events.ALL_DAY, false);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
        intent.putExtra(Events.TITLE, title);
        try {
            context.startActivity(intent);
        } catch (Exception e) { /* no calendar handler*/
            Logger.e("Exception adding calendar event " + e);
        }

    }

}
