package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;


public class AirplaneModeAction extends BaseAction {

    public static final int REQUEST_AIRPLANE_MODE = 3;
    public static final String EXTRA_AIRPLANE_MODE = "AirplaneModeAction";
    private BroadcastReceiver mReceiver;

    @Override
    public String getCommand() {
        return Constants.MODULE_AIRPLANE_MODE;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_AIRPLANE_MODE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {

        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option003, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.AirplaneSpinner);
        ArrayAdapter<CharSequence> adapter7 = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
        adapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter7);

        /* Check for any pre-populated arguments */
        if (hasArgument(arguments,CommandArguments.OPTION_INITIAL_STATE)) {
            String state = arguments.getValue(CommandArguments.OPTION_INITIAL_STATE);
            if (state.equals(Constants.COMMAND_ENABLE)) {
                spinner.setSelection(0);
            } else if (state.equals(Constants.COMMAND_DISABLE)) {
                spinner.setSelection(1);
            } else if (state.equals(Constants.COMMAND_TOGGLE)) {
                spinner.setSelection(2);
            }
        }

        return dialogView;
    }

    @Override
    public String getName() {
        return "Airplane Mode";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner adapter = (Spinner) actionView.findViewById(R.id.AirplaneSpinner);
        String ap = (String) adapter.getSelectedItem();
        // Check to verify Application is in the map to retrieve package
        // name
        String message = "";
        String prefix = "";
        String suffix = context.getString(R.string.listWifiAirplaneModeText);
        if (ap.equals(context.getString(R.string.enableText))) {
            message = Constants.COMMAND_ENABLE + ":" + Constants.MODULE_AIRPLANE_MODE;
            prefix = context.getString(R.string.enableText);
        } else if (ap.equals(context.getString(R.string.disableText))) {
            message = Constants.COMMAND_DISABLE + ":" + Constants.MODULE_AIRPLANE_MODE;
            prefix =  context.getString(R.string.disableText);
        } else {
            message = Constants.COMMAND_TOGGLE + ":" + Constants.MODULE_AIRPLANE_MODE;
            prefix = context.getString(R.string.toggleText);
        }

        return new String[] {message, prefix, suffix };
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listWifiAirplaneModeText);
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Airplane Mode - " + operation);
        setAutoRestart(currentIndex + 1);
        operationSetAirplaneMode(context, operation);
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.wifiOptionsAirplaneMode));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.wifiOptionsAirplaneMode));
    }

    public void setReceiver(BroadcastReceiver receiver) {
        mReceiver = receiver;
    }

    @SuppressLint("InlinedApi") @SuppressWarnings("deprecation")
    private boolean operationSetAirplaneMode(Context context, int mode) {

        boolean isAirplaneModeOn = false;
        if (Build.VERSION.SDK_INT < 17) {
            isAirplaneModeOn = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            isAirplaneModeOn = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }

        mNeedReset = true;

        if ((mode == Constants.OPERATION_ENABLE) && (!isAirplaneModeOn)) {
            setAirplaneMode(context, mode, true);
        } else if ((mode == Constants.OPERATION_DISABLE) && (isAirplaneModeOn)) {
            setAirplaneMode(context, mode, false);
        } else if (mode == Constants.OPERATION_TOGGLE) {
            if (isAirplaneModeOn) {
                setAirplaneMode(context, Constants.OPERATION_DISABLE, false);
            } else {
                setAirplaneMode(context, Constants.OPERATION_ENABLE, true);
            }
        } else {
            mNeedReset = false;
            return false;
        }

        return mNeedReset;

    }

    private void setAirplaneMode(Context context, int mode, boolean enable) {
        Logger.d("Setting Airplane Mode to " + mode + "," + enable);
        putAirplaneMode(context, mode);
        try {
            if (Build.VERSION.SDK_INT < 18) {
                Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                intent.putExtra("state", enable);
                context.sendOrderedBroadcast(intent, null, mReceiver, null, Activity.RESULT_OK, null, null);    
            } else {
                if (Utils.isRootPresent()) {
                    context.registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
                    new AirplaneModeBroadcast().execute(enable);
                }
            }

        } catch (Exception e) {
            Logger.e("Exception boradcasting Airplane mode to system", e);
        }
    }

    @SuppressLint("InlinedApi") @SuppressWarnings("deprecation")
    private void putAirplaneMode(Context context, int value) {
        if (Build.VERSION.SDK_INT < 17) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, value);
        } else {
            PackageManager pm = context.getPackageManager();
            if ((pm.checkPermission(permission.WRITE_SECURE_SETTINGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
                Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, value);
                // Try root here

            } else {
                try {
                    Utils.requestWriteSecureSettings();
                    if ((pm.checkPermission(permission.WRITE_SECURE_SETTINGS, context.getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
                        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, value);
                    }
                } catch (IOException e) {
                    Logger.e("Could not get root access");
                    Toast.makeText(context, context.getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    Logger.e("Could not get root access");
                    Toast.makeText(context, context.getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class AirplaneModeBroadcast extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... args) {
            boolean enable = args[0];
            try {
                Utils.runCommandAsRoot(new String[] {"LD_LIBRARY_PATH='/vendor/lib:/system/lib' "
                        + "/system/bin/am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + enable});
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception setting airplane mode " + enable, e);
            } 
            return null;
        }
    }

    @Override
    public boolean scheduleWatchdog() {
        return true;
    }
}
