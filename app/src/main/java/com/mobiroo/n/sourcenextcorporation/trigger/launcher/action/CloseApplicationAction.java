package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.AppData;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CloseApplicationAction extends BaseAction {

    private PopulateApplicationListAsyncTask mPopulateApplicationListTask;

    @Override
    public String getCommand() {
        return Constants.COMMAND_KILL_APPLICATION;
    }

    @Override
    public String getCode() {
        return Codes.KILL_APPLICATION;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option050, null, false);
        final Spinner appListSpinner = (Spinner) dialogView.findViewById(R.id.PackagesSpinner);
        appListSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                AppData data = (AppData) appListSpinner.getSelectedItem();
                if (data.getActivity() != null) {
                    String apText = data.getActivity();
                    if (apText != null) {
                        try { ((EditText) view.findViewById(R.id.taskText)).setText(apText); }
                        catch (Exception e) { }
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mPopulateApplicationListTask = new PopulateApplicationListAsyncTask(context, appListSpinner, "", context.getString(R.string.appListLoadingText), false, arguments);
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            String[] data = Utils.getNameAndClassFromApplicationString(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
            mPopulateApplicationListTask.setAppClass(data[0]);
            mPopulateApplicationListTask.setAppName(data[1]);
        }
        mPopulateApplicationListTask.execute();
        
        return dialogView;
    }

    @Override
    public String getName() {
        return "Kill Application";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        
        Spinner adapter = (Spinner) actionView.findViewById(R.id.PackagesSpinner);
        AppData data = (AppData) adapter.getSelectedItem();
        if (data.getActivity() != null) {
            return new String[] {Constants.COMMAND_KILL_APPLICATION + ":" + (String) data.getActivity(), context.getString(R.string.layoutAppKillAppText), data.getName()};
        } else {

            // Check to verify Application is in the map to retrieve package name
            if (data.getPackage() != null) {
                return new String[] {Constants.COMMAND_KILL_APPLICATION + ":" + data.getPackage(), context.getString(R.string.layoutAppKillAppText), data.getName()};
            }
        }
        return new String[] { "" };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.layoutAppKillAppText);
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
        
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();

        Logger.d("Killing Application " + args[1]);
        
        String[] data = Utils.getNameAndClassFromApplicationString(args[1]);
        
        String name = data[1];
        String clazz = data[0];

        if (SettingsHelper.isDebuggingEnabled(context)) {
            listAllProcesses(runningProcesses);
            checkPossibleMatches(name, runningProcesses);
        }
        
        Logger.d("Looking for processes belonging to " + name);
        boolean found = killByName(name, runningProcesses, manager);
        
        if (!found) {
            Logger.d("Missed by name, checking class " + clazz);
            killByName(clazz, runningProcesses, manager);
        }

        if (mBroadcastList.containsKey(clazz)) {
            String action = mBroadcastList.get(clazz);
            Logger.d("Sending broadcast to close " + clazz);
            try {
                context.sendBroadcast(new Intent(action));
            } catch (Exception e) {
                Logger.d("Exception sending close Broadcast: " + e);
            }
        }
    }
    
    private void listAllProcesses(List<RunningAppProcessInfo> runningProcesses) {
        /*for (RunningAppProcessInfo info : runningProcesses) {
            Logger.d("Process: " + info.processName);
            for (String pkg: info.pkgList) {
                Logger.d("Package: " + pkg);
            }
        }*/
    }

    private final HashMap<String, String> mBroadcastList = new HashMap<String, String>(){
        {
            put("com.waze", "Eliran_Close_Intent");
            put("de.blitzer", "de.blitzer.KILL_APP");
        }
    };
    private boolean killByName(String queryName, List<RunningAppProcessInfo> runningProcesses, ActivityManager manager) {
        boolean foundApp = false;
        for (RunningAppProcessInfo info : runningProcesses) {
            if (info.processName.equals(queryName)) {
                int processId = info.pid;
                Logger.d("Got process " + processId + " for " + queryName + " trying to close via APIs");
                foundApp = true;
                performKill(queryName, processId, manager);
            } else {
                /* Check associated packages */
                for (String pkg: info.pkgList) {
                    if (pkg.equals(queryName)) {
                        int processId = info.pid;
                        Logger.d("Got associated process " + processId + " for " + queryName);
                        foundApp = true;
                        performKill(queryName, processId, manager);
                    }
                }
            }
        }
        return foundApp;
        
    }
    
    private void performKill(String name, int processId, ActivityManager manager) {
        
        Logger.d("Got process " + processId + " for " + name + " trying to close via APIs");
        
        // Deprecated APIs - likely won't work
        android.os.Process.killProcess(processId);
        android.os.Process.sendSignal(processId, android.os.Process.SIGNAL_KILL);
        
        Logger.d("Killing background processes for " + name);
        // This stops background tasks from re-launching a process
        manager.killBackgroundProcesses(name);
        
        Logger.d("Sending SIG_KILL to " + processId);
        new KillProcessTask().execute(String.valueOf(processId));
    }
    
    private void checkPossibleMatches(String name, List<RunningAppProcessInfo> runningProcesses) {
        String[] components = name.split(".");
        String prefix = name;
        if (components.length > 0) {
            for (int i=0; i< components.length - 1; i++) {
                if (!prefix.isEmpty()) {
                    prefix += ".";
                }
                prefix += components[i];
            }
        }
        
        Logger.d("Found prefix " + prefix);
        
        /*for (RunningAppProcessInfo info : runningProcesses) {
            if (info.processName.contains(prefix)) {
                Logger.d("Matches: " + info.processName);
                for (String pkg: info.pkgList) {
                    Logger.d("Package: " + pkg);
                }
            }
        }*/
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.listCloseApp);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
       return context.getString(R.string.listCloseApp);
    }

    private static class KillProcessTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Logger.d("Trying to kill " + params[0]);
            try {
                Utils.runCommandAsRoot(new String[] {"kill -9 " + params[0]});
            } catch (IOException e) {
                Logger.e(Constants.TAG, "Exception killing app with root access", e);
            } catch (InterruptedException e) {
                Logger.e(Constants.TAG, "Exception killing app with root access", e);
            }
            return null;
        }
        
    }
    
}
