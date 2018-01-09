package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.TimePickerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TimeTask;

import java.util.Calendar;
import java.util.Date;

public class TimeTriggerFragment extends BaseFragment {
    
    private SparseIntArray mDaysSelected;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_time_trigger,  container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mDaysSelected = new SparseIntArray();
        ((TextView) getView().findViewById(R.id.time)).setText(TimeTask.time_12.format(Calendar.getInstance().getTime()));
        
        
        getView().findViewById(R.id.time).setOnClickListener(timeClicked);
        getView().findViewById(R.id.sunday).setOnClickListener(dayClicked);
        getView().findViewById(R.id.monday).setOnClickListener(dayClicked);
        getView().findViewById(R.id.tuesday).setOnClickListener(dayClicked);
        getView().findViewById(R.id.wednesday).setOnClickListener(dayClicked);
        getView().findViewById(R.id.thursday).setOnClickListener(dayClicked);
        getView().findViewById(R.id.friday).setOnClickListener(dayClicked);
        getView().findViewById(R.id.saturday).setOnClickListener(dayClicked);
        
        if (Task.isStringValid(mTrigger.getExtra(1))) {
            Logger.d("Formatting " + mTrigger.getExtra(1));
            try {
                Date t = TimeTask.time_24.parse(mTrigger.getExtra(1));
                ((TextView) getView().findViewById(R.id.time)).setText(TimeTask.time_12.format(t));
            } catch (Exception e) {
                ((TextView) getView().findViewById(R.id.time)).setText(mTrigger.getExtra(1));
            }

        }
        
        if (Task.isStringValid(mTrigger.getExtra(2))) {
            
            String days_string = mTrigger.getExtra(2);
            String[] days = days_string.split(",");
            for (int i=0; i< days.length; i++) {
                try {
                    addDay(Integer.parseInt(days[i]));
                } catch (Exception e) {
                    addDay(days[i]);
                }

            }
        }
        
        (getActivity().findViewById(R.id.button_next)).setEnabled((mDaysSelected.size() > 0) ? true : false);
    }
    
    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.time_task));
    }

    private void addDay(int day) {
        TextView t = null;

        switch(day) {
            case Calendar.MONDAY:
                t = (TextView) getView().findViewById(R.id.monday);
                break;
            case Calendar.TUESDAY:
                t = (TextView) getView().findViewById(R.id.tuesday);
                break;
            case Calendar.WEDNESDAY:
                t = (TextView) getView().findViewById(R.id.wednesday);
                break;
            case Calendar.THURSDAY:
                t = (TextView) getView().findViewById(R.id.thursday);
                break;
            case Calendar.FRIDAY:
                t = (TextView) getView().findViewById(R.id.friday);
                break;
            case Calendar.SATURDAY:
                t = (TextView) getView().findViewById(R.id.saturday);
                break;
            case Calendar.SUNDAY:
                t = (TextView) getView().findViewById(R.id.sunday);
                break;
        }

        if (t != null) {
            mDaysSelected.put(day, day);
            t.setTextColor(getResources().getColor(R.color.time_selected));
            t.setTypeface(null, Typeface.BOLD);
        }
    }

    // Support legacy entries
    private void addDay(String day) {
        TextView t = null;
        
        if (getString(R.string.day_sunday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.sunday);
        } else if (getString(R.string.day_monday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.monday);
        } else if (getString(R.string.day_tuesday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.tuesday);
        } else if (getString(R.string.day_wednesday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.wednesday);
        } else if (getString(R.string.day_thursday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.thursday);
        } else if (getString(R.string.day_friday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.friday);
        } else if (getString(R.string.day_saturday).equals(day)) {
            t = (TextView) getView().findViewById(R.id.saturday);
        }
        
        if (t != null) {
            int pending = getDayFromView(t);
            mDaysSelected.put(pending, pending);
            t.setTextColor(getResources().getColor(R.color.time_selected));
            t.setTypeface(null, Typeface.BOLD);
        }
    }
    
    private OnClickListener dayClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int day = getDayFromView(v);

            if (mDaysSelected.get(day, -1) != -1) {
                mDaysSelected.delete(day);
                ((TextView) v).setTextColor(getResources().getColor(R.color.time_unselected));
                ((TextView) v).setTypeface(null, Typeface.NORMAL);
            } else {
                mDaysSelected.put(day, day);
                ((TextView) v).setTextColor(getResources().getColor(R.color.time_selected));
                ((TextView) v).setTypeface(null, Typeface.BOLD);
            }
            
            getActivity().findViewById(R.id.button_next).setEnabled((mDaysSelected.size() > 0) ? true : false);
        }
    };

    private int getDayFromView(View v) {

        switch (v.getId()) {
            case R.id.monday:
                return Calendar.MONDAY;
            case R.id.tuesday:
                return Calendar.TUESDAY;
            case R.id.wednesday:
                return Calendar.WEDNESDAY;
            case R.id.thursday:
                return Calendar.THURSDAY;
            case R.id.friday:
                return Calendar.FRIDAY;
            case R.id.saturday:
                return Calendar.SATURDAY;
            case R.id.sunday:
                return Calendar.SUNDAY;
            default:
                return -1;
        }
    }

    private OnClickListener timeClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final TextView time = (TextView) v;
            DialogFragment newFragment = new TimePickerFragment() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String hours = (hourOfDay < 10) ? "0" + hourOfDay : String.valueOf(hourOfDay);
                    String minutes = (minute < 10) ? "0" + minute : String.valueOf(minute);
                    try {
                        Date t = TimeTask.time_24.parse(hours + ":" + minutes);
                        time.setText(TimeTask.time_12.format(t));
                    } catch (Exception e) {
                        time.setText(hours + ":" + minutes);
                    }


                }
            };
            newFragment.show(getFragmentManager(), "timePicker");
        }
    };
    
    @Override
    protected void updateTrigger() {
        mTrigger.setCondition(DatabaseHelper.TRIGGER_TIME);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_TIME);
        String time = ((TextView) getView().findViewById(R.id.time)).getText().toString();
        try {
            Date t = TimeTask.time_12.parse(time);
            mTrigger.setExtra(1, TimeTask.time_24.format(t));
        } catch (Exception e) {
            mTrigger.setExtra(1, time);
        }
        StringBuilder b = new StringBuilder();
        for (int i=0; i<mDaysSelected.size(); i++) {
            int day = mDaysSelected.valueAt(i);
            if (i > 0) {
                b.append(",");
            }
            b.append(day);
        }
        Logger.d("Adding days " + b.toString());
        mTrigger.setExtra(2, b.toString());
    }

}
