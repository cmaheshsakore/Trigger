package com.mobiroo.n.sourcenextcorporation.trigger.activity;

import android.app.Activity;

import com.mobiroo.n.sourcenextcorporation.trigger.application.WearApplication;

/**
 * Created by krohnjw on 7/17/2014.
 */
public class BaseActivity extends Activity {

    @Override
    public void onResume() {
        super.onResume();
        WearApplication.setApplicationActive(this, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        WearApplication.setApplicationActive(this, false);
    }
}
