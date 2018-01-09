package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.wear;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

/**
 * Created by krohnjw on 7/3/2014.
 */
public class WearListeningService extends WearableListenerService {

    private String TAG = "Trigger";

    private static final String PATH_RUN_TASK = "/run/";
    private static final String PATH_SYNC_TASKS = "/sync/";

    @Override
    public void onMessageReceived(MessageEvent event) {
        Logger.d("Message received with action " + event.getPath());
        String path = event.getPath();

        if (path.equals("/sync/")) {
            startService(new Intent(this, WearMessagingService.class));
            stopSelf();
        } else if (path.contains("run")) {
            byte[] argBytes = event.getData();
            if ((argBytes == null) || (argBytes.length == 0)) { return; }

            // Message should be ID + Name for running a task

            String data = new String(argBytes);
            Logger.d("Received data " + data);

            String[] task = data.split("##");

            if (task.length >= 2) {
                Intent i = new Intent(this, ParserService.class);
                i.putExtra(ParserService.EXTRA_TAG_ID, task[0]);
                i.putExtra(ParserService.EXTRA_TAG_NAME, task[1]);

                if (task.length >= 3) {
                    i.putExtra(ParserService.EXTRA_ALT_TAG_ID, task[2]);
                }

                if (task.length >= 4) {
                    i.putExtra(ParserService.EXTRA_ALT_TAG_NAME, task[3]);
                }
                startService(i);
            }
            stopSelf();
        }


    }
}
