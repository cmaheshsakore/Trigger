package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.StateUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers.BluetoothReceiverIntentService;

public class BluetoothDisconnectReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("BluetoothDisconnectReceiver: Disconnected from BT device");
        // Set default values
        String name = SettingsHelper.getPrefString(context, Constants.PREF_LAST_BLUETOOTH_NAME);
        String mac = SettingsHelper.getPrefString(context, Constants.PREF_LAST_BLUETOOTH_MAC);

        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            // Try to get values from device on intent
            try {
                name = device.getName();
            } catch (Exception ignored) { /* fail silently */ }
            try {
                mac = device.getAddress();
            } catch (Exception ignored) { /* fail silently */ }
        }

        StateUtils.removeBluetoothDevice(context, name, mac);

        if (SettingsHelper.getPrefBool(context, Constants.PREF_IS_BLUETOOTH_TRIGGER_ACTIVE, false)) {
            Logger.d("BluetoothDisconnectReceiver: Starting service to process disconnect");
            Intent service = new Intent(context, BluetoothReceiverIntentService.class);
            service.putExtras(intent.getExtras());
            service.putExtra(BluetoothReceiverIntentService.EXTRA_OPERATION, BluetoothReceiverIntentService.OPERATION_DISCONNECT);
            startWakefulService(context, service);
        }
    }
}

