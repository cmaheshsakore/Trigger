package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.ContactQuery;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public class EmailAction extends BaseAction {

    public static final int REQUEST_PICK_CONTACT = 9002;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_SEND_EMAIL;
    }

    @Override
    public String getCode() {
        return Codes.EMAIL;
    }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option055, null, false);
        if (Build.VERSION.SDK_INT >= 11) {
            /* Contacts contract API is 10+.  10 getting phased out */
            ((ImageButton) dialogView.findViewById(R.id.contact_picker)).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK,  Email.CONTENT_URI);
                    ((Activity) context).startActivityForResult(intent, REQUEST_PICK_CONTACT);  
                }  
            });
        } else {
            ((ImageButton) dialogView.findViewById(R.id.contact_picker)).setVisibility(View.GONE);
        }
        
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((TextView) dialogView.findViewById(R.id.emailAddress)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            ((TextView) dialogView.findViewById(R.id.emailMessage)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
        }
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            ((TextView) dialogView.findViewById(R.id.emailSubject)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE));
        }
        return dialogView;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public void setContact(Context context, final View actionView, Uri result) {
        
        Logger.d("uri is " + result);
        List<String> numbers = ContactQuery.getContactDataFor(
                context, 
                result, 
                new String[] {Email.ADDRESS}, 
                null, 
                null, 
                Email.ADDRESS);
        
        if (numbers.size() == 1) {
            ((EditText) actionView.findViewById(R.id.emailAddress)).setText(numbers.get(0));
        } else if (numbers.size() > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final ArrayAdapter<String> list = new ArrayAdapter<String>(context, R.layout.list_item_single, numbers.toArray(new String[numbers.size()]));
            builder.setAdapter(
                    list, 
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((EditText) actionView.findViewById(R.id.emailAddress)).setText((String)list.getItem(which));
                        }
                    });
            builder.create().show();
        }
        
    }
    @Override
    public String getName() {
        return "Send Email";
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        TextView to = (TextView) actionView.findViewById(R.id.emailAddress);
        TextView body = (TextView) actionView.findViewById(R.id.emailMessage);
        TextView subject = (TextView) actionView.findViewById(R.id.emailSubject);
        
        String address = (to == null) ? "" : Utils.encodeData(to.getText().toString());
        String message = (body == null) ? "": Utils.encodeData(body.getText().toString());
        String sub = (subject == null) ? "" : Utils.encodeData(subject.getText().toString());
        
        return new String[] { Constants.COMMAND_SEND_EMAIL + ":" + address + ":" + message + ":" + sub, context.getString(R.string.listDisplayEmail), address};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listDisplayEmail);
        try { 
            display += " " + args[0];
        } catch (Exception e) { 
            /* Ignore any exception here */
        }
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseEncodedString(args, 2, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.tryParseEncodedString(args, 3, ""))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        String to = Utils.tryParseEncodedString(args, 1, "");
        String body = Utils.tryParseEncodedString(args, 2, "");
        String subject = Utils.tryParseEncodedString(args, 3, "");
        operationSendEmail(context, to, subject, body);
        setupManualRestart(currentIndex + 1);
    }

    private String addArg(String args, String name, String value) {
        if (!args.isEmpty()) {
            args += "&";
        } 
        args += String.format("%s=%s", name, Uri.encode(value));
        
        return args;
    }
    
    private void operationSendEmail(Context context, String to, String subject, String body) {
        String uri = "mailto:" + Uri.encode(to);
        String args = "";
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        
        if (!subject.isEmpty()) {
            subject = Utils.removePlaceHolders(subject);
            args = addArg(args, "subject", subject);
            intent.putExtra("subject", subject);
        }
        if (!body.isEmpty()) {
            body = Utils.removePlaceHolders(body);
            args = addArg(args, "body", body);
            intent.putExtra("body", body);
        }
        
        uri += "?" + args;

        Logger.d("Email URI is " + Uri.parse(uri));
        
        intent.setData(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        /* Check if there is a default handler here */
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            context.startActivity(intent);
        } else {
            Intent i = Intent.createChooser(intent, context.getString(R.string.sendEmailChooserTitle));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widget_send_email);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.action_send_email);
    }
    
}
