package com.mobiroo.n.sourcenextcorporation.trigger.launcher.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.WidgetIcons;

public class WidgetSingleTask extends AppWidgetProvider {

    public static String mLauncherClass = "com.mobiroo.n.sourcenextcorporation.trigger";
    public static String mLauncherActivity = "com.mobiroo.n.sourcenextcorporation.trigger.NFCActionLauncher";
    public static final ComponentName mComponentName = new ComponentName(mLauncherClass, mLauncherActivity);
    
    
    public static RemoteViews buildRemoteViews(Context context, String iconName) {
        return buildRemoteViews(context, iconName, 0);
    }
    
    public static RemoteViews buildRemoteViews(Context context, String iconName, int id) {
        RemoteViews views = null;
        
        Intent intentPicker = new Intent(Intent.ACTION_MAIN, null);
        intentPicker.addCategory(Intent.CATEGORY_LAUNCHER);
        intentPicker.setComponent(mComponentName);
        intentPicker.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        int iconResId = 0;
        if (!iconName.isEmpty()) {
            try {
                iconResId = (WidgetIcons.getIconByName(iconName)).getResourceId();
            } catch (Exception e) { iconResId = 0; }
        } else if (id != 0) {
            try {
                iconResId = (WidgetIcons.getIconByName(SettingsHelper.getPrefString(context, Constants.PREF_WIDGET_SINGLE_ICON_KEY + id, ""))).getResourceId();
            } catch (Exception e) { iconResId = 0; }
        }
        
        if (iconResId != 0) {
            
        }
        
        
        
        return views;
    }
    
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;
        for (int j = 0; j < N; j++) {            
            appWidgetManager.updateAppWidget(appWidgetIds[j], buildRemoteViews(context, "", appWidgetIds[j]));
        }

    }
    
    @Override
    public void onEnabled(Context context) {        
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName comp = new ComponentName(context.getPackageName(), WidgetSingleTask.class.getName());
        mgr.updateAppWidget(comp, buildRemoteViews(context, SettingsHelper.getPrefString(context, Constants.PREF_WIDGET_LAST_TEXT, "")));

    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                this.onDeleted(context, new int[] { appWidgetId });
            }
        } else {
            String iconName = "";
            if (intent.hasExtra("tag_widget_update_text")) {
                iconName = intent.getStringExtra("tag_widget_update_text");
            } 
            updateWidget(context, iconName);
        }
    }
    
    public void updateWidget(Context context, String iconName) {        
        ComponentName myComponentName = new ComponentName(context, WidgetSingleTask.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myComponentName, buildRemoteViews(context, iconName));
    }
    
    
}
