package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TaskWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TaskDataChangedListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TaskStateListener;

import java.util.ArrayList;

public class TaskBuilderBaseFragment extends Fragment implements TaskStateListener {

    protected TaskDataChangedListener   mDataChangedListener;
    protected int                       mListenerTag;
    protected View                      mEmpty;
    protected boolean                   mShowHelp = true;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && (savedInstanceState.containsKey("tag"))) {
            mListenerTag = savedInstanceState.getInt("tag");
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDataChangedListener = (TaskDataChangedListener) activity;
        } catch (Exception e) {
            Logger.e("Exception binding to listener in onAttach: " + e, e);
        }
        
        ((TaskWizardActivity) getActivity()).addListener(this, mListenerTag);
    }
    
    public void setTag(int tag) {
        mListenerTag = tag;
    }
   
    public int getListenerTag() {
        return mListenerTag;
    }
   
    
    protected void hideHelp() {
        mShowHelp = false;
        changeHelp(View.GONE);
    }
    
    protected void showHelp() {
        mShowHelp = true;
        changeHelp(View.VISIBLE);
    }
    
    protected void changeHelp(int mode) {
        //mEmpty.setVisibility(View.VISIBLE); //mode);
    }
    
    protected void updateHelpDisplay() {
        if (mEmpty != null) {
            mShowHelp = !mShowHelp;
           changeHelp(
                   (mEmpty.getVisibility() == View.VISIBLE)
                       ? View.GONE
                       : View.VISIBLE);
        }
    }
    
    @Override
    public boolean isRequestPending() {
        return false;
    }
    
    @Override
    public Task getTask(int which) {
        return null;
    }

    @Override
    public ArrayList<Trigger> getTriggers() {
        return null;
    }

    @Override
    public void notifyResult(int requestCode, int resultCode, Intent data) {
        
    }
    
    @Override
    public void toggleHelpState(int position) {
        Logger.d("Toggling help for " + mListenerTag + " at " + position);
        if (position == mListenerTag) {
            updateHelpDisplay();
        }
    }

    @Override
    public void addClicked(int position) {
    }

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Received Request code " + requestCode);
    }
    
   
}
