package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by krohnjw on 4/25/2014.
 */
public class ShowToastAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_SHOW_TOAST;
    }

    @Override
    public String getCode() {
        return Codes.SHOW_TOAST;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        View v = View.inflate(context, R.layout.configuration_dialog_option074, null);

        ((TextView) v.findViewById(R.id.title)).setText(R.string.show_toast);

        if (arguments.hasArgument(CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) v.findViewById(R.id.user_message)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }

        return v;
    }

    @Override
    public String getName() {
        return "Show Toast";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = ((EditText) actionView.findViewById(R.id.user_message)).getText().toString();
        if (!message.isEmpty()) {
            return new String[] { Constants.COMMAND_SHOW_TOAST + ":" + Utils.encodeData(message), context.getString(R.string.show_toast),  message };
        } else {
            return new String[0];
        }
    }

    @Override
    public int getMinArgLength() {
        return 1;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.show_toast) + ":" + Utils.tryParseEncodedString(args, 1, "");
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 1, ""))
        );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String message = Utils.tryParseEncodedString(args, 1, "");
        if (!message.isEmpty()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.show_toast);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.show_toast);
    }

}
