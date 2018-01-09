package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers.HeadsetReceiverIntentService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class HeadsetPluggedReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(getClass().getSimpleName() + ": Received broadcast");
        Intent service = new Intent(context, HeadsetReceiverIntentService.class);
        service.putExtras(intent.getExtras());
        startWakefulService(context, service);
    }

}
