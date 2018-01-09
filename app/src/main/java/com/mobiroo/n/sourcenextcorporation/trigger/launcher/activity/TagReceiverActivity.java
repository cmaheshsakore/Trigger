package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import java.util.Arrays;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;

public class TagReceiverActivity extends Activity{

    @SuppressWarnings("unused")
    private Intent mIntent;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processIntent(getIntent());
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        mIntent = intent;
        processIntent(intent);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(this)) {
            Usage.startTracker(this, this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        SettingsHelper.loadPreferences(this);
        if (Usage.canLogData(this)) {
            Usage.logInstallDate(this);
            Usage.logAppRan(this);
        }
        
    }
    
    @Override
    public void onStop() {
        if (Usage.canLogData(this)) {
            Usage.stopTracker(this);
        }
        super.onStop();
    }
    
    private void processIntent(Intent intent) {
        Logger.d("Received full tag");
        mIntent = intent;

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        
        if (tag == null) {
            exitCancel();
        }
        
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            exitCancel();
        }
        
        String payload = null;
        try {
            NdefMessage message = ndef.getCachedNdefMessage();
            payload = new String(message.getRecords()[0].getPayload());
            
            if (payload.endsWith("tags.to/ntl")) {
                try {
                    payload = new String(message.getRecords()[1].getPayload());
                } catch (Exception e) {
                    Logger.e("Exception pulling second record", e);
                }
            }
            
            payload = trimPayload(payload);
            
        } catch (Exception e) {
            exitCancel();
        }
        
        if (payload != null) {
            // Pass payload to parser service
            Usage.logTrigger(TagReceiverActivity.this, Usage.TRIGGER_NFC);
            Intent service = new Intent(this, ParserService.class);
            service.putExtra(ParserService.EXTRA_PAYLOAD, payload);
            service.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_NFC);
            startService(service);
            this.finish();
        } else {
            exitCancel();
        }
    }
    
    private String trimPayload(String in) {
        String out = in;
        if (in.length() > 3) {
            if (Arrays.equals(in.substring(1, 3).getBytes(), "en".getBytes())) {
                out = in.substring(3);
            }
        }
        return out;
    }
    
    private void exitCancel() {
        setResult(RESULT_CANCELED);
        this.finish();
    }
}
