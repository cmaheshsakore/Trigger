package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.AgentChangedReceiver;

/**
 * Created by krohnjw on 7/9/2014.
 */
public class AgentTrigger {

    public static final String EXTRA_AGENT = "com.tryagent.extra_agent";
    public static final String ACTION_AGENT_CHANGED = "com.tryagent.agent_changed";
    public static final int ENABLED = 1;
    public static final int DISABLED = 0;

    public static void enable(Context context) {
        modify(context, true);
    }

    public static void disable(Context context) {
        modify(context, false);
    }

    public static void modify(Context context, boolean enable) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, AgentChangedReceiver.class),
                (enable) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                , PackageManager.DONT_KILL_APP);
    }

}
