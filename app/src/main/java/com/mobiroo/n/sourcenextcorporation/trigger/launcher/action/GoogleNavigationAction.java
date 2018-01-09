package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class GoogleNavigationAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_NAVIGATION;
    }

    @Override
    public String getCode() {
        return Codes.NAVIGATION;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option038, null, false);

        Spinner spinner = (Spinner) dialogView.findViewById(R.id.navTypeToggle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.NavTypes, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.navText)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            String choice = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
            if ("w".equals(choice)) {
                spinner.setSelection(2);
            } else if ("r".equals(choice)) {
                spinner.setSelection(1);
            }
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Navigation";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText et = (EditText) actionView.findViewById(R.id.navText);
        String ap = et.getText().toString();

        Spinner spinner = (Spinner) actionView.findViewById(R.id.navTypeToggle);
        String choice = spinner.getSelectedItem().toString();

        @SuppressWarnings("unused")
        String navDriving = context.getString(R.string.NavTypeDriving);
        String navWalking = context.getString(R.string.NavTypeWalking);
        String navTransit = context.getString(R.string.NavTypeTransit);

        String navType = "d";
        if (choice.equals(navWalking)) {
            navType = "w";
        } else if (choice.equals(navTransit)) {
            navType = "r";
        }

        return new String[] { Constants.COMMAND_NAVIGATION + ":" + ap + ":" + navType, context.getString(R.string.listNavText), ap};
    }


    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listNavText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, ""))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;
        String address = Utils.tryParseString(args, 1, "");
        Logger.d("Navigating to " + address);
        String dirFlag = Utils.tryParseString(args, 2, "d");
        
        String navFlag = "&nav=1";
        if (dirFlag.equals("w") || (dirFlag.equals("r")))
            navFlag = "";
        String baseURL = "http://maps.google.com/maps?myl=saddr&daddr=" + address + "&dirflg=" + dirFlag + navFlag;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(baseURL));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        
        setupManualRestart(currentIndex + 1);
        
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetNavigate);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionNavigate);
    }

    
}
