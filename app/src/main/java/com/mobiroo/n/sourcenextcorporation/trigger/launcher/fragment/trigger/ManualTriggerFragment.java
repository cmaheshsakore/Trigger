package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

/**
 * Created by krohnjw on 5/8/2014.
 */
public class ManualTriggerFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_manual_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateTrigger() {
        mTrigger.setCondition(DatabaseHelper.TRIGGER_NO_CONDITION);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_MANUAL);
    }

    @Override
    public boolean useConstraints() {
        return false;
    }
}
