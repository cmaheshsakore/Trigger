package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TimeTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.NotificationSoundAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.RingerSoundAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.TimePickerFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by krohnjw on 1/24/14.
 */
public class SilenceExample extends BaseExample implements Example {
    private FragmentManager mManager;

    public SilenceExample(int name, int icon, int description, boolean isPro, String tag) {
        super(name, icon, description, isPro, tag);
    }

    @Override
    public void showConfigurationDialog(final Context context, FragmentManager manager) {
        // Pop a configuration dialog that asks for time to go to sleep and time to wake up
        mManager = manager;

        final SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.hideAllTitles();
        final View child = View.inflate(context, R.layout.include_example_silence, null);

        child.findViewById(R.id.start).setOnClickListener(timeClicked);
        child.findViewById(R.id.end).setOnClickListener(timeClicked);

        dialog.setChildView(child);
        dialog.setPositiveButton(context.getString(R.string.menu_done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = ((TextView) child.findViewById(R.id.start)).getText().toString();
                String end = ((TextView) child.findViewById(R.id.end)).getText().toString();
                Intent intent = new Intent();
                intent.putExtra("key1", start);
                intent.putExtra("key2", end);
                createExample(context, intent);
                dialog.dismiss();
            }
        });
        dialog.show(manager, getTag());

    }


    private View.OnClickListener timeClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final TextView time = (TextView) v;
            DialogFragment newFragment = new TimePickerFragment() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Calendar now = Calendar.getInstance();
                    now.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    now.set(Calendar.MINUTE, minute);
                    time.setText(TimeTask.time_12.format(now.getTime()));
                }
            };
            Bundle args = new Bundle();
            args.putBoolean(TimePickerFragment.EXTRA_24_HOUR, false);
            newFragment.setArguments(args);
            newFragment.show(mManager, "timePicker");
        }
    };

    @Override
    public void createExample(Context context, Intent intent) {
        super.createExample(context, intent);

        // Create two time tasks.  One named Goodnight and one named Good morning
        Date start = null;
        try { start = TimeTask.time_12.parse(intent.getStringExtra("key1")); }
        catch (Exception e) { Logger.e("Exception parsing start time: " + e, e); }

        Date end = null;
        try { end = TimeTask.time_12.parse(intent.getStringExtra("key2")); }
        catch (Exception e) { Logger.e("Exception parsing start time: " + e, e); }

        if ((start != null) && (end != null)) {

            String days = Calendar.SUNDAY + "," +
                    Calendar.MONDAY + "," +
                    Calendar.TUESDAY + "," +
                    Calendar.WEDNESDAY + "," +
                    Calendar.THURSDAY + "," +
                    Calendar.FRIDAY + "," +
                    Calendar.SATURDAY + "";

            Task task = new Task("", context.getString(R.string.example_task_night), "", null);

            task.addAction(new SavedAction(Constants.COMMAND_NOTIFICATION_VOLUME + ":0", context.getString(R.string.listSoundNotificationVolume), "0", new NotificationSoundAction().getCode()));
            task.addAction(new SavedAction(Constants.COMMAND_RINGER_VOLUME + ":0", context.getString(R.string.listSoundRingerVolume), "0", new RingerSoundAction().getCode()));
            task.addAction(new SavedAction(Constants.COMMAND_RINGER_TYPE+ ":" + Constants.RINGER_TYPE_SILENT, context.getString(R.string.soundRingerTypeText), context.getString(R.string.ringerTypeSilent), new RingerSoundAction().getCode()));

            String id = DatabaseHelper.saveTask(context, task, null, false);
            // Time is extra 1, days are extra 2
            Trigger trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_TIME, DatabaseHelper.TRIGGER_TIME, TimeTask.time_24.format(start), days, id);

            DatabaseHelper.saveTrigger(context, trigger, id);

            // do the same for end

            task = new Task("", context.getString(R.string.example_task_morning), "", null);

            task.addAction(new SavedAction(Constants.COMMAND_NOTIFICATION_VOLUME + ":7", context.getString(R.string.listSoundNotificationVolume), "7", new NotificationSoundAction().getCode()));
            task.addAction(new SavedAction(Constants.COMMAND_RINGER_VOLUME + ":15", context.getString(R.string.listSoundRingerVolume), "15", new RingerSoundAction().getCode()));
            task.addAction(new SavedAction(Constants.COMMAND_RINGER_TYPE+ ":" + Constants.RINGER_TYPE_NORMAL, context.getString(R.string.soundRingerTypeText), context.getString(R.string.ringerTypeNormal), new RingerSoundAction().getCode()));

            id = DatabaseHelper.saveTask(context, task, null, false);
            // Time is extra 1, days are extra 2
            trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_TIME, DatabaseHelper.TRIGGER_TIME, TimeTask.time_24.format(end), days, id);

            DatabaseHelper.saveTrigger(context, trigger, id);

            TimeTrigger.scheduleTimeTasks(context);

            Toast.makeText(context, String.format(context.getString(R.string.example_created), context.getString(R.string.example_task_night)), Toast.LENGTH_LONG).show();
            Toast.makeText(context, String.format(context.getString(R.string.example_created), context.getString(R.string.example_task_morning)), Toast.LENGTH_LONG).show();
        }
    }
}
