package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import java.util.List;

public class GeofenceService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        SettingsHelper.loadPreferences(this);
        Logger.d("GEO: Received incoming intent");

        if (!IabClient.checkLocalUnlockOrTrial(this)) {
            Logger.d("GEO: User is not authorized for this feature");
            this.stopSelf();
            return START_NOT_STICKY;
        }

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            int errorCode = event.getErrorCode();
            Logger.e("GEO: Location services error: " + Integer.toString(errorCode));
        } else {
            int transition = event.getGeofenceTransition();
            Logger.d("GEO: Transition is " + transition);
            if ((transition == Geofence.GEOFENCE_TRANSITION_DWELL) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {

                if (transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                    transition = Geofence.GEOFENCE_TRANSITION_ENTER;
                }

                Logger.d("GEO: Received enter / exit transition");

                /* We have a valid transition */
                List <Geofence> triggeringFences = event.getTriggeringGeofences();
                Logger.d("GEO: Fence was triggered by " + TextUtils.join(",", triggeringFences));


                Logger.d("GEO: Event triggered by " + triggeringFences.size() + " fences");

                for (int i=0; i< triggeringFences.size(); i++) {

                    String triggeringId = triggeringFences.get(i).getRequestId();
                    Logger.i("GEO: Found Geofence id of " + triggeringId);
                    
                    TaskSet set =  DatabaseHelper.getTaskSetByTriggerId(this, TaskTypeItem.TASK_TYPE_GEOFENCE, triggeringId);
                    Logger.d("Set has " + set.getTasks().size() + " tasks");
                    if (!set.shouldUse()) {
                        Logger.d("GEO: Skipping task");
                        continue;
                    }


                    String name = set.getTask(0).getName();
                    String taskId = set.getTask(0).getId();
                    Logger.d("GEO: Task: "+ name + ", " + taskId);

                    if (!name.isEmpty()) {
                        if (set.getTrigger(0).constraintsSatisfied(this)) {

                            String payload = Task.getPayload(this, taskId, name);

                            Logger.d("GEO: Payload is " + payload);

                            Usage.logTrigger(getApplication(), Usage.TRIGGER_GEOFENCE);
                            Intent run = new Intent(GeofenceService.this, ParserService.class);
                            run.putExtra(ParserService.EXTRA_TAG_NAME, name);
                            run.putExtra(ParserService.EXTRA_PAYLOAD, payload);
                            run.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_GEOFENCE);
                            getApplication().startService(run);
                        }
                    }


                }

            }
        }

        this.stopSelf();
        return START_NOT_STICKY;
    }
}
