package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
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

public class WifiDisplayAction extends BaseAction{

    @Override
    public String getCommand() {
        return Constants.COMMAND_WIFI_DISPLAY_CONNECT;
    }

    @Override
    public String getCode() {
        return Codes.WIFI_DISPLAY_CONNECT;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
    @Override
    public View getView(Context context, CommandArguments arguments) {
        final Context mContext = context;
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option069, null, false);
        Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinner);
        DisplayManager manager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = manager.getDisplays();
        ArrayAdapter<Display> adapter = new ArrayAdapter<Display>(context, R.layout.configuration_spinner, displays) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.configuration_spinner, null);
                }

                Display item = getItem(position);
                ((TextView) convertView).setText(item.getName()); 
                return convertView;
            }
        };
        spinner.setAdapter(adapter);

        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            String mac = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
            for (int i=0; i< displays.length; i++) {
                if (getAddressFromDisplay(displays[i]).equals(mac)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
        return dialogView;
    }



    @Override
    public String getName() {
        return "Wireless Display Connect";
    }

    private String getAddressFromDisplay(Display item) {
        String address = "";
        try {
            Method getAddress = Display.class.getMethod("getAddress", null);
            address = (String) getAddress.invoke(item, null);
        } catch (Exception e) {
            Logger.e("Exception getting address", e);
        }
        
        return address;
    }
    
    @Override
    public String[] buildAction(View actionView, Context context) {
        Spinner spinner = (Spinner) actionView.findViewById(R.id.spinner);
        Display item = (Display) spinner.getAdapter().getItem(spinner.getSelectedItemPosition());

        String address = getAddressFromDisplay(item);
        

        if (!address.isEmpty()) {
            return new String[] { Constants.COMMAND_WIFI_DISPLAY_CONNECT + ":" + Utils.encodeData(address) + ":" + item.getName(), "Connect to ", item.getName()};
        } else {
            return new String[0];
        }

    }

    @Override
    public int getMinArgLength() {
        return 2;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        return "Connect to wireless display";
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseEncodedString(args, 1, ""))
                );
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        // Display ID comes in as arg #2
        String address = Utils.tryParseEncodedString(args, 1, "");
        if (!address.isEmpty()) {
            Logger.d("Got display with address of " + address);
            DisplayManager manager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);

            connectDevice(address, manager);
        }
    }

    private void connectDevice(String address, DisplayManager manager) {
        try {
            Logger.d("Connecting");
            Method connect = DisplayManager.class.getMethod("connectWifiDisplay", String.class);
            connect.invoke(manager, address);

        } catch (NoSuchMethodException e) {
            Logger.e("No method", e);
        } catch (InvocationTargetException e) {
            Logger.e("InvocationTargetException", e);
        } catch (IllegalAccessException e) {
            Logger.e(e.toString(), e);
        }
    }
    @Override
    public String getWidgetText(Context context, int operation) {
        return null;
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return null;
    }

    @Override
    public int getRestartDelay() {
        return 0;
    }

    @Override
    public void setArgs(String[] args) {
    }

    @Override
    public boolean scheduleWatchdog() {
        return false;
    }

}
