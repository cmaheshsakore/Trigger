package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

public class BatteryTriggerFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_battery_trigger,  container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView text = (TextView) getView().findViewById(R.id.percentage);
        text.setText("100 %");
        SeekBar bar = (SeekBar) getView().findViewById(R.id.percentage_slider);
        bar.setMax(100);
        bar.setProgress(100);
        bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    text.setText(progress + " %");
                } catch (Exception e) {
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (DatabaseHelper.TRIGGER_BATTERY_GOES_ABOVE.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_above);
        }

        if (Task.isStringValid(mTrigger.getExtra(1))) {
            ((SeekBar) getView().findViewById(R.id.percentage_slider)).setProgress(Integer.parseInt(mTrigger.getExtra(1)));
            (getActivity().findViewById(R.id.button_next)).setEnabled(true);
        }
    }
    
    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.battery_task));
    }
    
    @Override
    protected void updateTrigger() {
        String condition = (((RadioGroup) getView().findViewById(R.id.condition)).getCheckedRadioButtonId() == R.id.radio_above) ? DatabaseHelper.TRIGGER_BATTERY_GOES_ABOVE
                : DatabaseHelper.TRIGGER_BATTERY_GOES_BELOW;

        String value = String.valueOf(((SeekBar) getView().findViewById(R.id.percentage_slider)).getProgress());

        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_BATTERY);
        mTrigger.setExtra(1, value);
        mTrigger.setExtra(2, "");
    }
}
