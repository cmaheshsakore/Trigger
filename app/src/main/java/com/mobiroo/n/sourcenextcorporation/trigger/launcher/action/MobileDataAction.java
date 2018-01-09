package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class MobileDataAction extends BaseAction {

    @Override
    public String getCommand() {
       return Constants.MODULE_MOBILE_DATA;
    }

    @Override
    public String getCode() {
        return Codes.OPERATE_MOBILE_DATA;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option043, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.WifiAdapterToggle);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.ToggleChoices, R.layout.configuration_spinner);
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
        return "Mobile Data";
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner toggle = (Spinner) actionView.findViewById(R.id.WifiAdapterToggle);

        String toggleChoice = (String) toggle.getSelectedItem();
        String adapterChoice = context.getString(R.string.adapterMobileData);

        // Append this pair to the list
        String message = "";

        if (toggleChoice.equals(context.getString(R.string.enableText)))
            message = Constants.COMMAND_ENABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.disableText)))
            message = Constants.COMMAND_DISABLE + ":";
        else if (toggleChoice.equals(context.getString(R.string.toggleText)))
            message = Constants.COMMAND_TOGGLE + ":";

        message += Constants.MODULE_MOBILE_DATA + "";

        return new String[] { message, toggleChoice, adapterChoice };
    }

    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return context.getString(R.string.adapterMobileData);
    }
    
    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        // Action should be in the form of (E|D|T|):Module
        return new CommandArguments(new BasicNameValuePair[] { new BasicNameValuePair(CommandArguments.OPTION_INITIAL_STATE, action.substring(0, 1))});
    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        Logger.d("Mobile Data");
        if (operation == Constants.OPERATION_ENABLE) {
            setMobileDataEnabled(context, true);
        } else if (operation == Constants.OPERATION_DISABLE) {
            setMobileDataEnabled(context, false);
        } else if (operation == Constants.OPERATION_TOGGLE) {
            if (!isMobileDataEnabled(context)) {
                setMobileDataEnabled(context, true);
            } else {
                setMobileDataEnabled(context, false);
            }
        }
        
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetSettingText(context, operation, context.getString(R.string.adapterMobileData));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionSettingText(context, operation, context.getString(R.string.adapterMobileData));
    }
    
    private boolean isMobileDataEnabled(Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Method method = cm.getClass().getMethod("getMobileDataEnabled");
                return (Boolean) method.invoke(cm);
            } catch (Exception e) {
                Logger.e("Exception when querying mobile data status", e);
                return false;
            }
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Method method = tm.getClass().getMethod("getDataEnabled");
                return (Boolean) method.invoke(tm);
            } catch (Exception e) {
                Logger.e("Exception when querying mobile data status", e);
                return false;
            }
        }
    }
    
    private void setMobileDataEnabled(Context context, boolean on) {
        if (Build.VERSION.SDK_INT < 21) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Method method = cm.getClass().getMethod("setMobileDataEnabled", boolean.class);
                method.invoke(cm, on);
            } catch (NoSuchMethodException e) {
                Logger.e("Exception when modifying mobile data, noSuchMethod", e);
            } catch (Exception e) {
                Logger.e("Exception when modifying mobile data." + e);
            }
        } else {
            try { setMobileNetworkfromLollipop(context, on ? 1 : 0); }
            catch (Exception e) {
                Logger.e("Exception when modifying mobile data for Lollipop : " + e);
                e.printStackTrace();
            }
        }
    }

    private static void setMobileNetworkfromLollipop(Context context, int state) throws Exception {
        String command = null;
        //int state = 0;
        try {
            // Get the current state of the mobile network.
            //state = isMobileDataEnabledFromLollipop(context) ? 0 : 1; // 0 will disable, 1 will enable
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            String transactionCode = getTransactionCode(context);
            // Android 5.1+ (API 22) and later.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                        // Execute the command via `su` to turn off
                        // mobile network for a subscription service.
                        command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0 (API 21) only.
                if (transactionCode != null && transactionCode.length() > 0) {
                    // Execute the command via `su` to turn off mobile network.
                    command = "service call phone " + transactionCode + " i32 " + state;
                    executeCommandViaSu(context, "-c", command);
                }
            }
        } catch(Exception e) {
            // Oops! Something went wrong, so we throw the exception here.
            throw e;
        }
    }

    private static boolean isMobileDataEnabledFromLollipop(Context context) {
        boolean state = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        return state;
    }

    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

    private static void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }

}
