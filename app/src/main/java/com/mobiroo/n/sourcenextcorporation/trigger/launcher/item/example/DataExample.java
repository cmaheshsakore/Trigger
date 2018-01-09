package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.MobileDataAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.WifiTrigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krohnjw on 1/24/14.
 */
public class DataExample extends BaseExample implements Example {

    private boolean mDisableOnFinish = false;
    ProgressBar mProgress;
    WifiManager mManager;
    ArrayAdapter<String> adapter;
    Spinner spinner;

    public DataExample(int name, int icon, int description, boolean isPro, String tag) {
        super(name, icon, description, isPro, tag);
    }

    private void setupWifiAdapter(Context context) {
        mProgress.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_app_list, populateWifiList(context));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mProgress.setVisibility(View.GONE);
    }

    private List<String> populateWifiList(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ArrayList<String> networks = new ArrayList<String>();
        if (wm != null) {
            try {
                for (WifiConfiguration c : wm.getConfiguredNetworks()) {
                    String name = c.SSID;
                    if (name != null) {
                        if (name.substring(0, 1).equals("\"")) {
                            name = name.substring(1, name.length() - 1);
                        }
                        networks.add(name);
                    }
                }
            } catch (NullPointerException e) {
                networks.add("Wifi not enabled");
            }
        }
        return networks;
    }

    @Override
    public void showConfigurationDialog(final Context context, FragmentManager manager) {

        final View child = View.inflate(context, R.layout.include_example_wifi, null);
        spinner = (Spinner) child.findViewById(R.id.spinner);
        mProgress = (ProgressBar) child.findViewById(android.R.id.progress);
        mManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!mManager.isWifiEnabled()) {
            mDisableOnFinish = true;
            context.registerReceiver(WifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            mProgress.setVisibility(View.VISIBLE);
            mManager.setWifiEnabled(true);
        } else {
            setupWifiAdapter(context);
        }

        final SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.hideAllTitles();

        dialog.setChildView(child);
        dialog.setPositiveButton(context.getString(R.string.menu_done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = ((Spinner) child.findViewById(R.id.spinner)).getSelectedItem().toString();
                Intent intent = new Intent();
                intent.putExtra("key1", ssid);
                createExample(context, intent);
                dialog.dismiss();
            }
        });
        dialog.show(manager, getTag());

    }

    @Override
    public void createExample(Context context, Intent intent) {
        super.createExample(context, intent);

        if (mDisableOnFinish) {
            context.unregisterReceiver(WifiStateChangedReceiver);
            mManager.setWifiEnabled(false);
        }

        // Turn off wifi and dim display for battery task
        String ssid = intent.getStringExtra("key1");
        if (ssid.isEmpty()) {
            return;
        }

        Task task = new Task("", context.getString(R.string.example_task_data_saver_on), "", null);
        task.addAction(new SavedAction("D:" + Constants.MODULE_MOBILE_DATA, context.getString(R.string.disableText), context.getString(R.string.adapterMobileData), new MobileDataAction().getCode()));

        String id = DatabaseHelper.saveTask(context, task, null, false);

        Trigger trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_WIFI, DatabaseHelper.TRIGGER_ON_CONNECT, ssid, "", id);

        DatabaseHelper.saveTrigger(context, trigger, id);

        task = new Task("", context.getString(R.string.example_task_data_saver_off), "", null);
        task.addAction(new SavedAction("E:" + Constants.MODULE_MOBILE_DATA, context.getString(R.string.enableText), context.getString(R.string.adapterMobileData), new MobileDataAction().getCode()));

        id = DatabaseHelper.saveTask(context, task, null, false);

        trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_WIFI, DatabaseHelper.TRIGGER_ON_DISCONNECT, ssid, "", id);

        DatabaseHelper.saveTrigger(context, trigger, id);

        WifiTrigger.enable(context);

        Toast.makeText(context, String.format(context.getString(R.string.example_created), context.getString(R.string.example_task_data_saver_on)), Toast.LENGTH_LONG).show();
        Toast.makeText(context, String.format(context.getString(R.string.example_created), context.getString(R.string.example_task_data_saver_off)), Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (extraWifiState == WifiManager.WIFI_STATE_ENABLED) {
                setupWifiAdapter(context);
            }
        }
    };
}