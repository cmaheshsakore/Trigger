package com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener;

import java.util.ArrayList;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;

public interface TriggerDataChangedListener {
    public void TriggerUpdated(Trigger trigger);
    public void ConstraintsUpdated(ArrayList<Constraint> constraints);
}
