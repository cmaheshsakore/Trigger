package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class ConfigureWifiTriggerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_wifi_trigger,  null);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        WifiManager wifiManager = (WifiManager) getActivity().getBaseContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        if (ssid != null) {
            if (Build.VERSION.SDK_INT > 16) {
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
            }
            Logger.i("SSID is " + ssid);
            ((EditText)view.findViewById(R.id.wifi_ssid)).setText(ssid);  
        }
    }
}
