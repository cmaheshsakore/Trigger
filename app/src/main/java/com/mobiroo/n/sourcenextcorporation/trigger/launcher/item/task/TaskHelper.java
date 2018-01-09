package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.ShareTagActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BluetoothTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.GeofenceTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.WifiTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.ArrayList;

public abstract class TaskHelper {
    public static SQLiteDatabase getReadableDatabase(Context context) {
        DatabaseHelper dbt = DatabaseHelper.getInstance(context);
        return dbt.getReadableDatabase();
    }

    public static SQLiteDatabase getWritableDatabase(Context context) {
        DatabaseHelper dbt = DatabaseHelper.getInstance(context);
        return dbt.getWritableDatabase();
    }

    public static void deleteTag(Context context, TaskSet set) {
        Task task = set.getTask(0);
        
        /* Remove all triggers */
        for (Trigger trigger: set.getTriggers()) {
            trigger.delete(context);
        }
        
        /* Delete actions associated with this task and saved task reference */
        task.delete(context);
        
        if (set.getTasks().size() > 1) {
            set.getTask(1).delete(context);
        }
        
        // Re-scan task list to modify receivers appropriately
        for (Trigger trigger: set.getTriggers()) {
            ArrayList<TaskSet> sets = null;
            switch (trigger.getType()) {
                case TaskTypeItem.TASK_TYPE_BLUETOOTH:
                    sets = DatabaseHelper.getAllBluetoothTasks(context);
                    if ((sets == null) || (sets.size() == 0)) {
                        Logger.d("Setting BT tasks to false");
                        BluetoothTrigger.disable(context);
                    }
                    break;
                case TaskTypeItem.TASK_TYPE_WIFI:
                    sets = DatabaseHelper.getAllWifiTasks(context);
                    if ((sets == null) || (sets.size() == 0)) {
                        Logger.d("Setting Wifi tasks to false");
                        WifiTrigger.disable(context);
                    }
                    break;
                case TaskTypeItem.TASK_TYPE_BATTERY:
                    sets = DatabaseHelper.getAllBatteryTasks(context);
                    if ((sets == null) || (sets.size() == 0)) {
                        Logger.d("Setting Battery tasks to false");
                        BatteryTrigger.disable(context);
                    }
                    break;
                case TaskTypeItem.TASK_TYPE_GEOFENCE:
                    GeofenceTrigger.registerGeofences(context);
                    break;
                    
            }
        }
    }

    public static void shareTag(Context context, TaskSet task) {
        Task tag = task.getTask(0);
        Intent intent = new Intent(context, ShareTagActivity.class);
        intent.putExtra(Constants.EXTRA_SAVED_TAG, tag);
        intent.putExtra(Constants.EXTRA_SHARE_TAG_NAME, tag.getName());
        intent.putExtra(Constants.EXTRA_SHARE_TAG_ID, tag.getId());
        context.startActivity(intent);
    }
}
