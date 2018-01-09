package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.ChargingTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.HeadsetTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;

import java.util.ArrayList;
import java.util.List;

public class BootCompletedIntentService extends IntentService {

    public BootCompletedIntentService() {
        super("BootCompletedIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d("Boot Completed: Service started");
        
        Context context = this;
        SettingsHelper.loadPreferences(context);

        Logger.d("Boot Completed: Checking for battery/charging tasks");
        if (BatteryTrigger.isEnabled(this) || ChargingTrigger.isEnabled(this)) {
            BatteryTrigger.enable(context);
        }

        Logger.d("Boot Completed: Checking for Geofences");
        if (BuildConfiguration.USE_GEOFENCES) {
            List<Geofence> fences = DatabaseHelper.getGeofences(this);
            if (fences.size() > 0) {
                Logger.d("Boot Completed: Registering all geofences");
                GeofenceClient geoClient = new GeofenceClient(context);
                geoClient.setGeofences(fences);
                geoClient.connectAndSave();

            }
        }
        
        Logger.d("Boot Completed: Checking for headset tasks");
        ArrayList<TaskSet> sets = DatabaseHelper.getHeadsetTasks(this);
        if ((sets != null) && (sets.size() > 0)) {
            HeadsetTrigger.enable(context);
        }
        
        Logger.d("Boot Completed: Checking for Time tasks");
        TimeTrigger.scheduleTimeTasks(context);
        
        
    }

}
