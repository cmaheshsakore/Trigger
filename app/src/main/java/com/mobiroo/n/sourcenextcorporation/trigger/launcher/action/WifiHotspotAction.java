package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WifiHotspotAction extends BaseAction {

    private int mLastKnownState;

    @Override
    public String getCommand() {
        return Constants.MODULE_HOTSPOT;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_HOTSPOT;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option008, null, false);
        /* Populate the Hotspot toggle menu */
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.HotspotToggle);
        ArrayAdapter<CharSequence> toggleAdapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        toggleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(toggleAdapter);

        final TableRow configRow1 = (TableRow) dialogView.findViewById(R.id.config1Text);
        final TableRow configRow2 = (TableRow) dialogView.findViewById(R.id.config2Text);
        final TableRow configRow3 = (TableRow) dialogView.findViewById(R.id.config3Text);

        final String enable = context.getString(R.string.enableText);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getItemAtPosition(position).equals(enable)) {
                    configRow1.setVisibility(View.INVISIBLE);
                    configRow2.setVisibility(View.INVISIBLE);
                    configRow3.setVisibility(View.INVISIBLE);
                } else {
                    configRow1.setVisibility(View.VISIBLE);
                    configRow2.setVisibility(View.VISIBLE);
                    configRow3.setVisibility(View.VISIBLE);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                configRow1.setVisibility(View.VISIBLE);
                configRow2.setVisibility(View.VISIBLE);
                configRow3.setVisibility(View.VISIBLE);
            }

        });

        ((CheckBox) dialogView.findViewById(R.id.reuse_config)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int mode = (isChecked) ? View.INVISIBLE : View.VISIBLE;
                configRow1.setVisibility(mode);
                configRow2.setVisibility(mode);
                configRow3.setVisibility(mode);

            }

        });
        /* Populate the Hotspot encryption menu */
        Spinner encryption = (Spinner) dialogView.findViewById(R.id.HotspotAuthType);
        ArrayAdapter<CharSequence> encryptionAdapter = ArrayAdapter.createFromResource(context, R.array.ToggleEncryption, R.layout.configuration_spinner);
        encryptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryption.setAdapter(encryptionAdapter);

        boolean hasOne = false;
        boolean hasTwo = false;
        boolean hasThree = false;

        if (hasArgument(arguments, CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            if (state.equals(Constants.COMMAND_ENABLE)) {
                spinner.setSelection(0);
            } else if (state.equals(Constants.COMMAND_DISABLE)) {
                spinner.setSelection(1);
            } else if (state.equals(Constants.COMMAND_TOGGLE)) {
                spinner.setSelection(2);
            }
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            hasOne = true;
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
            if (!value.isEmpty() && !value.equals("-1")) {
                ((EditText) dialogView.findViewById(R.id.entrySSID)).setText(value);
            }
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_TWO)) {
            hasTwo = true;
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO);
            int selection = 0;
            if (value.equals(String.valueOf(Constants.AUTH_OPEN))) {
                selection = 0;
            } else if (value.equals(String.valueOf(Constants.AUTH_WEP))) {
                selection = 1;
            } else if (value.equals(String.valueOf(Constants.AUTH_WPA))) {
                selection = 2;
            } else if (value.equals(String.valueOf(Constants.AUTH_WPA2))) {
                selection = 3;
            }
            encryption.setSelection(selection);
        }

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_THREE)) {
            hasThree = true;
            String value = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE);
            if (!value.isEmpty() && !value.equals("-1")) {
                ((EditText) dialogView.findViewById(R.id.entryKey)).setText(value);
            }
        }

        // Check if one, two and three are all -1

        if (hasOne && hasTwo && hasThree) {
            if (arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE).equals("-1")
                    && arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO).equals("-1")
                    && arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_THREE).equals("-1")) {

                ((CheckBox) dialogView.findViewById(R.id.reuse_config)).setChecked(true);

                int mode = View.INVISIBLE;
                (dialogView.findViewById(R.id.config1Text)).setVisibility(mode);
                (dialogView.findViewById(R.id.config2Text)).setVisibility(mode);
                (dialogView.findViewById(R.id.config3Text)).setVisibility(mode);
            }
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Mobile Hotspot";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner hotspotActionSpinner = (Spinner) actionView.findViewById(R.id.HotspotToggle);
        Spinner hotspotEncryptionSpinner = (Spinner) actionView.findViewById(R.id.HotspotAuthType);
        String toggleChoice = (String) hotspotActionSpinner.getSelectedItem();
        String encryptionChoice = (String) hotspotEncryptionSpinner.getSelectedItem();

        EditText etSSID = (EditText) actionView.findViewById(R.id.entrySSID);
        EditText etKey = (EditText) actionView.findViewById(R.id.entryKey);

        String ssid = etSSID.getText().toString();
        String key = etKey.getText().toString();

        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText))) {
            message = Constants.COMMAND_ENABLE + ":";
        } else if (toggleChoice.equals(context.getString(R.string.disableText))) {
            message = Constants.COMMAND_DISABLE + ":";
        } else {
            message = Constants.COMMAND_TOGGLE + ":";
        }

        int authChoice = Constants.AUTH_WPA;

        if (((CheckBox) actionView.findViewById(R.id.reuse_config)).isChecked()) {
            ssid = "-1";
            key = "-1";
            authChoice = -1;
        } else {

            if (encryptionChoice.equals(new String(context.getString(R.string.authTypeOpen)))) {
                authChoice = Constants.AUTH_OPEN;
            } else if (encryptionChoice.equals(new String(context.getString(R.string.authTypeWEP)))) {
                authChoice = Constants.AUTH_WEP;
            } else if (encryptionChoice.equals(new String(context.getString(R.string.authTypeWPA2)))) {
                authChoice = Constants.AUTH_WPA2;
            }

            // To save space don't write down when we have default values
            // for the SSID or the Key
            if (ssid.equals(new String("AndroidAP"))) {
                ssid = "";
            }
            if (key.equals(new String("1234567890"))) {
                key = "";
            }

            if (message.equals(Constants.COMMAND_TOGGLE)) {
                // Blank KEY and SSID here to save space
                key = "";
                ssid = "";
            }

            key = Utils.encodeData(key);
            ssid = Utils.encodeData(ssid);

        }

        message += Constants.MODULE_HOTSPOT;

        if (toggleChoice.equals(context.getString(R.string.enableText))) {
            message += ":" + ssid + ":" + authChoice + ":" + key;
        }


        return new String[]{message, toggleChoice, context.getString(R.string.listHotspotText)};
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listHotspotText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, Utils.tryParseString(args, 0, Constants.COMMAND_TOGGLE)),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 2, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 3, String.valueOf(Constants.AUTH_OPEN))),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_THREE, Utils.tryParseEncodedString(args, 4, ""))
        );
    }


    private String DEFAULT_VALUE = "-1";

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Wifi Hotspot");

        mResumeIndex = currentIndex;

        String SSID = Utils.tryParseEncodedString(args, 2, "AndroidAP", "Exception parsing SSID for Configure AP");
        int authType = Utils.tryParseInt(args, 3, Constants.AUTH_OPEN, "Exception parsing auth type for Configure AP");
        String key = Utils.tryParseEncodedString(args, 4, "1234567890", "Exception parsing key for Configure AP");

        managePortableHotspot(context, currentIndex, operation, SSID, authType, key);
    }

    private void managePortableHotspot(Context context, int currentIndex, int operation, String SSID, int authType, String key) {
        if (SSID.isEmpty()) {
            SSID = "AndroidAP";
        }
        if (key.isEmpty()) {
            key = "1234567890";
        }
        boolean isWifiEnabled = false;
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Get current portable hotspot state
        Method[] wmMethods2 = wifi.getClass().getDeclaredMethods();
        for (Method method : wmMethods2) {
            if (method.getName().equals("isWifiApEnabled")) {
                try {
                    isWifiEnabled = Boolean.parseBoolean((method.invoke(wifi)).toString());
                    Logger.d(Constants.TAG, "WifiAPEnabled = " + isWifiEnabled);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Perform necessary operation on portable hostpot given desired
        // function and current state
        if ((operation == Constants.OPERATION_ENABLE) || (operation == Constants.OPERATION_DISABLE)) {
            operatePortableHotspot(context, currentIndex, operation, SSID, authType, key, (DEFAULT_VALUE.equals(SSID)));
        } else if (operation == Constants.OPERATION_TOGGLE) {
            if (isWifiEnabled) {
                operatePortableHotspot(context, currentIndex, Constants.OPERATION_DISABLE, DEFAULT_VALUE, -1, DEFAULT_VALUE, true);
            } else {
                operatePortableHotspot(context, currentIndex, Constants.OPERATION_ENABLE, DEFAULT_VALUE, -1, DEFAULT_VALUE, true);
            }
        }
    }

    private WifiConfiguration getCurrentConfiguration(Context context, WifiManager wifi) {
        WifiConfiguration netConfig = new WifiConfiguration();

        try {
            Logger.d("Trying to get cached AP configuration");
            Method getAP = wifi.getClass().getMethod("getWifiApConfiguration", (Class<?>[]) null);
            netConfig = (WifiConfiguration) getAP.invoke(wifi, (Object[]) null);
            Logger.d("Set config to " + netConfig.SSID);
        } catch (Exception e) {
            Logger.e("Exception getting cached AP config: " + e);
        }

        return netConfig;
    }


    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    private static int WIFI_AP_STATE_DISABLING = 0;
    private static int WIFI_AP_STATE_DISABLED = 1;
    public int WIFI_AP_STATE_ENABLING = 2;
    public int WIFI_AP_STATE_ENABLED = 3;
    private static int WIFI_AP_STATE_FAILED = 4;

    private void enableHotspot(Context context, WifiManager wifi, String ssid, int authType, String key) {
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Method getState = wifi.getClass().getMethod("getWifiApState");
            state = (Integer) getState.invoke(wifi);
        } catch (NoSuchMethodException e) {
            Logger.d("No Such Method exception getting state: " + e);
        } catch (InvocationTargetException e) {
            Logger.d("Invocation exception getting state: " + e);
        } catch (IllegalAccessException e) {
            Logger.d("Illegal Access exception getting state: " + e);
        }

        Logger.d("Found unaltered state of " + state);

        if (state >= 10) {
            // Workaround for Android 4.0+?
            WIFI_AP_STATE_DISABLING = 10;
            WIFI_AP_STATE_DISABLED = 11;
            WIFI_AP_STATE_ENABLING = 12;
            WIFI_AP_STATE_ENABLED = 13;
            WIFI_AP_STATE_FAILED = 14;
        }


        try {
            Method enable = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            WifiConfiguration config = getConfiguration(context, wifi, ssid, authType, key);
            if (config == null) {
                Logger.d("Wifi Configuration is null, cannot apply to hotspot");
                return;

            }
            Logger.d("Calling enable1");
            enable.invoke(wifi, null, false);
            Logger.d("Calling enable2");
            enable.invoke(wifi, config, true);

        } catch (Exception e) {
            Logger.d("Exception toggling hotspot: " + e);
            e.printStackTrace();
            Logger.d("Trying to use root access");

            try {
                Utils.runCommandAsRoot(new String[]{"service call wifi 34 i32 0 i32 1"});
            } catch (IOException e1) {
                Logger.d("IOException running hotspot command as root: " + e);
                e.printStackTrace();
            } catch (InterruptedException e1) {
                Logger.d("Interrupted running hotspot command as root: " + e);
                e.printStackTrace();
            }
        }

    }


    private WifiConfiguration getConfiguration(Context context, WifiManager wifi, String ssid, int authType, String key) {
        WifiConfiguration config = getCurrentConfiguration(context, wifi);
        if (!DEFAULT_VALUE.equals(ssid)) {
            // We have some valid config info here, use it
            Logger.d("Setting SSID to " + ssid);
            config.SSID = ssid;

            // Set auth type
            if (authType == Constants.AUTH_WPA2) {
                Logger.d(Constants.TAG, "Setting WPA2");
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.preSharedKey = key;
                // WPA2 is not present in our target API, not introduced until 4.0 is int 4
                config.allowedKeyManagement.set(4);

            } else if (authType == Constants.AUTH_WPA) {
                Logger.d(Constants.TAG, "Setting WPA");
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.preSharedKey = key;
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            } else {
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }


        }

        return config;
    }

    private void operatePortableHotspot(Context context, int currentIndex, int operation, String ssid, int authType, String key, boolean useCachedAP) {

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (operation == Constants.OPERATION_DISABLE) {
            Logger.d("Disabling Portable hotspot");
            try {
                Method method = wifi.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method.invoke(wifi, null, false);
            } catch (Exception e) {
                Logger.d("Exception disabling hotspot: " + e);
                e.printStackTrace();
                Logger.d("Trying root");
                try {
                    Utils.runCommandAsRoot(new String[]{"service call wifi 34 i32 0 i32 0"});
                } catch (IOException e1) {
                    Logger.d("IOException running hotspot command as root: " + e);
                    e.printStackTrace();
                } catch (InterruptedException e1) {
                    Logger.d("Interrupted running hotspot command as root: " + e);
                    e.printStackTrace();
                }
            }
        } else if (operation == Constants.OPERATION_ENABLE) {
            Logger.d("Enabling Portable hotspot");
            // Disable wifi
            if (wifi.isWifiEnabled()) {
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                mLastKnownState = WifiManager.WIFI_STATE_ENABLED;
                wm.setWifiEnabled(false);
                setAutoRestart(currentIndex);
                return;
            }

            enableHotspot(context, wifi, ssid, authType, key);
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.listHotspotText));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.listHotspotText));
    }

    public int getCurrentState(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mLastKnownState = (wm.isWifiEnabled()) ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;
        return mLastKnownState;
    }

}
