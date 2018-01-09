package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class GooglePlacesCheckinAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_LATITUDE_PLACES;
    }

    @Override
    public String getCode() {
        return Codes.CHECKIN_LATITUDE_PLACES;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        return inflater.inflate(R.layout.configuration_dialog_option040, null, false);
    }

    @Override
    public String getName() {
        return "Google Places Checkin";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_ENABLE + ":" + Constants.COMMAND_LATITUDE_PLACES;
        return new String[] {message, context.getString(R.string.listPlacesText), ""};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listPlacesText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Google Places Checkin");
        mNeedManualRestart = true;
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("latitude://latitude/checkin"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (Exception e) {
            /* No longer a supported URI */
        }
        setupManualRestart(currentIndex + 1);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetCheckinText(context, operation, context.getString(R.string.social_google_places));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionCheckinText(context, operation, context.getString(R.string.social_google_places));
    }
    
}
