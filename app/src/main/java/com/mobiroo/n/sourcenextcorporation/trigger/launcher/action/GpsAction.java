package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class GpsAction extends BaseAction {

    private boolean mHasRequestedSecureSettingsPermission = false;

    private final String PREF_LAST_SETTING = "GpsAction.LastSetting";

    @Override
    public String getCommand() {
        return Constants.MODULE_GPS;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_GPS;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option011, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.WifiAdapterToggle);
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
        return "GPS";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.WifiAdapterToggle);

        String toggleChoice = (String) toggle.getSelectedItem();
        String adapterChoice = context.getString(R.string.adapterGPS);

        // Append this pair to the list
        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.MODULE_GPS + "";

        return new String[] { message, toggleChoice, adapterChoice };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.adapterGPS);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;

        if (Utils.hasWriteSecureSettings(context)) {
            Logger.d("Modifying GPS Settings");
            if (operation == Constants.OPERATION_ENABLE) {
                operationTurnGPSOn(context);
            } else if (operation == Constants.OPERATION_DISABLE) {
                operationTurnGPSOff(context);
            } else if (operation == Constants.OPERATION_TOGGLE) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                    if (!provider.contains("gps")) {
                        operationTurnGPSOn(context);
                    } else {
                        operationTurnGPSOff(context);
                    }
                } else {
                    int value = Settings.Secure.getInt(context.getContentResolver(), "location_mode", 2);
                    if (value == 3) {
                        operationTurnGPSOff(context);
                    } else {
                        operationTurnGPSOn(context);
                    }
                }
            }
        } else {
            Logger.d("Skipping GPS, do not have permission to modify");
            /* If we are going to reset it will come from the permission request */
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

    public boolean shouldRequestPermission(Context context) {
        if (Utils.isRootPresent() && !Utils.hasWriteSecureSettings(context)) {
            if (!mHasRequestedSecureSettingsPermission) { 
                mHasRequestedSecureSettingsPermission = true;
                mNeedReset = true;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
        
    }

    private String getProvider(Context context) {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        return (provider != null) ? provider : "";
    }
    private void operationTurnGPSOn(Context context) {

        Logger.d(Constants.TAG, "Turning on GPS");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String provider = getProvider(context);
            Logger.d(Constants.TAG, "Current provider is " + provider);

            if (!provider.contains("gps")) {
                // if gps is disabled
                Logger.d("Setting provider to " + provider);
                try {
                    Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, provider + ",gps");
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception setting location provider", e);
                    e.printStackTrace();
                }
                provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

                if (!provider.contains("gps")) {
                    Intent settings = new Intent();
                    settings.setComponent(new ComponentName("com.android.settings", "com.android.settings.SubSettings"));
                    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        ((Activity) context).startActivity(settings);
                    } catch (Exception ex) {
                    }
                }
            }
        } else {
            try {
                int previous = Settings.Secure.getInt(context.getContentResolver(), "location_mode");
                Logger.d("Storing location mode of " + previous);
                SettingsHelper.setPrefInt(context, PREF_LAST_SETTING, previous);
                Settings.Secure.putInt(context.getContentResolver(), "location_mode", 3); }
            catch (Exception e) { e.printStackTrace(); }

        }

    }

    // / Needs to be a system app
    private void operationTurnGPSOff(Context context) {
        Logger.d(Constants.TAG, "Turning off GPS");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String provider = getProvider(context);
            Logger.d(Constants.TAG, "Current provider is " + provider);

            if (provider.contains("gps")) {
                provider = provider.replace("gps", "");
                while (provider.contains(",,")) {
                    provider = provider.replace(",,", "");
                }

                if (provider.equals(""))
                    provider = ",";

                Logger.d("Setting provider to " + provider);

                try {
                    Settings.Secure.putString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED, provider);
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception setting location provider", e);
                    e.printStackTrace();
                }
                provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (provider.contains("gps")) {
                    Intent settings = new Intent();
                    settings.setComponent(new ComponentName("com.android.settings", "com.android.settings.SubSettings"));
                    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        ((Activity) context).startActivity(settings);
                    } catch (Exception ex) {
                    }
                }

            }
        } else {
            int mode = SettingsHelper.getPrefInt(context, PREF_LAST_SETTING, 2);
            Logger.d("Setting location to mode " + mode);
            try { Settings.Secure.putInt(context.getContentResolver(), "location_mode", mode); }
            catch (Exception e) { e.printStackTrace(); }

        }
    }


}
