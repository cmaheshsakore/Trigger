package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.PowerService;

/**
 * Created by krohnjw on 1/24/14.
 */
public class BatteryTrigger {

    public static void enable(Context context) {
        modify(context, true);
    }

    public static void disable(Context context) {
        modify(context, false);
    }

    public static void modify(Context context, boolean enable) {

        SettingsHelper.setPrefBool(context, Constants.PREF_IS_BATTERY_TRIGGER_ACTIVE, enable);
        Intent service = new Intent(context, PowerService.class);

        if (!enable) {
            if (!ChargingTrigger.isEnabled(context)) {
                // Only stop the service IF the charging trigger is also disabled
                context.stopService(service);
            }
        } else  {
            context.startService(service);
        }
    }

    public static boolean isEnabled(Context context) {
        return SettingsHelper.getPrefBool(context, Constants.PREF_IS_BATTERY_TRIGGER_ACTIVE, false);
    }
}
