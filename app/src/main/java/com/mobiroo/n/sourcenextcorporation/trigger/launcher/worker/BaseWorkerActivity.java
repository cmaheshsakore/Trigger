package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ActionService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

/**
 * Created by krohnjw on 1/2/14.
 */
public class BaseWorkerActivity extends Activity {

    private int     mResumePosition = -1;
    private String  mResumePayload  = "";
    private String  mResumeName     = "";

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);

        mResumePosition = getIntent().getIntExtra(ActionService.EXTRA_START_POSITION, -1);

        mResumePayload = getIntent().getStringExtra(ActionService.EXTRA_PAYLOAD);
        if (mResumePayload == null) mResumePayload = "";

        mResumeName = getIntent().getStringExtra(ActionService.EXTRA_TAG_NAME);
        if (mResumeName == null) mResumeName = "";
    }

    protected void resumeProcessing(int result) {
        resumeProcessing(result, null);
    }

    protected void resumeProcessing(int result, Intent data) {
        Logger.d("Resume processing being called");
        setResult(result, data);
        if ((mResumePosition == -1) || (mResumePayload.isEmpty())) {
            Logger.d("Finish called");
            finish();
        } else {
            Logger.d("Restarting action service");
            Intent intent = new Intent(this, ActionService.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(ActionService.EXTRA_PAYLOAD, mResumePayload);
            intent.putExtra(ActionService.EXTRA_START_POSITION, mResumePosition);
            intent.putExtra(ActionService.EXTRA_TAG_NAME, mResumeName);
            startService(intent);
            finish();
        }
    }
}
