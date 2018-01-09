package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;

/**
 * Created by krohnjw on 9/12/2014.
 */
public class TimezoneChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            TimeTrigger.scheduleTimeTasks(context);
        }
    }
}
