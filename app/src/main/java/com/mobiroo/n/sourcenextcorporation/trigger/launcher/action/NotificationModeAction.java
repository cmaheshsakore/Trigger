package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class NotificationModeAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.MODULE_NOTIFICATION_MODE;
    }

    @Override
    public String getCode() {
        return Codes.NOTIFICATION_MODE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option085, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.WifiAdapterToggle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.notification_modes, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /* Check for any pre-populated arguments */
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            if (state.equals(Constants.NOTIFICATION_MODE_ALL)) {
                spinner.setSelection(0);
            } else if (state.equals(Constants.NOTIFICATION_MODE_PRIORITY)) {
                spinner.setSelection(1);
            } else if (state.equals(Constants.NOTIFICATION_MODE_NONE)) {
                spinner.setSelection(2);
            }
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Notification Mode";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.WifiAdapterToggle);

        String toggleChoice = (String) toggle.getSelectedItem();
        String adapterChoice = context.getString(R.string.notification_mode_title);

        // Append this pair to the list
        String message = Constants.MODULE_NOTIFICATION_MODE + ":";

        if (toggleChoice.equals(context.getString(R.string.notification_all)))
            message += Constants.NOTIFICATION_MODE_ALL;
        else if (toggleChoice.equals(context.getString(R.string.notification_priority)))
            message += Constants.NOTIFICATION_MODE_PRIORITY;
        else if (toggleChoice.equals(context.getString(R.string.notification_none)))
            message += Constants.NOTIFICATION_MODE_NONE;

        return new String[] { message, toggleChoice, adapterChoice };
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.notification_mode_title);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, args.length > 1 ? args[1] : "")});
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;

        if (Utils.hasWriteSecureSettings(context)) {
            Logger.d("Modifying Notification Mode settings");

            try {
                Logger.d("Setting Zen Mode to " + Integer.parseInt(args[1]));
                Settings.Global.putInt(context.getContentResolver(), "zen_mode", Integer.parseInt(args[1]));
                Logger.d("Zen mode setting is " + Settings.Global.getInt(context.getContentResolver(), "zen_mode"));
            }
            catch (Exception e) { e.printStackTrace(); }
        } else {
            Logger.d("Skipping Notification Mode, do not have permission to modify");
        }

    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.adapterGPS));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.adapterGPS));
    }
}
