package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class DisplayTimeoutAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_SET_DISPLAY_TIMEOUT;
    }

    @Override
    public String getCode() {
        return Codes.DISPLAY_TIMEOUT;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option041, null, false);

        Spinner timeout = (Spinner) dialogView.findViewById(R.id.TimeoutSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.displayTimeoutChoices, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeout.setAdapter(adapter);
        
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            timeout.setSelection(getPositionFromValue(arguments.getValue(CommandArguments.OPTION_INITIAL_STATE)));
            
        }
        
        return dialogView;
    }

    private int getPositionFromValue(String value) {
        int level = 60;
        try { 
            level = Integer.parseInt(value);
        } catch (Exception e) {
            
        }
        
        switch(level) {
            case 15:
                return 0;
            case 30:
                return 1;
            case 60:
                return 2;
            case 120:
                return 3;
            case 300:
                return 4;
            case 600:
                return 5;
            case 1800:
                return 6;
            case -1:
                return 7;
            default:
                return 2;           
        }
    }
    @Override
    public String getName() {
        return "Display Timeout";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner adapter = (Spinner) actionView.findViewById(R.id.TimeoutSpinner);
        String ap = (String) adapter.getSelectedItem();

        int position = adapter.getSelectedItemPosition();
        int timeoutSeconds = 15;
        switch (position) {
            case 0:
                timeoutSeconds = 15;
                break;
            case 1:
                timeoutSeconds = 30;
                break;
            case 2:
                timeoutSeconds = 60;
                break;
            case 3:
                timeoutSeconds = 120;
                break;
            case 4:
                timeoutSeconds = 300;
                break;
            case 5:
                timeoutSeconds = 600;
                break;
            case 6:
                timeoutSeconds = 1800;
                break;
            case 7:
                timeoutSeconds = -1;
                break;
        }
        return new String[] { Constants.COMMAND_SET_DISPLAY_TIMEOUT + ":" + timeoutSeconds, getBaseWidgetSetText(context, 0, context.getString(R.string.layoutDisplayTimeout)), ap};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = getBaseWidgetSetText(context, 0, context.getString(R.string.layoutDisplayTimeout));
        try {
            display += " " + args[0];
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, Utils.tryParseString(args, 1, "60"))
                );
    }

    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        int seconds = Utils.tryParseInt(args, 1, -2);
        if (seconds > -1) {
            int timeout = seconds * 1000;
            Logger.d("Setting display timeout to " + timeout);
            Utils.updateSetting(context, Settings.System.SCREEN_OFF_TIMEOUT, timeout);
        } else if (seconds == -1) {
            Logger.d("Setting display timeout to never");
            try {
                Logger.d("Current value is " + Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT));
            } catch (Exception e) { }

            // Alternate value 2592000000
            // Also set fallback screen_off_timeout_rollback ?
            if (Utils.isLgDevice()) {
                Utils.updateSetting(context, Settings.System.SCREEN_OFF_TIMEOUT, 2147483647);
            } else {
                Utils.updateSetting(context, Settings.System.SCREEN_OFF_TIMEOUT, 259200000);
            }

            Utils.updateSetting(context, "screen_off_timeout_rollback", 120000); // Default to 2 min
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetText(context, operation, context.getString(R.string.layoutDisplayTimeout));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetText(context, operation, context.getString(R.string.layoutDisplayTimeout));
    }
    
}
