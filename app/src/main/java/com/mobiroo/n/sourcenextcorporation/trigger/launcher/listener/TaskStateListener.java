package com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener;

import java.util.ArrayList;

import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;

public interface TaskStateListener {
    public Task getTask(int which);
    public ArrayList<Trigger> getTriggers();
    public void notifyResult(int requestCode, int resultCode, Intent data);
    public boolean isRequestPending();
    public void toggleHelpState(int position);
    public void addClicked(int position);
}
