package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TimeTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.WakeUtils;

public class TimeTaskService extends Service {

    public static final String EXTRA_TASK   = "TimeTaskService.EXTRA_TASK";
    public static final String EXTRA_ID     = "TimeTaskService.EXTRA_ID";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SettingsHelper.loadPreferences(this);
        Logger.d("TimeTask: Service fired");
        WakeUtils.getInstance(this).acquireWakeLock(this, "nfctl-timer");

        if (!IabClient.checkLocalUnlockOrTrial(this)) {
            Logger.d("TimeTask: User is not authorized for this feature");
            WakeUtils.getInstance(this).releaseWakeLock();
            this.stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            Logger.d("TimeTask: Intent received");

            TimeTask task = null;
            Logger.d("TimeTask: Listing all extras");
            Utils.logExtras("TimeTask", intent);

            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                String[] parts = action.split("_");
                if (parts.length > 1) {
                    task = DatabaseHelper.getTimeTaskFromId(
                            this,
                            parts[1]);
                }
            }
            if (task == null) {
                if (intent.hasExtra(EXTRA_ID)) {
                    // Prefer ID
                    String id = intent.getStringExtra(EXTRA_ID);

                    Logger.d("TimeTask: Has ID " + id);

                    if (id != null) {
                        task = DatabaseHelper.getTimeTaskFromId(
                                this,
                                id);
                        Logger.d("TimeTask: Task loaded, is null ? " + (task == null));
                    }
                }
            }

            if (task == null) {
                // Missed on ID or could not load.  Try to grab from intent
                try {
                    Logger.d("TimeTask: Missed loading task by ID - grabbing from parcel");
                    task = intent.getParcelableExtra(EXTRA_TASK); 
                    // Re-query task to pick up any edits that have possibly been made
                    task = DatabaseHelper.getTimeTaskFromId(
                            this,
                            task.getId());
                } catch (Exception e) { Logger.e("Exception getting time task from parcel", e); }        	
            }

            if (task != null) {
                Logger.d("TimeTask: Received " + task.getId());

                /* Re-schedule the next alarm */
                TimeTrigger.setAlarmForTask(this, task);

                /* Fire this Task */
                TaskSet set = DatabaseHelper.getTaskSetByTriggerId(this, TaskTypeItem.TASK_TYPE_TIME, task.getId());
                if ((set.getTasks().size() > 0) && (set.getTask(0).isEnabled())) {
                    String name = set.getTask(0).getName();
                    Logger.d("TimeTask: Name is " + name);
                    if (!name.isEmpty()) {
                        if (set.getTrigger(0).constraintsSatisfied(this)) {
                            Logger.d("TimeTask: Starting service with " + task.getTaskId() + ", " + name);
                            String payload = Task.getPayload(this, task.getTaskId(), name);
                            Logger.d("TimeTask: Found payload " + payload);
                            
                            Usage.logTrigger(this, Usage.TRIGGER_TIME);
                            Intent parser = new Intent(this, ParserService.class);
                            parser.putExtra(ParserService.EXTRA_PAYLOAD, payload);
                            parser.putExtra(ParserService.EXTRA_TAG_NAME, name);
                            parser.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_TIME);
                            startService(parser);
                        }
                    }
                }
            }


        } else {
            Logger.d("TimeTask: Intent was null");
        }

        WakeUtils.getInstance(this).releaseWakeLock();
        this.stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
}
