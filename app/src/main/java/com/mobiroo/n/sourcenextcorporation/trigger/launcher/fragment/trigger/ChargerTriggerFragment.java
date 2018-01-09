package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

public class ChargerTriggerFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_charger_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (DatabaseHelper.TRIGGER_CHARGER_ON.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_connect);
        } else if (DatabaseHelper.TRIGGER_WIRELESS_CHARGER.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_connect_wireless);
        } else if (DatabaseHelper.TRIGGER_CHARGER_OFF.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_disconnect);
        } else if (DatabaseHelper.TRIGGER_CHARGER_ON_AC.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_connect_ac);
        } else if (DatabaseHelper.TRIGGER_CHARGER_ON_USB.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_connect_usb);
        } 

        //removing the wireless charging option for the systems
        //which run on below jellybean version...
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Wireless charging extra is not supported, hide
            (getView().findViewById(R.id.radio_condition_connect_wireless)).setVisibility(View.GONE);
        }
    }

    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.charger_title));
    }

    @Override
    protected void updateTrigger() {
        String condition = DatabaseHelper.TRIGGER_CHARGER_ON;

        switch (((RadioGroup) getView().findViewById(R.id.condition)).getCheckedRadioButtonId()) {
            case R.id.radio_condition_connect_wireless:
                condition = DatabaseHelper.TRIGGER_WIRELESS_CHARGER;
                break;
            case R.id.radio_condition_disconnect:
                condition = DatabaseHelper.TRIGGER_CHARGER_OFF;
                break;
            case R.id.radio_condition_connect_usb:
                condition = DatabaseHelper.TRIGGER_CHARGER_ON_USB;
                break;
            case R.id.radio_condition_connect_ac:
                condition = DatabaseHelper.TRIGGER_CHARGER_ON_AC;
                break;
            case R.id.radio_condition_connect:
            default:
                condition = DatabaseHelper.TRIGGER_CHARGER_ON;
                break;

        }

        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_CHARGER);
        mTrigger.setExtra(1, "");;
        mTrigger.setExtra(2, "");
    }

}
