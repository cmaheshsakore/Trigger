package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TextDisplayActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by krohnjw on 4/25/2014.
 */
public class ShowTextAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_SHOW_TEXT;
    }

    @Override
    public String getCode() {
        return Codes.SHOW_TEXT;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        View v = View.inflate(context, R.layout.configuration_dialog_option074, null);

        if (arguments.hasArgument(CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) v.findViewById(R.id.user_message)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }

        return v;
    }

    @Override
    public String getName() {
        return "Show Text";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = ((EditText) actionView.findViewById(R.id.user_message)).getText().toString();
        if (!message.isEmpty()) {
            return new String[] { Constants.COMMAND_SHOW_TEXT + ":" + Utils.encodeData(message), context.getString(R.string.show_user_text),  message };
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
        return context.getString(R.string.show_user_text) + ":" + Utils.tryParseEncodedString(args, 1, "");
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
            Intent intent = new Intent(context, TextDisplayActivity.class);
            intent.putExtra(TextDisplayActivity.EXTRA_LAYOUT, R.layout.activity_show_text_window);
            intent.putExtra(TextDisplayActivity.EXTRA_MESSAGE, message);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.show_user_text);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.show_user_text);
    }

}
