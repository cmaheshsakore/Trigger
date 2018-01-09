package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.AppData;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class OpenActivityAction extends BaseAction {

    private PopulateApplicationListAsyncTask mPopulateApplicationListTask;
    
    private String  mActivity = "";
    private int     mSelection = -1;
    @Override
    public String getCommand() {
        return Constants.COMMAND_LAUNCH_TASK;
    }

    @Override
    public String getCode() {
        return Codes.LAUNCH_CUSTOM_TASK;
    }

    private class ActivityAdapter extends ArrayAdapter<ActivityInfo> {

        @SuppressWarnings("unused")
        private ActivityInfo[] mActivities;
        private Context mContext;
        
        public ActivityAdapter(Context context, int textViewResourceId, ActivityInfo[] objects) {
            super(context, textViewResourceId, objects);
            mActivities = objects;
            mContext = context;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.multi_line_spinner_dropdown_item, null);
            }
            try {
                ActivityInfo info = getItem(position);
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(info.name);
            } catch (Exception e) {
                ((TextView) convertView.findViewById(android.R.id.text1)).setText("");
            }
            
            return convertView;
        }
        
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.multi_line_spinner_dropdown_item, null);
            }
            
            ActivityInfo info = getItem(position);
            String[] name = info.name.split("\\.");
            String nameTop = "";
            String nameBottom = "";
            for (int i=0; i < name.length; i++) {
                if (i < 4) {
                    switch (i) {
                        case 0:
                            nameTop = name[i];
                            break;
                        default:
                            nameTop += "." + name[i];
                    }
                } else {
                    switch (i) {
                        case 4:
                            nameBottom = name[i];
                            break;
                        default:
                            nameBottom += "." + name[i];
                    }
                }
            }
            
            if (!nameBottom.isEmpty()) {
                nameTop += "\n" + nameBottom;
          
            }
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(nameTop);
            
            return convertView;
        }
        
    }
    
    @Override
    public View getView(final Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option051, null, false);
        final Context mContext = context;
        
        final Spinner activitySpinner = (Spinner) dialogView.findViewById(R.id.activitiesSpinner);
        Spinner appListSpinner = (Spinner) dialogView.findViewById(R.id.PackagesSpinner);
        
        
        appListSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Spinner spinnerApps = (Spinner) parent.findViewById(R.id.PackagesSpinner);
                AppData data = (AppData) spinnerApps.getSelectedItem();

                String packageName = data.getPackage();
                PackageManager pm = mContext.getPackageManager();

                ArrayList<ActivityInfo> activities = new ArrayList<ActivityInfo>();
                try {
                    PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                    for (ActivityInfo ainfo : info.activities) {

                        if (ainfo.targetActivity != null) {
                            activities.add(ainfo);
                            //activityList.add(ainfo.name);
                        } else {
                            if (ainfo.exported) {
                                activities.add(ainfo);
                                //activityList.add(ainfo.name);
                            }
                        }
                        
                        if (mActivity.equals(ainfo.name)) {
                            mSelection = activities.size() - 1;
                        }

                    }
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception getting activities from package " + packageName, e);
                }
                
                //activityList.setDropDownViewResource(R.layout.multi_line_spinner_dropdown_item);
                activitySpinner.setAdapter(new ActivityAdapter(context, R.layout.multi_line_spinner_dropdown_item, activities.toArray(new ActivityInfo[activities.size()])));
                if (mSelection > -1) {
                    activitySpinner.setSelection(mSelection);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mPopulateApplicationListTask = new PopulateApplicationListAsyncTask(context, appListSpinner, "", context.getString(R.string.appListLoadingText), false, arguments);
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            String[] data = Utils.getNameAndClassFromApplicationString(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
            mActivity = data[1];
            mPopulateApplicationListTask.setAppClass(data[0]);
            mPopulateApplicationListTask.setAppName(data[1]);
        } else {
            mActivity = "";
            mSelection = -1;
        }
        
        mPopulateApplicationListTask.execute();
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Launch Activity";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        
        Spinner spinnerApps = (Spinner) actionView.findViewById(R.id.PackagesSpinner);
        Spinner activitiesAdapter = (Spinner) actionView.findViewById(R.id.activitiesSpinner);
        
        AppData data = (AppData) spinnerApps.getSelectedItem();
        String packageName = data.getPackage();
        ActivityInfo info = (ActivityInfo) activitiesAdapter.getSelectedItem();
        
        if ((packageName != null) && (info != null)) {
            return new String[] { Constants.COMMAND_LAUNCH_TASK + ":" + packageName + "/" + info.name, context.getString(R.string.listLaunchTextCustom), data.getName() };
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
        return context.getString(R.string.listLaunchTextCustom);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, ""))
                );
    }
    
    @SuppressLint("InlinedApi") 
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        mResumeIndex = currentIndex + 1;
        
        Logger.d("Launching Custom Task");
        
        String task = args[1];
     
        Intent i = new Intent(Intent.ACTION_VIEW);
        String className = "";

        // Check if we have split on /
        
        /* Three possible incoming data sets
         * class / acvitity (full / full)
         * class / .activity
         * activity (try to extract class name from activity)
         */
        
        String[] classData = task.split("/");
        if (classData.length > 1) {
            /* This is either class / activity or class / .activity */
            
            className = classData[0];
            
            task = classData[1];
            if (task.startsWith(".")) {
                task = className + task;
            }
            
        } else {
            className = getClassNameFromTask(task, 1);

            String[] override = checkForOverride(className, task);
            className = override[0];
            task = override[1];
        }
        
        // Assuming that the package name is everything but the Last part of the
        // activity - with the last part being the specific class within the
        // package to start

        // Log app launch here
        Logger.d("Launching activity " + className);
        Usage.storeTuple(context, className, "start_app", 1);

        Logger.d(Constants.TAG, "Class = " + className + ", Activity = " + task);
        i.setClassName(className, task);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT > 10) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        
        try {
            setupManualRestart(currentIndex + 1);
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.actionCustomTaskError), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        
    }
    
    private String[] checkForOverride(String className, String task) {
        String[] out = new String[2];
        out[0] = className;
        out[1] = task;
        
        // Set an explicit check here for maps activities
        if (className.contains("com.google.android.maps")) {
            out[0] = "com.google.android.apps.maps";
        } else if (className.contains("com.google.android.music")) {
            out[0] = "com.google.android.music";
        } else if (className.contains("com.android.music")) {
            out[0] = "com.google.android.music";
        } else if (className.contains("com.android.deskclock")) {
            out[0] = "com.google.android.deskclock";
        } else if (className.contains("com.google.android.deskclock")) {
            out[0] = "com.google.android.deskclock";
            out[1] = "com.android.deskclock.DeskClock";
        } else if (task.equals("com.google.android.gm.ui.MailActivityGmail")) {
            out[0] = "com.google.android.gm";
            out[1] = "com.google.android.gm.ConversationListActivityGmail";       
        } else if (task.equals("com.google.android.talk.BuddyListCombo")) {
            out[1] = "com.google.android.talk.SigningInActivity";
        } else if (task.startsWith("com.tivophone.android.setup")) {
            out[0] = "com.tivophone.android";
            out[1] = "com.tivophone.android.view.home.SplashScreenActivity";
        } else if (task.startsWith("com.android.camera.CameraActivity")) {
            out[0] = "com.google.android.gallery3d";
            out[1] = "com.android.camera.CameraLauncher";
        }
        
        return out;
    }
    private String getClassNameFromTask(String task, int offset) {
        String[] taskData = task.split("\\.");
        String className = "";
        
        for (int n = 0; n < taskData.length - offset; n++) {
            if (className != "")
                className += ".";
            className += taskData[n];
        }
        
        return className;
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetLaunchActivity);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
       return context.getString(R.string.actionLaunchActivity);
    }
    

}
