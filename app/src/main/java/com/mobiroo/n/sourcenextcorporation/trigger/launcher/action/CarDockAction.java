package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class CarDockAction extends BaseAction {

    private final String SAMSUNG_DRIVING_MODE = "driving_mode_on";

    @Override
    public String getCommand() {
        return Constants.MODULE_CAR_DOCK;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_CAR_DOCK;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option046, null, false);
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
        
        /* Check for sVoice flag */
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            if (arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE).equals("1")) {
                ((CheckBox) dialogView.findViewById(R.id.sVoiceCheck)).setChecked(true);
            }
        }



        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            String state = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO);
            ((CheckBox) dialogView.findViewById(R.id.hardwareDock)).setChecked(("1".equals(state)) ? true : false);
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Car Dock";
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

        message += Constants.MODULE_CAR_DOCK + "";

        // Also check for SVoice check
        String sVoice = ((CheckBox) actionView.findViewById(R.id.sVoiceCheck)).isChecked() ? "1" : "0";
        message += ":" + sVoice;

        int hardware = ((CheckBox) actionView.findViewById(R.id.hardwareDock)).isChecked() ? 1 : 0;
        message += ":" + hardware;

        return new String[] { message, toggleChoice, context.getString(R.string.layoutAppCarDock)};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.layoutAppCarDock);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        String dockFlag = "0";
        String[] args = action.split(":");
        if (args.length > 1) {
            try { dockFlag = args[2]; }
            catch (Exception e) { }
        }
        return new CommandArguments(new BasicNameValuePair[] { 
                new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1)),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, dockFlag),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseEncodedString(args, 3, "0"))
                });
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Car Dock");
        boolean useSVoice = (Utils.tryParseString(args, 2, "").equals("1")) ? true: false;

        boolean hardware = (Utils.tryParseString(args, 3, "0").equals("1")) ? true : false;

        if (operation == Constants.OPERATION_ENABLE) {
            // Also check if we are enabling S-Voice
            if (useSVoice) {
                Logger.d("Using Driving Mode");
                Utils.updateSetting(context, SAMSUNG_DRIVING_MODE, 1);
            }

            setCarDockModeSW(context, Constants.OPERATION_ENABLE);

            if (hardware && Utils.isRootPresent()) {
                new DockModeRootTask().execute(Intent.EXTRA_DOCK_STATE_CAR);
            }

        } else if (operation == Constants.OPERATION_DISABLE) {
            // Also check if we are enabling S-Voice
            if (useSVoice) {
                Logger.d("Using Driving Mode");
                Utils.updateSetting(context, SAMSUNG_DRIVING_MODE, 0);
            }

            setCarDockModeSW(context, Constants.OPERATION_DISABLE);

            if (hardware && Utils.isRootPresent()) {
                new DockModeRootTask().execute(Intent.EXTRA_DOCK_STATE_UNDOCKED);
            }
        }
    }

    private static void setCarDockModeSW(Context context, int operation) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService("uimode");
        if (operation == Constants.OPERATION_ENABLE)
            uiModeManager.enableCarMode(1);
        else
            uiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.layoutAppCarDock));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.layoutAppCarDock));
    }
}
