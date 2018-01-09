package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.IntentService;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.CalendarTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;

/**
 * Created by krohnjw on 1/10/14.
 */
public class CalendarTriggerService extends IntentService {

    public static final String ACTION_SCAN = "ACTION_SCAN";
    public static final String ACTION_PREFIX = "CALENDAR_TRIGGER_";
    public static final String ACTION_PREFIX_START = "CALENDAR_TRIGGER_START_";
    public static final String ACTION_PREFIX_END = "CALENDAR_TRIGGER_END_";
    public static final String EXTRA_ID = "trigger_id";

    public static final String ID_SCAN = "-9";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CalendarTriggerService(String name) {
        super(name);
    }

    public CalendarTriggerService() {
        super("NFCTL: Calendar Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SettingsHelper.loadPreferences(this);
        Logger.d("CALENDAR: Service fired");

        //checking if the user is using the trial version or the paid version
        //is its trail version the service is stopped...
        if (!IabClient.checkLocalUnlockOrTrial(this)) {
            Logger.d("User is not authorized for this feature");
            this.stopSelf();
            return;
        }


        if (intent != null) {
            String action = intent.getAction();

            if (!ACTION_SCAN.equals(action)) {

                if (!intent.hasExtra(EXTRA_ID)) {
                    Logger.d("CALENDAR: No id received");
                    return;
                }

                String id = intent.getStringExtra(EXTRA_ID);

                TaskSet set = DatabaseHelper.getTaskSetByTriggerId(this, TaskTypeItem.TASK_TYPE_CALENDAR, id);
                if ((set.getTasks().size() > 0) && (set.getTask(0).isEnabled())) {
                    String name = set.getTask(0).getName();
                    Logger.d("CALENDAR: Name is " + name);
                    if (!name.isEmpty()) {
                        if (set.getTrigger(0).constraintsSatisfied(this)) {
                            Logger.d("CALENDAR: Starting service with " + set.getTask(0).getId() + ", " + name);
                            String payload = Task.getPayload(this, set.getTask(0).getId(), name);
                            Logger.d("CALENDAR: Found payload " + payload);

                            Usage.logTrigger(this, Usage.TRIGGER_CALENDAR);
                            Intent parser = new Intent(this, ParserService.class);
                            parser.putExtra(ParserService.EXTRA_PAYLOAD, payload);
                            parser.putExtra(ParserService.EXTRA_TAG_NAME, name);
                            parser.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_CALENDAR);
                            startService(parser);
                        }
                    }
                }

                Logger.d("CALENDAR: Scheduling");
                CalendarTrigger.scheduleNextEvent(this);

            }
        }
    }
}
