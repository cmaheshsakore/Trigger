package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;

import java.util.ArrayList;


public class TriggerConstraintFragment extends BaseFragment {

    protected ArrayList<Constraint> mOptions;
    protected ArrayList<Constraint> mOutput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trigger_constraint,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPosition = 1;
        super.onViewCreated(view, savedInstanceState);

        mOptions = Constraint.getAllConstraints(mTrigger.getType());

        // Load any incoming constraints for this trigger
        if (mTrigger.getConstraints().size() == 0) {
            mTrigger.loadConstraints(getActivity());
        }

        for (Constraint l: mOptions) {
            for (Constraint c: mTrigger.getConstraints()) {
                if (c.getType().equals(l.getType())) {
                    l.loadData(getActivity(), c);
                }
            }
        }

        LinearLayout container = ((LinearLayout) view.findViewById(R.id.choices));

        // Inject constraints into mBaseView view
        for (Constraint c: mOptions) {
            container.addView(c.getView(getActivity()));
        }
    }

    @Override
    protected void updateTriggerConstraints() {
        mOutput = new ArrayList<Constraint>();

        for (Constraint c: mOptions) {
            Constraint o = c.getConstraint(getActivity());
            if (o != null) {
                Logger.d("Adding constraint " + o.toString() + o.getClass());
                mOutput.add(o);
            }
        }

        mTrigger.setConstraints(mOutput);
    }

}
