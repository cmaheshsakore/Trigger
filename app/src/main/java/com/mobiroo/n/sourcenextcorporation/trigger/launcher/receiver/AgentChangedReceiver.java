package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.AgentChangedIntentService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

/**
 * Created by krohnjw on 7/16/2014.
 */
public class AgentChangedReceiver extends WakefulBroadcastReceiver {

    /* Taken from AGENT*/
    public static final String 	ACTION_ACTIVE 		= "com.tryagent.agent_active";
    public static final String 	ACTION_INACTIVE 	= "com.tryagent.agent_inactive";
    public static final String	ACTION_INSTALLED 	= "com.tryagent.agent_installed";
    public static final String	ACTION_UNINSTALLED	= "com.tryagent.agent_uninstalled";

    public static final String  EXTRA_GUID          = "com.tryagent.agent_guid";

    @Override
    public void onReceive(Context context, Intent intent) {

        String guid = intent.hasExtra(EXTRA_GUID) ? intent.getStringExtra(EXTRA_GUID) : "";

        Logger.d("Received signal that agent has run " + guid + " - " + intent.getAction());

        if (guid.isEmpty()) { return; }

        Intent service = new Intent(context, AgentChangedIntentService.class);
        service.setAction(intent.getAction());
        service.putExtras(intent.getExtras());
        startWakefulService(context, service);

    }
}
