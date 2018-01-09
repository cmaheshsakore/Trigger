package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerTwitter;

import org.apache.http.message.BasicNameValuePair;

public class TwitterTweetAction extends BaseAction {

    public static final int REQUEST_TWITTER_TWEET = 4;

    @Override
    public String getCommand() {
        return Constants.COMMAND_TWITTER_TWEET;
    }

    @Override
    public String getCode() {
        return Codes.TWITTER_TWEET;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option032, null, false);

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.tweetText)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Twitter Tweet";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText tweetBox = (EditText) actionView.findViewById(R.id.tweetText);
        String tweet = tweetBox.getText().toString();
        tweet = Utils.encodeData(tweet);
        String message = Constants.COMMAND_TWITTER_TWEET + ":" + tweet;

        return new String[]{message, context.getString(R.string.listTweetText), ""};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listTweetText);
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
        if (Utils.isConnectedToNetwork(context)) {
            String tweet = Utils.tryParseEncodedString(args, 1, "");
            if (!tweet.isEmpty()) {
            /* See if tweet contains any placeholders */
                tweet = Utils.removePlaceHolders(tweet);
                Logger.d("Sending tweet : " + tweet);
                setAutoRestart(currentIndex + 1);
                Intent intent = new Intent(context, workerTwitter.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.EXTRA_TWITTER_MESSAGE, tweet);
                intent = buildReturnIntent(intent);
                context.startActivity(intent);
            }
        } else {
            Logger.d("No network connection, skipping tweet");
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetSendTweet);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionSendTweet);
    }

}
