package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.BootCompletedIntentService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {        
        Logger.d("Boot Completed: Starting service to check for tasks");
        context.startService(new Intent(context, BootCompletedIntentService.class));
    }
}
