package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.PreferenceCategory;

import junit.framework.Assert;

public class SettingsFragment extends BaseSettingsFragment implements SettingsInterface {

    public static final String KEY_CATEGORY = "com.trigger.launcher.settings.KEY_CATEGORY";
    
    protected Bundle                mArguments;
    protected PreferenceCategory    mCategory;
    protected View                  mView;
    protected Context               mContext;
    protected boolean               mIsAttached;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mContext = getActivity();
        loadSettings();
        setupClickHandlers();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mContext = getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIsAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
    }

    @Override
    public void onClick(View v) {
        Assert.fail("Class must override onClick");
    }
    
    protected void setupClickHandlers() {
        Assert.fail("Class must override click handlers");
    }
    protected void loadSettings() {
        Assert.fail("Class must override load settings");
    }
}
