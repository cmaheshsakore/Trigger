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
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.MusicUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class NotificationVolumeAction extends BaseAction {

    private int mVolume = 0;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_NOTIFICATION_VOLUME;
    }

    @Override
    public String getCode() {
        return Codes.SOUND_NOTIFICATION_VOLUME;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        AudioManager am = (AudioManager) context.getSystemService(Activity.AUDIO_SERVICE);
        mVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option024, null, false);

        final TextView sliderValue = (TextView) dialogView.findViewById(R.id.sliderLevel);
        sliderValue.setText(String.valueOf(mVolume));

        SeekBar notificationBar = (SeekBar) dialogView.findViewById(R.id.NotificationVolumeSeek);
        notificationBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        notificationBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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
            notificationBar.setProgress(Integer.parseInt(state));
            sliderValue.setText(state);
            mVolume = Integer.parseInt(state);
        } else {
            notificationBar.setProgress(mVolume);
        }
        
        return dialogView;
        
    }

    @Override
    public String getName() {
        return "Notification Volume";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_NOTIFICATION_VOLUME + ":" + mVolume;
        return new String[] {message, context.getString(R.string.listSoundNotificationVolume), "" + mVolume + ""};
    }


    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listSoundNotificationVolume);
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
        
        Logger.d("Setting Notification volume to " + args[1]);
        int stream = AudioManager.STREAM_NOTIFICATION;
        int position = Utils.tryParseInt(args, 1, 7);

        MusicUtils.sendShushBroadcast(context);

        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(stream, position, 0);
        } catch (Exception e) {
            /* Internal error thrown here possibly */
        }
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSetToText(context, operation, context.getString(R.string.soundOptionsNotificationVolume), mArgs[1]);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSetToText(context, operation, context.getString(R.string.soundOptionsNotificationVolume), mArgs[1]);
    }
}
