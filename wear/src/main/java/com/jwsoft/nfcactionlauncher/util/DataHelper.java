package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.mobiroo.n.sourcenextcorporation.trigger.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.application.WearApplication;

/**
 * Created by krohnjw on 7/14/2014.
 */
public class DataHelper {

    private static final String TAG = "DataHelper";

    public static void cacheData(Context context, DataEventBuffer dataEvents) {
        Log.d("Trigger", "Data Item changed");
        for (DataEvent event : dataEvents) {
            Log.d("Trigger", "Got Data event " + event.getType());
            DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());

            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
                context.getSharedPreferences(Constants.TASKS_PREF_NAME, 0).edit().putString(Constants.PREF_TASKS, "").commit();
                loadNewMain(context);
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                cacheDataItem(context, item);
            }
        }
        dataEvents.close();

    }

    public static String cacheDataItem(Context context, DataMapItem item) {
        return cacheDataItem(context, item, true);
    }

    public static String cacheDataItem(Context context, DataMapItem item, boolean restart) {
        Uri uri = item.getUri();
        String nodeId = uri.getHost(); // This is the sending device.  Store device ID
        DataMap map = item.getDataMap();
        String tasks = map.getString("tasks");

        Log.d(TAG, "Syncing " + tasks);
        context.getSharedPreferences(Constants.TASKS_PREF_NAME, 0).edit().putString(Constants.PREF_TASKS, tasks).commit();
        if (restart) {
            loadNewMain(context);
        }
        return tasks;
    }

    public static void loadNewMain(Context context) {
        Log.d("Trigger", "Calling load new main - isActive = " + WearApplication.isApplicationActive(context));
        if (WearApplication.isApplicationActive(context)) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(MainActivity.EXTRA_SYNC_DATA, false);
            context.startActivity(i);
        }
    }
}
