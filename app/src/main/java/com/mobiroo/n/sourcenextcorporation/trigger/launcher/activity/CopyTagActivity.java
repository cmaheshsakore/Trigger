package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class CopyTagActivity extends BaseNfcActivity {
    
    private TextView mStatusOne;
    private TextView mStatusTwo;
    private TextView mStatusThree;
    private NdefMessage mMsgs;

    @Override
    public void onCreate(Bundle savedState) {
        super.hideMenu();
        super.onCreate(savedState);
        
        setContentView(R.layout.tag_copy);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow_back_black);
        toolbar.setTitle(getString(R.string.copyTitle));

        mStatusOne = (TextView) findViewById(R.id.statusOne);
        mStatusTwo = (TextView) findViewById(R.id.statusTwo);
        mStatusThree = (TextView) findViewById(R.id.statusThree);

        mMsgs = null;
        mStatusOne.setTextColor(getResources().getColor(R.color.TextColor));
        mStatusTwo.setTextColor(getResources().getColor(R.color.TitleGrey));
        mStatusThree.setTextColor(getResources().getColor(R.color.TitleGrey));

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            boolean isExit = data.getBooleanExtra("doExit", false);
            if (isExit) {
                this.setResult(RESULT_OK, new Intent().putExtra("doExit", true));
                this.finish();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (NFCUtil.hasTech(tag, Ndef.class)) {
            Ndef ndef = Ndef.get(tag);
            if (mMsgs == null) {
                Logger.d("Copying messages");
                mStatusOne.setTextColor(getResources().getColor(R.color.TitleGrey));
                mStatusTwo.setTextColor(getResources().getColor(R.color.TextColor));
                try {
                    if (!ndef.isConnected())
                        ndef.connect();
                    if (ndef.isConnected())
                        mMsgs = ndef.getNdefMessage();

                    mStatusTwo.setTextColor(getResources().getColor(R.color.TitleGrey));
                    mStatusThree.setTextColor(getResources().getColor(R.color.TextColor));
                    showDialog(getString(R.string.copyDialogCopied));
                } catch (Exception e) {
                    showDialog(getString(R.string.copyDialogCopyError));
                }
            } else {
                try {
                    if (!ndef.isConnected())
                        ndef.connect();
                    if (ndef.isConnected() && ndef.isWritable()) {
                        Logger.d("Writing messages");
                        ndef.writeNdefMessage(mMsgs);
                        showDialog(getString(R.string.copyDialogWritten));
                    }
                } catch (Exception e) {
                    showDialog(getString(R.string.copyDialogWriteError));
                }
            }
        } else {
            if (mMsgs == null) {
                showDialog(getString(R.string.copyDialogCopyError));
            } else {
                if (NFCUtil.hasTech(tag, NdefFormatable.class)) {
                    NdefFormatable ndef = NdefFormatable.get(tag);
                    try {
                        if (!ndef.isConnected())
                            ndef.connect();
                        if (ndef.isConnected()) {
                            ndef.format(mMsgs);
                            showDialog(getString(R.string.copyDialogWritten));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showDialog(getString(R.string.copyDialogWriteError));
                    }
                } else {
                    showDialog(getString(R.string.copyDialogWriteError));
                }
            }
        }
    }
}
