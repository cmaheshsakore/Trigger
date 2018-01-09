package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver.BluetoothDisconnectReceiver;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;

import java.util.ArrayList;

/**
 * Created by krohnjw on 4/7/2014.
 */
public class BluetoothReceiverIntentService extends BaseIntentService {

    public static final String EXTRA_OPERATION = "com.tryagent.operaton";
    public static final int OPERATION_CONNECT = 1;
    public static final int OPERATION_DISCONNECT = 2;
    public static final int OPERATION_UNKNOWN = 0;


    private final String NAME = "name";
    private final String MAC = "mac";

    @Override
    protected void onHandleIntent(Intent intent) {
        int operation = intent.getIntExtra(EXTRA_OPERATION, OPERATION_UNKNOWN);

        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String name = null;
        String mac = null;

        if (operation == OPERATION_CONNECT) {

            String last_name = SettingsHelper.getPrefString(this, Constants.PREF_LAST_BLUETOOTH_NAME, "");
            String last_mac = SettingsHelper.getPrefString(this, Constants.PREF_LAST_BLUETOOTH_MAC, "");

            String last_action = SettingsHelper.getPrefString(this, Constants.PREF_LAST_BLUETOOTH_ACTION, Constants.BLUETOOTH_DISCONNECT);

            if (device != null) {
                logd("Got Bluetooth device");
                try {
                    name = device.getName();
                    mac = device.getAddress();
                    SettingsHelper.setPrefString(this, Constants.PREF_LAST_BLUETOOTH_NAME, name);
                    SettingsHelper.setPrefString(this, Constants.PREF_LAST_BLUETOOTH_MAC, mac);
                    logd("Name = " + device.getName());
                    logd("Address = " + device.getAddress());
                    logConnect(this);
                } catch (Exception e) {
                    Logger.e("Exception getting device info", e);
                    name = null;
                    mac = null;
                }

            }

            /*
             * handle rapid connect / disconnect / connect from some car stereos
             */
            if (last_action.equals(Constants.BLUETOOTH_CONNECT) && (last_name.equals(name) || last_mac.equals(mac))) {
                long connect_time = SettingsHelper.getPrefLong(this, Constants.PREF_LAST_BLUETOOTH_ACTION_TIME, 0);
                if ((System.currentTimeMillis() - connect_time) < Constants.BLUETOOTH_RECONNECT_TIMEOUT_MS) {
                    logd("Ignoring connect as we've connected to this device within " + Constants.BLUETOOTH_ACTION_TIMEOUT_MS + " ms");
                    releaseWakeLocks(operation, intent);
                    return;
                }
            }

            logConnectTime(this);

            boolean foundTask = false;

            if ((name != null) && (!name.isEmpty())) {
                foundTask = checkBluetoothMatches(this, DatabaseHelper.TRIGGER_ON_CONNECT, name, NAME);
            }
            if ((!foundTask) && (mac != null) && (!mac.isEmpty())) {
                checkBluetoothMatches(this, DatabaseHelper.TRIGGER_ON_CONNECT, mac, MAC);
            }

            logd("Did not find any matching connect tasks");

        } else if (operation == OPERATION_DISCONNECT) {

            name = SettingsHelper.getPrefString(this, Constants.PREF_LAST_BLUETOOTH_NAME);
            mac = SettingsHelper.getPrefString(this, Constants.PREF_LAST_BLUETOOTH_MAC);

            if (device != null) {
                // Try to get values from device on intent
                logd("Got a device with disconnect");
                try {
                    name = device.getName();
                } catch (Exception e) { /* fail silently */ }
                try {
                    mac = device.getAddress();
                } catch (Exception e) { /* fail silently */ }
            }

            /*
             * handle rapid connect / disconnect / connect from some car stereos
             */
            String last_action = SettingsHelper.getPrefString(this, Constants.PREF_LAST_BLUETOOTH_ACTION, Constants.BLUETOOTH_DISCONNECT);
            if (last_action.equals(Constants.BLUETOOTH_CONNECT)) {
                long connect_time = SettingsHelper.getPrefLong(this, Constants.PREF_LAST_BLUETOOTH_ACTION_TIME, 0);
                if ((System.currentTimeMillis() - connect_time) < Constants.BLUETOOTH_ACTION_TIMEOUT_MS) {
                    logd("Ignoring disconnect as we've connected within " + Constants.BLUETOOTH_ACTION_TIMEOUT_MS + " ms");
                    releaseWakeLocks(operation, intent);
                    return;
                }
            }


            logDisconnect(this);

            boolean foundTask = false;
            if ((name != null) && (!name.isEmpty())) {
                foundTask = checkBluetoothMatches(this, DatabaseHelper.TRIGGER_ON_DISCONNECT, name, NAME);
            }
            if ((!foundTask) && (mac != null) && (!mac.isEmpty())) {
                checkBluetoothMatches(this, DatabaseHelper.TRIGGER_ON_DISCONNECT, mac, MAC);
            }

            logd("Did not find any matching disconnect tasks");

        } else {
            logd("Received invalid operation - skipping");
        }

        releaseWakeLocks(operation, intent);
    }

    private void releaseWakeLocks(int operation, Intent intent) {
        switch (operation) {
            case OPERATION_CONNECT:
                logd("Releasing connect wake");
                //BluetoothConnectReceiver.completeWakefulIntent(intent);
                break;
            case OPERATION_DISCONNECT:
                logd("Releasing disconnect wake");
                BluetoothDisconnectReceiver.completeWakefulIntent(intent);
                break;
        }
    }

    private boolean checkBluetoothMatches(Context context, String test, String data, String type) {

        boolean foundTask = false;
        ArrayList<TaskSet> sets = null;
        if (NAME.equals(type)) {
            sets = DatabaseHelper.getBluetoothTasksForDeviceName(context, data, context.getString(R.string.any_device));
        } else {
            sets = DatabaseHelper.getBluetoothTasksForDeviceMac(context, data);
        }
        resetTasksRan();
        if ((sets != null) && (sets.size() > 0)) {
            for (TaskSet set : sets) {
                Trigger trigger = set.getTrigger(0);
                Task task = set.getTask(0);
                if (!set.shouldUse()) {
                    logd("Skipping task");
                    continue;
                }

                logd("Ensuring valid restrictions for for " + task.getFullName());
                try {
                    String condition = trigger.getCondition();
                    if (condition.equals(test)) {
                        if (trigger.constraintsSatisfied(context)) {
                            Usage.logTrigger(context, Usage.TRIGGER_BLUETOOTH);
                            foundTask = true;
                            String id = task.getId();
                            String name = task.getName();
                            logd("Running " + name);
                            String payload = Task.getPayload(context, id, name);
                            logd("Payload is " + payload);
                            executePayload(context, name, payload, TaskTypeItem.TASK_TYPE_BLUETOOTH);
                        }
                    }
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception starting scheduled Bluetooth activity for " + data, e);
                }

            }
        }
        return foundTask;
    }

    private void logConnect(Context context) {
        SettingsHelper.setPrefString(context, Constants.PREF_LAST_BLUETOOTH_ACTION, Constants.BLUETOOTH_CONNECT);
    }

    private void logConnectTime(Context context) {
        SettingsHelper.setPrefLong(context, Constants.PREF_LAST_BLUETOOTH_ACTION_TIME, System.currentTimeMillis());
    }

    private void logDisconnect(Context context) {
        SettingsHelper.setPrefString(context, Constants.PREF_LAST_BLUETOOTH_ACTION, Constants.BLUETOOTH_DISCONNECT);
        SettingsHelper.setPrefLong(context, Constants.PREF_LAST_BLUETOOTH_ACTION_TIME, System.currentTimeMillis());
    }
}
