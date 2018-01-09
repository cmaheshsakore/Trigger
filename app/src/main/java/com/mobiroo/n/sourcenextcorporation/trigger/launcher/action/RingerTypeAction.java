package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.MusicUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class RingerTypeAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_RINGER_TYPE;
    }

    @Override
    public String getCode() {
        return Codes.SOUND_RINGER_TYPE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option018, null, false);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.RingerTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.RingerTypes, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /* Check for any pre-populated arguments */
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            if (state.equals(String.valueOf(Constants.RINGER_TYPE_NORMAL))) {
                spinner.setSelection(0);
            } else if (state.equals(String.valueOf(Constants.RINGER_TYPE_SILENT))) {
                spinner.setSelection(1);
            } else if (state.equals(String.valueOf(Constants.RINGER_TYPE_VIBRATE))) {
                spinner.setSelection(2);
            }
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Ringer Type";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = "";
        String typeChoice = "";

        Spinner type = (Spinner) actionView.findViewById(R.id.RingerTypeSpinner);
        if (type != null) {
            message = Constants.COMMAND_RINGER_TYPE + ":";
            typeChoice = (String) type.getSelectedItem();

            switch(type.getSelectedItemPosition()) {
                case 0:
                    // Normal
                    message += Constants.RINGER_TYPE_NORMAL;
                    break;
                case 1:
                    // Silent
                    message += Constants.RINGER_TYPE_SILENT;
                    break;
                case 2:
                    // Vibrate  
                    message += Constants.RINGER_TYPE_VIBRATE;
                    break;
                default:
                    message += Constants.RINGER_TYPE_NORMAL;
                    break;
                        
            }

        }

        return new String[] { message, context.getString(R.string.soundRingerTypeText), typeChoice};
    }


    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.soundRingerTypeText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(2))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        int mode = Utils.tryParseInt(args, 1, -1);
        int rMode;
        switch (mode) {
            case Constants.RINGER_TYPE_SILENT:
                rMode = AudioManager.RINGER_MODE_SILENT;
                break;
            case Constants.RINGER_TYPE_VIBRATE:
                rMode = AudioManager.RINGER_MODE_VIBRATE;
                break;
            default:
                rMode = AudioManager.RINGER_MODE_NORMAL;
                break;
        }

        Logger.d("Setting Ringer Type to " + args[1] + "," + rMode);
        MusicUtils.sendShushBroadcast(context);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setRingerMode(rMode);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetToText(context, operation, context.getString(R.string.soundOptionsRinger), getNameFromArg(context, mArgs));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetToText(context, operation, context.getString(R.string.soundOptionsRinger), getNameFromArg(context, mArgs));
    }

    private String getNameFromArg(Context context, String[] args) {
        switch (Integer.parseInt(args[1])) {
            case 0:
                return context.getString(R.string.ringerTypeSilent);
            case 2:
                return context.getString(R.string.ringerTypeVibrate);
            default:
                return context.getString(R.string.ringerTypeNormal);
        }
    }
}
