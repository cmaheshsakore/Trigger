package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

public class EraseTagActivity extends FragmentActivity {
    
    private NfcAdapter mAdapter = null;
    private PendingIntent mPendingIntent;
    @SuppressWarnings("unused")
    private IntentFilter[] _mFilters;
    @SuppressWarnings("unused")
    private String[][] _mTechLists;
    private String _LEFT = "left";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        try {
            mAdapter = NfcAdapter.getDefaultAdapter(this);
        } catch (Exception e) {
            Logger.e("NFCT", "Error getting NFC Adapter " + e);
        }

        setContentView(R.layout.tag_erase);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow_back_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle(R.string.layoutHomeFormat);

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(EraseTagActivity.this)) {
            Usage.startTracker(this, this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Usage.canLogData(EraseTagActivity.this)) {
            Usage.stopTracker(this);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (mAdapter != null)
                mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        } catch (Exception e) {
            Logger.e("NFCT", "Error " + e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        
        String tagId = NFCUtil.getTagUuidAsString(tag);
        if (!tagId.isEmpty()) {
            // Clear local mapping
            getContentResolver().delete(TaskProvider.Contract.LOCAL_MAPPING, "UUID=?", new String[] {tagId});
            // Push empty payload to cloud
            TagstandManager.putTag(EraseTagActivity.this, tagId, "");
        }
        
        if (ndef != null) {

            int maxSize = ndef.getMaxSize() - 5;
            if (maxSize > 144)
                maxSize = 135;
            Logger.i("Max size is " + maxSize);
            if (maxSize < 0) {
                maxSize = 0;
            }
            byte[] eraseMessage = new byte[maxSize];
            for (int i = 0; i < maxSize; i++)
                eraseMessage[i] = (byte) 0x00;

            try {
                ndef.connect();
                NdefRecord[] ndr = { new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], eraseMessage) };
                ndef.writeNdefMessage(new NdefMessage(ndr));
                ndef.close();
                showDialog(getString(R.string.dialogWriteTitle), getString(R.string.writeSuccessfulWrite), _LEFT, true);
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception erasing tag", e);
                e.printStackTrace();
            }
        } else {
            // Format for NDEF
            NdefFormatable formatable = NdefFormatable.get(tag);
            if (formatable != null) {
                try {
                    formatable.connect();
                    byte[] eraseMessage = { (byte) 0x00 };
                    NdefRecord[] ndr = { new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], eraseMessage) };
                    formatable.format(new NdefMessage(ndr));
                    formatable.close();
                    showDialog(getString(R.string.dialogWriteTitle), getString(R.string.writeSuccessfulWrite), _LEFT, true);
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception erasing tag", e);
                    e.printStackTrace();
                    e.printStackTrace();
                }
            } else {
                // Can't erase this tag as it's not NDEF or NdefFormatable

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDialog(String title, String message, String alignment, boolean finishTask) {
        final SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.setPositiveButton(getString(R.string.dialogOK), new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show(getSupportFragmentManager(), "done");
    }

}
