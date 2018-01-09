package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class CheckReceiversBackgroundService extends Service {
    public CheckReceiversBackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Utils.checkReceivers(this);

        stopSelf();

        return START_NOT_STICKY;
    }
}
