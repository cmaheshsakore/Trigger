package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class ConfigureSsidAction extends BaseAction {

    private int mLastKnownState;
    private Hashtable<String, Integer> mSsidMap;

    @Override
    public String getCommand() {
        return Constants.COMMAND_CONFIG;
    }

    @Override
    public String getCode() {
        return Codes.WIFI_CONFIGURE_NETWORK;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option006, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.SSIDAuthType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleEncryption, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            ((EditText) dialogView.findViewById(R.id.entrySSIDSSID)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE));
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            try {
                int auth = Integer.parseInt(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO));
                spinner.setSelection(getPositionFromChoice(auth));
            } catch (Exception e) {
                
            }
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            ((EditText) dialogView.findViewById(R.id.entrySSIDKey)).setText(arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE));
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_FOUR)) {
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_FOUR);
            if ((!value.isEmpty()) && (value.equals("1"))) {
                ((CheckBox) dialogView.findViewById(R.id.hiddenSSID)).setChecked(true);
            }
        }
        return dialogView;
    }

    @Override
    public String getName() {
        return "Configure Network";
    }

    private int getPositionFromChoice(int auth) {
        switch (auth) {
            case Constants.AUTH_OPEN:
                return 0;
            case Constants.AUTH_WEP:
                return 1;
            case Constants.AUTH_WPA:
                return 2;
            case Constants.AUTH_WPA2:
                return 3;
            default:
                return 0;
        }

    }
    @Override
    public String[] buildAction(View actionView, Context context) {
        String message = Constants.COMMAND_CONFIG + ":";
        Spinner hotspotEncryptionSpinner = (Spinner) actionView.findViewById(R.id.SSIDAuthType);
        String encryptionChoice = (String) hotspotEncryptionSpinner.getSelectedItem();
        boolean useHiddenSSID = ((CheckBox) actionView.findViewById(R.id.hiddenSSID)).isChecked();

        EditText etSSID = (EditText) actionView.findViewById(R.id.entrySSIDSSID);
        EditText etKey = (EditText) actionView.findViewById(R.id.entrySSIDKey);

        String SSID = etSSID.getText().toString();
        String Key = etKey.getText().toString();

        if (SSID.equals(""))
            SSID = "AndroidAP";
        if (Key.equals(""))
            Key = "1234567890";

        SSID = Utils.encodeData(SSID);
        Key = Utils.encodeData(Key);

        String SSIDDisplay = SSID;

        int authChoice = Constants.AUTH_WPA;
        if (encryptionChoice.equals(new String(context.getString(R.string.authTypeOpen))))
            authChoice = Constants.AUTH_OPEN;
        else if (encryptionChoice.equals(new String(context.getString(R.string.authTypeWPA2))))
            authChoice = Constants.AUTH_WPA2;
        else if (encryptionChoice.equals(new String(context.getString(R.string.authTypeWEP))))
            authChoice = Constants.AUTH_WEP;

        // To save space don't write down when we have default values
        // for the SSID or the Key
        if (SSID.equals(new String("AndroidAP")))
            SSID = "";
        if (Key.equals(new String("1234567890")))
            Key = "";

        message += "I4:" + SSID + ":" + authChoice + ":" + Key;

        if (useHiddenSSID)
            message += ":1";

        return new String[] { message, context.getString(R.string.listWifiConfigureConnectText), SSIDDisplay };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String text =  context.getString(R.string.listWifiAssociateText);
        int nameIndex = 0;
        if (command.equals(Constants.COMMAND_CONFIG)) {
            nameIndex = 1;
        }

        try {
            text += " " + args[nameIndex];
        } catch (Exception e) {
            /* Ignore any exception here */
        }
        return text;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.decodeData(Utils.tryParseString(args, 2, ""))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 3, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.decodeData(Utils.tryParseString(args, 4, ""))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_FOUR, Utils.tryParseString(args, 5, ""))
                );
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {

        String command = Utils.tryParseString(args,0, "");
        String SSID = Utils.tryParseEncodedString(args, 2, "AndroidAP", "Exception parsing SSID for Configure AP");
        int authType = Utils.tryParseInt(args, 3, Constants.AUTH_OPEN, "Exception parsing key for Configure AP");
        String key = Utils.tryParseEncodedString(args, 4, "1234567890", "Exception parsing key for Configure AP");
        String hiddenArg = Utils.tryParseString(args, 5, "0");
        boolean isHidden = (hiddenArg.equals("1")) ? true: false;

        if (!command.isEmpty() && command.equals(Constants.COMMAND_WIFI_ASSOCIATE)) {
            /* this is a legacy tag, SSID is first param */
            SSID = Utils.tryParseString(args, 1, "AndroidAP");
        }

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            mLastKnownState = WifiManager.WIFI_STATE_ENABLED;
            wifi.setWifiEnabled(true);
            setAutoRestart(currentIndex);
            return;
        } else {
            mResumeIndex = -1;
        }

        populateWifiList(context);

        Logger.d("Found " + mSsidMap.size() + " SSIDs");
        Logger.d("Looking for " + SSID);
        // Check if the AP already exists based on SSID
        if (mSsidMap.containsKey("\"" + SSID + "\"")) {
            // SSID is known, connect
            Integer netId = (Integer) mSsidMap.get("\"" + SSID + "\"");
            Logger.d("Found SSID in list, re-using " + netId);
            connectNetwork(wifi, netId);
        } else if (mSsidMap.containsKey(SSID)) {
            // SSID is known, connect
            Integer netId = (Integer) mSsidMap.get(SSID);
            Logger.d("Found SSID in list, re-using " + netId);
            connectNetwork(wifi, netId);
        } else {
            Logger.d("Did not find in list.  Creating new connection");
            // Configure new AP and connect
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = "\"" + SSID + "\"";
            if (isHidden) {
                netConfig.hiddenSSID = true;
            }

            if ((authType == Constants.AUTH_WPA)) {
                Logger.d("Creating WPA connection");
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                netConfig.preSharedKey = "\"" + key + "\"";
            } else if (authType == Constants.AUTH_WPA2) {
                Logger.d("Creating WPA2 connection");
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.preSharedKey = "\"" + key + "\"";
            } else if (authType == Constants.AUTH_WEP) {
                Logger.d("Creating WEP connection");
                netConfig.wepKeys[0] = "" + key + "";
                netConfig.wepTxKeyIndex = 0;
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            } else {
                Logger.d("Creating Open connection");
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }

            // Save Configuration
            int netId = wifi.addNetwork(netConfig);
            Logger.d("Connecting to " + netId);
            connectNetwork(wifi, netId);
        }

    }

    public void connectNetwork(final WifiManager wifi, final int id) {
        wifi.enableNetwork(id, true);
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                List<WifiConfiguration> configured = wifi.getConfiguredNetworks();
                if (configured != null) {
                    for (WifiConfiguration config: configured) {
                        if (config.networkId != id) {
                            wifi.enableNetwork(config.networkId, false);
                        }
                    }
                }
                
            }
        }, 3500);
        
    }
    
    @Override
    public String getWidgetText(Context context, int operation) {
        return context.getString(R.string.widgetConfigureSSID);
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return context.getString(R.string.actionConfigureSSID);
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

    public boolean resumeIsCurrentAction() {
        return true;
    }
}
