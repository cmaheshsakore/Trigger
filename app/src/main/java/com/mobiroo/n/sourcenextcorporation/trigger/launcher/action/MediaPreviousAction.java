package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.MusicUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class MediaPreviousAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_MEDIA_PREVIOUS;
    }

    @Override
    public String getCode() {
        return Codes.MEDIA_PREVIOUS;
    }


    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option063, null, false);
        return dialogView;
    }

    @Override
    public String getName() {
        return "Media Previous";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_MEDIA_PREVIOUS + ":X";
        return new String[] {message, context.getString(R.string.previous_track), ""};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.previous_track);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Media Playback Previous");
        MusicUtils.sendKeyCodeToReceiver(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS, null);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetMediaPrevious);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionMediaPrevious);
    }
    
}
