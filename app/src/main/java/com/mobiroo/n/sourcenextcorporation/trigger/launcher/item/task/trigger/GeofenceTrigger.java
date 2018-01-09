package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.content.Context;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.List;

/**
 * Created by krohnjw on 1/24/14.
 */
public class GeofenceTrigger {
    public static void registerGeofences(Context context) {
        if (context == null) { return; }
        if (BuildConfiguration.USE_GEOFENCES) {
            List<Geofence> fences = DatabaseHelper.getGeofences(context);
            GeofenceClient geoClient = new GeofenceClient(context);
            if (fences.size() > 0) {
                geoClient.setGeofences(fences);
                geoClient.connectAndSave();
            } else {

                geoClient.connectAndRemoveAll();
            }
        }
    }

    public static void cleanUpGeofences(final Context context) {
        GeofenceClient.cleanUpGeofences(context);
        final GeofenceClient client = new GeofenceClient(context);
        client.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Logger.d("GEO: Cleanup delete all fences");
                    List<Geofence> fences = DatabaseHelper.getGeofences(context);
                    if (fences.size() > 0) {
                        client.setGeofences(fences);
                        client.connectAndSave();
                    }
                }
            }
        });
        client.connectAndRemoveAll();
    }

}
