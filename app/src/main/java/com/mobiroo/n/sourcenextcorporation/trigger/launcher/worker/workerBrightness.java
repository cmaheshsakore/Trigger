package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class workerBrightness extends BaseWorkerActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_LEVEL = "level";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.brightness_set);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Auto close after 3s
        final Handler handler = new Handler();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        finishMe();
                    }
                });
            }
        }, 3000);
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);

        // Get values from intent and adjust window
        int level = intent.getIntExtra(EXTRA_LEVEL, -1);

        if (level != -1) {
            Logger.d("Setting brightness for local window");
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.screenBrightness = (level / 255.0F);
            layoutParams.screenBrightness = Math.max(layoutParams.screenBrightness, 0.05F);
            getWindow().setAttributes(layoutParams);
        }


    }

    public void finishMe() {
        Logger.d("Calling finish in Brightness");
        resumeProcessing(RESULT_OK);
    }
}
