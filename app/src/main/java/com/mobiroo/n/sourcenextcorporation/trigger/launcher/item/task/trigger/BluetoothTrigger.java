package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.BluetoothConnectReceiver;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.BluetoothDisconnectReceiver;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

/**
 * Created by krohnjw on 1/24/14.
 */
public class BluetoothTrigger {
    public static void enable(Context context) {
        modify(context, true);
    }

    public static void disable(Context context) {
        modify(context, false);
    }

    public static void modify(Context context, boolean enable) {

        SettingsHelper.setPrefBool(context, Constants.PREF_IS_BLUETOOTH_TRIGGER_ACTIVE, enable);

        if (enable) {
            // DO NOT DISABLE currently.  This needs to run to maintain a list of connected devices for Bluetooth constraints
            context.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(context, BluetoothConnectReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            context.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(context, BluetoothDisconnectReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}
