package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

import org.apache.http.message.BasicNameValuePair;

import java.net.URLEncoder;

public class SendGlympseAction extends BaseAction {

    @Override
    public String getCommand() {
        return Constants.COMMAND_SEND_GLYMPSE;
    }

    @Override
    public String getCode() {
        return Codes.GLYMPSE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option059, null, false);
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.glympse_address)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            ((EditText) dialogView.findViewById(R.id.glympse_message)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
        }
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE);
            
            int position = 0;
            if (value.equals(Constants.GLYMPSE_TYPE_SMS)) {
                position = 1;
            } else if (value.equals(Constants.GLYMPSE_TYPE_TWITTER)) {
                position = 2;
            } else if (value.equals(Constants.GLYMPSE_TYPE_FACEBOOK)) {
                position = 3;
            }
            
            ((Spinner) dialogView.findViewById(R.id.spinner)).setSelection(position);
        }
        
        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_FOUR)) {
            ((EditText) dialogView.findViewById(R.id.glympse_duration)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_FOUR));
        }
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Send Glympse";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_SEND_GLYMPSE;
        Spinner spinner = (Spinner) actionView.findViewById(R.id.spinner);
        Spinner spinner_units = (Spinner) actionView.findViewById(R.id.spinner_duration_units);
        String type = Constants.GLYMPSE_TYPE_EMAIL;
        String units = Constants.GLYMPSE_UNITS_MINUTES;
        String duration;
        
        if (spinner_units.getSelectedItemPosition() == 1) {
            units = Constants.GLYMPSE_UNITS_HOURS;
        }
        
        int duration_time = 30;
        try { duration_time = Integer.parseInt(((EditText) actionView.findViewById(R.id.glympse_duration)).getText().toString()); }
        catch (Exception e) { }
        
        if (units == Constants.GLYMPSE_UNITS_HOURS) {
            duration = Integer.toString(duration_time * 60);
        } else {
            duration = Integer.toString(duration_time);
        }
        
        int selected = spinner.getSelectedItemPosition();
        if (selected == 1) {
            type = Constants.GLYMPSE_TYPE_SMS;
        } else if (selected == 2) {
            type = Constants.GLYMPSE_TYPE_TWITTER;
        } else if (selected == 3) {
            type = Constants.GLYMPSE_TYPE_FACEBOOK;
        }
            
        String recipient = "";
        String text = "";
        try { 
            recipient = ((EditText) actionView.findViewById(R.id.glympse_address)).getText().toString();
            recipient = Utils.encodeData(recipient);
        }
        catch (Exception e) { /* Fail silently */ }
        
        try { 
            text = ((EditText) actionView.findViewById(R.id.glympse_message)).getText().toString();
            text = Utils.encodeData(text);
        }
        catch (Exception e) { /* Fail silently */ }
        
        
        return new String[] { message + ":" + recipient + ":" + text + ":" + type + ":" + duration, context.getString(R.string.heading_glympse), ""};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.heading_glympse);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseEncodedString(args, 2, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.tryParseString(args, 3, Constants.GLYMPSE_TYPE_EMAIL)),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_FOUR, Utils.tryParseString(args, 4, "30"))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String to = Utils.tryParseEncodedString(args, 1, "");
        String message = Utils.tryParseEncodedString(args, 2, "");
        message = Utils.removePlaceHolders(message);
        String type = Utils.tryParseString(args, 3, "");
        String duration = Utils.tryParseString(args, 4, "");
        sendGlympse(context, to, message, type, duration);
        setupManualRestart(currentIndex + 1);
        
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetSendGlympse);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionSendGlympse);
    }

    private void sendGlympse(Context context, String recipient, String message, String type, String duration) {
        String uri = "glympse:";
        String args = "screen=send&src=mw10gk8n";
        String addType = "email";
        
        if (type == null) {
            addType = "email";
        } else {
            if (Constants.GLYMPSE_TYPE_EMAIL.equals(type)) {
                addType = "email";
            }
            else if (Constants.GLYMPSE_TYPE_SMS.equals(type)) {
                addType = "sms";
            } else if (Constants.GLYMPSE_TYPE_TWITTER.equals(type)) {
                addType = "twitter";
            } else if (Constants.GLYMPSE_TYPE_FACEBOOK.equals(type)) {
                addType = "facebook";
            }
        }
        args += "&rec_type=" + addType;
        
        if ((recipient != null) && (!recipient.isEmpty())) {
            try { args += "&rec_addr=" + URLEncoder.encode(recipient, "UTF-8"); }
            catch (Exception e) { Logger.e("Exception encoding Glympse recipient " + e); }
        }
        
        if ((message != null) && (!message.isEmpty())) {
            try { args += "&msg_text=" + URLEncoder.encode(message, "UTF-8"); }
            catch (Exception e) { Logger.e("Exception encoding Glympse message " + e); }
        }
        
        if ((duration != null) && (!duration.isEmpty())) {
            try { args += "&dur_mins=" + duration; }
            catch (Exception e) { Logger.e("Exception adding Glympse duration"); }
        }
        
        uri += args;
        
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // See if we have an intent handler
        if (Utils.isHandlerPresentForIntent(context, intent)) {
            context.startActivity(intent);
        } else {
            Intent glympse = new Intent(Intent.ACTION_VIEW);
            glympse.setData(Uri.parse("http://play.google.com/store/apps/details?id=com.glympse.android.glympse"));
            glympse.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(glympse);
        }
        
    }
    
}
