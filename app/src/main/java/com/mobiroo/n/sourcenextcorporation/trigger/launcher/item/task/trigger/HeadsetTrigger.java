package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.HeadsetService;

/**
 * Created by krohnjw on 1/24/14.
 */
public class HeadsetTrigger {
    public static void enable(Context context) {
        modify(context, true);
    }

    public static void disable(Context context) {
        modify(context, false);
    }

    public static void modify(Context context, boolean enable) {
        SettingsHelper.setPrefBool(context, Constants.PREF_IS_HEADSET_TRIGGER_ACTIVE, enable);
        Intent service = new Intent(context, HeadsetService.class);
        if (enable) {
            context.startService(service);
        } else {
            context.stopService(service);
        }
    }
}
