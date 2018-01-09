package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildTools;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.ContactQuery;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public class SmsAction extends BaseAction {

    public static final int REQUEST_PICK_CONTACT = 9001;

    @Override
    public String getCommand() {
        return Constants.COMMAND_SEND_SMS;
    }

    @Override
    public String getCode() {
        return Codes.PHONE_SMS;
    }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option047, null, false);
        TextView optionalText = (TextView) dialogView.findViewById(R.id.smsSubText);
        if (Utils.isSMSPluginInstalled(context) 
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_KOREA)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_NXP)) {
            optionalText.setText(context.getString(R.string.optionsPhoneSMS));
        } else  {
            final Context thisContext = context;
            optionalText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("market://details?id=com.trigger.nfctl.plugins.sms");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    thisContext.startActivity(intent);
                }

            });
        }

        if (BuildTools.showContactPickers()){ 
            ((ImageButton) dialogView.findViewById(R.id.contact_picker)).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK,  Phone.CONTENT_URI);
                    ((Activity) context).startActivityForResult(intent, REQUEST_PICK_CONTACT);  
                }  
            });
        } else {
            ((ImageButton) dialogView.findViewById(R.id.contact_picker)).setVisibility(View.GONE);
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.smsNumber)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            ((EditText) dialogView.findViewById(R.id.smsText)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
        }
        return dialogView;
    }

    public void setContact(Context context, final View actionView, Uri result) {

        List<String> numbers = ContactQuery.getContactDataFor(
                context, 
                result, 
                new String[] {Phone.NUMBER}, 
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "=?", 
                new String[] { "1" }, 
                Phone.NUMBER);

        if (numbers.size() == 1) {
            ((EditText) actionView.findViewById(R.id.smsNumber)).setText(numbers.get(0));
        } else if (numbers.size() > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final ArrayAdapter<String> list = new ArrayAdapter<String>(context, R.layout.list_item_single, numbers.toArray(new String[numbers.size()]));
            builder.setAdapter(
                    list, 
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((EditText) actionView.findViewById(R.id.smsNumber)).setText((String)list.getItem(which));
                        }
                    });
            builder.create().show();
        }

    }

    @Override
    public String getName() {
        return "Send SMS";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        EditText tweetBox = (EditText) actionView.findViewById(R.id.smsText);
        String text = tweetBox.getText().toString();
        text = Utils.encodeData(text);

        String number = "";
        EditText numberBox = (EditText) actionView.findViewById(R.id.smsNumber);
        number = numberBox.getText().toString();

        if (number.length() > 0) {
            String message = Constants.COMMAND_SEND_SMS + ":" + number + ":" + text;
            return new String[] { message, context.getString(R.string.listPhoneSMS), number};
        } else {
            return new String[] { "" };
        }

    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listPhoneSMS);
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
        String to = Utils.tryParseEncodedString(args, 1, "");
        String message = Utils.tryParseEncodedString(args, 2, "");

        if (to.length() > 0) {
            message = Utils.removePlaceHolders(message);
            Logger.d("SMS Data To:" + to + ", Body: " + message);

            if (Utils.isSMSPluginInstalled(context)) {
                Logger.d("Sending SMS via Plugin");
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.trigger.nfctl.plugins.sms", "com.trigger.nfctl.plugins.sms.SmsSender");
                intent.putExtra("nfctl_sms_to", to);
                if (message.length() > 0) {
                    intent.putExtra("nfctl_sms_body", message);
                }
                context.startService(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (message.length() > 0) {
                    intent.setData(Uri.parse("sms:" + to));

                    intent.putExtra("address", to);
                    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, to);

                    intent.putExtra("sms_body", message);
                    intent.putExtra(Intent.EXTRA_TEXT, message);
                } else {
                    intent.setData(Uri.parse("sms:" + to));
                }

                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    Logger.e("Exception sending SMS", e);
                }
            }
            setupManualRestart(currentIndex + 1);
        }  
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetSendSMS);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionSendSMS);
    }

}
