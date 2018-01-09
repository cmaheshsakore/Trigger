package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import org.apache.http.message.BasicNameValuePair;

import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

public class AutoSyncAction extends BaseAction {
    
    @Override
    public String getCommand() {
        return Constants.MODULE_SYNC;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_AUTO_SYNC;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option004, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.SyncSpinner);
        ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter5);
        
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
        return "Auto Sync";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner adapter = (Spinner) actionView.findViewById(R.id.SyncSpinner);
        String ap = (String) adapter.getSelectedItem();

        String message = "";
        String prefix = "";
        String suffix = context.getString(R.string.listWifiAutoSync);
        
        if (ap.equals(context.getString(R.string.enableText))) {
            message += Constants.COMMAND_ENABLE + ":" + Constants.MODULE_SYNC;
            prefix = context.getString(R.string.enableText);
        } else if (ap.equals(context.getString(R.string.disableText))) {
            message += Constants.COMMAND_DISABLE + ":" + Constants.MODULE_SYNC;
            prefix = context.getString(R.string.disableText);
        } else {
            message += Constants.COMMAND_TOGGLE + ":" + Constants.MODULE_SYNC;
            prefix = context.getString(R.string.toggleText);
        }
        
        return new String[] {message, prefix, suffix };
    }

    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listWifiAutoSync);
    }
    
    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        boolean enabled = true;
        if (operation == Constants.OPERATION_ENABLE) {
            enabled = true;
        } else if (operation == Constants.OPERATION_DISABLE) {
            enabled = false;
        } else if (operation == Constants.OPERATION_TOGGLE) {
            enabled = (ContentResolver.getMasterSyncAutomatically()) ? false : true;
        }
        
        ContentResolver.setMasterSyncAutomatically(enabled);
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.wifiOptionsAutoSync));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.wifiOptionsAutoSync));
    }
    
}
