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
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.ContactQuery;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public class CallAction extends BaseAction {
    
    public static final int REQUEST_PICK_CONTACT = 9003;
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_CALL;
    }

    @Override
    public String getCode() {
        return Codes.PHONE_CALL;
    }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option048, null, false);
        TextView optionalText = (TextView) dialogView.findViewById(R.id.smsText);
        if (Utils.isCallPluginInstalled(context)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_KOREA)
                || BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_NXP)) {
            optionalText.setText(context.getString(R.string.optionsPhoneCall)); 
        }  else  {
            final Context thisContext = context;
            optionalText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("market://details?id=com.trigger.nfctl.plugins.call");
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
            ((EditText) dialogView.findViewById(R.id.phoneNumber)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
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
            ((EditText) actionView.findViewById(R.id.phoneNumber)).setText(numbers.get(0));
        } else if (numbers.size() > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final ArrayAdapter<String> list = new ArrayAdapter<String>(context, R.layout.list_item_single, numbers.toArray(new String[numbers.size()]));
            builder.setAdapter(
                    list, 
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((EditText) actionView.findViewById(R.id.phoneNumber)).setText((String)list.getItem(which));
                        }
                    });
            builder.create().show();
        }
        
    }
    
    @Override
    public String getName() {
        return "Place a Call";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        String number = "";
        EditText numberBox = (EditText) actionView.findViewById(R.id.phoneNumber);
        number = numberBox.getText().toString();
        if (number.length() > 0) {
            String message = Constants.COMMAND_CALL + ":" + number;
            return new String[] { message, context.getString(R.string.listPhoneCall), ""};
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
        return context.getString(R.string.listPhoneCall);
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
        String to = Utils.tryParseEncodedString(args, 1, "");
        
        if (!to.isEmpty()) {
            to = to.replace("#", "%23");
            Logger.d("Starting URI tel:" + to);

            if (Utils.isCallPluginInstalled(context)) {
                /* Strip extra characters */
                to = to.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.trigger.nfctl.plugins.call", "com.trigger.nfctl.plugins.call.CallPlugin");
                intent.putExtra("nfctl_call_phone", to);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:" + to));
                context.startActivity(intent);
            }
            
            setupManualRestart(currentIndex + 1);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetPhoneCall);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionPhoneCall);
    }
    
}
