package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class BluetoothTriggerFragment extends BaseFragment {
    
    private ListView    mList;
    private boolean     mReceiverRegistered = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_bluetooth_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList = (ListView) view.findViewById(R.id.known);
        
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                setupBluetoothList(mList);
            } else {
                promptUserToEnableBluetooth();
            }

            ((TextInputLayout) getView().findViewById(R.id.bluetooth_name)).setHint(getString(R.string.enter_bluetooth_name));
            ((TextInputLayout) getView().findViewById(R.id.bluetooth_name)).getEditText().addTextChangedListener(mPrimaryWatcher);

            ((TextInputLayout) getView().findViewById(R.id.bluetooth_mac)).setHint(getString(R.string.enter_bluetooth_mac));
            ((TextInputLayout) getView().findViewById(R.id.bluetooth_mac)).getEditText().addTextChangedListener(mSecondaryWatcher);
        } else {
            cancelActivity();
        }
        
        if (DatabaseHelper.TRIGGER_ON_DISCONNECT.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_disconnect);
        }

        if (Task.isStringValid(mTrigger.getExtra(1))) {
            ((TextInputLayout) getView().findViewById(R.id.bluetooth_name)).getEditText().setText(mTrigger.getExtra(1));
        }
        if (Task.isStringValid(mTrigger.getExtra(2))) {
            ((TextInputLayout) getView().findViewById(R.id.bluetooth_mac)).getEditText().setText(mTrigger.getExtra(2));
        }
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mReceiverRegistered) {
            mReceiverRegistered = false;
            try {
                getActivity().unregisterReceiver(this.BluetoothStateChangedReceiver);
            } catch (Exception e) { /* ignore exception */}
        }
        
    }
    
    @Override
    protected void getDataFromArgs(Bundle args) {
        super.getDataFromArgs(args);
        
    }
    
    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.adapterBluetooth));
    }
    
    private BroadcastReceiver BluetoothStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state == BluetoothAdapter.STATE_ON) {
                if ((mDialog != null) && (mDialog.isShowing())) {
                    mDialog.dismiss();
                }
                if (mList != null) {
                    setupBluetoothList(mList);
                }
            }
        }
    };

    private class CustomDevice {
        public String name;
        public String mac;

        public CustomDevice(String name, String mac) {
            this.name = name;
            this.mac = mac;
        }
    }

    private void setupBluetoothList(ListView known) {

        mDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        List<CustomDevice> list = new ArrayList<CustomDevice>();
        list.add(new CustomDevice(getActivity().getString(R.string.any_device), ""));

        for (BluetoothDevice d: adapter.getBondedDevices()) {
            String name = d.getName();
            if (name != null) {
                try {
                    if (name.substring(0, 1).equals("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                } catch (Exception e) { }

                list.add(new CustomDevice(name, d.getAddress()));
            }
        }

        known.setAdapter(new ArrayAdapter<CustomDevice>(getActivity(), R.layout.list_item_single_small, list) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (null == convertView) {
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_single_small, parent, false);
                }
                ((TextView) convertView.findViewById(R.id.row1Text)).setText(getItem(position).name);
                return convertView;
            }
        });

        known.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                CustomDevice d = (CustomDevice) adapter.getItemAtPosition(position);
                ((TextInputLayout) getView().findViewById(R.id.bluetooth_name)).getEditText().setText(d.name);
                ((TextInputLayout) getView().findViewById(R.id.bluetooth_mac)).getEditText().setText(d.mac);
            }

        });
        mDialog.dismiss();
    }
    
    private void startBluetooth() {
        mDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        getActivity().registerReceiver(this.BluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        mReceiverRegistered = true;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.enable();
    }
    
    private void promptUserToEnableBluetooth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.enable_bluetooth));
        builder.setNegativeButton(getString(R.string.dialogCancel), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.enableText), new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startBluetooth();
            }

        });

        builder.create().show();
    }
    
    @Override
    protected void updateTrigger() {
        String condition = (((RadioGroup) getView().findViewById(R.id.condition)).getCheckedRadioButtonId() == R.id.radio_condition_disconnect) 
                ? DatabaseHelper.TRIGGER_ON_DISCONNECT
                : DatabaseHelper.TRIGGER_ON_CONNECT;
        String name = ((TextInputLayout) getView().findViewById(R.id.bluetooth_name)).getEditText().getText().toString();
        String mac = ((TextInputLayout) getView().findViewById(R.id.bluetooth_mac)).getEditText().getText().toString();
        
        
        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_BLUETOOTH);
        mTrigger.setExtra(1, name);
        mTrigger.setExtra(2, mac);
    }
}
