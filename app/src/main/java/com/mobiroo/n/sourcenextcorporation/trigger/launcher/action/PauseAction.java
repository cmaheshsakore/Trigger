package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class PauseAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_PAUSE;
    }

    @Override
    public String getCode() {
        return Codes.PAUSE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option054, null, false);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.pauseLength)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Pause";
    }


    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText length = (EditText) actionView.findViewById(R.id.pauseLength);
        String inputLength = length.getText().toString();
        if ((inputLength.equals("")) || (inputLength.equals("0")))
            inputLength = "1";

        return new String[] {Constants.COMMAND_PAUSE + ":" + inputLength, context.getString(R.string.layoutAppPause), "" };
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.layoutAppPause);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "5"))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        
        int length = Utils.tryParseInt(args, 1, -1);
        if (length > -1) {
            setupManualRestart(currentIndex + 1, length);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetPause) + " " + mArgs[1] + " " + context.getString(R.string.timeSeconds);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionPause) + " " + mArgs[1] + " " + context.getString(R.string.timeSeconds);
    }
    
}
