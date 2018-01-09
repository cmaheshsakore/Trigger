package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.AppData;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class PopulateApplicationListAsyncTask extends AsyncTask<Void, Void, ArrayAdapter<AppData>> {

    private Context mContext;
    private ProgressDialog pDialog;

    private String mDialogTitle;
    private String mDialogBody;
    private boolean mCanCancelDialog;
    private Spinner mSpinner;
    private CommandArguments mArguments;

    private static Hashtable<String, String> mAppMap;
    private static Hashtable<String, String> mAppTaskMap;

    private String mName = "";
    private String mClass = "";
    
    public void setAppName(String name) {
        mName = name;
    }
    
    public void setAppClass(String clazz) {
        mClass = clazz;
    }
    
    public Hashtable<String, String> getAppMap() {
        return (mAppMap != null) ? mAppMap : new Hashtable<String, String> ();
    }
    
    public Hashtable<String, String> getAppTaskMap() {
        return (mAppTaskMap != null) ? mAppTaskMap : new Hashtable<String, String> ();
    }
    
    public PopulateApplicationListAsyncTask(Context context, Spinner spinner, String dialogTitle, String dialogText, boolean canCancelDialog, CommandArguments arguments) {
        mContext = context;
        mSpinner = spinner;
        mDialogTitle = dialogTitle;
        mDialogBody = dialogText;
        mCanCancelDialog = canCancelDialog;
        mArguments = arguments;
    }

    protected void onPreExecute() {
        pDialog = ProgressDialog.show(mContext, mDialogTitle, mDialogBody, true, mCanCancelDialog);
        pDialog.show();
    }

    @Override
    protected ArrayAdapter<AppData> doInBackground(Void... args) {
        return populateAppList(mContext);
    }

    protected void onPostExecute(ArrayAdapter<AppData> adapter) {
        try {
            mSpinner.setAdapter(adapter);
            
            if (mArguments.hasArgument(CommandArguments.OPTION_SELECTED_APPLICATION)) {
                try {
                    for (int i=0; i< adapter.getCount(); i++) {
                        AppData d = adapter.getItem(i);
                        if (mArguments.getValue(CommandArguments.OPTION_SELECTED_APPLICATION).equals(d.getName())) {
                            mSpinner.setSelection(i);
                        } 
                    }
                } catch (Exception e) { Logger.e(Constants.TAG, "Failed to set spinner position for " + mArguments.getValue(CommandArguments.OPTION_SELECTED_APPLICATION), e); }

            } else if (!mName.isEmpty()) {
                for (int i=0; i< adapter.getCount(); i++) {
                    AppData d = adapter.getItem(i);
                    if (mName.equals(d.getActivity())) {
                        mSpinner.setSelection(i);
                    } else if (mClass.equals(d.getPackage())) {
                        mSpinner.setSelection(i);
                    }
                }
            }
        } catch (Exception e) { Logger.e("Exception setting adapter"); }

        try {
            pDialog.dismiss();
        } catch (IllegalArgumentException e) { Logger.e("Exception dismissing dialog"); }


    }

    
    
    private static ArrayAdapter<AppData> populateAppList(Context context) {
        final Context mContext = context;
        
        // Create new adapter
        ArrayAdapter<AppData> adapter = new ArrayAdapter<AppData>(mContext, R.layout.spinner_app_list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                
                if (null == convertView) {
                    convertView = View.inflate(mContext, R.layout.spinner_app_list, null);
                }
                
                AppData data = (AppData) getItem(position);
                
                
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(data.getName());
                return convertView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Get all main activities
        PackageManager pm = mContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        
        List<ResolveInfo> appList = null;
        try { 
            appList = pm.queryIntentActivities(mainIntent, 0); 
        } catch (Exception e) {
            Logger.e("Package list is too large to load", e);
        }
        
        if (appList == null) {
            // Query with default match only
            try {
                appList = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
            } catch (Exception e) {
                Logger.e("default only package list exception", e);
            }
        }
        
        if (appList!= null) {
            Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

            for (int i = 0; i < appList.size(); i++) {
                // Add to adapter for display
                //Logger.d("Adding " + appList.get(i).loadLabel(pm));
                //Logger.d("Alt = " + appList.get(i).activityInfo.name + ", " + appList.get(i).activityInfo.packageName);

                // Add a custom mapped task as well if it differs from the
                // package name
                if (appList.get(i).activityInfo.name != null) {
                    adapter.add(
                            new AppData(appList.get(i).loadLabel(pm).toString(), 
                                    appList.get(i).activityInfo.packageName, 
                                    appList.get(i).activityInfo.name)
                            );
                } else if (appList.get(i).activityInfo.targetActivity != null) {
                    adapter.add(
                            new AppData(appList.get(i).loadLabel(pm).toString(), 
                                    appList.get(i).activityInfo.packageName, 
                                    appList.get(i).activityInfo.targetActivity)
                            );                
                } else if (appList.get(i).activityInfo.packageName.indexOf("air.adobe.flex") >= 0) {
                    adapter.add(
                            new AppData(appList.get(i).loadLabel(pm).toString(), 
                                    appList.get(i).activityInfo.packageName, 
                                    appList.get(i).activityInfo.packageName + ".AppEntry")
                            );
                } else if (appList.get(i).activityInfo.packageName.equals("com.google.android.apps.maps")) {
                    adapter.add(
                            new AppData(appList.get(i).loadLabel(pm).toString(), 
                                    appList.get(i).activityInfo.packageName, 
                                    appList.get(i).activityInfo.name)
                            );
                } else {
                    adapter.add(
                            new AppData(appList.get(i).loadLabel(pm).toString(), 
                                    appList.get(i).activityInfo.packageName, 
                                    null)
                            );
                }

            }
        }
        return adapter;
    }


}
