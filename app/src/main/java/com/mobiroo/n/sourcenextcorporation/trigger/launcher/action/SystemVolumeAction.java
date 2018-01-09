package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

public class SystemVolumeAction extends BaseAction {
    private int mVolume = 0;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SYSTEM_VOLUME;
    }

    @Override
    public String getCode() {
        return Codes.SOUND_SYSTEM_VOLUME;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        AudioManager am = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
        mVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option061, null, false);

        final TextView sliderValue = (TextView) dialogView.findViewById(R.id.sliderLevel);
        sliderValue.setText(String.valueOf(mVolume));

        SeekBar mediaBar = (SeekBar) dialogView.findViewById(R.id.VolumeSeek);
        mediaBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        mediaBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    sliderValue.setText(String.valueOf(progress));
                    mVolume = progress;
                } catch (Exception e) {
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            mediaBar.setProgress(Integer.parseInt(state));
            sliderValue.setText(state);
            mVolume = Integer.parseInt(state);
        } else {
            mediaBar.setProgress(mVolume);
        }
        
        return dialogView;
        
    }

    @Override
    public String getName() {
        return "System Volume";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_SYSTEM_VOLUME + ":" + mVolume;
        return new String[] {message, context.getString(R.string.system_volume), "" + mVolume + ""};
    }


    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.system_volume);
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
        return new CommandArguments(new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, Utils.tryParseString(args, 1, "7")));
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        int position = Utils.tryParseInt(args, 1, 7);
        Logger.d("Setting System Volume to " + position);

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, position, 0);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetSystemVolume) + " " + mArgs[1];
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionSystemVolume) + " " + mArgs[1];
    }
    
}
