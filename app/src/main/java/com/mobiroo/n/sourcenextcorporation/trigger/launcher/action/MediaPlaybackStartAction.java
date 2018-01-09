package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.MusicUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

public class MediaPlaybackStartAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_MEDIA_START;
    }

    @Override
    public String getCode() {
        return Codes.MEDIA_START;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option064, null, false);
        return dialogView;
    }

    @Override
    public String getName() {
        return "Media Playback Start";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_ENABLE + ":" + Constants.COMMAND_MEDIA_START;
        return new String[] {message, context.getString(R.string.start_media_playback), ""};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.start_media_playback);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }

    @SuppressLint("InlinedApi") 
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Start media playback");
        
        MusicUtils.sendKeyCodeToReceiver(context, (Build.VERSION.SDK_INT > 10 ) ? KeyEvent.KEYCODE_MEDIA_PLAY : KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, null);
        
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.start_media_playback);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.start_media_playback);
    }
    
}
