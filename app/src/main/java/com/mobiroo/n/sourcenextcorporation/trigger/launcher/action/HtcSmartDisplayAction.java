package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krohnjw on 5/5/2014.
 */
public class HtcSmartDisplayAction extends BaseAction {

    private String[] SETTINGS = { "screen_off_timeout"};

    private List<int[]> VALUES = new ArrayList<int[]>()
    {
        {
            add(new int[] { 2 * 60 * 1000, -2});
        }
    };

    int title = R.string.htc_smart_display;

    @Override
    public String getCommand() {
        return Constants.COMMAND_HTC_SMART_DISPLAY;
    }

    @Override
    public String getCode() {
        return Codes.HTC_SMART_DISPLAY;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option_toggle_generic, null, false);

        ((TextView) dialogView.findViewById(R.id.title)).setText(title);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner);
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
        return "HTC Smart Display";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.spinner);

        String toggleChoice = (String) toggle.getSelectedItem();
        String item = context.getString(title);


        // Append this pair to the list
        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += getCommand();

        return new String[] {message, toggleChoice, item};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(title);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    final String PREF_VALUE = "HtcSmartDisplayPreviousValue";

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d(getName() + "=" + operation);
        for (int i=0; i< SETTINGS.length; i++) {
            String setting = SETTINGS[i];
            int[] values = VALUES.get(i);
            int on = values[1];
            int off = values[0];

            int value = on;
            int current = Utils.getSetting(context, setting, off);
            if (operation == Constants.OPERATION_TOGGLE) {
                // Get current value
                if (current == on) {
                    // See if we have a previously saved value to default back to
                    value = getCachedTimeout(context, off);
                } else {
                    // Write down current value
                    saveExistingTimeout(context, current);
                    value = on;
                }
            } else {
                if (operation == Constants.OPERATION_ENABLE) {
                    // Save existing value prior to enabling to use with "disable"
                    saveExistingTimeout(context, current);
                    value = on;
                } else {
                    value = getCachedTimeout(context, off);
                }
            }

            Utils.updateSetting(context, setting, value);
        }
    }

    private void saveExistingTimeout(Context context, int current) {
        SettingsHelper.setPrefInt(context, PREF_VALUE, current);
    }

    private int getCachedTimeout(Context context, int def) {
        int t = SettingsHelper.getPrefInt(context, PREF_VALUE, -876);
        return (t != -876) ? t : def;
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(title));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(title));
    }

}
