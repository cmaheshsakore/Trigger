package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.EventConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.NotificationTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by krohnjw on 10/12/2015.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TriggerNotificationListenerService extends NotificationListenerService {
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Logger.d("Not reading message due to unsupported platform version");
            return;
        }

        String packageName = sbn.getPackageName();
        if (TextUtils.equals(packageName, "com.mobiroo.n.sourcenextcorporation.trigger")) {
            return;
        }

        Bundle extras = sbn.getNotification().extras;
        if(extras == null) {
            return;
        }


        // android.title can be null so check before converting to string
        CharSequence charsTitle = extras.getCharSequence("android.title");
        String title = charsTitle == null ? "" : charsTitle.toString();

        // android.text can be null so check before converting to string
        CharSequence charsText = extras.getCharSequence("android.text");
        String text = charsText == null ? "" : charsText.toString();

        Logger.d("NOTIFICATION: Got new notification : \n" + title + "\n " + text);
        Logger.d("Notification: Is title empty? " + TextUtils.isEmpty(title) + " |" + title + "|");
        Logger.d("Notification: Is text empty? " + TextUtils.isEmpty(text) + " |" + text + "|");
        if (TextUtils.isEmpty(title)  && TextUtils.isEmpty(text)) {
            return;
        }

        ArrayList<TaskSet> tasks = DatabaseHelper.getAllNotificationTasks(this);
        if (tasks != null && tasks.size() > 0) {
            for (int i = 0; i < tasks.size(); i++) {
                TaskSet set = tasks.get(i);
                Trigger trigger = set.getTrigger(0);
                EventConfiguration configuration = null;

                try {
                    configuration = EventConfiguration.deserializeExtra(trigger.getExtra(1));
                } catch (JSONException e) {
                    Logger.e("NOTIFICATION: Exception deserializing event in match check: " + e, e);
                }

                if (configuration == null) {
                    continue;
                }


                if (NotificationTrigger.eventMatchesTask(set, title, text)) {
                    if (set.shouldUse()) {
                        Task task = set.getTask(0);
                        String id = task.getId();
                        String name = task.getName();

                        String payload = Task.getPayload(this, id, name);
                        Utils.executePayload(this, name, payload, TaskTypeItem.TASK_TYPE_NOTIFICATION);
                    } else {

                        Logger.d("NOTIFICATION: Skipping task");
                    }
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // http://cdn.shopify.com/s/files/1/0175/8784/products/Number6DontCare400w_large.jpg?v=1377739464
    }
}
