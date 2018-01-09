package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    public static final String EXTRA_24_HOUR = "24_hr";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        //setting the time format as millitary time format i.e 24hrs clock...
        boolean millitary_time = (getArguments() != null)  ? getArguments().getBoolean(EXTRA_24_HOUR, DateFormat.is24HourFormat(getActivity())) : DateFormat.is24HourFormat(getActivity());

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, millitary_time);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
    }
}