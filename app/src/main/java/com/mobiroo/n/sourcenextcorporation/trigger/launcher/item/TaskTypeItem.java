package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.FlavorInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.BaseFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.BatteryTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.ChargerTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.GeofenceTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.HeadsetTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.ManualTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.WifiTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TrialDialogActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TriggerWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.AgentTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.BluetoothTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.CalendarTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.NfcTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.NotificationTriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.TimeTriggerFragment;

import java.util.ArrayList;

public class TaskTypeItem implements ListItem {

    public static final TaskTypeItem TaskTypeNfc = new TaskTypeItem(R.string.nfc_task, R.string.nfc_task_description, R.drawable.ic_action_tag, R.drawable.ic_action_tag_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_NFC, TaskTypeItem.LAYOUT_NFC);
    public static final TaskTypeItem TaskTypeBluetooth = new TaskTypeItem(R.string.bluetooth_task, R.string.bluetooth_task_description, R.drawable.ic_action_bluetooth, R.drawable.ic_action_bluetooth_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_BLUETOOTH, TaskTypeItem.LAYOUT_BLUETOOTH);
    public static final TaskTypeItem TaskTypeWifi = new TaskTypeItem(R.string.wifi_task, R.string.wifi_task_description, R.drawable.ic_action_wifi, R.drawable.ic_action_wifi_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_WIFI, TaskTypeItem.LAYOUT_WIFI);
    public static final TaskTypeItem TaskTypeGeofence = new TaskTypeItem(R.string.geofence_task, R.string.geofence_beta_description, R.drawable.ic_action_globe, R.drawable.ic_action_globe_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_GEOFENCE, TaskTypeItem.LAYOUT_GEOFENCE);
    public static final TaskTypeItem TaskTypeBattery = new TaskTypeItem(R.string.battery_task, R.string.battery_task_description, R.drawable.ic_action_battery, R.drawable.ic_action_battery_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_BATTERY, TaskTypeItem.LAYOUT_BATTERY);
    public static final TaskTypeItem TaskTypeTime = new TaskTypeItem(R.string.time_task, R.string.time_task_description, R.drawable.ic_action_clock, R.drawable.ic_action_clock_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_TIME, TaskTypeItem.LAYOUT_TIME);
    public static final TaskTypeItem TaskTypeCharger = new TaskTypeItem(R.string.charger_title, R.string.charger_description, R.drawable.ic_action_plug, R.drawable.ic_action_plug_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_CHARGER, TaskTypeItem.LAYOUT_CHARGER);
    public static final TaskTypeItem TaskTypeHeadset = new TaskTypeItem(R.string.headset_title, R.string.headset_description, R.drawable.ic_action_headphones, R.drawable.ic_action_headphones_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_HEADSET, TaskTypeItem.LAYOUT_HEADSET);
    public static final TaskTypeItem TaskTypeCalendar = new TaskTypeItem(R.string.calendar_title, R.string.calendar_description, R.drawable.ic_action_calendar_month, R.drawable.ic_action_calendar_month_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_CALENDAR, TaskTypeItem.LAYOUT_CALENDAR);
    public static final TaskTypeItem TaskTypeAgent = new TaskTypeItem(R.string.menu_agent, R.string.agent_description, R.drawable.ic_category_agent, R.drawable.ic_category_agent_green, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_AGENT, TaskTypeItem.LAYOUT_AGENT);
    public static final TaskTypeItem TaskTypeManual = new TaskTypeItem(R.string.menu_manual, R.string.manual_description, R.drawable.ic_manual_grey, R.drawable.ic_manual, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_MANUAL, TaskTypeItem.LAYOUT_MANUAL);
    public static final TaskTypeItem TaskTypeActivityDetection = new TaskTypeItem(R.string.menu_activity_detection, R.string.activity_detection_description, R.drawable.ic_activity, R.drawable.ic_activity, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_NFC, TaskTypeItem.LAYOUT_NFC);
    public static final ListTaskTypeSpacer ProSpacer = new ListTaskTypeSpacer(R.string.upgrade_spacer, -1, true, TrialDialogActivity.class);
    public static final TaskTypeItem TaskTypeNotification = new TaskTypeItem(R.string.menu_notification, R.string.notification_description, R.drawable.ic_action_perm_device_info_grey, R.drawable.ic_action_perm_device_info, TriggerWizardActivity.class, TaskTypeItem.TASK_TYPE_NOTIFICATION, TaskTypeItem.LAYOUT_NOTIFICATION);

    @SuppressWarnings("rawtypes")
    private final Class mActivityClass;
    private final int mTitleId;
    private final int mDescriptionId;
    private final int mIconId;
    private final int mAltIconId;
    private final int mExtraValue;
    private final int mLayoutId;
    private boolean mLocked;

    public static final String EXTRA_TASK_TYPE = "com.trigger.launcher.new_task_type";
    public static final String EXTRA_LAYOUT_ID = "com.trigger.launcher.new_task_layout_id";
    public static final String EXTRA_KEY_1_VALUE = "com.trigger.launcher.EXTRA_KEY_1_VALUE";
    public static final String EXTRA_KEY_2_VALUE = "com.trigger.launcher.EXTRA_KEY_2_VALUE";
    public static final String EXTRA_TASK_CONDITION = "com.trigger.launcher.EXTRA_TASK_CONDITION";

    public static final int LAYOUT_BLUETOOTH = R.layout.fragment_configure_bluetooth_trigger;
    public static final int LAYOUT_WIFI = R.layout.fragment_configure_wifi_trigger;
    public static final int LAYOUT_GEOFENCE = R.layout.fragment_configure_geofence_trigger;
    public static final int LAYOUT_BATTERY = R.layout.fragment_configure_battery_trigger;
    public static final int LAYOUT_TIME = R.layout.fragment_configure_time_trigger;
    public static final int LAYOUT_CHARGER = R.layout.fragment_configure_charger_trigger;
    public static final int LAYOUT_HEADSET = R.layout.fragment_configure_headset_trigger;
    public static final int LAYOUT_CALENDAR = R.layout.fragment_configure_calendar_trigger;
    public static final int LAYOUT_NFC = R.layout.fragment_configure_nfc_trigger;
    public static final int LAYOUT_AGENT = R.layout.fragment_configure_agent_trigger;
    public static final int LAYOUT_MANUAL = R.layout.fragment_configure_manual_trigger;
    public static final int LAYOUT_NOTIFICATION = R.layout.fragment_configure_notification_trigger;

    public static final int TASK_TYPE_NFC = 1;
    public static final int TASK_TYPE_BLUETOOTH = 2;
    public static final int TASK_TYPE_WIFI = 3;
    public static final int TASK_TYPE_SWITCH = 4;
    public static final int TASK_TYPE_GEOFENCE = 5;
    public static final int TASK_TYPE_BATTERY = 6;
    public static final int TASK_TYPE_TIME = 7;
    public static final int TASK_TYPE_MANUAL = 8;
    public static final int TASK_TYPE_CHARGER = 9;
    public static final int TASK_TYPE_HEADSET = 10;
    public static final int TASK_TYPE_CALENDAR = 11;
    public static final int TASK_TYPE_AGENT = 12;
    public static final int TASK_TYPE_ACTIVITY_DETECTION = 13;
    public static final int TASK_TYPE_NOTIFICATION = 14;

    public static final int PAID_TRIGGER_COUNT = 8;

    public static TaskTypeItem[] getItems(Context context) {
        ArrayList<TaskTypeItem> items = new ArrayList<TaskTypeItem>();
        items.add(TaskTypeWifi);
        items.add(TaskTypeBluetooth);

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            items.add(TaskTypeNfc);
        }

        items.add(TaskTypeManual);

        if (BuildConfiguration.isPlayStoreAvailable() && FlavorInfo.SHOW_UPGRADE) {
            if (!(IabClient.checkLocalUnlock(context) || IabClient.grantUnlock(context))) {
                // Don't show the spacer if the user has unlocked with a local setting or via unlock APK
                items.add(ProSpacer);
            }
            items.add(TaskTypeBattery);
            items.add(TaskTypeTime);
            items.add(TaskTypeCharger);
            items.add(TaskTypeHeadset);
            items.add(TaskTypeCalendar);
            items.add(TaskTypeAgent);
            items.add(TaskTypeGeofence);
            items.add(TaskTypeNotification);
            //items.add(TaskTypeActivityDetection);
        }

        return items.toArray(new TaskTypeItem[items.size()]);
    }

    protected TaskTypeItem() {
        mTitleId = -1;
        mDescriptionId = -1;
        mIconId = -1;
        mAltIconId = -1;
        mActivityClass = null;
        mExtraValue = -1;
        mLayoutId = R.layout.list_item_single;
        mLocked = false;
    }

    @SuppressWarnings("rawtypes")
    protected TaskTypeItem(int titleId, int descriptionId, int iconId, int altIcon, Class activityClass, int extraValue, int layoutId) {
        mTitleId = titleId;
        mDescriptionId = descriptionId;
        mIconId = iconId;
        mAltIconId = altIcon;
        mActivityClass = activityClass;
        mExtraValue = extraValue;
        mLayoutId = layoutId;
        mLocked = false;
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        Activity activity = adapter.getActivity();

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.list_item_trigger_short, null);
        }

        if (mIconId > 0)
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(mIconId);
        if (mTitleId > 0)
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(mTitleId);
        if (mDescriptionId > 0)
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(mDescriptionId);

        convertView.findViewById(android.R.id.icon2).setVisibility(View.GONE);

        return convertView;
    }

    public View getView(ListItemsAdapter adapter, int position, View convertView, boolean purchased) {
        Activity activity = adapter.getActivity();

        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.list_item_trigger_short, null);
        }

        if (mIconId> 0)
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(mIconId);
        if (mTitleId > 0)
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(mTitleId);
        if (mDescriptionId > 0)
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(mDescriptionId);

        if (!purchased) {
            convertView.findViewById(android.R.id.icon2).setVisibility(View.VISIBLE);
            mLocked = true;
        } else {
            convertView.findViewById(android.R.id.icon2).setVisibility(View.GONE);
            mLocked = false;
        }
        return convertView;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public int getExtraValue() {
        return mExtraValue;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

    @SuppressWarnings("rawtypes")
    public Class getActivityClass() {
        return mActivityClass;
    }

    public boolean isLocked() {
        return mLocked;
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    public static BaseFragment getFragmentFromType(int type) {
        switch (type) {
            case TASK_TYPE_BLUETOOTH:
                return new BluetoothTriggerFragment();
            case TASK_TYPE_WIFI:
                return new WifiTriggerFragment();
            case TASK_TYPE_GEOFENCE:
                return new GeofenceTriggerFragment();
            case TASK_TYPE_BATTERY:
                return new BatteryTriggerFragment();
            case TASK_TYPE_TIME:
                return new TimeTriggerFragment();
            case TASK_TYPE_CHARGER:
                return new ChargerTriggerFragment();
            case TASK_TYPE_HEADSET:
                return new HeadsetTriggerFragment();
            case TASK_TYPE_CALENDAR:
                return new CalendarTriggerFragment();
            case TASK_TYPE_NFC:
                return new NfcTriggerFragment();
            case TASK_TYPE_AGENT:
                return new AgentTriggerFragment();
            case TASK_TYPE_MANUAL:
                return new ManualTriggerFragment();
            case TASK_TYPE_NOTIFICATION:
                return new NotificationTriggerFragment();
            default:
                return null;
        }
    }

    public static int getIconFromType(int type) {
        switch (type) {
            case TASK_TYPE_BLUETOOTH:
                return R.drawable.ic_action_bluetooth;
            case TASK_TYPE_WIFI:
                return R.drawable.ic_action_wifi;
            case TASK_TYPE_GEOFENCE:
                return R.drawable.ic_action_globe;
            case TASK_TYPE_BATTERY:
                return R.drawable.ic_action_battery;
            case TASK_TYPE_TIME:
                return R.drawable.ic_action_clock;
            case TASK_TYPE_CHARGER:
                return R.drawable.ic_action_plug;
            case TASK_TYPE_HEADSET:
                return R.drawable.ic_action_headphones;
            case TASK_TYPE_CALENDAR:
                return R.drawable.ic_action_calendar_month;
            case TASK_TYPE_MANUAL:
                return R.drawable.ic_manual;
            case TASK_TYPE_NOTIFICATION:
                return R.drawable.ic_action_perm_device_info;
            default:
                return R.drawable.ic_action_tag;
        }
    }

    public static int getLayoutFromType(int type) {
        switch (type) {
            case TASK_TYPE_BLUETOOTH:
                return LAYOUT_BLUETOOTH;
            case TASK_TYPE_WIFI:
                return LAYOUT_WIFI;
            case TASK_TYPE_GEOFENCE:
                return LAYOUT_GEOFENCE;
            case TASK_TYPE_BATTERY:
                return LAYOUT_BATTERY;
            case TASK_TYPE_TIME:
                return LAYOUT_TIME;
            case TASK_TYPE_CHARGER:
                return LAYOUT_CHARGER;
            case TASK_TYPE_HEADSET:
                return LAYOUT_HEADSET;
            case TASK_TYPE_CALENDAR:
                return LAYOUT_CALENDAR;
            case TASK_TYPE_NFC:
                return LAYOUT_NFC;
            case TASK_TYPE_MANUAL:
                return LAYOUT_MANUAL;
            case TASK_TYPE_NOTIFICATION:
                return LAYOUT_NOTIFICATION;
            default:
                return LAYOUT_WIFI;
        }
    }

    public static String getTaskName(Context context, int type) {

        switch (type) {
            case TASK_TYPE_BLUETOOTH:
                return context.getString(R.string.adapterBluetooth);
            case TASK_TYPE_WIFI:
                return context.getString(R.string.adapterWifi);
            case TASK_TYPE_GEOFENCE:
                return context.getString(R.string.geofence);
            case TASK_TYPE_BATTERY:
                return context.getString(R.string.battery_task);
            case TASK_TYPE_TIME:
                return context.getString(R.string.time_task);
            case TASK_TYPE_CHARGER:
                return context.getString(R.string.charger_title);
            case TASK_TYPE_HEADSET:
                return context.getString(R.string.headset_title);
            case TASK_TYPE_NFC:
            case TASK_TYPE_SWITCH:
                return context.getString(R.string.nfc_task);
            case TASK_TYPE_CALENDAR:
                return context.getString(R.string.calendar_title);
            case TASK_TYPE_MANUAL:
                return context.getString(R.string.menu_manual);
            case TASK_TYPE_NOTIFICATION:
                return context.getString(R.string.menu_notification);
            default:
                return "";
        }
    }

    public static TaskTypeItem getItemFromType(int type) {
        switch (type) {
            case TASK_TYPE_NFC:
            case TASK_TYPE_SWITCH:
                return TaskTypeNfc;
            case TASK_TYPE_BLUETOOTH:
                return TaskTypeBluetooth;
            case TASK_TYPE_WIFI:
                return TaskTypeWifi;
            case TASK_TYPE_GEOFENCE:
                return TaskTypeGeofence;
            case TASK_TYPE_TIME:
                return TaskTypeTime;
            case TASK_TYPE_BATTERY:
                return TaskTypeBattery;
            case TASK_TYPE_CHARGER:
                return TaskTypeCharger;
            case TASK_TYPE_HEADSET:
                return TaskTypeHeadset;
            case TASK_TYPE_CALENDAR:
                return TaskTypeCalendar;
            case TASK_TYPE_MANUAL:
                return TaskTypeManual;
            case TASK_TYPE_NOTIFICATION:
                return TaskTypeNotification;
            default:
                return TaskTypeNfc;
        }
    }
}
