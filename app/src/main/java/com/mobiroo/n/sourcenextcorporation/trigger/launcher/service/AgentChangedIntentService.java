package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.AgentChangedReceiver;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import java.util.ArrayList;

/**
 * Created by krohnjw on 7/16/2014.
 */
public class AgentChangedIntentService extends com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers.BaseIntentService {

    private void releaseWake(Intent intent) {
        logd("Releasing wake");
        AgentChangedReceiver.completeWakefulIntent(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        logd("Received intent");
        if (!IabClient.checkLocalUnlockOrTrial(this)) {
            logd("User is not authorized for this feature");
            releaseWake(intent);
            return;
        }

        String guid = intent.hasExtra(AgentChangedReceiver.EXTRA_GUID) ? intent.getStringExtra(AgentChangedReceiver.EXTRA_GUID) : "";
        String action = intent.getAction();

        String desired_condition = (AgentChangedReceiver.ACTION_ACTIVE.equals(action)) ? DatabaseHelper.TRIGGER_AGENT_STARTS : DatabaseHelper.TRIGGER_AGENT_ENDS;

        ArrayList<TaskSet> sets = DatabaseHelper.getAgentTasks(this);

        logd("Checking for tasks for " + guid + " - " + action);
        if ((sets != null) && (sets.size() > 0)) {
            logd("Found " + sets.size() + " tasks");
            for (TaskSet set:  sets) {
                Trigger trigger = set.getTrigger(0);
                Task task = set.getTask(0);
                if (set.shouldUse()) {
                    String trigger_condition = trigger.getCondition();
                    String trigger_guid = trigger.getExtra(1);

                    logd("Task data " + trigger_condition + " for " + trigger_guid);
                    logd("Received " + desired_condition + " for " + guid);
                    if (trigger_condition.equals(desired_condition) && trigger_guid.equals(guid)) {
                        logd("Checking task constraints");
                        if (trigger.constraintsSatisfied(this)) {
                            Usage.logTrigger(this, Usage.TRIGGER_AGENT);

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
