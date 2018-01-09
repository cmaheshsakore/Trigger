package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public class UnifiedRemoteAction extends BaseAction {

    public static final String ACTION_URI_SEND = "com.unifiedremote.ACTION_URI_SEND";
    public static final String ACTION_URI_CONFIGURE = "com.unifiedremote.ACTION_URI_CONFIGURE";
    public static final String EXTRA_URI = "com.unifiedremote.EXTRA_URI";

    public static final int REQUEST_CODE_UNIFIED_REMOTE = 782;

    private String mUri;

    @Override
    public String getCommand() {
        return Constants.COMMAND_UNIFIED_REMOTE;
    }

    @Override
    public String getCode() {
        return Codes.UNIFIED_REMOTE;
    }

    @Override
    public View getView(final Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option087, null, false);
        dialogView.findViewById(R.id.build_uri).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUnifiedRemote(context);
            }
        });

        if (hasArgument(arguments,CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            mUri = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Unified Remote";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {

        if (!TextUtils.isEmpty(mUri)) {
            mUri = Utils.encodeURL(mUri);
            mUri = Utils.encodeData(mUri);
            String message = Constants.COMMAND_UNIFIED_REMOTE + ":" + mUri;
            String prefix = context.getString(R.string.unified_remote);
            String suffix = "";
            return new String[]{message, prefix, suffix};
        }

        return new String[] { };
    }

    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.unified_remote);
    }
    
    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, action.substring(2))});
    }
    
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        try {
            String uri = Utils.decodeURL(args[1]);
            uri = Utils.decodeData(uri);
            Logger.d("UnifiedRemote: Sending uri");
            context.sendBroadcast(new Intent(ACTION_URI_SEND).putExtra(EXTRA_URI, uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.unified_remote));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.unified_remote));
    }

    private void startUnifiedRemote(Context context) {
        Intent intent = new Intent(ACTION_URI_CONFIGURE);

        // verify that the intent resolves (i.e Unified Remote is installed)
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            ((Activity) context).startActivityForResult(intent, REQUEST_CODE_UNIFIED_REMOTE);
        } else {
            Toast.makeText(context, "Unified Remote not installed...", Toast.LENGTH_LONG).show();
        }
    }

    public void setUri(String uri) {
        mUri = uri;
    }
}
