package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerFoursquareVenueSearch;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

public class FoursquareCheckinGenericAction extends BaseAction {

    public static final int REQUEST_FOURSQUARE_SEARCH = 8;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_FOURSQUARE_SEARCH;
    }

    @Override
    public String getCode() {
        return Codes.CHECKIN_FOURSQUARE_SEARCH;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option029, null, false);
        return dialogView;
    }

    @Override
    public String getName() {
        return "Foursquare Search";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_FOURSQUARE_SEARCH + ":" + "X";
        return new String[] { message, context.getString(R.string.listFoursquareText), ""};
    }

    
    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listFoursquareText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        if (Utils.isConnectedToNetwork(context)) {
            setAutoRestart(currentIndex + 1);
            Logger.d("Calling foursquare search activity");
            Intent intent = new Intent(context, workerFoursquareVenueSearch.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("text", getWidgetText(context, 0));
            intent = buildReturnIntent(intent);
            context.startActivity(intent);
        } else {
            Logger.d("Network connection not available, skipping checkin");
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetCheckinText(context, operation, context.getString(R.string.social_foursquare));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionCheckinText(context, operation, context.getString(R.string.social_foursquare));
    }
}
