package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

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

import org.apache.http.message.BasicNameValuePair;

public class SamsungPowerSaverAction extends BaseAction {

    private final String SAMSUNG_POWER_SAVER = "psm_switch";
    private final String SAMSUNG_POWER_SAVER_S5 = "powersaving_switch";
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SAMSUNG_POWER_SAVER;
    }

    @Override
    public String getCode() {
        return Codes.SAMSUNG_POWER_SAVER;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option058, null, false);
        
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.adapter_toggle);
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
        return "Samsung Power Saver";
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.adapter_toggle);

        String toggleChoice = (String) toggle.getSelectedItem();
        String item = context.getString(R.string.display_power_saver);
        

        // Append this pair to the list
        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.COMMAND_SAMSUNG_POWER_SAVER;

        return new String[] {message, toggleChoice, item};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.display_power_saver);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Samsung Power Saving Mode");
        Utils.operateGeneralSetting(context, operation, SAMSUNG_POWER_SAVER);
        Utils.operateGeneralSetting(context, operation, SAMSUNG_POWER_SAVER_S5);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.power_saver));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.power_saver));
    }

}
