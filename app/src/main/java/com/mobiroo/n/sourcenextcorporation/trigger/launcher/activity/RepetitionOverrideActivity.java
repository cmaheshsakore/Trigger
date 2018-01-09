package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import java.util.Timer;
import java.util.TimerTask;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class RepetitionOverrideActivity extends Activity {

    private String mPayload;
    private Timer mTimer;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_override_dialog);

        mPayload = getIntent().getStringExtra(ParserService.EXTRA_PAYLOAD);

        setText();
        setupClickHandling();
        setupTimer();

    }

    @Override
    public void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        
        super.onDestroy();
    }
    
    private void setText() {
        SharedPreferences settings = getSharedPreferences(SettingsHelper.PREFS_NAME, 0);
        String units = settings.getString(Constants.PREF_REPEAT_THRESHHOLD_UNITS, "Minutes");
        int time = settings.getInt(Constants.PREF_REPEAT_THRESHHOLD, 30);

        ((TextView) findViewById(R.id.heading)).setText(String.format(getString(R.string.limitThreshholdOverrideDialogPrefix), time, units));
    }
    
    private void setupClickHandling() {

        (findViewById(R.id.run_button)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepetitionOverrideActivity.this, ParserService.class);
                intent.putExtra(ParserService.EXTRA_PAYLOAD, mPayload);
                intent.putExtra(ParserService.EXTRA_SKIP_CHECK, true);
                startService(intent);
                finish();

            }
        });
        (findViewById(R.id.cancel_button)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    
    private void setupTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                finish();
            }
        }, 5000); 
    }
}
