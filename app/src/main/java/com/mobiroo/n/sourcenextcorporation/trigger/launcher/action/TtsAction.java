package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.TtsWorker;

import org.apache.http.message.BasicNameValuePair;

public class TtsAction extends BaseAction {
   
    public static final int REQUEST_TTS_SPEAK = 9;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SPEAK_TTS;
    }

    @Override
    public String getCode() {
        return Codes.TTS;
    }
    
    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option060, null, false);
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.tts_text)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Speak Text";                
    }


    @Override
    public String[] buildAction(View actionView, Context context) {
        String text = "";
        try { text = ((TextView) actionView.findViewById(R.id.tts_text)).getText().toString(); }
        catch (Exception e) { Logger.e("Exception getting TTS Text"); }
        
        if (!text.isEmpty()) {
            return new String[] { Constants.COMMAND_SPEAK_TTS + ":" + Utils.encodeData(text), context.getString(R.string.heading_tts), text};
        } else {
            return new String[] { "" };
        }
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.heading_tts);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, ""))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        String text = Utils.tryParseEncodedString(args, 1, "");
        text = Utils.removePlaceHolders(text);
        if (!text.isEmpty()) {
            setAutoRestart(currentIndex + 1);
            Intent intent = new Intent(context, TtsWorker.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(TtsWorker.EXTRA_TTS_MESSAGE, text);
            intent = buildReturnIntent(intent);
            context.startActivity(intent);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widget_speak_text);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.action_speak_text);
    }
    
    @Override
    public boolean scheduleWatchdog() {
        return true;
    }
}
