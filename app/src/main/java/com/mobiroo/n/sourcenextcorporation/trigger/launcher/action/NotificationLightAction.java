package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
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

public class NotificationLightAction extends BaseAction {

    private final String NOTIFICATION_LIGHT_PULSE = "notification_light_pulse";
    private final String SAMSUNG_NOTIFICATION_LIGHT_MISSED = "led_indicator_missed_event";
    private final String SAMSUNG_NOTIFICATION_LIGHT_INCOMING = "led_indicator_incoming_notification";
    private final String SAMSUNG_NOTIFICATION_LOW_BATTERY = "led_indicator_low_battery";
    private final String SAMSUNG_NOTIFICATION_CHARGING = "led_indicator_charging";
    /* Samsung typo'd their OWN setting.  Using both what that value should be and the mis-spelled version */
    private final String SAMSUNG_NOTIFICATION_CHARGING_CURRENT = "led_indicator_charing";

    private final String LG_NOTIFICATION_LIGHT = "lge_notification_light_pulse";

    @Override
    public String getCommand() {
        return Constants.MODULE_NOTIFICATION_LIGHT;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_NOTIFICATION_LIGHT;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option012, null, false);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.NotificationLightToggle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /* Check for any pre-populated arguments */
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            if (state.equals(String.valueOf(Constants.COMMAND_ENABLE))) {
                spinner.setSelection(0);
            } else if (state.equals(String.valueOf(Constants.COMMAND_DISABLE))) {
                spinner.setSelection(1);
            } else if (state.equals(String.valueOf(Constants.COMMAND_TOGGLE))) {
                spinner.setSelection(2);
            }
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Notification Light";
    } 

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner adapter = (Spinner) actionView.findViewById(R.id.NotificationLightToggle);
        String ap = (String) adapter.getSelectedItem();

        String message = "";
        String prefix = "";
        String suffix = context.getString(R.string.listDisplayNotificationLightText);

        if (ap.equals(context.getString(R.string.enableText))) {
            message = Constants.COMMAND_ENABLE + ":" + Constants.MODULE_NOTIFICATION_LIGHT;
            prefix = context.getString(R.string.enableText);
        } else if (ap.equals(context.getString(R.string.disableText))) {
            message = Constants.COMMAND_DISABLE + ":" + Constants.MODULE_NOTIFICATION_LIGHT;
            prefix = context.getString(R.string.disableText);
        } else {
            message = Constants.COMMAND_TOGGLE + ":" + Constants.MODULE_NOTIFICATION_LIGHT;
            prefix = context.getString(R.string.toggleText);
        }

        return new String[] { message, prefix, suffix };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listDisplayNotificationLightText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Notification Light");
        int curSetting = getNotificationLightState(context);
        Logger.d("Current setting is " + curSetting);

        if ((operation == Constants.OPERATION_ENABLE)) {
            operateNotificationLight(context, Constants.OPERATION_ENABLE);
        } else if ((operation == Constants.OPERATION_DISABLE)) {
            operateNotificationLight(context, Constants.OPERATION_DISABLE);
        } else if (operation == Constants.OPERATION_TOGGLE) {
            if (curSetting == 1) {
                operateNotificationLight(context, Constants.OPERATION_DISABLE);
            } else {
                operateNotificationLight(context, Constants.OPERATION_ENABLE);
            }
        }
    }

    private int getSetting(SharedPreferences settings, String pref, int default_value) {
        return settings.getInt(pref, default_value);
    }

    private void restoreSetting(Context context, SharedPreferences settings, String pref, int value) {
        Utils.updateSetting(
                context, 
                pref, 
                getSetting(settings, pref, value)
                );
    }
    private void saveSetting(Context context, SharedPreferences.Editor editor, String pref, int value) {
        editor.putInt(pref, Utils.getSetting(context, pref, 1));
    }


    private int getNotificationLightState(Context context) {
        if (Utils.isLgDevice() && (!Build.HOST.equals("cyanogenmod"))) {
            return Utils.getSetting(context, LG_NOTIFICATION_LIGHT, 1);
        } else {
            return Utils.getSetting(context, NOTIFICATION_LIGHT_PULSE, 1);
        }
    }

    private void setNotificationLightState(Context context, int value) {
        if (Utils.isLgDevice()) {
            Utils.updateSetting(context, LG_NOTIFICATION_LIGHT, value);
            if (Build.HOST.equals("cyanogenmod")) {
                Utils.updateSetting(context, NOTIFICATION_LIGHT_PULSE, value);
            }
        } else {
            Utils.updateSetting(context, NOTIFICATION_LIGHT_PULSE, value);
        }
    }

    private void saveSamsungSettings(Context context) {
        SharedPreferences settings = context.getSharedPreferences("notification_light_settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        /* Save settings first for when we restore */
        saveSetting(context, editor, SAMSUNG_NOTIFICATION_LIGHT_MISSED, 1);
        saveSetting(context, editor, SAMSUNG_NOTIFICATION_LIGHT_INCOMING,  1);
        saveSetting(context, editor, SAMSUNG_NOTIFICATION_LOW_BATTERY, 1);
        saveSetting(context, editor, SAMSUNG_NOTIFICATION_CHARGING, 1);
        saveSetting(context, editor, SAMSUNG_NOTIFICATION_CHARGING_CURRENT, 1);
        editor.commit();
    }

    private void restoreSamsungSettings(Context context) {
        SharedPreferences settings = context.getSharedPreferences("notification_light_settings", 0);
        restoreSetting(context, settings, SAMSUNG_NOTIFICATION_LIGHT_MISSED, 1);
        restoreSetting(context, settings, SAMSUNG_NOTIFICATION_LIGHT_INCOMING, 1);
        restoreSetting(context, settings, SAMSUNG_NOTIFICATION_LOW_BATTERY, 1);
        restoreSetting(context, settings, SAMSUNG_NOTIFICATION_CHARGING, 1);
        restoreSetting(context, settings, SAMSUNG_NOTIFICATION_CHARGING_CURRENT, 1);
    }

    private void disableSamsungSettings(Context context) {
        Utils.updateSetting(context, SAMSUNG_NOTIFICATION_LIGHT_MISSED, 0);
        Utils.updateSetting(context, SAMSUNG_NOTIFICATION_LIGHT_INCOMING, 0);
        Utils.updateSetting(context, SAMSUNG_NOTIFICATION_LOW_BATTERY, 0);
        Utils.updateSetting(context, SAMSUNG_NOTIFICATION_CHARGING, 0);
        Utils.updateSetting(context, SAMSUNG_NOTIFICATION_CHARGING_CURRENT, 0);
    }

    private void operateNotificationLight(Context context, int operation) {

        switch (operation) {
            case Constants.OPERATION_ENABLE:
                enable(context);
                break;
            case Constants.OPERATION_DISABLE:
                disable(context); 
                break;
            default:
                if (getNotificationLightState(context) == 1) {
                    disable(context);
                } else  {
                    enable(context);
                }
        }
    }

    private void enable(Context context) {
        setNotificationLightState(context, 1);
        if (!Utils.isLgDevice()) {
            // Samsung specific settings
            restoreSamsungSettings(context);
        }
    }

    private void disable(Context context) {
        setNotificationLightState(context, 0);
        if (!Utils.isLgDevice()) {
            saveSamsungSettings(context);
            disableSamsungSettings(context);
        }

    }
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.layoutDisplayNotificationLight));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.layoutDisplayNotificationLight));
    }


}
