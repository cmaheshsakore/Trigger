package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import org.apache.http.message.BasicNameValuePair;

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
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class SipAction extends BaseAction {
    
    @Override
    public String getCommand() {
        return Constants.MODULE_SIP;
    }

    @Override
    public String getCode() {
        return Codes.SIP;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option066, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.AdapterToggle);
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
        return "Receive SIP calls";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.AdapterToggle);
        String toggleChoice = (String) toggle.getSelectedItem();
        String adapterChoice = context.getString(R.string.sip_calls);
        
        String message = "";
        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.MODULE_SIP + "";
        
        return new String[] { message, toggleChoice, adapterChoice };
    }

    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.receive_sip_calls);
    }
    
    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }
    
    private final String SETTING = "sip_receive_calls";
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;

        try {
            if (operation == Constants.OPERATION_ENABLE) {
                Utils.updateSetting(context, SETTING, 1);
            } else if (operation == Constants.OPERATION_DISABLE) {
                Utils.updateSetting(context, SETTING, 0);
            } else if (operation == Constants.OPERATION_TOGGLE) {
                Utils.updateSetting(context, SETTING, (Utils.getSetting(context, SETTING, 0) == 0) ? 1 : 0); 
            }
        } catch (Exception e) { Logger.e("Exception modifying SIP setting", e); }
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.sip_calls));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.sip_calls));
    }
    
}
