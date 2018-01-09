package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListStringItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.StateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by krohnjw on 12/19/13.
 */
public class BluetoothConstraint extends Constraint {

    private TextView            mBluetoothView;
    private ArrayList<String>   mBluetoothDevices;
    private List<ListItem>      mBluetoothList;
    private String              mCondition = CONNECTED;

    public BluetoothConstraint() { }

    public BluetoothConstraint(String id, String trigger_id, String type, String key1, String key2) {
        super(id, trigger_id, type, key1, key2);
    }

    public BluetoothConstraint(int type, String key1, String key2) {
        super(type, key1, key2);
    }

    @Override
    public String getType() {
        return String.valueOf(TYPE_BLUETOOTH);
    }

    @Override
    public boolean isConstraintSatisfied(Context context) {
        String condition = getExtra(2);
        logd("Checking constraint: " + condition);
        boolean satisfied = true;

        if (IS_OFF.equals(condition)) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            satisfied = !adapter.isEnabled();
            logd("satisfied = " + satisfied);
            return satisfied;
        } else if (IS_ON.equals(condition)) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            satisfied = adapter.isEnabled();
            logd("satisfied = " + satisfied);
            return satisfied;
        }

        // Grab user devices
        String user_devices = getExtra(1);

        if (condition.equals(NOT_CONNECTED)) {
            if (user_devices.isEmpty()) user_devices = StateUtils.ANY_DEVICE;

            logd("User devices " + user_devices);
            List<String> search = Arrays.asList(user_devices.split(","));
            satisfied = !StateUtils.isBluetoothDeviceConnected(context, search);
            logd("satisfied = " + satisfied);
            return satisfied;
        } else {
            if ((user_devices.isEmpty())) return true;

            logd("User devices " + user_devices);
            List<String> search = Arrays.asList(user_devices.split(","));
            satisfied = StateUtils.isBluetoothDeviceConnected(context, search);
            logd("satisfied = " + satisfied);
            return satisfied;
        }
    }

    @Override
    public View getView(Context context) {
        View base = super.getView(context);

        View child = View.inflate(context, R.layout.constraint_bluetooth, null);

        mBluetoothView = (TextView) child.findViewById(R.id.bluetooth_picker);

        mBluetoothList = new ArrayList<ListItem>();
        mBluetoothDevices = new ArrayList<String>();

        mBluetoothView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateBluetoothList(mContext);
            }
        });


        int id = R.id.connected;
        if (NOT_CONNECTED.equals(mCondition)) {
            id = R.id.not_connected;
        } else if (IS_ON.equals(mCondition)) {
            id = R.id.is_on;
        } else if (IS_OFF.equals(mCondition)) {
            id = R.id.is_off;
        }
        ((RadioButton) child.findViewById(id)).setChecked(true);

        addChildToContainer(base, child);

        return base;
    }


    @Override
    public int getText() {
        return R.string.constraint_bluetooth;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_action_bluetooth;
    }

    @Override
    public Constraint buildConstraint(Context context) {
        int checked = ((RadioGroup) getBaseView().findViewById(R.id.connection_type)).getCheckedRadioButtonId();
        String condition = CONNECTED;
        switch (checked) {
            case R.id.is_off:
                condition = IS_OFF;
                break;
            case R.id.is_on:
                condition = IS_ON;
                break;
            case R.id.not_connected:
                condition = NOT_CONNECTED;
                break;
            case R.id.connected:
                condition = CONNECTED;
                break;
        }
        return new BluetoothConstraint(TYPE_BLUETOOTH, TextUtils.join(",", mBluetoothDevices), condition);
    }

    @Override
    public void loadData(Context context, Constraint c) {
        super.loadData(context, c);
        String[] devices = c.getExtra(1).split(",");
        mBluetoothDevices = new ArrayList<String>();
        for (int i=0; i< devices.length; i++) {
            mBluetoothDevices.add(devices[i]);
        }

        mCondition = (!c.getExtra(2).isEmpty()) ? c.getExtra(2) : CONNECTED;
    }

    @Override
    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        String devices = getExtra(1);
        String condition = getExtra(2);

        if (NOT_CONNECTED.equals(condition)) {
            Logger.d("Devices is " + devices);
            if (devices.isEmpty()) {
                devices = context.getString(R.string.any_device);
            }
            holder.constraint_bluetooth_text.setVisibility(View.VISIBLE);
            holder.constraint_bluetooth_text.setText(String.format(context.getString(R.string.display_not_connected),
                    devices.replace(",", ", ")));
        } else if (IS_ON.equals(condition)) {
            holder.constraint_bluetooth_text.setVisibility(View.VISIBLE);
            holder.constraint_bluetooth_text.setText(String.format("%s: %s", context.getString(R.string.constraint_bluetooth), context.getString(R.string.is_on)));
        } else if (IS_OFF.equals(condition)) {
            holder.constraint_bluetooth_text.setVisibility(View.VISIBLE);
            holder.constraint_bluetooth_text.setText(String.format("%s: %s", context.getString(R.string.constraint_bluetooth), context.getString(R.string.is_off)));
        } else {
            if (!devices.isEmpty()) {
                holder.constraint_bluetooth_text.setVisibility(View.VISIBLE);
                holder.constraint_bluetooth_text.setText(String.format(context.getString(R.string.display_wifi_network),
                        devices.replace(",", ", ")));
            } else {
                holder.constraint_bluetooth_text.setVisibility(View.GONE);
            }
        }

    }

    private void populateBluetoothList(Context context) {
        if ((mBluetoothList == null) || (mBluetoothList.size() == 0)) {
            mBluetoothList = new ArrayList<ListItem>();

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                if (adapter.isEnabled()) {
                    Set<BluetoothDevice> devices = adapter.getBondedDevices();
                    if (devices != null) {
                        for (BluetoothDevice device : devices) {
                            mBluetoothList.add(new ListStringItem(context, device.getName()));
                        }
                    }
                    showBluetoothChooser();
                    try {
                        context.unregisterReceiver(receiver);
                    } catch (Exception ignored) {
                    }
                } else {
                    context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                    adapter.enable();
                }
            }
        } else {
            showBluetoothChooser();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state == BluetoothAdapter.STATE_ON) {
                populateBluetoothList(mContext);
                BluetoothAdapter.getDefaultAdapter().disable();
            }
        }
    };

    private void updateBluetoothUi() {
        Logger.d("mBluetoothDevices has " + mBluetoothDevices.size());
        if (mBluetoothDevices.size() == 0) {
            mBluetoothView.setText(mContext.getString(R.string.any_device));
        } else if (mBluetoothDevices.size() == 1) {
            mBluetoothView.setText((mBluetoothDevices.get(0) == null || mBluetoothDevices.get(0).isEmpty())
                    ? mContext.getString(R.string.any_device)
                    : mBluetoothDevices.get(0));
        } else {
            mBluetoothView.setText(mContext.getString(R.string.two_or_more_devices));
        }
    }

    private void showBluetoothChooser() {
        mBluetoothDevices = new ArrayList<String>();
        final SimpleDialogFragment fragment = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        fragment.setTitle(mContext.getString(R.string.select_bluetooth_device));
        fragment.setMultiSelect(true);
        fragment.setPositiveButton(mContext.getString(R.string.menu_done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = fragment.getListView().getCheckedItemPositions();
                for (int i=0; i< fragment.getListView().getCount(); i++) {
                    if (checked.get(i)) {
                        String text = ((ListStringItem) mBluetoothList.get(i)).getText();
                        if (!mBluetoothDevices.contains(text)) {
                            mBluetoothDevices.add(text);
                        }
                    }
                }

                updateBluetoothUi();

                fragment.dismiss();
            }
        });

        fragment.setListAdapter(new ListItemsAdapter(((FragmentActivity) mContext), mBluetoothList));
        fragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "dialog");
    }

}
