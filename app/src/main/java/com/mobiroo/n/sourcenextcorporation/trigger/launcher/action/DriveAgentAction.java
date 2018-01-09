package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class DriveAgentAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.MODULE_DRIVE_AGENT;
    }

    @Override
    public String getCode() {
        return Codes.DRIVE_AGENT;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option067, null, false);
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
        return "Drive Agent";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.AdapterToggle);

        String toggleChoice = (String) toggle.getSelectedItem();

        // Append this pair to the list
        String message = "";
        String prefix = "";
        String suffix = context.getString(R.string.drive_agent);
        if (toggleChoice.equals(context.getString(R.string.enableText))) {
            message = Constants.COMMAND_ENABLE + ":";
            prefix = context.getString(R.string.enableText);
        }
        else if (toggleChoice.equals(context.getString(R.string.disableText))) {
            message = Constants.COMMAND_DISABLE + ":";
            prefix = context.getString(R.string.disableText);
        }
        else if (toggleChoice.equals(context.getString(R.string.toggleText))) {
            message = Constants.COMMAND_TOGGLE + ":";
            prefix = context.getString(R.string.toggleText);
        }
        
        message += Constants.MODULE_DRIVE_AGENT;


        return new String[] {message, prefix, suffix };
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.drive_agent);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        
        Logger.d("Drive Agent");
        
        /* Check if drive agent is installed */
        if (Utils.isPackageInstalled(context, "com.trigger.driveagent")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.trigger.driveagent", "com.trigger.driveagent.activity.SilentLauncherActivity");
            if (operation == Constants.OPERATION_ENABLE) {
                intent.putExtra("a_start", true);
            } else if (operation == Constants.OPERATION_DISABLE) {
                intent.putExtra("a_start", false);
            }
            setupManualRestart(currentIndex + 1);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=com.trigger.driveagent"));
            context.startActivity(intent);
            setupManualRestart(currentIndex + 1);
        }
    }


    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.drive_agent));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.drive_agent));
    }

}
