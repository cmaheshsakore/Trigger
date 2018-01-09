package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.WifiReceiver;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

/**
 * Created by krohnjw on 1/24/14.
 */
public class WifiTrigger {

    public static void enable(Context context) {
        modify(context, true);
    }

    public static void disable(Context context) {
        modify(context, false);
    }

    public static void modify(Context context, boolean enable) {
        SettingsHelper.setPrefBool(context, Constants.PREF_IS_WIFI_TRIGGER_ACTIVE, enable);
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, WifiReceiver.class),
                (enable) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                , PackageManager.DONT_KILL_APP);
    }
}
