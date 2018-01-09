package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

public class VibrationAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_VIBRATE;
    }

    @Override
    public String getCode() {
        return Codes.VIBRATE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option037, null, false);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.VibrateSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.VibrateChoices, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            if (arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE).equals(String.valueOf(Constants.VIBRATE_NEVER))) {
                spinner.setSelection(1);
            }
        }
        return dialogView;
    }

    @Override
    public String getName() {
        return "Vibration";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_ENABLE + ":" + Constants.COMMAND_VIBRATE;
        String typeChoice = "";

        // GET SETTING FROM SPINNER
        Spinner type = (Spinner) actionView.findViewById(R.id.VibrateSpinner);
        if (type != null) {
            typeChoice = (String) type.getSelectedItem();

            if (typeChoice.equals(context.getString(R.string.alwaysText))) {
                message += ":" + Constants.VIBRATE_ALWAYS;
            } else if (typeChoice.equals(context.getString(R.string.neverText))) {
                message += ":" + Constants.VIBRATE_NEVER;
            }
        }
        return new String[] {message, context.getString(R.string.listSoundVibrate), typeChoice};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listSoundVibrate);
        try {
            String type = args[0];
            if (type.equals(Constants.VIBRATE_ALWAYS))
                display += " " + context.getString(R.string.alwaysText);
            else if (type.equals(Constants.VIBRATE_NEVER))
                display += " " + context.getString(R.string.neverText);
            else if (type.equals(Constants.VIBRATE_SILENT))
                display += " " + context.getString(R.string.whenSilentText);
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 2, String.valueOf(Constants.VIBRATE_ALWAYS)))
                );
    }

    @SuppressWarnings("deprecation")
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Setting vibration");
        int mode = Utils.tryParseInt(args, 2, 0);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int vMode = AudioManager.VIBRATE_SETTING_ONLY_SILENT;

        if (mode == Constants.VIBRATE_ALWAYS) {
            Logger.d("Setting Vibrate ON");
            vMode = AudioManager.VIBRATE_SETTING_ON;
        } else if (mode == Constants.VIBRATE_NEVER) {
            Logger.d("Setting Vibrate OFF");
            vMode = AudioManager.VIBRATE_SETTING_OFF;
        }

        am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, vMode);
        am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, vMode);

        if (vMode != AudioManager.VIBRATE_SETTING_OFF) {
            Utils.updateSetting(context,"vibrate_when_ringing", 1);
        }
        else {
            Utils.updateSetting(context,"vibrate_when_ringing", 0);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetSetVibration);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionSetVibration);
    }
    
}
