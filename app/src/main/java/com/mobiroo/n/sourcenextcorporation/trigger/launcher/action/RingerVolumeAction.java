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
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.MusicUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

public class RingerVolumeAction extends BaseAction {

    private int mVolume = 0;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_RINGER_VOLUME;
    }

    @Override
    public String getCode() {
        return Codes.SOUND_RINGER_VOLUME;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option021, null, false);

        AudioManager am = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
        mVolume = am.getStreamVolume(AudioManager.STREAM_RING);

        SeekBar ringerBar = (SeekBar) dialogView.findViewById(R.id.RingtoneVolumeSeek);
        ringerBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_RING));
        final TextView sliderValue = (TextView) dialogView.findViewById(R.id.sliderLevel);
        sliderValue.setText(String.valueOf(mVolume));
        ringerBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    sliderValue.setText(String.valueOf(progress));
                    mVolume = progress;
                } catch (Exception e) {}
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }    
        });
        
        /* Check for any pre-populated arguments */
        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            ringerBar.setProgress(Integer.parseInt(state));
            sliderValue.setText(state);
            mVolume = Integer.parseInt(state);
        } else {
            ringerBar.setProgress(mVolume);
        }
        
        return dialogView;
        
    }

    @Override
    public String getName() {
        return "Ring Volume";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_RINGER_VOLUME + ":" + mVolume;
        return new String[] {message, context.getString(R.string.listSoundRingerVolume), "" + mVolume + ""};
    }


    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listSoundRingerVolume);
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
        Logger.d("Setting Ringer volume to " + args[1]);
        int stream = AudioManager.STREAM_RING;
        int position = Utils.tryParseInt(args, 1, 7);

        MusicUtils.sendShushBroadcast(context);

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if ((stream == AudioManager.STREAM_RING) && (position > 0)) {
            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        am.setStreamVolume(stream, position, 0);
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetToText(context, operation, context.getString(R.string.soundOptionsRingVolume), mArgs[1]);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetToText(context, operation, context.getString(R.string.soundOptionsRingVolume), mArgs[1]);
    }


}
