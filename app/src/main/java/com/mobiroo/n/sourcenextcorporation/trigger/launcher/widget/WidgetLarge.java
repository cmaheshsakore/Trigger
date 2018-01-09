package com.mobiroo.n.sourcenextcorporation.trigger.launcher.widget;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.app.PendingIntent;

public class WidgetLarge extends AppWidgetProvider {
  
    public static String mPickerLauncherClass = "com.mobiroo.n.sourcenextcorporation.trigger";
    public static String mPickerLauncherActivity = "SavedTagPickerActivity";

    private static String mMainLauncherClass = "com.mobiroo.n.sourcenextcorporation.trigger";
    private static String mMainLauncherActivity = "MainActivity";
    
    public static final ComponentName mComponentNamePicker= new ComponentName(mPickerLauncherClass, mPickerLauncherActivity);
    public static final ComponentName mComponentNameLauncher = new ComponentName(mMainLauncherClass, mMainLauncherActivity);
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;
        for (int j = 0; j < N; j++) {            
            appWidgetManager.updateAppWidget(appWidgetIds[j], buildRemoteViews(context, null));
        }

    }

    public static RemoteViews buildRemoteViews(Context context, String newText) {
        Intent intentPicker = new Intent(Intent.ACTION_MAIN, null);
        intentPicker.addCategory(Intent.CATEGORY_LAUNCHER);
        intentPicker.setComponent(mComponentNamePicker);
        intentPicker.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setComponent(mComponentNameLauncher);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        PendingIntent pickerPendingIntent = PendingIntent.getActivity(context, 0, intentPicker, 0);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
        
        int preferredLayout = SettingsHelper.getPrefInt(context, Constants.PREF_WIDGET_BACKGROUND, 0);
        
        RemoteViews views = null;
        
        if (preferredLayout == 1) {
            views = new RemoteViews(context.getPackageName(), R.layout.widget_primary_light);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.widget_primary);
        }
        views.setOnClickPendingIntent(R.id.widgetButton, mainPendingIntent);
        views.setOnClickPendingIntent(R.id.widgetText, pickerPendingIntent);
        
        if ((newText != null) && (!newText.isEmpty())) {
            views.setTextViewText(R.id.widgetText, newText);
            SettingsHelper.setPrefString(context, Constants.PREF_WIDGET_LAST_TEXT, newText);
        }
        return views;
    }
    @Override
    public void onEnabled(Context context) {        
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName comp = new ComponentName(context.getPackageName(), WidgetLarge.class.getName());
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
            String widgetText = context.getString(R.string.app_name);
            if (intent.hasExtra("tag_widget_update_text")) {
                widgetText = intent.getStringExtra("tag_widget_update_text");
            } else {
                widgetText = SettingsHelper.getPrefString(context, Constants.PREF_WIDGET_LAST_TEXT, "");
            }
            updateWidget(context, widgetText);
        }

    }

    public void updateWidget(Context context, String widgetText) {        
        ComponentName myComponentName = new ComponentName(context, WidgetLarge.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        
        manager.updateAppWidget(myComponentName, buildRemoteViews(context, widgetText));
    }

}