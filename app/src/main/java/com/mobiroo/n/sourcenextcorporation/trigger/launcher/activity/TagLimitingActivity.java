package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TagLimitingActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "NFCTaskLauncherPrefs";
    private RadioGroup mGroup;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_rate_limit);

        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        mGroup = ((RadioGroup) findViewById(R.id.limiting_options));
        
        if (SettingsHelper.getPrefBool(TagLimitingActivity.this, Constants.PREF_CHECK_SEQUENTIAL, false)) {
            ((RadioButton) findViewById(R.id.ignore_duplicate_check)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.ignore_time_check)).setChecked(true);
        }
        setUpTimeLimit();
        setUpLimitOptionsSpinner();
        setUpOverrideCheck();
        
        ((Button) findViewById(R.id.cancel_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setRateLimitPref(false, false);
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        
        ((Button) findViewById(R.id.ok_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (mGroup.getCheckedRadioButtonId() == R.id.ignore_duplicate_check) {
                    setRateLimitPref(true, true);
                } else {
                    setRateLimitPref(true, false);
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    
    private void setUpLimitOptionsSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.limitThreshholdSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.limitThreshholdChoices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        String prefUnits = SettingsHelper.getPrefString(this, Constants.PREF_REPEAT_THRESHHOLD_UNITS);
        int spinnerPos = adapter.getPosition(prefUnits);
        spinner.setSelection(spinnerPos);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Your code here
                String units = "Seconds";
                switch (i) {
                    case 0:
                        units = "Seconds";
                        break;
                    case 1:
                        units = "Minutes";
                        break;
                    case 2:
                        units = "Hours";
                        break;
                }
                Logger.i("Setting units to " + units);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(Constants.PREF_REPEAT_THRESHHOLD_UNITS, units);
                editor.commit();

            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
    
    private void setUpOverrideCheck() {
        boolean allowOverride = SettingsHelper.getPrefBool(getBaseContext(), Constants.PREF_OVERRIDE_LIMIT, false);
        CheckBox check = (CheckBox) findViewById(R.id.limitThreshholdCheck);
        check.setChecked(allowOverride);
        check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.PREF_OVERRIDE_LIMIT, isChecked);
                editor.commit();
                BackupManager.dataChanged(getPackageName());
            }
        });
    }
    private void setUpTimeLimit() {
        int currentTimeLimit = SettingsHelper.getPrefInt(TagLimitingActivity.this, Constants.PREF_REPEAT_THRESHHOLD, 30);
        EditText et = (EditText) findViewById(R.id.limitThreshhold);
        et.setText(String.valueOf(currentTimeLimit));
        et.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                int tTime = 30;
                if (!(value.equals(""))) {
                    try {
                        tTime = Integer.parseInt(value);
                    } catch (Exception e) {
                        tTime = 30;
                    }
                }
                Logger.i("Setting thresh to " + tTime);
                SettingsHelper.setPrefInt(TagLimitingActivity.this, Constants.PREF_REPEAT_THRESHHOLD, tTime);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }
    
    private void setRateLimitPref(boolean enable, boolean repeat) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.PREF_CHECK_REPEAT, enable);
        editor.putBoolean(Constants.PREF_CHECK_SEQUENTIAL, repeat);
        editor.commit();
    }
}
