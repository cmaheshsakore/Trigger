package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.StateUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers.BluetoothReceiverIntentService;

public class BluetoothConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("BluetoothConnectReceiver: Connected to BT device");
        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        StateUtils.storeConnectedBluetoothDevice(context, device);

        if (SettingsHelper.getPrefBool(context, Constants.PREF_IS_BLUETOOTH_TRIGGER_ACTIVE, false)) {
            Logger.d("BluetoothConnectReceiver: Scheduling service to process connect");
            Intent service = new Intent(context, BluetoothReceiverIntentService.class);
            service.putExtras(intent.getExtras());
            service.putExtra(BluetoothReceiverIntentService.EXTRA_OPERATION, BluetoothReceiverIntentService.OPERATION_CONNECT);

            // Schedule a wakeup for Bluetooth connect.  This may (in theory) allow for appropriate modification of the Media volume stream
            // by giving the device time to finish the connection
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, PendingIntent.getService(context, 801, service, PendingIntent.FLAG_ONE_SHOT));
        }
    }
}

