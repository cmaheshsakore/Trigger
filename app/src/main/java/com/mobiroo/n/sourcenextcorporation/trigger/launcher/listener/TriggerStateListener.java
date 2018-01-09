package com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener;

import java.util.ArrayList;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;

public interface TriggerStateListener {
    public ArrayList<Constraint> getUpdatedConstraints();
    public Trigger getUpdatedTrigger();

}
