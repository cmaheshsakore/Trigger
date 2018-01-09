package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.content.Context;
import android.content.Intent;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class MyDashClockExtension extends DashClockExtension {

    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        if (!isReconnect) {
            Logger.i("Adding URI");
            addWatchContentUris(new String[] {"nfctl://update/dashclock" });
        }
        setUpdateWhenScreenOn(true);
    }
    
    @Override
    protected void onUpdateData(int reason) {
        Context context = MyDashClockExtension.this;
        String last_action = SettingsHelper.getPrefString(context, Constants.PREF_WIDGET_LAST_TEXT, "");
        String last_time = "Tap to run a task";
        
        if (!last_action.isEmpty()) {
            // Get last used date/time
            last_time = Utils.formatLastUsed(SettingsHelper.getPrefString(context, Constants.PREF_WIDGET_LAST_TIME, ""), getString(R.string.used_never));
        }
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.icon)
                .status(last_action)
                .expandedBody(last_time)
                .clickIntent(new Intent().setClassName("com.mobiroo.n.sourcenextcorporation.trigger", "SavedTagPickerActivity")));
    }

}
