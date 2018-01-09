package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.WifiAdapterAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.WriteTagActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.OpenApplicationAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BluetoothTrigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krohnjw on 1/24/14.
 */
public class DrivingExample extends BaseExample implements Example {
    private boolean mDisableOnFinish = false;
    private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private Spinner mSpinner;
    ProgressBar mProgress;

    public DrivingExample(int name, int icon, int description, boolean isPro, String tag) {
        super(name, icon, description, isPro, tag);
    }

    @Override
    public void showConfigurationDialog(final Context context, FragmentManager manager) {

        final View child = View.inflate(context, R.layout.include_example_driving, null);
        mSpinner = (Spinner) child.findViewById(R.id.devices);
        mProgress = (ProgressBar) child.findViewById(android.R.id.progress);

        if (!mAdapter.isEnabled()) {
            mDisableOnFinish = true;
            context.registerReceiver(BluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            mAdapter.enable();
            mProgress.setVisibility(View.VISIBLE);
        } else {
            setupBluetoothList(context);
        }

        final SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.hideAllTitles();
        dialog.setChildView(child);
        dialog.setPositiveButton(context.getString(R.string.menu_done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (((RadioGroup) child.findViewById(R.id.choices)).getCheckedRadioButtonId() == R.id.choice_bluetooth) {
                    String device = (String) mSpinner.getSelectedItem();
                    intent.putExtra("type", TaskTypeItem.TASK_TYPE_BLUETOOTH);
                    intent.putExtra("key1", device);
                } else {
                    intent.putExtra("type", TaskTypeItem.TASK_TYPE_NFC);
                }

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
            try { context.unregisterReceiver(BluetoothStateChangedReceiver); }
            catch (Exception ignored) { }
            mAdapter.disable();
        }

        int type = intent.getIntExtra("type", TaskTypeItem.TASK_TYPE_NFC);

        Task task = new Task("", context.getString(R.string.NavTypeDriving), "", null);
        task.addAction(new SavedAction("D:" + Constants.MODULE_WIFI, context.getString(R.string.disableText), context.getString(R.string.adapterWifi), new WifiAdapterAction().getCode()));
        task.addAction(new SavedAction(Constants.COMMAND_LAUNCH_APPLICATION + ":com.google.android.music", context.getString(R.string.listLaunchText), "Play Music", new OpenApplicationAction().getCode()));
        String id = DatabaseHelper.saveTask(context, task, null, false);

        Trigger trigger;

        if (type == TaskTypeItem.TASK_TYPE_NFC) {
            trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_NFC, DatabaseHelper.TRIGGER_NO_CONDITION, "", "", id);
        } else {
            trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_BLUETOOTH, DatabaseHelper.TRIGGER_ON_CONNECT, intent.getStringExtra("key1"), "", id);
        }

        DatabaseHelper.saveTrigger(context, trigger, id);

        if (type == TaskTypeItem.TASK_TYPE_NFC) {
            // Launch finish activity to write NFC tag
            task.setId(id);
            Intent write = new Intent(context, WriteTagActivity.class);
            write.putExtra(Constants.EXTRA_SAVED_TAG, new Task[] { task });
            context.startActivity(write);
        } else {
            BluetoothTrigger.enable(context);
        }

        Toast.makeText(context, String.format(context.getString(R.string.example_created), context.getString(R.string.NavTypeDriving)), Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver BluetoothStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

                int extraBTState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                Logger.d("Got Bluetooth State " + extraBTState);

                if (extraBTState == BluetoothAdapter.STATE_ON) {
                    setupBluetoothList(context);
                }

            }
    };

    private void setupBluetoothList(final Context context) {
        mProgress.setVisibility(View.VISIBLE);
        List<BluetoothDevice> existing = new ArrayList<BluetoothDevice>(mAdapter.getBondedDevices());
        ArrayList<String> devices = new ArrayList<String>();
        for (BluetoothDevice d: existing) {
            String name = d.getName();
            if (name != null) {
                try {
                    if (name.substring(0, 1).equals("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                } catch (Exception ignored) {}
                devices.add(name);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,  R.layout.spinner_app_list, devices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mProgress.setVisibility(View.GONE);
    }
}
