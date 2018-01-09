package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.ChargingTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.MusicUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListAppItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.PowerService;

import java.util.ArrayList;
import java.util.List;

public class GeneralSettingsFragment extends SettingsFragment {

    private ListAppItem[]       mAlarmHandlers;
    private ListAppItem[]       mMediaHandlers;
    private String              mNone;
    private TextView            mAlarmName;
    private TextView            mMediaName;
    private TextView            mPowerDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_general,  null);
    }
    
    @Override
    protected void setupClickHandlers() {
        setCheckChangedListener(R.id.prefShowDescriptionCheckbox, this);
        setCheckChangedListener(R.id.prefPowerForegroundService, this);
    }

    @Override
    protected void loadSettings() {
        setChecked((mView.findViewById(R.id.prefShowDescriptionCheckbox)).getId(), getBoolean(getActivity(), Constants.PREF_SHOW_TASK_DESCRIPTION, true));
        setChecked((mView.findViewById(R.id.prefPowerForegroundService)).getId(), getBoolean(getActivity(), Constants.PREF_USE_FOREGROUND_POWER_SERVICE, SettingsHelper.POWER_SERVICE_DEFAULT));

        mNone =  mContext.getString(R.string.layoutPreferencesNotificationsSubNone);

        mAlarmHandlers = getAlarmHandlers();
        mAlarmName = (TextView) mView.findViewById(R.id.alarmPackageSubText);
        String currentAlarm = SettingsHelper.getPrefString(mContext, Constants.PREF_DESIRED_ALARM_PACKAGE_NAME, "");
        if (!currentAlarm.isEmpty()) {
            mAlarmName.setText(currentAlarm);
        }
        
        mMediaHandlers = getMediaHandlers();
        mMediaName = (TextView) mView.findViewById(R.id.mediaPackageSubText);
        String currentMedia = SettingsHelper.getPrefString(mContext, Constants.PREF_DESIRED_MEDIA_PACKAGE_NAME, "");
        if (!currentMedia.isEmpty()) {
            mMediaName.setText(currentMedia);
        }

        mPowerDescription = (TextView) mView.findViewById(R.id.prefPowerText);
        
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.showWelcomeButton:
                showWelcome();
                break;
            case R.id.alarmPackageButton:
                showAlarmHandlersDialog();
                break;
            case R.id.mediaPackageButton:
                showMediaHandlersDialog();
                break;
        }
        
        updatePreferences();
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        switch(v.getId()) {
            case R.id.prefShowDescriptionCheckbox:
                setChecked(v.getId(), isChecked);
                getEditor(getActivity()).putBoolean(Constants.PREF_SHOW_TASK_DESCRIPTION, isChecked).commit();
                break;
            case R.id.prefPowerForegroundService:
                Logger.d("Saving pref as " + isChecked);
                setChecked(v.getId(), isChecked);
                getEditor(getActivity()).putBoolean(Constants.PREF_USE_FOREGROUND_POWER_SERVICE, isChecked).commit();
                mPowerDescription.setText(isChecked ? R.string.power_service_on : R.string.power_service_off);

                if (BatteryTrigger.isEnabled(getActivity()) || ChargingTrigger.isEnabled(getActivity())) {
                    Intent service = new Intent(getActivity(), PowerService.class);
                    getActivity().stopService(service);
                    getActivity().startService(service);
                }

                break;
        }
    }
    
    private void showMediaHandlersDialog() {
        final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        dialog.setTitle(getString(R.string.select_media_app));
        dialog.setListOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                ListAppItem item = (ListAppItem) adapter.getItemAtPosition(position);
                if (item.getPackageName().equals(mNone)) {
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_MEDIA_PACKAGE, "");
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_MEDIA_PACKAGE_NAME, "");
                    mMediaName.setText(getString(R.string.layoutPreferencesNotificationsSubNone));
                } else {
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_MEDIA_PACKAGE, item.getPackage());
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_MEDIA_PACKAGE_NAME, item.getLabel());
                    mMediaName.setText(item.getLabel());
                }

                dialog.dismiss();
            }
        });

        dialog.setListAdapter(new ListItemsAdapter(getActivity(), mMediaHandlers));
        dialog.show(getFragmentManager(), "media-handlers");
    }
    
    private void showAlarmHandlersDialog() {
        final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        dialog.setTitle(getString(R.string.select_alarm_app));
        dialog.setListOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                ListAppItem item = (ListAppItem) adapter.getItemAtPosition(position);
                if (item.getPackageName().equals(mNone)) {
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_ALARM_PACKAGE, "");
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_ALARM_PACKAGE_NAME, "");
                    mAlarmName.setText(getString(R.string.layoutPreferencesNotificationsSubNone));
                } else {
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_ALARM_PACKAGE, item.getPackage());
                    SettingsHelper.setPrefString(mContext, Constants.PREF_DESIRED_ALARM_PACKAGE_NAME, item.getLabel());
                    mAlarmName.setText(item.getLabel());
                }
                dialog.dismiss();
            }
        });
        
        dialog.setListAdapter(new ListItemsAdapter(getActivity(), mAlarmHandlers));
        dialog.show(getFragmentManager(), "alarm-handlers");
        
    }

    private void showWelcome() {
        
        SettingsHelper.setPrefBool(getActivity(), Constants.PREF_HIDE_WELCOME, false);
        SettingsHelper.setPrefBool(getActivity(), Constants.PREF_DISPLAYED_SSO_DIALOG, false);
        SettingsHelper.setPrefBool(getActivity(), Constants.PREF_SHOW_BUILDER_HELP, true);
        SettingsHelper.setPrefBool(getActivity(), Constants.PREF_SHOW_TASK_HELP, true);

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private ListAppItem[] getMediaHandlers() {
        List<ResolveInfo> list = MusicUtils.getMediaReceivers(mContext);

        List<ListAppItem> names = new ArrayList<ListAppItem>();
        names.add(new ListAppItem(mContext, null));
        for (ResolveInfo info: list) {
            Logger.i("Data: " + info.activityInfo.packageName + ", " + info.activityInfo.name);
            names.add(new ListAppItem(mContext, info.activityInfo));
        }
        return names.toArray(new ListAppItem[list.size()]);
    }

    private ListAppItem[] getAlarmHandlers() {
        Intent i = new Intent();
        i.setAction(AlarmClock.ACTION_SET_ALARM);
        final PackageManager packageManager = mContext.getPackageManager();
        final Intent intent = new Intent(i);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        List<ListAppItem> names = new ArrayList<ListAppItem>();
        names.add(new ListAppItem(mContext, null));
        for (ResolveInfo info: list) {
            names.add(new ListAppItem(mContext, info.activityInfo));
        }
        return names.toArray(new ListAppItem[list.size()]);
    }
}
