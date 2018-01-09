package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.EventConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.CalendarTriggerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by krohnjw on 1/8/14.
 */
public class CalendarTrigger extends Trigger {

    private static HashMap<String, String> accountMap;

    public CalendarTrigger() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot instantiate through default constructor");
    }

    public static String[] getAccounts(Context context) {

        Cursor c = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{CalendarContract.Calendars.ACCOUNT_NAME}, null, null, null);
        ArrayList<String> accounts = new ArrayList<String>();
        if (c != null) {
            while (c.moveToNext()) {
                if (!accounts.contains(c.getString(0)))
                    accounts.add(c.getString(0));
            }
        }
        return accounts.toArray(new String[accounts.size()]);
    }

    public static String serializeExtraFromView(View v) throws JSONException {
        JSONObject output = new JSONObject();

        // Store name
        output.put(EventConfiguration.KEY_NAME, ((TextInputLayout) v.findViewById(R.id.name)).getEditText().getText().toString());
        output.put(EventConfiguration.KEY_DESCRIPTION, ((TextInputLayout) v.findViewById(R.id.description)).getEditText().getText().toString());
        // Get match types for name ane description
        int match = (((RadioGroup) v.findViewById(R.id.name_option)).getCheckedRadioButtonId()
                == R.id.option_contains) ? EventConfiguration.MATCH_TYPE_CONTAINS
                : EventConfiguration.MATCH_TYPE_MATCHES;
        output.put(EventConfiguration.KEY_NAME_MATCH, match);

        match = (((RadioGroup) v.findViewById(R.id.description_option)).getCheckedRadioButtonId()
                == R.id.option_contains) ? EventConfiguration.MATCH_TYPE_CONTAINS
                : EventConfiguration.MATCH_TYPE_MATCHES;

        output.put(EventConfiguration.KEY_DESCRIPTION_MATCH, match);

        // Add busy and availability
        int value = EventConfiguration.ANY;
        switch (((Spinner) v.findViewById(R.id.availability)).getSelectedItemPosition()) {
            case 1:
                value = EventConfiguration.BUSY;
                break;
            case 2:
                value = EventConfiguration.FREE;
                break;
        }

        output.put(EventConfiguration.KEY_AVAILABILITY, value);

        String account = String.valueOf(EventConfiguration.ANY);
        int position = ((Spinner) v.findViewById(R.id.account)).getSelectedItemPosition();
        switch (position) {
            case 0:
                account = String.valueOf(EventConfiguration.ANY);
                break;
            default:
                account = ((Spinner) v.findViewById(R.id.account)).getItemAtPosition(position).toString();
                break;
        }

        output.put(EventConfiguration.KEY_ACCOUNT, account);
        Logger.d("CALENDAR: Storing " + output.toString());
        return output.toString();
    }





    private static final String[] INSTANCES_PROJECTION = new String[]{
            Instances.TITLE,        // 0
            Instances.BEGIN,        // 1
            Instances.END,          // 2
            Instances.AVAILABILITY, // 3
            Instances._ID,          // 4
            Instances.ALL_DAY,      // 5
            Instances.STATUS,       // 6
            Instances.ORGANIZER,    // 7
            Instances.OWNER_ACCOUNT, // 8
            Instances.SELF_ATTENDEE_STATUS, // 9
            Instances.EVENT_ID,       // 10
            Instances.DESCRIPTION,   // 11
            Instances.SELF_ATTENDEE_STATUS, // 12  - Unsure if this will properly populate.  Will need to test
            Instances.CALENDAR_ID // 13
    };

    public static void scheduleNextEvent(Context context) {
        ArrayList<TaskSet> tasks = DatabaseHelper.getAllCalendarTasks(context);
        List<String> scheduled = new ArrayList<String>();
        List<String> previous = getPreviousEvents(context);
        List<String> remove = new ArrayList<String>();
        Cursor c;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        accountMap = new HashMap<String, String>();
        c = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME},
                null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                accountMap.put(c.getString(0), c.getString(1));
            }
            c.close();
        } else {
            return; // return if we have no accounts listed with events from the calendar
        }


        if (tasks.size() > 0) {
            // Grab all upcoming calendar events within our defined range and schedule matches
            c = getUpcomingEvents(context);
            if (c == null) {
                Calendar now = Calendar.getInstance();
                now.add(Calendar.DAY_OF_YEAR, 5);
                manager.set(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), buildIntent(context, CalendarTriggerService.ID_SCAN, CalendarTriggerService.ACTION_SCAN));
                return;
            }

            c.moveToFirst();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            long endTime = 0;

            do {
                // For each event iterate over the tasks list and see if a trigger matches this event.  If so schedule a pending alarm for that task using its ID in the action
                // and remove it from the set (so that only 1 alarm per task is scheduled
                for (TaskSet task : tasks) {
                    if (eventMatchesTask(c, task)) {
                        // Schedule a wake up to fire this task at the start time of the event
                        if ((c.getColumnCount() > 0) && (c.getCount() > 0)) {
                            String eventId = c.getString(4);
                            String id = task.getTrigger(0).getId();
                            String condition = task.getTrigger(0).getCondition();
                            long time = condition.equals(DatabaseHelper.TRIGGER_CALENDAR_START)
                                    ? c.getLong(1) : c.getLong(2);

                            if (time > Calendar.getInstance().getTimeInMillis()) {
                                String prefix = condition.equals(DatabaseHelper.TRIGGER_CALENDAR_START)
                                        ? CalendarTriggerService.ACTION_PREFIX_START : CalendarTriggerService.ACTION_PREFIX_END;

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(time);

                                // Verify we have no already scheduled an event with this ID
                                if (!scheduled.contains(id)) {
                                    if (condition.equals(DatabaseHelper.TRIGGER_CALENDAR_END)) {
                                        endTime = time;
                                    }
                                    Logger.d("CALENDAR: Scheduling event for " + task.getTrigger(0).getCondition() + " with id: " + eventId + ", Trigger: " + id + " for " + sdf.format(cal.getTime()));
                                    scheduled.add(id);
                                    manager.set(AlarmManager.RTC_WAKEUP, time, buildIntent(context, id, prefix + id));
                                } else if (condition.equals(DatabaseHelper.TRIGGER_CALENDAR_END) && (time < endTime)) {
                                    endTime = time;
                                    Logger.d("CALENDAR: Re-scheduling event for " + condition + " with id: " + eventId + ", Trigger: " + id + " for " + sdf.format(cal.getTime()));
                                    manager.set(AlarmManager.RTC_WAKEUP, time, buildIntent(context, id, prefix + id));
                                } else {
                                    Logger.d("CALENDAR: Ignoring event for event for " + condition + " with id: " + eventId + ", Trigger: " + id + " at " + sdf.format(cal.getTime()) + " as it was previously scheduled");
                                }
                            }

                        }
                    }
                }
            } while (c.moveToNext());

            c.close();
            // Sort scheduled list
            Collections.sort(scheduled);

            for (String s : previous) {
                if (!scheduled.contains(s)) {
                    remove.add(s);
                }
            }

            if (remove.size() > 0) {
                for (String id : remove) {
                    // Cancel any pending intent with this action
                    if (!id.isEmpty()) {
                        Logger.d("CALENDAR: Cancelling any outstanding alarm for " + id);
                        manager.cancel(buildIntent(context, id));
                    }
                }
            }

            saveList(context, scheduled);
        }
    }


    private static PendingIntent buildIntent(Context context, String id) {
        return buildIntent(context, id, CalendarTriggerService.ACTION_PREFIX + id);
    }

    private static PendingIntent buildIntent(Context context, String id, String action) {
        Intent intent = new Intent(context, CalendarTriggerService.class);
        intent.setAction(action);
        intent.putExtra(CalendarTriggerService.EXTRA_ID, id);
        return PendingIntent.getService(context, Integer.parseInt(id), intent, PendingIntent.FLAG_ONE_SHOT);
    }


    final static int DAYS_TO_SCAN = 5;
    final static String PREF_EVENTS = "CalendarTriggerEventsList";

    private static List<String> getPreviousEvents(Context context) {
        String values = SettingsHelper.getPrefString(context, PREF_EVENTS, "");
        return Arrays.asList(values.split(","));
    }

    private static void saveList(Context context, List<String> list) {
        SettingsHelper.setPrefString(context, PREF_EVENTS, TextUtils.join(",", list));
    }

    private static Cursor getUpcomingEvents(Context context) {
        // Grab all events in the next 30 days
        long startMillis = System.currentTimeMillis() - 3000;
        long endMillis = startMillis + DAYS_TO_SCAN * AlarmManager.INTERVAL_DAY;

        // Set up a URI with our start and end times for the query
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);


        String selection = "(" +
                "(((" + Instances.BEGIN + " > " + startMillis + ") AND (" + Instances.BEGIN + " < " + endMillis + "))" +
                " OR ((" + Instances.END + " > " + startMillis + ") AND (" + Instances.END + " < " + endMillis + ")))" +
                " AND ((" + Instances.STATUS + " IS NULL) OR (" + Instances.STATUS + "<>" + Instances.STATUS_CANCELED + ")))";

        return context.getContentResolver().query(builder.build(), INSTANCES_PROJECTION, selection, null, Instances.BEGIN + " ASC");
    }

    private static String tryGetStringFromCursor(Cursor c, int column) {
        String value = "";
        try {
            value = c.getString(column);
            if (value == null) {
                value = "";
            }
        } catch (CursorIndexOutOfBoundsException ignored) {
            return "";
        }

        return value;
    }

    private static int tryGetIntFromCursor(Cursor c, int column) {
        int value = -999999;
        try {
            value = c.getInt(column);
        } catch (CursorIndexOutOfBoundsException ignored) {
            return -999999;
        }

        return value;
    }

    private static boolean eventMatchesTask(Cursor c, TaskSet task) {
        boolean matches = true;
        if (c == null) return false;
        if (task == null || task.getTriggers() == null) return false;

        // Check title
        Trigger trigger = task.getTrigger(0);
        EventConfiguration configuration = null;

        try {
            configuration = EventConfiguration.deserializeExtra(trigger.getExtra(1));
        } catch (JSONException e) {
            Logger.e("CALENDAR: Exception deserializing event in match check: " + e, e);
        }

        if (configuration == null) return false;

        if (!configuration.name.isEmpty()) {
            String name = tryGetStringFromCursor(c, 0);

            Logger.d(String.format("CALENDAR: Checking for name match %1$s, type %2$s", name, configuration.name_match_type));
            if (configuration.name_match_type == EventConfiguration.MATCH_TYPE_CONTAINS) {
                matches = name.toLowerCase().contains(configuration.name.toLowerCase()) || name.toLowerCase().startsWith(configuration.name.toLowerCase());
            } else {
                matches = name.equalsIgnoreCase(configuration.name);
            }
            Logger.d("CALENDAR: Matches is " + matches);
        }

        if (!matches) return false;

        if (!configuration.description.isEmpty()) {
            String description = tryGetStringFromCursor(c, 11);
            Logger.d(String.format("CALENDAR: Checking for description match %1$s, type %2$s", description, configuration.description_match_type));
            if (configuration.description_match_type == EventConfiguration.MATCH_TYPE_CONTAINS) {
                matches = description.toLowerCase().contains(configuration.description.toLowerCase()) || (description.toLowerCase().startsWith(configuration.name.toLowerCase()));
            } else {
                matches = description.equalsIgnoreCase(configuration.description);
            }
            Logger.d("CALENDAR: Matches is " + matches);
        }

        if (!matches) return false;

        if (configuration.availability != EventConfiguration.ANY) {
            int availability = tryGetIntFromCursor(c, 3);
            Logger.d(String.format("CALENDAR: Checking for availability %1$s, %2$s", availability, configuration.availability));
            switch (configuration.availability) {
                case EventConfiguration.BUSY:
                    matches = (availability == Instances.AVAILABILITY_BUSY);
                    break;
                case EventConfiguration.FREE:
                    matches = (availability == Instances.AVAILABILITY_FREE);
                    break;
            }
            Logger.d("CALENDAR: Matches is " + matches);
        }

        if (!matches) return false;

        if (!(String.valueOf(EventConfiguration.ANY).equals(configuration.account))) {
            String id = tryGetStringFromCursor(c, 13);
            String account = "";
            if (!accountMap.isEmpty()) {
                try {
                    account = accountMap.get(id);
                } catch (Exception ignored) {}
            }
            Logger.d(String.format("CALENDAR: Checking account %1$s against %2$s", configuration.account, account));
            matches = (account != null) && (!account.isEmpty()) && (account.equalsIgnoreCase(configuration.account));
            Logger.d("CALENDAR: Matches is " + matches);
        }

        return matches;
    }
}
