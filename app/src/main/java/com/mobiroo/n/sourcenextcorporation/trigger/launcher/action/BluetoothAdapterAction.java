package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import org.apache.http.message.BasicNameValuePair;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class BluetoothAdapterAction extends BaseAction {

    private int mLastKnownState;
    
    @Override
    public String getCommand() {
        return Constants.MODULE_BT;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_BLUETOOTH;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option002, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.BluetoothAdapterToggle);
        ArrayAdapter<CharSequence> badapter2 = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        badapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(badapter2);
        
        /* Check for any pre-populated arguments */
        if (hasArgument(arguments,CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            if (state.equals(Constants.COMMAND_ENABLE)) {
                spinner.setSelection(0);
            } else if (state.equals(Constants.COMMAND_DISABLE)) {
                spinner.setSelection(1);
            } else if (state.equals(Constants.COMMAND_TOGGLE)) {
                spinner.setSelection(2);
            }
        }
        return dialogView;
    }

    @Override
    public String getName() {
        return "Bluetooth";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.BluetoothAdapterToggle);

        String toggleChoice = (String) toggle.getSelectedItem();
        String adapterChoice = context.getString(R.string.adapterBluetooth);
        
        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.MODULE_BT + "";
        
        return new String[] { message, toggleChoice, adapterChoice };
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
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            Logger.d("BLUETOOTH: Modifying Bluetooth adapter: " + operation);
            if (operation == Constants.OPERATION_ENABLE) {
                if ((!mBluetoothAdapter.isEnabled())) {
                    mLastKnownState = BluetoothAdapter.STATE_OFF;
                    setAutoRestart(currentIndex + 1);
                    mBluetoothAdapter.enable();
                }
            } else if (operation == Constants.OPERATION_DISABLE) {
                if ((mBluetoothAdapter.isEnabled())) {
                    mLastKnownState = BluetoothAdapter.STATE_ON;
                    setAutoRestart(currentIndex + 1);
                    mBluetoothAdapter.disable();
                }
            } else if (operation == Constants.OPERATION_TOGGLE) {
                mNeedReset = true;

                if ((!mBluetoothAdapter.isEnabled())) {
                    mLastKnownState = BluetoothAdapter.STATE_OFF;
                    mBluetoothAdapter.enable();
                } else if ((mBluetoothAdapter.isEnabled())) {
                    mLastKnownState = BluetoothAdapter.STATE_ON;
                    mBluetoothAdapter.disable();
                }
            }
        }
        
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.adapterBluetooth));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.adapterBluetooth));
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
        return true;
    }
    
}
