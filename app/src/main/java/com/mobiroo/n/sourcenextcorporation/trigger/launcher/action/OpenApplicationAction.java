package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.AppData;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

public class OpenApplicationAction extends BaseAction {
    
    @Override
    public String getCommand() {
        return Constants.COMMAND_LAUNCH_APPLICATION;
    }

    @Override
    public String getCode() {
        return Codes.LAUNCH_APPLICATION;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option013, null, false);
        final Spinner appListSpinner = (Spinner) dialogView.findViewById(R.id.PackagesSpinner);
        
        new PopulateApplicationListAsyncTask(context, appListSpinner, "", context.getString(R.string.appListLoadingText), false, arguments).execute();
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Launch Applicaiton";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        
        Spinner adapter = (Spinner) actionView.findViewById(R.id.PackagesSpinner);
        AppData data = (AppData) adapter.getSelectedItem();
        
        if (data != null) {
            
            if (data.getPackage() != null) {
                // Prefer using package name and intent lookup
                if (context.getPackageManager().getLaunchIntentForPackage(data.getPackage()) != null) {
                   // We can successfully query a launch intent based on package name, store.
                    Logger.d("Adding Application by package name");
                    Usage.storeTuple(context, Codes.LAUNCH_APPLICATION, Codes.COMMAND_ADD, 0);
                    return new String[] { Constants.COMMAND_LAUNCH_APPLICATION + ":" + data.getPackage(), context.getString(R.string.listLaunchText), data.getName() };
                }
                Logger.d("Could not find launch intent for " + data.getPackage());
            }
            
            if (data.getActivity() != null) {
                Logger.d("Adding Activity");
                Usage.storeTuple(context, Codes.LAUNCH_CUSTOM_TASK, Codes.COMMAND_ADD, 0);
                return new String[] { Constants.COMMAND_LAUNCH_TASK + ":" + data.getActivity(), context.getString(R.string.listLaunchTextCustom), data.getName() };
            }
            
            Logger.d("Could not add package " + data.getName());
        }

        return new String[] { "" };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listLaunchText);
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(args[0], 0);
            if (info != null)
                display += " " + packageManager.getApplicationLabel(info);
        } catch (Exception ignored) {}
        
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        return null;
    }
    
    @SuppressLint("InlinedApi") 
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;
        
        String packageName = args[1];
        Logger.d("Launching application " + packageName);
        Usage.storeTuple(context, packageName, "start_app", 1);

        PackageManager pm = context.getPackageManager();
        try {
            Intent i = pm.getLaunchIntentForPackage(packageName);
            if (i != null) {
                // Package is installed, launch
                Logger.d(Constants.TAG, "Launching " + packageName);
                try {
                    context.startActivity(i);
                    setupManualRestart(currentIndex + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // The package is not installed - go to Market with package name
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                setupManualRestart(currentIndex + 1);
            }
        } catch (Exception ignored) { /* Do nothing - proceed with processing below */ }
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetLaunchPackage);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
       return context.getString(R.string.actionLaunchPackage);
    }

}
