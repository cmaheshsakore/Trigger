package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class DeskDockAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.MODULE_DESK_DOCK;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_DESK_DOCK;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option045, null, false);
        // populate spinner
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.dockOperation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoicesNoToggle, R.layout.configuration_spinner);
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

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            String state = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
            ((CheckBox) dialogView.findViewById(R.id.hardwareDock)).setChecked(("1".equals(state)) ? true : false);
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Desk Dock";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.dockOperation);
        String toggleChoice = (String) toggle.getSelectedItem();
        // Append this pair to the list
        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.MODULE_DESK_DOCK + "";

        int hardware = ((CheckBox) actionView.findViewById(R.id.hardwareDock)).isChecked() ? 1 : 0;

        message += ":" + hardware;

        return new String[] { message, toggleChoice, context.getString(R.string.layoutAppDeskDock) };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.layoutAppDeskDock);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        String[] args = action.split(":");

        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, Utils.tryParseEncodedString(args, 0, "E")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 2, "0"))
        );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Desk Dock");
        boolean hardware = (Utils.tryParseString(args, 3, "0").equals("1")) ? true : false;

        if (operation == Constants.OPERATION_ENABLE) {
            if (hardware && Utils.isRootPresent()) {
                new DockModeRootTask().execute(Intent.EXTRA_DOCK_STATE_DESK);
            } 
        } else if (operation == Constants.OPERATION_DISABLE) {
            if (hardware && Utils.isRootPresent()) {
                new DockModeRootTask().execute(Intent.EXTRA_DOCK_STATE_UNDOCKED);
            } 
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.layoutAppDeskDock));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.layoutAppDeskDock));
    }
    
}
