package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.AgentAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.ArrayList;

public class AgentTriggerFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_agent_trigger, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<AgentAction.AgentListItem> agents = AgentAction.generateAgentList(getActivity());
        ArrayAdapter<AgentAction.AgentListItem> adapter
                = new ArrayAdapter<AgentAction.AgentListItem>(getActivity(), R.layout.list_item_single, agents) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = View.inflate(getActivity(), R.layout.list_item_single, null);
                        }
                        AgentAction.AgentListItem i = getItem(position);
                        ((TextView) convertView.findViewById(R.id.row1Text)).setText(i.getName());
                        return convertView;
                    }
                };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        if (Task.isStringValid(mTrigger.getExtra(1))) {
            for (int i = 0; i < agents.size(); i++) {
                AgentAction.AgentListItem item = agents.get(i);
                if (item.getGuid().equals(mTrigger.getExtra(1))) {
                    spinner.setSelection(i);
                }
            }
        }
        if (DatabaseHelper.TRIGGER_AGENT_ENDS.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.option_starts);
        }
    }

    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.menu_agent));
    }

    @Override
    protected void updateTrigger() {
        String condition = (((RadioGroup) getView().findViewById(R.id.condition)).getCheckedRadioButtonId() == R.id.option_starts)
                ? DatabaseHelper.TRIGGER_AGENT_STARTS
                : DatabaseHelper.TRIGGER_AGENT_ENDS;

        Spinner spinner = (Spinner) getView().findViewById(R.id.spinner);
        AgentAction.AgentListItem item = (AgentAction.AgentListItem) spinner.getSelectedItem();

        Logger.d("Selected item is " + item.getName() + ", " + item.getGuid());

        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_AGENT);
        mTrigger.setExtra(1, item.getGuid());
        mTrigger.setExtra(2, item.getName());
    }

}
