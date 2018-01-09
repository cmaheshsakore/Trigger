package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.StateUtils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;

/**
 * Created by krohnjw on 12/19/13.
 */
public class ChargingConstraint extends Constraint {

    private int         mChargingValue = -1;
    private boolean     mIsPreJbMr1;

    public ChargingConstraint() {}

    public ChargingConstraint(String id, String trigger_id, String type, String key1, String key2) {
        super(id, trigger_id, type, key1, key2);
    }

    public ChargingConstraint(int type, String key1, String key2) {
        super(type, key1, key2);
    }

    @Override
    public String getType() {
        return String.valueOf(TYPE_CHARGING);
    }

    @Override
    public boolean isConstraintSatisfied(Context context) {
        logd("Checking charging constraint");
        StateUtils.ChargingState state = StateUtils.getChargingState(context);
        logd("Found status " + state.status);
        logd("Type is " + state.type);
        int desiredState = Integer.parseInt(getExtra(1));
        logd("Looking for state " + desiredState);


        if (desiredState == -1) {
            logd("Ignoring state");
            // This is the ANY state
            return true;
        } else if (desiredState == (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB)) {
            // Accept either wireless, AC or USB here
            logd("Checking for any charging");
            return (state.type == BatteryManager.BATTERY_PLUGGED_AC)
                    || (state.type == BatteryManager.BATTERY_PLUGGED_USB)
                    || (state.type == BatteryManager.BATTERY_PLUGGED_WIRELESS);
        } else if (desiredState == BatteryManager.BATTERY_PLUGGED_AC) {
            logd("Checking for AC charging");
            return (state.type == BatteryManager.BATTERY_PLUGGED_AC);
        } else if (desiredState == BatteryManager.BATTERY_PLUGGED_USB) {
            logd("Checking for USB charging");
            return (state.type == BatteryManager.BATTERY_PLUGGED_USB);
        } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) && (desiredState == BatteryManager.BATTERY_PLUGGED_WIRELESS)) {
            logd("Checking for wireless charging");
            return (state.type == BatteryManager.BATTERY_PLUGGED_WIRELESS);
        } else if (desiredState == 0) {
            logd("Checking for no charging");
            return !(((state.type == BatteryManager.BATTERY_PLUGGED_AC)
                    || (state.type == BatteryManager.BATTERY_PLUGGED_USB)
                    || (state.type == BatteryManager.BATTERY_PLUGGED_WIRELESS)));
        } else {
            logd("Missed all conditions, returning true");
            return true;
        }
    }

    public static String getChargingStringFromValue(Context context, int value) {
        switch (value) {
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return context.getString(R.string.when_wireless_charger_connected);
            case BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB:
                return context.getString(R.string.when_charger_connected);
            case BatteryManager.BATTERY_PLUGGED_AC:
                return context.getString(R.string.when_charging_ac);
            case BatteryManager.BATTERY_PLUGGED_USB:
                return context.getString(R.string.when_charging_usb);
            case 0:
                return context.getString(R.string.when_charger_disconnected);
            case -1:
                return context.getString(R.string.any_time);
        }
        return "";
    }

    @Override
    public View getView(Context context) {
        View base = super.getView(context);

        View child = View.inflate(context, R.layout.constraint_charging, null);

        mIsPreJbMr1 = (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1);
        // Set up wireless charging constraint to not show wireless charging for API level below 17
        if (mIsPreJbMr1) {
            ArrayAdapter<String> a = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.charging_state_choices_no_wireless));
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((Spinner) child.findViewById(R.id.charging_picker)).setAdapter(a);
        }

        ((Spinner) child.findViewById(R.id.charging_picker)).setSelection(getChargingSpinnerPositionFromValue(mChargingValue));

        addChildToContainer(base, child);

        return base;
    }

    @Override
    public int getText() {
        return R.string.constraint_charging;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_action_plug;
    }

    @Override
    public Constraint buildConstraint(Context context) {
        int value = getValueFromSpinner();
        return new ChargingConstraint(TYPE_CHARGING, String.valueOf(value), "");
    }

    @Override
    public void loadData(Context context, Constraint c) {
        super.loadData(context, c);
        mChargingValue = Integer.parseInt(c.getExtra(1));
    }

    @Override
    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        if (!getExtra(1).isEmpty() && (!getExtra(1).equals("-1"))) {
            holder.constraint_charging_text.setVisibility(View.VISIBLE);
            holder.constraint_charging_text.setText(ChargingConstraint.getChargingStringFromValue(context, Integer.parseInt(getExtra(1))));
        } else {
            holder.constraint_charging_text.setVisibility(View.GONE);
        }
    }

    @SuppressLint("InlinedApi")
    private int getValueFromSpinner() {
        int position = ((Spinner) getBaseView().findViewById(R.id.charging_picker)).getSelectedItemPosition();

        switch (position) {
            case 0:
                return -1;
            case 1:
                return BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB;
            case 2:
                return BatteryManager.BATTERY_PLUGGED_AC;
            case 3:
                return BatteryManager.BATTERY_PLUGGED_USB;
            case 4:
                if (mIsPreJbMr1) {
                    return 0;
                } else {
                    return BatteryManager.BATTERY_PLUGGED_WIRELESS;
                }
            case 5:
                return 0;
            default:
                return -1;
        }
    }

    private int getChargingSpinnerPositionFromValue(int value) {
        switch (value) {
            case -1:
                return 0;
            case BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB:
                return 1;
            case BatteryManager.BATTERY_PLUGGED_AC:
                return 2;
            case BatteryManager.BATTERY_PLUGGED_USB:
                return 3;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return 4;
            case 0:
                return mIsPreJbMr1 ? 4 : 5;
            default:
                return 0;
        }


    }
}
