package com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener;

import java.util.ArrayList;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;

public interface TaskDataChangedListener {
    public void     triggersChanged(ArrayList<Trigger> triggers);
    public void     taskChanged(Task task, int tag);
    public void     signalLoadFinished();
}
