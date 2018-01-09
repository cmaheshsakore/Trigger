package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.provider.Settings;
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

public class AutoRotationAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_AUTO_ROTATE;
    }

    @Override
    public String getCode() {
        return Codes.DISPLAY_AUTO_ROTATE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option039, null, false);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.autoRotateToggle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
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
        return "Auto Rotation";
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner adapter = (Spinner) actionView.findViewById(R.id.autoRotateToggle);
        String ap = (String) adapter.getSelectedItem();

        String message = "";
        String prefix = "";
        String suffix = context.getString(R.string.listDisplayAutoRotationText);
        
        if (ap.equals(context.getString(R.string.enableText))) {
            message = Constants.COMMAND_ENABLE + ":" + Constants.COMMAND_AUTO_ROTATE;
            prefix = context.getString(R.string.enableText);
        } else if (ap.equals(context.getString(R.string.disableText))) {
            message = Constants.COMMAND_DISABLE + ":" + Constants.COMMAND_AUTO_ROTATE;
            prefix = context.getString(R.string.disableText);
        } else {
            message = Constants.COMMAND_TOGGLE + ":" + Constants.COMMAND_AUTO_ROTATE;
            prefix = context.getString(R.string.toggleText);
        }
        
        return new String[] { message, prefix, suffix };
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listDisplayAutoRotationText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        int setting = operation; // OPERATION_DISABLE is 0, OPERATION_ENABLE is 1

        try {
            // Use opposite of existing setting if we are toggling
            if (operation == Constants.OPERATION_TOGGLE) {
                int currentState = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
                setting = (currentState == 0) ? 1 : 0;
            }
        } catch (Exception e) {
            Logger.e("Exception getting rotation setting: " + setting + ": " + e, e);
        }

        Logger.d("ROTATION: setting rotation to " + setting);
        Utils.updateSetting(context, Settings.System.ACCELEROMETER_ROTATION, setting);
        
    }
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.layoutDisplayAutoRotation));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.layoutDisplayAutoRotation));
    }

}
