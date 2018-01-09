package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

/**
 * Created by krohnjw on 4/7/2014.
 */
public class BaseIntentService extends IntentService {

    protected long      DEFAULT_DELAY   = 500;
    protected boolean   USE_ALARM       = true;

    protected int       NUM_TASKS_RAN   = 0;
    protected long      WAIT_DELAY      = 5000;

    protected void resetTasksRan() {
        NUM_TASKS_RAN = 0;
    }

    protected void incrementTasksRan() {
        NUM_TASKS_RAN++;
    }

    public BaseIntentService() {
        super("ReceiverIntentService");
    }

    protected void logd(String message) {
        Logger.d(getClass().getSimpleName() + ": " + message);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void executePayload(Context context, String name, String payload, int type) {
        Intent intent = new Intent(context, ParserService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }

        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        intent.setAction("RUN_TASK" + System.currentTimeMillis()); // Ensure a unique pending intent via the action
        intent.putExtra(ParserService.EXTRA_PAYLOAD, payload);
        intent.putExtra(ParserService.EXTRA_TAG_NAME, name);
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, type);

        long delay = DEFAULT_DELAY + (NUM_TASKS_RAN * WAIT_DELAY);
        Logger.d("Setting alarm to run: " + name + " - " + payload + " after " + delay);
        AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)) {
            m.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, PendingIntent.getService(context, 1, intent, 0));
        } else {
            m.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, PendingIntent.getService(context, 1, intent, 0));
        }

        incrementTasksRan();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
