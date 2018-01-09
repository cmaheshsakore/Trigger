package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

public class WifiAdapterAction extends BaseAction {
    
    private int mLastKnownState;
    
    @Override
    public String getCommand() {
        return Constants.MODULE_WIFI;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_WIFI;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option001, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.WifiAdapterToggle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        /* Check for any pre-populated arguments */
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
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
        return "Wifi";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.WifiAdapterToggle);
        String toggleChoice = (String) toggle.getSelectedItem();
        String adapterChoice = context.getString(R.string.adapterWifi);
        
        String message = "";
        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.MODULE_WIFI + "";
        
        return new String[] { message, toggleChoice, adapterChoice };
    }

    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.adapterWifi);
    }
    
    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;
        
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        try {
            if (operation == Constants.OPERATION_ENABLE) {
                if ((!wm.isWifiEnabled())) {
                    mLastKnownState = WifiManager.WIFI_STATE_DISABLED;
                    setAutoRestart(currentIndex + 1);
                    wm.setWifiEnabled(true);
                }
            } else if (operation == Constants.OPERATION_DISABLE) {
                if ((wm.isWifiEnabled())) {
                    mLastKnownState = WifiManager.WIFI_STATE_ENABLED;
                    wm.setWifiEnabled(false);
                    setAutoRestart(currentIndex + 1);
                }
            } else if (operation == Constants.OPERATION_TOGGLE) {
                if ((!wm.isWifiEnabled())) {
                    mLastKnownState = WifiManager.WIFI_STATE_DISABLED;
                    wm.setWifiEnabled(true);
                    setAutoRestart(currentIndex + 1);
                } else if ((wm.isWifiEnabled())) {
                    mLastKnownState = WifiManager.WIFI_STATE_ENABLED;
                    wm.setWifiEnabled(false);
                    setAutoRestart(currentIndex + 1);
                }
            }
        } catch (Exception e) { Logger.e("Exception modifying wifi state", e); }
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.adapterWifi));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.adapterWifi));
    }
    
    public int getCurrentState(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mLastKnownState = (wm.isWifiEnabled()) ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;
        return mLastKnownState;
    }
 
    @Override
    public boolean scheduleWatchdog() {
        return true;
    }
}
