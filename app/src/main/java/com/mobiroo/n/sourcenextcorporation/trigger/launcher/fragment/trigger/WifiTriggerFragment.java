package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

import java.util.ArrayList;
import java.util.List;

public class WifiTriggerFragment extends BaseFragment {

    private ListView    mList;
    private String      mCurrentNetwork;
    private boolean     mReceiverRegistered = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_wifi_trigger,  container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList = (ListView) view.findViewById(R.id.known);
        
        ((TextView) view.findViewById(R.id.wifi_ssid)).addTextChangedListener(mPrimaryWatcher);
        WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            if (wm.isWifiEnabled()) {
                setupWifiList(mList);
            } else {
                promptUserToEnableWifi();
            }
        } else {
            cancelActivity();
        }
        
        if (DatabaseHelper.TRIGGER_ON_DISCONNECT.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_disconnect);
        }

        if (Task.isStringValid(mTrigger.getExtra(1))) {
            ((TextView) getView().findViewById(R.id.wifi_ssid)).setText(mTrigger.getExtra(1));
        }
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mReceiverRegistered) {
            mReceiverRegistered = false;
            try {
                getActivity().unregisterReceiver(this.WifiStateChangedReceiver);
            } catch (Exception e) { /* ignore exception */}
        }
        
    }
    
    @Override
    protected void getDataFromArgs(Bundle args) {
        super.getDataFromArgs(args);
        
    }
    
    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.adapterWifi));
    }
    
    private String getCurrentSsid(Context context) {
        String ssid = "";

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            ssid = wifiManager.getConnectionInfo().getSSID();
        } catch (Exception e) {
            Logger.e("Exception getting currently connected SSID", e);
        }

        if ((ssid != null) && (!ssid.isEmpty())) {
            if (Build.VERSION.SDK_INT > 16) {
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
            }
        }

       ssid = (!isSsidValid(ssid)) ? "" : ssid;
       
       if (!ssid.isEmpty()) {
           ((TextView) getView().findViewById(R.id.wifi_ssid)).setText(ssid);
       }

        return ssid;
    }
    
    private boolean isSsidValid(String ssid) {
        boolean valid = true;
        valid &= !(ssid == null);
        if (valid) {
            valid &= !(ssid.equals("<unknown ssid>"));
            valid &= !(ssid.equals("0x"));
        }
        return valid;
    }

    private List<String> populateWifiList() {
        List<String> nets = new ArrayList<String>();
        nets.add(getActivity().getString(R.string.any_network));
        WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if ((wm != null) && (wm.isWifiEnabled()) && (wm.getConfiguredNetworks() != null)) {
            for (WifiConfiguration config: wm.getConfiguredNetworks()) {
                String name = config.SSID;
                if (name != null) {
                    if (name.substring(0, 1).equals("\"")) {
                        name = name.substring(1, name.length() - 1);
                    }
                    nets.add(name);
                }
            }

        }
        return nets;
    }
    
    private void setupWifiList(ListView known) {
        mDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        mCurrentNetwork = getCurrentSsid(getActivity());

        known.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item_single_small, populateWifiList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (null == convertView) {
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_single_small, parent, false);
                }
                String name = getItem(position);

                if (name != null) {
                    ((TextView) convertView.findViewById(R.id.row1Text)).setText(name);
                    if (mCurrentNetwork.equals(name)) {
                        ((TextView) convertView.findViewById(R.id.row1Text)).setTypeface(null, Typeface.BOLD);
                    } else {
                        ((TextView) convertView.findViewById(R.id.row1Text)).setTypeface(null, Typeface.NORMAL);
                    }
                }

                return convertView;
            }
        });

        known.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                ((TextView) getView().findViewById(R.id.wifi_ssid)).setText((String) adapter.getItemAtPosition(position));
            }

        });

        mDialog.dismiss();

    }
    
    private void promptUserToEnableWifi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.enable_wifi));
        builder.setNegativeButton(getString(R.string.dialogCancel), new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.enableText), new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startWifi();
            }

        });

        builder.create().show();
    }
    
    private void startWifi() {
        mDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        getActivity().registerReceiver(this.WifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        mReceiverRegistered = true;
        WifiManager wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        wm.setWifiEnabled(true);
    }
    
    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (state == WifiManager.WIFI_STATE_ENABLED) {
                if ((mDialog != null) && (mDialog.isShowing())) {
                    mDialog.dismiss();
                }
                if (mList != null) {
                    setupWifiList(mList);
                }
            }
        }
    };
    
    @Override
    protected void updateTrigger() {
        String condition = (((RadioGroup) getView().findViewById(R.id.condition)).getCheckedRadioButtonId() == R.id.radio_condition_disconnect) ? DatabaseHelper.TRIGGER_ON_DISCONNECT
                : DatabaseHelper.TRIGGER_ON_CONNECT;
        String ssid = ((EditText) getView().findViewById(R.id.wifi_ssid)).getText().toString();
        
        
        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_WIFI);
        mTrigger.setExtra(1, ssid);
        mTrigger.setExtra(2, "");
    }
}
