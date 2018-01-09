package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerFacebookCheckin;

public class FacebookCheckinAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_FACEBOOK;
    }

    @Override
    public String getCode() {
        return Codes.CHECKIN_FACEBOOK;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
         LayoutInflater inflater = getLayoutInflater(context);
         return inflater.inflate(R.layout.configuration_dialog_option030, null, false);
    }

    @Override
    public String getName() {
        return "Facebook Checkin";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_FACEBOOK + ":" + "X";
        return new String[] { message, context.getString(R.string.listFacebookText), ""};
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listFacebookText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Check in to Facebook");
        if (Utils.isConnectedToNetwork(context)) {
            setupManualRestart(currentIndex + 1);
            Intent intent = new Intent(context, workerFacebookCheckin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Logger.d("No network connection, skipping facebook");
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetCheckinText(context, operation, context.getString(R.string.social_facebook));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionCheckinText(context, operation, context.getString(R.string.social_facebook));
    }
    
}
