package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import java.io.File;

public class AdvancedSettingsFragment extends SettingsFragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings_advanced,  null);
        return v;
    }

    @Override
    protected void setupClickHandlers() {
        setCheckChangedListener(R.id.prefDebug, this);
        setCheckChangedListener(R.id.prefAnalytics, this);
    }

    @Override
    protected void loadSettings() {
        setChecked(R.id.prefDebug, getBoolean(getActivity(), Constants.PREF_DEBUGGING, false));
        setChecked(R.id.prefAnalytics, getBoolean(getActivity(), Constants.PREF_USE_ANALYTICS, true));
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.prefDebugSendContainer:
                sendDebugLog();
                break;
            case R.id.requestRoot:
                try {
                    Utils.requestWriteSecureSettings();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Could not get root access.  Exception thrown", Toast.LENGTH_LONG).show();
                    Logger.e("Exception getting root access: " + e, e);
                }
                break;
        }
        
        updatePreferences();
        
    }
    
    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        switch(v.getId()) {
            case R.id.prefDebug:
                getEditor(getActivity()).putBoolean(Constants.PREF_DEBUGGING, isChecked).commit();
                if (!isChecked) {
                    deleteLogFile();
                }
                break;
            case R.id.prefAnalytics:
                if (!isChecked) {
                    Usage.logAnalyticsRemoved(getActivity());
                }
                getEditor(getActivity()).putBoolean(Constants.PREF_USE_ANALYTICS, isChecked).commit();
                break;
        }
        
        updatePreferences();
    }
    
    private void deleteLogFile() {
        File root = Environment.getExternalStorageDirectory();
        File container = new File(root.getPath() + "/" + Logger.DIR_NAME + "/");
        final File log = new File(container, Logger.FILE_NAME);
        if (log.exists()) {
            try {
                log.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendDebugLog() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        // Intent.EXTRA_EMAIL
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { BuildConfiguration.FEEDBACK_EMAIL_ADDRESS});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Trigger Debug Log");
        try {
            intent.putExtra(Intent.EXTRA_TEXT, String.format("Version: %s\nOS: %s\nModel: %s\nDevice: %s\nManufacturer: %s\n\n", getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName, Build.VERSION.RELEASE, Build.MODEL, Build.DEVICE, Build.MANUFACTURER));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Add Attachment

        try {
            File root = Environment.getExternalStorageDirectory();
            File container = new File(root.getPath() + "/" + Logger.DIR_NAME + "/");
            final File log = new File(container, Logger.FILE_NAME);
            if (!log.exists() || !log.canRead()) {
                Toast.makeText(getActivity(), "Log does not exist or cannot be read", Toast.LENGTH_LONG).show();
            } else {
                Logger.i("Attaching " + log);
                Uri uri = Uri.parse("file://" + log);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(Intent.createChooser(intent, getString(R.string.menu_chooser_mail)));
    }
    
}
