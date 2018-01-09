package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.app.AlarmManager;
import android.content.Context;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TimeTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by krohnjw on 1/24/14.
 */
public class TimeTrigger {

        public static void scheduleTimeTasks(Context context) {
            ArrayList<TimeTask> list = DatabaseHelper.getTimeTasksList(context);
            Logger.d("TIME: Found " + list.size());
            for (TimeTask task : list) {
                Logger.d("TIME: Scheduling wakeup for " + task.getId());
                setAlarmForTask(context, task);
            }
        }

        public static void scheduleTimeTask(Context context, String id, String time, String days, String taskId) {
            Logger.d("TIME: Scheduling wakeup for " + id);
            setAlarmForTask(context, new TimeTask(id, time, days, taskId));
        }

        public static void setAlarmForTask(Context context, TimeTask task) {

            if (task == null) {
                Logger.d("TimeTask is null, returning");
                return;
            }

            String time = task.getTime();
            int[] days = task.getDaysOfWeek(context);
            String id = task.getId();

            Logger.d("TIME: Building alarm for " + id);

            SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm");

            Calendar cal = Calendar.getInstance();

            try {
                Date proposed = timeParser.parse(time);
                Date current = timeParser.parse(cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));

                if ((id != null) && (!id.isEmpty())) {
                    int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                    int pendingDay = -1;

                    for (int i = 0; i < days.length; i++) {
                        int day = days[i];
                        Logger.d("TIME: Checking day " + day);
                        if (day > 0) {
                            if ((day == currentDay) && (current.before(proposed))) {
                            /*
                             * We have a task schedule for later today, don't bother
                             * checking others
                             */
                                Logger.d("TIME: Scheduling for later today");
                                pendingDay = day;
                                break;
                            } else {

                                // We have three possible outcomes here
                                // 1.) We have a day for this event that is AFTER our
                                // current day that will become next
                                // 2.) Current day is the end of it week and we need to
                                // use the smallest day in the list of days as next
                                // 3.) Current day is NOT 7 but we don't have any more
                                // pending events during the week. We need to get the
                                // next smallest day for the next event

                                // Days are 1-7 Sun - Sat

                                int proposedDay = day;

                                if (day <= currentDay) {
                                    // Pad this day one week forward for easier
                                    // comparison
                                    proposedDay = day + 7;
                                }

                                //Logger.d("TIME: Proposed Day is " + proposedDay);
                                //Logger.d("TIME: Pending day is " + pendingDay);

                                // Days that occur PRIOR to our current day are now
                                // shifted one week out
                                // So instead of dealing with 1,2,3,4,5,6 we have
                                // something like the following
                                // Current day is 5
                                // Proposed days are 6,8,9,10,11,12
                                // In this manner we can always use the nearest day to
                                // our current day

                                if (pendingDay == -1) {
                                    pendingDay = proposedDay;
                                } else {
                                    // Check if proposed day occurs before pending day
                                    if (proposedDay < pendingDay) {
                                        pendingDay = proposedDay;
                                    }
                                }
                            }
                        }
                    }

                    if (pendingDay >= 0) {
                    /*
                     * Schedule an alarm for this ID at the specified day and
                     * time
                     */
                        int daysForward = 0;

                        // pending day is either later in the current week
                        // or the following week offset with a 7 day padding

                        // Determine how many days forward this alarm should be
                        // scheduled for
                        daysForward = pendingDay - currentDay;

                        String[] times = time.split(":");
                        Calendar scheduled = Calendar.getInstance();
                        scheduled.add(Calendar.DATE, daysForward);
                        scheduled.set(Calendar.HOUR_OF_DAY, Integer.parseInt(times[0]));
                        scheduled.set(Calendar.MINUTE, Integer.parseInt(times[1]));
                        scheduled.set(Calendar.SECOND, 0);

                        Logger.d("TIME: Scheduling " + task.getId() + " for " + new SimpleDateFormat("MM-dd-yyyy kk:mm").format(scheduled.getTime()));

                        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        manager.set(AlarmManager.RTC_WAKEUP, scheduled.getTimeInMillis(), task.getIntent(context));
                    }

                } else {
                    Logger.d("TIME: ID was empty, skipping");
                }

            } catch (ParseException e) {
                Logger.e("TIME: Exception parsing times", e);
            }
        }


}
