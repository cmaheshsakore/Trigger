package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.CalendarTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

/**
 * Created by krohnjw on 1/8/14.
 */
public class CalendarReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("CALENDAR: Calendar update received");
        CalendarTrigger.scheduleNextEvent(context);
    }
}
