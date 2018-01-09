package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TriggerDataChangedListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TriggerWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TriggerStateListener;

import junit.framework.Assert;

import java.util.ArrayList;

public class BaseFragment extends Fragment {

    protected int           mTaskType = TaskTypeItem.TASK_TYPE_BLUETOOTH;
    protected Bundle        mArgs;
    protected Trigger       mTrigger;
    
    protected boolean       mIsKey1Valid = false;
    protected boolean       mIsKey2Valid = false;
    
    protected ProgressDialog mDialog;
    
    protected TriggerDataChangedListener mListener;
    
    protected int           mPosition = 0;
    public void setListener(TriggerDataChangedListener mDataChangedListener) {
        mListener = mDataChangedListener;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Assert.fail("Must implement a subclass of BaseFragment");
        return null;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TriggerWizardActivity) getActivity()).addListener(new StateListener(), mPosition);
        
        mArgs = getArguments();
        getDataFromArgs(mArgs);

    }
    
    @Override 
    public void onPause() {
        super.onPause();
        if ((mDialog != null) && (mDialog.isShowing())) {
            mDialog.dismiss();
        }
    }
    
    protected void getDataFromArgs(Bundle args) {
        if (args == null) { return; }

        if (mArgs.containsKey(Trigger.EXTRA_TRIGGER)) {
            Logger.d("Received trigger");
            mTrigger = (Trigger) mArgs.get(Trigger.EXTRA_TRIGGER);
        }
        
        if (mArgs.containsKey(TaskTypeItem.EXTRA_TASK_TYPE)) {
            mTaskType = mArgs.getInt(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_BLUETOOTH);
            if (mTrigger == null) {
                mTrigger = new Trigger(mTaskType, null, null, null);
            }
            Logger.d("Received task type " +  mTaskType);
        }
        
    }
    
    protected void assignDataFromArgs() {}
    
    protected void setupOnClickListeners() {}
    
    protected void loadSetupData() {}
    
    protected void populateFieldsFromArgs() {}
    
    public String getTitle() {
        return getActivity().getString(R.string.configure_connection_task_title);
    }
   
    protected boolean checkStatus() {
        return (mIsKey1Valid || mIsKey2Valid);
    }
    
    protected void cancelActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }
    
    protected TextWatcher mPrimaryWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mIsKey1Valid = (count != 0);
            checkStatus();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    protected TextWatcher mSecondaryWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mIsKey2Valid = (count != 0);
            checkStatus();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    
    protected void updateTriggerConstraints() {
    }

    public boolean useConstraints() {
        return true;
    }

    protected void updateTrigger() {
        Assert.fail("Sub classes must override updateTrigger");
    }
    
    public class StateListener implements TriggerStateListener {

        @Override
        public ArrayList<Constraint> getUpdatedConstraints() {
            updateTriggerConstraints();
            return mTrigger.getConstraints();
        }

        @Override
        public Trigger getUpdatedTrigger() {
            updateTrigger();
            return mTrigger;
        }
        
    }
}
