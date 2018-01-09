package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import org.apache.http.message.BasicNameValuePair;

public class BluetoothDiscoverableAction extends BaseAction {

    private int mLastKnownState;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_BLUETOOTH_DISCOVERABLE;
    }

    @Override
    public String getCode() {
        return Codes.BLUETOOTH_DISCOVERABLE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option007, null, false);
        return dialogView;
    }

    @Override
    public String getName() {
        return "Bluetooth Discoverable";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_ENABLE + ":" + Constants.COMMAND_BLUETOOTH_DISCOVERABLE;
        return new String[] { message, context.getString(R.string.enableText), context.getString(R.string.listWifiBluetoothDiscoverableText)};
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }
    
    @Override
    public int getMinArgLength() {
        return 0;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listWifiBluetoothDiscoverableText);
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Enabling Bluetooth Discoverable");
        mResumeIndex = currentIndex;
        
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if ((!mBluetoothAdapter.isEnabled())) {
            mLastKnownState = BluetoothAdapter.STATE_OFF;
            setAutoRestart(currentIndex);
            mBluetoothAdapter.enable();
            return;
        }

        if (operation == Constants.OPERATION_ENABLE) {
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                context.startActivity(discoverableIntent);
            }
        }
        
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetText(context, operation, context.getString(R.string.wifiOptionsBluetoothDiscoverable));
    }
    
    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetText(context, operation, context.getString(R.string.wifiOptionsBluetoothDiscoverable));
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
    
}
