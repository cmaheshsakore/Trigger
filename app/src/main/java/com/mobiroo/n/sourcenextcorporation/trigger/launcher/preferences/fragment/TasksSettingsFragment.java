package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TagLimitingActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import java.io.IOException;

public class TasksSettingsFragment extends SettingsFragment {

    public static final boolean RATE_LIMITING_DEFAULT = false;
    public static final boolean SEQUENTIAL_CHECK_DEFAULT = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_tasks,  null);
    }

    @Override
    protected void setupClickHandlers() {
        setCheckChangedListener(R.id.prefExpireSwitchCheckbox, this);
        setCheckChangedListener(R.id.prefNFCAirplaneCheckbox, this);
        setCheckChangedListener(R.id.prefShowBytesCheckbox, this);
    }

    @Override
    protected void loadSettings() {
        setChecked((mView.findViewById(R.id.prefExpireSwitchCheckbox)).getId(), getBoolean(getActivity(), Constants.PREF_EXPIRE_SWITCH, false));
        setChecked((mView.findViewById(R.id.prefNFCAirplaneCheckbox)).getId(), !isNFCToggledInAirplaneMode());
        setChecked((mView.findViewById(R.id.prefShowBytesCheckbox)).getId(), getBoolean(getActivity(), Constants.PREF_SHOW_TASK_SIZE, false));
        
        updateLimitDisplay();
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.LimitingContainer:
                startActivityForResult(new Intent(getActivity(), TagLimitingActivity.class), 1);
                updateLimitDisplay();
                break;
        }
        
        updatePreferences();
        
    }
    
    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        switch(v.getId()) {
            case R.id.prefExpireSwitchCheckbox:
                setChecked(v.getId(), isChecked); 
                getEditor(getActivity()).putBoolean(Constants.PREF_EXPIRE_SWITCH, isChecked).commit();
                break;
            case R.id.prefNFCAirplaneCheckbox:
                setChecked(v.getId(), isChecked); 
                handleNfcInAirplaneMode(v);
                break;
            case R.id.prefShowBytesCheckbox:
                setChecked(v.getId(), isChecked);
                getEditor(getActivity()).putBoolean(Constants.PREF_SHOW_TASK_SIZE, isChecked).commit();
                break;
                
        }
        
        updatePreferences();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateLimitDisplay();
    }
    
    private void updateLimitDisplay() {
        boolean enabled = getBoolean(mContext, Constants.PREF_CHECK_REPEAT, RATE_LIMITING_DEFAULT) 
                || getBoolean(mContext, Constants.PREF_CHECK_SEQUENTIAL, SEQUENTIAL_CHECK_DEFAULT);
                
        if (enabled) {
            boolean checkSequential = getBoolean(mContext, Constants.PREF_CHECK_SEQUENTIAL, false);
            if (checkSequential) {
                ((TextView) mView.findViewById(R.id.prefLimitingSubText)).setText(getString(R.string.limit_last_tag_read));
            } else {
                // Set preference screen text appropriately
                String units = getString(mContext, Constants.PREF_REPEAT_THRESHHOLD_UNITS, "minutes");
                int time = getInt(mContext, Constants.PREF_REPEAT_THRESHHOLD, 30);
                ((TextView) mView.findViewById(R.id.prefLimitingSubText)).setText(String.format(getString(R.string.limit_time_frame), time, units.toLowerCase()));
            }
        } else {
            ((TextView) mView.findViewById(R.id.prefLimitingSubText)).setText(getString(R.string.layoutPreferencesEnableLimitingSub));
        }
    }
    
    private void handleNfcInAirplaneMode(View v) {
        boolean showDialog = false;
        boolean isChecked = getChecked(v.getId());
        if (Build.VERSION.SDK_INT < 17) {
            if (isChecked)  {
                removeNFCFromAirplaneModeRadios();
            } else {
                addNFCtoAirplaneModeRadios();
            }
            showDialog = true;
        } else {
            PackageManager pm = mContext.getPackageManager();
            if ((pm.checkPermission(permission.WRITE_SECURE_SETTINGS, mContext.getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
                if (isChecked)  {
                    removeNFCFromAirplaneModeRadios();
                } else {
                    addNFCtoAirplaneModeRadios();
                }
                showDialog = true;
            } else {
                // Display that we can't get the appropriate permission
                final boolean settingChecked = isChecked;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.alertSecureSettingTitle));
                builder.setMessage(getString(R.string.alertSecureSetting));
                builder.setPositiveButton(getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {   
                        if (Utils.isRootPresent()) {
                            Logger.i("Root is present");
                            new AirplaneModeRootTask().execute(settingChecked);

                        } else {
                            Logger.i("Root is not present");
                            Toast.makeText(mContext, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        }
        if (showDialog) {
            showDialog(getString(R.string.rebootTitle), getString(R.string.rebootText), getString(R.string.dialogOK));
        }
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) @SuppressWarnings("deprecation")
    private String getAirplaneModeRadios() {
        return (Build.VERSION.SDK_INT < 17) ? Settings.System.getString(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_RADIOS)
                : Settings.Global.getString(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_RADIOS);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) @SuppressWarnings("deprecation")
    private String getRadioNfc() {
        return (Build.VERSION.SDK_INT < 17) ? Settings.System.RADIO_NFC : Settings.Global.RADIO_NFC;
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) @SuppressWarnings("deprecation")
    private String getAirplaneModeRadiosKey() {
        return (Build.VERSION.SDK_INT < 17) ? Settings.System.AIRPLANE_MODE_RADIOS : Settings.Global.AIRPLANE_MODE_RADIOS;
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void putAirplaneModeRadios(String value) {

        boolean set = true;
        
        PackageManager pm = mContext.getPackageManager();
        set = (pm.checkPermission(permission.WRITE_SECURE_SETTINGS, mContext.getPackageName()) == PackageManager.PERMISSION_GRANTED);
        
        if (!set && (Build.VERSION.SDK_INT >= 16)) {
            Logger.d("Permission not granted, requesting");
            try {
                Utils.requestWriteSecureSettings();
            } catch (Exception e) {
                Logger.e("Exception requesting permission", e);
            }
        } else {
            Logger.i("Permission is granted");
        }
        
        if (set) {
            Logger.i("Putting value " + value);
            Logger.d("Putting in " + getAirplaneModeRadiosKey());
            Logger.d("Build is " + Build.VERSION.SDK_INT);
            if (Build.VERSION.SDK_INT < 17) {
                
                Settings.System.putString(mContext.getContentResolver(), getAirplaneModeRadiosKey(), value);
            } else {
                Settings.Global.putString(mContext.getContentResolver(), getAirplaneModeRadiosKey(), value);
            }
            
        }
        
    }
    
    private boolean isNFCToggledInAirplaneMode() {
        String radios = getAirplaneModeRadios();
        String radio_var = getRadioNfc();
        return radios != null && radio_var != null && radios.contains(radio_var);
    }

    private void removeNFCFromAirplaneModeRadios() {

        String radios = getAirplaneModeRadios();
        String radio_var = getRadioNfc();

        if (radios.contains(radio_var)) {
            radios = radios.replace(radio_var, "");
            radios = radios.replace(",,", ",");
            // Check for trailing comma and remove
            try {
                if ((radios.length() > 0) && (radios.charAt(radios.length() - 1) == ',')) {
                    radios = radios.substring(0, radios.length() - 1);
                }
            } catch (Exception e) { /* fail silently */ }

        }
        putAirplaneModeRadios(radios);
    }

    private void addNFCtoAirplaneModeRadios() {

        String radios = getAirplaneModeRadios();
        String radio_var = getRadioNfc();

        if (radios.indexOf(radio_var) <= 0)
            radios += "," + radio_var;

        putAirplaneModeRadios(radios);

    }
    
    private class AirplaneModeRootTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... args) {
            try {
                Logger.d("Trying to grant permission");
                Utils.requestWriteSecureSettings();
                if (!args[0]) {
                    removeNFCFromAirplaneModeRadios();
                } else {
                    addNFCtoAirplaneModeRadios();
                }
                if (mIsAttached) {
                    showDialog(getString(R.string.rebootTitle), getString(R.string.rebootText), getString(R.string.dialogOK));
                }
            } catch (IOException e) {
                Logger.e("Could not get root access");
            } catch (InterruptedException e) {
                Logger.e("Could not get root access");
            }
            return null;
        }
    }

    public void showDialog(String title, String message, String buttonText) {
        SimpleDialogFragment dialog = new SimpleDialogFragment();
        if (title != null) {
            dialog.setTitle(title);
        }
        if (message != null) {
            dialog.setMessage(message);
        }
        if (buttonText != null) {
            dialog.setPositiveButton(buttonText, dialog.dismissListener);
        }

        try {
            dialog.show(getFragmentManager(), "dialog");
        } catch (Exception ignored) {

        }

    }
    
    
}
