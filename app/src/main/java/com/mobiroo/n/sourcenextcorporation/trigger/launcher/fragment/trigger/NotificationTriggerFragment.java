package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.EventConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.NotificationTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import org.json.JSONException;

public class NotificationTriggerFragment extends BaseFragment {

    ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_notification_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String extra = mTrigger.getExtra(1);

        ((TextInputLayout) getView().findViewById(R.id.name)).setHint(view.getContext().getString(R.string.optionsFoursquareSearchVenue));
        ((TextInputLayout) getView().findViewById(R.id.description)).setHint(view.getContext().getString(R.string.description));

        if ((extra != null) && !extra.isEmpty()) {
            try {
                EventConfiguration config = EventConfiguration.deserializeExtra(extra);
                updateView(config);
            }
            catch (Exception e) {
                Logger.e("Exception building config from extras: " + e, e);
            }
        }


        ContentResolver contentResolver = getActivity().getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = getActivity().getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
        {
            // in this situation we know that the user has not granted the app the Notification access permission
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }

    }

    private void updateView(EventConfiguration config) {
        if (!config.name.isEmpty()) {
            ((TextInputLayout) getView().findViewById(R.id.name)).getEditText().setText(config.name);
        }

        if (!config.description.isEmpty()) {
            ((TextInputLayout) getView().findViewById(R.id.description)).getEditText().setText(config.description);
        }

        ((RadioGroup) getView().findViewById(R.id.time_option)).check(
                mTrigger.getCondition().equals(DatabaseHelper.TRIGGER_CALENDAR_END)
                        ? R.id.option_ends: R.id.option_starts);

        // Setup radio buttons
        int value = config.name_match_type;
        ((RadioGroup) getView().findViewById(R.id.name_option)).check(
                value == EventConfiguration.MATCH_TYPE_CONTAINS ? R.id.option_contains
                        : R.id.option_matches);

        value = config.description_match_type;
        ((RadioGroup) getView().findViewById(R.id.description_option)).check(
                value == EventConfiguration.MATCH_TYPE_CONTAINS ? R.id.option_contains
                        : R.id.option_matches);
    }

    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.calendar_title));
    }

    @Override
    protected void updateTrigger() {
        String extra = "";
        try {
            extra = NotificationTrigger.serializeExtraFromView(getView());
        } catch (JSONException e) { Logger.e("Couldn't create data string for extras: " + e, e); }

        mTrigger.setCondition(DatabaseHelper.TRIGGER_NO_CONDITION);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_NOTIFICATION);
        mTrigger.setExtra(1, extra);
        mTrigger.setExtra(2, "");
    }

}
