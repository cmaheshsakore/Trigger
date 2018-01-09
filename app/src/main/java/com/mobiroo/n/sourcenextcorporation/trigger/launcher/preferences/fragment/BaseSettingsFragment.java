package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import junit.framework.Assert;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class BaseSettingsFragment extends Fragment implements OnCheckedChangeListener {
    
    public static final String PREFS_NAME = SettingsHelper.PREFS_NAME;
    
    protected SharedPreferences mSharedPrefs;
    private View mView;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mView = view;
    }
    
    protected void setCheckChangedListener(int id, OnCheckedChangeListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ((SwitchCompat) mView.findViewById(id)).setOnCheckedChangeListener(listener);
        } else {
            ((CheckBox) mView.findViewById(id)).setOnCheckedChangeListener(listener);
        } 
    }
    
    protected void setChecked(int id, boolean checked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ((SwitchCompat) mView.findViewById(id)).setChecked(checked);
        } else {
            ((CheckBox) mView.findViewById(id)).setChecked(checked);
        }
    }
    
    protected boolean getChecked(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ((SwitchCompat) mView.findViewById(id)).isChecked();
        } else {
            return ((CheckBox) mView.findViewById(id)).isChecked();
        }
    }
    protected SharedPreferences getPrefs(Context context) {
        if (mSharedPrefs == null) {
            mSharedPrefs = context.getSharedPreferences(PREFS_NAME, 0);
        }
        return mSharedPrefs;
    }
    
    protected SharedPreferences.Editor getEditor(Context context) {
        mSharedPrefs = getPrefs(context);
        return mSharedPrefs.edit();
    }
    
    protected boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }
    
    protected boolean getBoolean(Context context, String key, boolean defValue) {
        return getPrefs(context).getBoolean(key, defValue);
    }
    
    protected int getInt(Context context, String key) {
        return getInt(context, key, 0);
    }
    
    protected int getInt(Context context, String key, int defValue) {
        return getPrefs(context).getInt(key, defValue);
    }
    
    protected String getString(Context context, String key) {
        return getString(context, key, "");
    }
    
    protected String getString(Context context, String key, String defValue) {
        return getPrefs(context).getString(key, defValue);
    }
    
    protected long getLong(Context context, String key) {
        return getLong(context, key, 0);
    }
    
    protected long getLong(Context context, String key, long defValue) {
        return getPrefs(context).getLong(key, defValue);
    }
    
    protected int getFragmentRequestCode(int requestCode) {
        return requestCode & 32767;
    }
    
    protected void updatePreferences() {
        try {
            SettingsHelper.loadPreferences(getActivity());
        } catch (Exception e) { }
        
        try {
            BackupManager.dataChanged(getActivity().getPackageName());
        } catch (Exception e) {
            
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Assert.fail("Fragments must implement their own handler");
    }
    
}
