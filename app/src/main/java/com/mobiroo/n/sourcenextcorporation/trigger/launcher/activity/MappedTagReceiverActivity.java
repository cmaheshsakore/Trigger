package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;


import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.MappedTagService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;


public class MappedTagReceiverActivity extends Activity {

    private String          mMime = "";
    private final String    KOVIO_MIME = "application/nfctl";
    private Context         mContext;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsHelper.loadPreferences(this);
        setContentView(R.layout.action_launcher);

        mContext = this;
        
        Logger.d("MAPPED: Received mapped tag");
        
        Tag tag = (Tag) getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            // Grab ID and look up payload
            String tagId = NFCUtil.getTagUuidAsString(tag);
            Logger.d("MAPPED: ID " + tagId);
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                try {
                    mMime = new String(ndef.getCachedNdefMessage().getRecords()[0].getType());
                    if (KOVIO_MIME.equals(mMime)) {
                        /* Log use of a kovio tag here */
                        Usage.storeAggregateTuple(MappedTagReceiverActivity.this, Constants.USAGE_CATEGORY_FREE_TAG, tagId, Constants.COMMAND_NO_ACTION, 1);
                    }   
                } catch (Exception e) {
                    /* This will fail for empty tags possibly or tags with a null type */
                }
                
                
            }
            if (!tagId.isEmpty()) {
                startService(tagId, tag);
            } else {
                Logger.d("MAPPED: Tag has an empty Id");
                promptCreateTask();
            }
        } else {
            Logger.d("MAPPED: Tag was returned as null");
            if (getIntent().hasExtra("tagid")) {
                Logger.d("MAPPED: Tag ID on intent");
                startService(getIntent().getStringExtra("tagid"), null);
                
            }
            Usage.logGeneralTag(MappedTagReceiverActivity.this);
            promptCreateTask();
        }
    }

    private void startService(String tagId, Tag tag) {
        Intent service = new Intent(this, MappedTagService.class);
        service.putExtra(MappedTagService.EXTRA_UUID, tagId);
        service.putExtra(MappedTagService.EXTRA_TAG, tag);
        startService(service);
        finish();
    }
    @Override
    public void onStart() {
        super.onStart();

        SettingsHelper.loadPreferences(MappedTagReceiverActivity.this);
        if (Usage.canLogData(this)) {
            Usage.startTracker(this, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Usage.canLogData(this)) {
            Usage.dispatchGa();
        }
    }
    @Override
    public void onStop() {
        super.onStop();

        SettingsHelper.loadPreferences(MappedTagReceiverActivity.this);
        if (Usage.canLogData(this)) {
            Usage.stopTracker(this);
        }
    }
  
    private void promptCreateTask() {
        /* Prompt user to create a tag using this NFC tag */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.import_create_tag));
        builder.setPositiveButton(getString(R.string.dialogYes), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadCreateTag();
            }
            
        });
        builder.setNegativeButton(getString(R.string.dialogNo), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        
        if (! ((Activity) mContext).isFinishing()) {
        	builder.create().show();
        }
    }

    private void loadCreateTag() {
        Intent intent = new Intent(this, TaskTypeItem.TaskTypeNfc.getActivityClass());
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TaskTypeNfc.getExtraValue());
        intent.putExtra(TaskTypeItem.EXTRA_LAYOUT_ID, TaskTypeItem.TaskTypeNfc.getLayoutId());
        startActivity(intent);
    }
}
