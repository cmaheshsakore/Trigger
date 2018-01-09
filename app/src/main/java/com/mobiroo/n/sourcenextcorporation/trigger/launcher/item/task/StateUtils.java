package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StateUtils {

    public static final String ANY_DEVICE = "any device-1";

    private static void logd(String message) {
        Logger.d("StateUtils: " + message);
    }

    private static void loge(String message, Exception e) {
        Logger.e("StateUtils: " + message, e);
    }

    public static class ChargingState {
        public int status;
        public int type;
        
        public ChargingState(int status, int type) {
            this.status = status;
            this.type = type;
        }
    }
    
    public static ChargingState getChargingState(Context context) { 
        int status = -1;
        int type = -1;
        
        IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent stats = context.getApplicationContext().registerReceiver(null,filter);
        if (stats != null) {
            type = stats.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            status = stats.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        }
        
        return new ChargingState(status, type);
    }

    private static BluetoothHeadset     mHeadset;
    private static BluetoothA2dp        mA2dp;

    private static final String BLUETOOTH_DEVICE_LIST = "pref_bluetooth_device_list";

    private static JSONObject getStoredDeviceList(Context context) {
        JSONObject data = new JSONObject();
        try { data = new JSONObject(SettingsHelper.getPrefString(context, BLUETOOTH_DEVICE_LIST, "")); }
        catch (JSONException e) { loge("Exception parsing stored bluetooth devices: " + e, e); }
        return data;
    }

    private static String getStringValue(JSONObject object, String key) {
        String value = "";
        try { value = object.getString(key); }
        catch (JSONException e) { }
        return value;
    }

    public static void storeConnectedBluetoothDevice(Context context, BluetoothDevice device) {
        logd("Called store");
        JSONObject devices = getStoredDeviceList(context);
        logd("Storing connection to " + device.getName());
        if (getStringValue(devices, device.getName()).isEmpty()) {
            try {devices.put(device.getName(), device.getAddress()); }
            catch (JSONException e) { loge("Exception saving device : " + e, e); }
            SettingsHelper.setPrefString(context, BLUETOOTH_DEVICE_LIST, devices.toString());
        }
    }

    public static void removeBluetoothDevice(Context context, String name, String mac) {
        logd("Called remove");
        JSONObject devices = getStoredDeviceList(context);
        logd("Removing connection to " + name);
        if (!getStringValue(devices, name).isEmpty()) {
            devices.remove(name);
            SettingsHelper.setPrefString(context, BLUETOOTH_DEVICE_LIST, devices.toString());
        }
    }

    public static boolean isBluetoothDeviceConnected(Context context, List<String> user_devices) {
        JSONObject connected = getStoredDeviceList(context);
        logd("Connected devices is " + connected.toString());
        if (user_devices.contains(ANY_DEVICE) && connected.keys().hasNext()) return true;

        for (String device: user_devices) {
            logd("Checking for device " + device);
            if (connected.has(device)) return true;
        }

        return false;
    }
    public static List<BluetoothDevice> getConnectedBluetoothDevice(Context context) {
        // Check if Bluetooth is connected
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) return new ArrayList<BluetoothDevice>();

        // Bluetooth is enabled, check if we are connected to one or more devices.
        adapter.getProfileProxy(context, new BluetoothProfile.ServiceListener()  {
            @Override
            public void onServiceDisconnected(int profile) {
            }

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    mHeadset = (BluetoothHeadset) proxy;
                }
            }
        }, BluetoothHeadset.HEADSET);

        adapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                mA2dp = (BluetoothA2dp) proxy;
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        }, BluetoothHeadset.A2DP);


        List<BluetoothDevice> connected = new ArrayList<BluetoothDevice>();

        if (mHeadset != null) {
            for (BluetoothDevice device: mHeadset.getConnectedDevices()) {
                connected.add(device);
            }
        }

        if (mA2dp != null) {
            for (BluetoothDevice device: mA2dp.getConnectedDevices()) {
                connected.add(device);
            }
        }

        return connected;
    }
}
