package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActionExecutor;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

/**
 * Created by krohnjw on 1/2/14.
 */
public class ActionService extends BaseService {

    public static final String EXTRA_TAG_NAME = "com.trigger.launcher.TagName";
    public static final String EXTRA_TAG_ID = "com.trigger.launcher.TagId";
    public static final String EXTRA_ALT_TAG_NAME = "com.trigger.launcher.AltTagName";
    public static final String EXTRA_ALT_TAG_ID = "com.trigger.launcher.AltTagId";
    public static final String EXTRA_PAYLOAD = "LoadedPayload";
    public static final String EXTRA_START_POSITION = "startPosition";


    @Override
    public int onStartCommand(Intent intent, int id, int flags) {

        if (Usage.canLogData(this)) {
            Usage.startTracking(this);
        }
        // Change to use ActionExecutor - start and end usage logging here
        new ActionExecutor(this).process(intent);

        if (Usage.canLogData(this)) {
            Usage.dispatchGa();
        }

        return START_NOT_STICKY;
    }
}
