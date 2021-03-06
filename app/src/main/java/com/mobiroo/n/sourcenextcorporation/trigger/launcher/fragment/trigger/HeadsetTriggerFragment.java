package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

public class HeadsetTriggerFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_headset_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (DatabaseHelper.TRIGGER_ON_CONNECT.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_connect);
        } else if (DatabaseHelper.TRIGGER_ON_DISCONNECT.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_condition_disconnect);
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
            case R.id.radio_condition_disconnect:
                condition = DatabaseHelper.TRIGGER_ON_DISCONNECT;
                break;
            case R.id.radio_condition_connect:
            default:
                condition = DatabaseHelper.TRIGGER_ON_CONNECT;
                break;

        }

        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_HEADSET);
        mTrigger.setExtra(1, "");
        mTrigger.setExtra(2, "");
    }

}
