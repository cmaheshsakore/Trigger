package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.EventConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.CalendarTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;

import org.json.JSONException;

import java.util.ArrayList;

public class CalendarTriggerFragment extends BaseFragment {

    ArrayAdapter<String> mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_calendar_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = (Spinner) getView().findViewById(R.id.account);
        spinner.setAdapter(createAccountsAdapter());

        String extra = mTrigger.getExtra(1);

        ((TextInputLayout) getView().findViewById(R.id.name)).setHint(view.getContext().getString(R.string.optionsFoursquareSearchVenue));
        ((TextInputLayout) getView().findViewById(R.id.description)).setHint(view.getContext().getString(R.string.description));

        if ((extra != null) && !extra.isEmpty()) {
            try {
                EventConfiguration config = EventConfiguration.deserializeExtra(extra);
                updateView(config);
            }
            catch (Exception e) {
                Logger.e("Exception building config from extras: " + e, e);
            }
        }
    }

    private void updateView(EventConfiguration config) {

        if (!config.name.isEmpty()) {
            ((TextInputLayout) getView().findViewById(R.id.name)).getEditText().setText(config.name);
        }

        if (!config.description.isEmpty()) {
            ((TextInputLayout) getView().findViewById(R.id.description)).getEditText().setText(config.description);
        }

        ((RadioGroup) getView().findViewById(R.id.time_option)).check(
                mTrigger.getCondition().equals(DatabaseHelper.TRIGGER_CALENDAR_END)
                        ? R.id.option_ends: R.id.option_starts);

        // Setup radio buttons
        int value = config.name_match_type;
        ((RadioGroup) getView().findViewById(R.id.name_option)).check(
                value == EventConfiguration.MATCH_TYPE_CONTAINS ? R.id.option_contains
                        : R.id.option_matches);

        value = config.description_match_type;
        ((RadioGroup) getView().findViewById(R.id.description_option)).check(
                value == EventConfiguration.MATCH_TYPE_CONTAINS ? R.id.option_contains
                        : R.id.option_matches);

        Spinner availability = (Spinner) getView().findViewById(R.id.availability);
        switch(config.availability) {
            case EventConfiguration.BUSY:
                availability.setSelection(1);
                break;
            case EventConfiguration.FREE:
                availability.setSelection(2);
                break;
            default:
                availability.setSelection(0);
        }

        // Select account
        Spinner spinner = (Spinner) getView().findViewById(R.id.account);
        if (!config.account.equals(getString(R.string.any))) {
            try {
                int position = mAdapter.getPosition(config.account);
                spinner.setSelection(position);
            } catch (Exception e) {
                Logger.e("Exception setting selected account: "  + e, e);
            }
        }
    }

    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.calendar_title));
    }

    @Override
    protected void updateTrigger() {
        String condition = ((RadioGroup) getView().findViewById(R.id.time_option)).getCheckedRadioButtonId() == R.id.option_starts
                ? DatabaseHelper.TRIGGER_CALENDAR_START : DatabaseHelper.TRIGGER_CALENDAR_END;

        String extra = "";
        try {
            extra = CalendarTrigger.serializeExtraFromView(getView());
        } catch (JSONException e) { Logger.e("Couldn't create data string for extras: " + e, e); }

        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_CALENDAR);
        mTrigger.setExtra(1, extra);
        mTrigger.setExtra(2, "");
    }

    private ArrayAdapter<String> createAccountsAdapter() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(getString(R.string.any));

        for (String s: CalendarTrigger.getAccounts(getActivity())) {
            list.add(s);
        }

        mAdapter =  new ArrayAdapter<String>(getActivity(), R.layout.spinner_app_list, list);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return mAdapter;
    }

}
