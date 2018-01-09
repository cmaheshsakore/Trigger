package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.KeyguardLocker;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.SoundPlayer;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class TimerExpiredActivity extends Activity {

    private SoundPlayer mPlayer;
    public static final String EXTRA_NID = "TimerExpiredActivity.extra_nid";
    public static final String EXTRA_NAME = "TimerExpiredActivity.extra_name";
    public static final String EXTRA_ELAPSED = "TimerExpiredActivity.extra_elapsed";

    private KeyguardManager mKm;
    private KeyguardLocker mLocker;
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        
        setContentView(R.layout.activity_timer_expired);

        int id = getIntent().getIntExtra(EXTRA_NID, -1);
        if (id != -1) {
            ((NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
        }

        mKm = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        mLocker = new KeyguardLocker(this, mKm);
        mLocker.execute(Constants.OPERATION_DISABLE);
        
        /*((TextView) findViewById(android.R.id.title)).setText(getTitle());*/
        
        String name = (getIntent().hasExtra(EXTRA_NAME)) ? getIntent().getStringExtra(EXTRA_NAME) : "";
        if (!name.isEmpty()) {
            ((TextView) findViewById(android.R.id.text1)).setText(name);
        }

        if (getIntent().hasExtra(EXTRA_ELAPSED)) {
            ((TextView) findViewById(android.R.id.text2)).setText(getIntent().getStringExtra(EXTRA_ELAPSED));
        }

        /* Play media file until dismissed */
        mPlayer = new SoundPlayer(TimerExpiredActivity.this, getAlarmUri());
        mPlayer.execute(SoundPlayer.SET_LOOPING_ENABLED);
        
        // Set up handler for click
        ((View) findViewById(R.id.button_close)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mPlayer.stopPlayback();
                    mLocker.enableKeyGuard();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.stopPlayback();
        }
        mLocker.enableKeyGuard();
        super.onDestroy();
    }
    
    private Uri getAlarmUri() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        String uriString = SettingsHelper.getPrefString(TimerExpiredActivity.this, Constants.PREF_TIMER_URI);
        if ((uriString != null) && (!uriString.isEmpty())) {
            try {
                alarmUri = Uri.parse(uriString);
            } catch (Exception e) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
        }
        
        return alarmUri;
    }
}
