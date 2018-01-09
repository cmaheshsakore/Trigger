package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.PreferenceCategory;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.SettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.MainSettingsFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment mFragment;
    private FragmentManager     mManager;
    private String              mTag;
    private boolean             mIsPhoneLayout;

    public static final int     REQUEST_SETTINGS_SUBSCREEN = 10001;
    public Toolbar mToolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Grab Toolbar and set home as up enabled
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_navigation_arrow_back_black);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra(SettingsFragment.KEY_CATEGORY)) {
            PreferenceCategory item = (PreferenceCategory) intent.getParcelableExtra(SettingsFragment.KEY_CATEGORY);
            mToolbar.setTitle(item.getName());
            mTag = getString(item.getName());
            addFragment(item.getFragment());
        } else {
            if (Utils.getWidthInDp(this) < 800) {
                /* Only load the menu fragment for phone layouts */
                mIsPhoneLayout = false;
                addFragment(new MainSettingsFragment());
            } else {
                mIsPhoneLayout = true;
            }
            if (savedInstanceState == null) {

                mToolbar.setTitle(R.string.layoutPreferencesTitleText);
            }
        }
    }

    public void addFragment(SettingsFragment fragment) {
        addFragment(fragment, mTag);
    }

    public void addFragment(SettingsFragment fragment, String tag) {
        
        FragmentTransaction transaction = getMyFragmentManager().beginTransaction();
        mTag = tag;
        SettingsFragment frag = getFragment(tag);
        if (frag == null) {
            transaction.replace(R.id.fragment, fragment, mTag);
        } else {
            transaction.replace(R.id.fragment, frag, mTag);
        }
        
        /*
        if (!mIsPhoneLayout) {
            transaction.addToBackStack(tag);
        }*/

        transaction.commit();
    }
    
    public FragmentManager getMyFragmentManager() {
        if (mManager == null) {
            mManager = getSupportFragmentManager();
        }
        return mManager;
        
    }
    
    public SettingsFragment getFragment(String tag) {
        return (SettingsFragment) getMyFragmentManager().findFragmentByTag(tag);
    }
    
    private SettingsFragment getFragment() {

        mFragment = (SettingsFragment) getMyFragmentManager().findFragmentByTag(mTag);

        if (mFragment == null) {
            mFragment = new SettingsFragment();
        }

        return mFragment; 
    }

    public void preferenceClicked(View v) {
        mFragment = getFragment();
        mFragment.onClick(v);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SETTINGS_SUBSCREEN) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode);
                finish();
            }
        } else {
            mFragment = getFragment();
            mFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return false;
        }
    }

}
