package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by krohnjw on 4/25/2014.
 */
public class ForgetWifiNetworkAction extends BaseAction {

    private int mLastKnownState;


    private Hashtable<String, Integer> mSsidMap;

    @Override
    public String getCommand() {
        return Constants.COMMAND_FORGET_WIFI;
    }

    @Override
    public String getCode() {
        return Codes.FORGET_WIFI_NETWORK;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {

        View v = View.inflate(context, R.layout.configuration_dialog_option073, null);
        Spinner s = (Spinner) v.findViewById(R.id.networks);

        WifiManager m = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> nets = m.getConfiguredNetworks();
        if (nets != null) {

            ArrayAdapter<WifiConfiguration> adapter = new ArrayAdapter<WifiConfiguration>(context, android.R.layout.simple_spinner_item, nets) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = View.inflate(getContext(), android.R.layout.simple_spinner_item, null);
                    }

                    WifiConfiguration item = getItem(position);
                    if (item.SSID != null) {
                        ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.SSID.replace("\"", ""));
                    }

                    return convertView;

                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = View.inflate(getContext(), R.layout.spinner_item_padded, null);
                    }

                    WifiConfiguration item = getItem(position);
                    if (item.SSID != null) {
                        ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.SSID.replace("\"", ""));
                    }

                    return convertView;
                }
            };
            adapter.setDropDownViewResource(R.layout.spinner_item_padded);
            s.setAdapter(adapter);
        }

        return v;
    }

    @Override
    public String getName() {
        return "Forget Wifi";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner s = (Spinner) actionView.findViewById(R.id.networks);
        WifiConfiguration net = (WifiConfiguration) s.getSelectedItem();
        if (net != null) {
            String ssid = net.SSID;
            int id = net.networkId;
            String message = Constants.COMMAND_FORGET_WIFI + ":" + Utils.encodeData(ssid) + ":" + id;
            return new String[] { message, context.getString(R.string.forget_wifi_network), ssid };
        }
        return new String[0];
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.forget_wifi_network) + ": " + Utils.tryParseEncodedString(args, 1, "");
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseEncodedString(args, 2, ""))
        );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            mLastKnownState = WifiManager.WIFI_STATE_ENABLED;
            wifi.setWifiEnabled(true);
            setAutoRestart(currentIndex);
            return;
        } else {
            mResumeIndex = -1;
        }

        String network = Utils.tryParseEncodedString(args, 1, "");
        int id = Utils.tryParseInt(args, 2, -1);
        Logger.d("Forgetting wifi network " + network + ", " + id);
        if (id != -1) {
            WifiManager m = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            m.removeNetwork(id);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.forget_wifi_network);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.forget_wifi_network);
    }

    public int getCurrentState(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mLastKnownState = (wm.isWifiEnabled()) ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;
        return mLastKnownState;
    }

    private void populateWifiList(Context context) {
        mSsidMap = new Hashtable<String, Integer>();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            List<WifiConfiguration> nets = wm.getConfiguredNetworks();
            if (nets != null) {
                for (WifiConfiguration net : nets) {
                    // Iterate over all, add networkID to map
                    if (net.SSID != null) {
                        if (!mSsidMap.containsKey(net.SSID)) {
                            Logger.d("Adding network " + net.SSID + ", " + net.networkId + " to the list");
                            mSsidMap.put(net.SSID, net.networkId);
                        }
                    }
                }
            }
        }
    }

}
