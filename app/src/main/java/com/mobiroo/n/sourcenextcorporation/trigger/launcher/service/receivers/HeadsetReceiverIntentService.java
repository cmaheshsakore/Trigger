package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers;

import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.HeadsetPluggedReceiver;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by krohnjw on 4/7/2014.
 */
public class HeadsetReceiverIntentService extends BaseIntentService {

    protected long DEFAULT_DELAY = 3000;
    protected boolean USE_ALARM = true;


    private final int STATE_PLUGGED     = 1;
    @SuppressWarnings("unused")
    private final int STATE_UNPLUGGED   = 0;
    private final int STATE_UNKNOWN     = -1;

    private final String PREF_LAST_STATE    = "headset_receiver_last_state";
    private final String PREF_LAST_TIME     = "headset_receiver_last_time";

    @Override
    protected void onHandleIntent(Intent intent) {
        logd("Received intent");
        if (!IabClient.checkLocalUnlockOrTrial(this)) {
            logd("User is not authorized for this feature");
            releaseWake(intent);
            return;
        }

        int state = intent.getIntExtra("state", STATE_UNKNOWN);

        logd("state is " + state);

        if (state != STATE_UNKNOWN) {
            boolean check = true;

            int last_state = getLastState(this);
            if (state == last_state) {
                // This is a repeat event of the last seen event.  This shouldn't occur and likely won't result in a desired result
                // Check if we've seen this event in the last
                Calendar last = Calendar.getInstance();
                last.setTimeInMillis(getLastTime(this));

                // Users are seeing this > 30 minutes apart.  Track state internally 100% of time.
                // Don't allow the same state unless it's been a considerable amount of time (2 hrs)?
                // Set a 10 minute time out
                long timeout = 2 * 60 * 60 * 10000;
                if ((Calendar.getInstance().getTimeInMillis() - last.getTimeInMillis()) > timeout) {
                    check = true;
                } else {
                    check = false;
                }
            }

            saveState(this, state);
            saveTime(this, Calendar.getInstance().getTimeInMillis());

            if (!check) {
                logd("Not checking because we received the same event within 10 minutes");
            }

            if (check) {
                ArrayList<TaskSet> sets = DatabaseHelper.getHeadsetTasks(this);

                String desired_condition = (state == STATE_PLUGGED) ? DatabaseHelper.TRIGGER_ON_CONNECT : DatabaseHelper.TRIGGER_ON_DISCONNECT;
                resetTasksRan();
                if ((sets != null) && (sets.size() > 0)) {
                    for (TaskSet set:  sets) {
                        Trigger trigger = set.getTrigger(0);
                        Task task = set.getTask(0);
                        if (set.shouldUse()) {
                            String trigger_condition = trigger.getCondition();
                            logd("Condition is " + trigger_condition);
                            logd("looking for " + desired_condition);
                            if (trigger_condition.equals(desired_condition)) {
                                if (trigger.constraintsSatisfied(this)) {
                                    Usage.logTrigger(this, Usage.TRIGGER_CHARGER);

                                    String id = task.getId();
                                    String name = task.getName();

                                    logd("Got " + id + ", " + name);

                                    String payload = Task.getPayload(this, id, name);

                                    logd("Running task " + name);
                                    logd("Payload is " + payload);

                                    executePayload(this, name, payload, TaskTypeItem.TASK_TYPE_HEADSET);
                                }
                            }
                        }
                    }
                }
            }
        }

        releaseWake(intent);
    }


    private void releaseWake(Intent intent) {
        logd("Releaing wake");
        HeadsetPluggedReceiver.completeWakefulIntent(intent);
    }
    private int getLastState(Context context) {
        return SettingsHelper.getPrefInt(context, PREF_LAST_STATE, STATE_UNKNOWN);
    }

    private long getLastTime(Context context) {
        return SettingsHelper.getPrefLong(context, PREF_LAST_TIME, 0);
    }

    private void saveState(Context context, int state) {
        SettingsHelper.setPrefInt(context, PREF_LAST_STATE, state);
    }

    private void saveTime(Context context, long time) {
        SettingsHelper.setPrefLong(context, PREF_LAST_TIME, time);
    }
}
