package com.mobiroo.n.sourcenextcorporation.trigger.listener;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.WearableListenerService;
import com.mobiroo.n.sourcenextcorporation.trigger.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.util.DataHelper;

/**
 * Created by krohnjw on 6/27/2014.
 */
public class WearMessageListenerService extends WearableListenerService {

    private String TAG = "Trigger";

    public static final String ACTION_DATA_CHANGED = "com.jwsoft.data_changed";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("Trigger", "Data Item changed");
        DataHelper.cacheData(WearMessageListenerService.this, dataEvents);
    }
}
