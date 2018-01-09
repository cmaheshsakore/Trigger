package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.KeyguardService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import org.apache.http.message.BasicNameValuePair;

public class ScreenLockAction extends BaseAction {

    @SuppressWarnings("unused")
    private DevicePolicyManager dpm; 

    @Override
    public String getCommand() {
        return Constants.MODULE_KEYGUARD;
    }

    @Override
    public String getCode() {
        return Codes.LOCK_SCREEN_KEYGUARD;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option049, null, false);
        /* Populate the Adapter list */
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.keyguardSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoicesNoToggle, R.layout.configuration_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        /* Check for any pre-populated arguments */
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

        return dialogView;
    }

    @Override
    public String getName() {
        return "Lockscreen (Keyguard)";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner adapter = (Spinner) actionView.findViewById(R.id.keyguardSpinner);
        String ap = (String) adapter.getSelectedItem();
        // Check to verify Application is in the map to retrieve package
        // name
        String message = "";
        String prefix = ""; 
        String suffix = context.getString(R.string.listSecurityKeyguardText);

        if (ap.equals(context.getString(R.string.enableText))) {
            message = Constants.COMMAND_ENABLE + ":" + Constants.MODULE_KEYGUARD;
            prefix = context.getString(R.string.enableText);
        } else if (ap.equals(context.getString(R.string.disableText))) {
            message = Constants.COMMAND_DISABLE + ":" + Constants.MODULE_KEYGUARD;
            prefix = context.getString(R.string.disableText);
        } else {
            message = Constants.COMMAND_TOGGLE + ":" + Constants.MODULE_KEYGUARD;
            prefix = context.getString(R.string.toggleText);
        }

        return new String[] { message, prefix, suffix };
    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.listSecurityKeyguardText);
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Keyguard");
        PackageManager packageManager = context.getPackageManager();

        boolean trustedUnlock = Utils.isPackageInstalled(packageManager, "com.krohnjw.trustedunlock");
        boolean lockPlugin = Utils.isPackageInstalled(packageManager, "com.trigger.launcher.nfctl.plugins.lockscreenplugin");


        if (operation == Constants.OPERATION_DISABLE) {

            if (lockPlugin) {
                Logger.d("Sending broadcast to lock plugin to disable");
                context.sendBroadcast(new Intent("com.trigger.launcher.plugins.lock.disable_lock_screen"));
                return;
            }

            if (trustedUnlock) {
                Intent intent = new Intent("com.krohnjw.LOCK_SCREEN_ACTION");
                intent.putExtra("ACTION", 0);
                context.sendBroadcast(intent, null);
                return;
            }

            Logger.d("Starting Keyguard service");
            Intent service = new Intent(context, KeyguardService.class);
            service.putExtra(KeyguardService.EXTRA_ACTION, operation);
            context.startService(service);

        } else {
            if (lockPlugin) {
                Logger.d("Sending broadcast to lock plugin to enable");
                context.sendBroadcast(new Intent("com.trigger.launcher.plugins.lock.enable_lock_screen"));
                return;
            }

            if (trustedUnlock) {
                Intent intent = new Intent("com.jwsoft.LOCK_SCREEN_ACTION");
                intent.putExtra("ACTION", 1);
                context.sendBroadcast(intent, null);
                return;
            }

            Logger.d("Stopping service, result = " + (context).stopService(new Intent(context, KeyguardService.class)));
        }

        /*
        KeyguardLocker lock = new KeyguardLocker(context);
        lock.execute(operation);*/
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.layoutOptionsKeyguard));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.layoutOptionsKeyguard));
    }


    public static class CustomDeviceAdminReceiver extends DeviceAdminReceiver {

        @Override
        public void onEnabled(Context context, Intent intent) {
            // intentionally left blank
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            // intentionally left blank
            return null;
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            // intentionally left blank
        }

        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            // intentionally left blank
        }

        @Override
        public void onPasswordFailed(Context context, Intent intent) {
            Toast.makeText(context, "Password change failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPasswordSucceeded(Context context, Intent intent) {
            // intentionally left blank
        }

        @Override
        public void onPasswordExpiring(Context context, Intent intent) {
            // intentionally left blank
        }
    }


}
