package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceConnectAction extends BaseAction {

    private int mLastKnownState;
    public static final int PROFILE_BOTH = 1;
    public static final int PROFILE_MEDIA = 2;
    public static final int PROFILE_PHONE = 3;
    public static final int PROFILE_PAN = 4;
    
    private Dialog mDialog;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_CONNECT_A2DP;
    }

    @Override
    public String getCode() {
        return Codes.BLUETOOTH_CONNECT_A2DP;
    }

    private Spinner mSpinner;
    private Context mContext;
    private String  mDeviceMac;
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option068, null, false);

        /* Populate list of devices */
        mSpinner = (Spinner) dialogView.findViewById(R.id.device);
        mContext = context;
        
        
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            mDeviceMac = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
        }
        
        setupDeviceList(context, mSpinner);
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            Spinner profile = (Spinner) dialogView.findViewById(R.id.profile);
            try {
                int selected = Integer.parseInt(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
                profile.setSelection(selected -1);
            } catch (Exception ignored) { }
            
        }
        return dialogView;
    }

    private BroadcastReceiver BluetoothStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state == BluetoothAdapter.STATE_ON) {
                cancelDialog();
                setupDeviceList(mContext, mSpinner);
            }
        }
    };
    
    private void cancelDialog() {
        if ((mDialog != null) && (mDialog.isShowing())) {
            mDialog.cancel();
        }
    }
    
    private void setupDeviceList(final Context context, Spinner known) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            mDialog = ProgressDialog.show(context, "", context.getString(R.string.loading));
            context.registerReceiver(BluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            adapter.enable();
        } else {
            try {
                context.unregisterReceiver(BluetoothStateChangedReceiver);
            } catch (Exception ignored) {}
            
            List<BluetoothDevice> existing = new ArrayList<BluetoothDevice>(adapter.getBondedDevices());
            ArrayAdapter<BluetoothDevice> spinner_adapter = new ArrayAdapter<BluetoothDevice>(context, R.layout.list_item_single_small, existing) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (null == convertView) {
                        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_single_small, parent, false);
                    }
                    
                    BluetoothDevice d = getItem(position);
                    
                    String name = d.getName();
                    
                    if (name != null) {
                        if ((name.length() > 0) && (name.substring(0,1).equals("\""))) {
                            name = name.substring(1, name.length() - 1);
                        }
                        ((TextView) convertView.findViewById(R.id.row1Text)).setText(name);
                    }
                    return convertView;
                }
                
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    if (null == convertView) {
                        convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                    }
                    BluetoothDevice d = getItem(position);
                    
                    TextView text = (TextView) convertView.findViewById(android.R.id.text1);
                    text.setText(d.getName());
                    
                    return convertView;
                }
                
            };
            spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            known.setAdapter(spinner_adapter);
            
            if ((mDeviceMac != null) && (!mDeviceMac.isEmpty())) {
                for (int i=0; i<existing.size(); i++) {
                    BluetoothDevice device = existing.get(i);
                    if (mDeviceMac.equals(device.getAddress())) {
                        known.setSelection(i);
                        break;
                    }
                }
            }
            
        }
    }
    
    @Override
    public String getName() {
        return "Bluetooth Connect";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner devices = (Spinner) actionView.findViewById(R.id.device);
        Spinner profile = (Spinner) actionView.findViewById(R.id.profile);
        int bt_profile = profile.getSelectedItemPosition() + 1;
        BluetoothDevice selected = (BluetoothDevice) devices.getSelectedItem();
        if (selected != null) {
            String mac = selected.getAddress();
            
            String message = Constants.COMMAND_CONNECT_A2DP + ":" + Utils.encodeData(mac) + ":" + bt_profile;
            return new String[] { message, context.getString(R.string.listWifiConfigureConnectText), mac};
        } else {
            return new String[0];
        }
    }

    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.adapterBluetooth);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.decodeData(Utils.tryParseString(args, 1, ""))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, ""))
                );
    }
    
    private void connectToDevice(Context context, String address, int profile) {

        Logger.d("Connecting for profile " + profile);
        List<BluetoothDevice> existing = new ArrayList<BluetoothDevice>(BluetoothAdapter.getDefaultAdapter().getBondedDevices());

        Logger.d("SDK version is " + Build.VERSION.SDK_INT);
        
        for (BluetoothDevice device: existing) {
            if (device.getAddress().equals(address)) {
                Logger.d("Found a match = " + device.getAddress());
                if ((profile == PROFILE_BOTH) || (profile == PROFILE_MEDIA)) {
                    Logger.d("Connecting to Media Profile");
                    try {
                        bindA2dpDeviceViaProxy(context, device);
                    } catch (Exception e) {
                        Logger.e("Exception connecting to device: " + e, e);
                    }
                }
                if ((profile == PROFILE_BOTH) || (profile == PROFILE_PHONE)) {
                    Logger.d("Connecting to Phone Profile");
                    try { bindHeadsetDeviceViaProxy(context, device); }
                    catch (Exception e) {
                        Logger.e("Exception connecting to device: " + e, e);
                    }
                }
            }
        }
    }
    
    private void bindHeadsetDeviceViaProxy(Context context, final BluetoothDevice device) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.getProfileProxy(context, new BluetoothProfile.ServiceListener()  {

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                BluetoothHeadset local = (BluetoothHeadset) proxy;
                try {
                    Method connect = BluetoothHeadset.class.getMethod("connect", BluetoothDevice.class);
                    connect.invoke(local, device);
                } catch (Exception e) {
                    Logger.e("Exception " + e.getClass().getSimpleName(), e);
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                
            }
            
        }, BluetoothProfile.HEADSET);
    }
    
    private void bindA2dpDeviceViaProxy(Context context, final BluetoothDevice device) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.getProfileProxy(context, new BluetoothProfile.ServiceListener()  {
            
            @Override
            public void onServiceDisconnected(int profile) {

            }
            
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                BluetoothA2dp local = (BluetoothA2dp) proxy;
                try {
                    Method connect = BluetoothA2dp.class.getMethod("connect", BluetoothDevice.class);
                    connect.invoke(local, device);
                } catch (Exception e) {
                    Logger.e("Exception " + e.getClass().getSimpleName(), e);
                }
                

            }
        }, BluetoothProfile.A2DP);
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Connecting to Bluetooth device");

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mLastKnownState = BluetoothAdapter.STATE_OFF;
            setAutoRestart(currentIndex);
            mBluetoothAdapter.enable();
            return;
        }
        
        
        String mac = Utils.decodeData(args[1]);
        int profile = Utils.tryParseInt(args, 2, PROFILE_BOTH);
        Logger.d("Connecting to device with address " + mac);

        connectToDevice(context, mac, profile);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widget_bluetooth_connect);
    }

    @Override
    public String getNotificationText(Context context, int operation) {       
        return context.getString(R.string.action_bluetooth_connect);
    }

    public int getCurrentState() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mLastKnownState = (mBluetoothAdapter.isEnabled()) ? BluetoothAdapter.STATE_ON : BluetoothAdapter.STATE_OFF;
        if ((mBluetoothAdapter.isEnabled())) {
            mLastKnownState = BluetoothAdapter.STATE_ON;
        } else 
            mLastKnownState = BluetoothAdapter.STATE_OFF;

        return mLastKnownState;
    }

    @Override
    public boolean scheduleWatchdog() {
        return !BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
    
    @Override
    public boolean resumeIsCurrentAction() {
        return !BluetoothAdapter.getDefaultAdapter().isEnabled();
    }
    
}