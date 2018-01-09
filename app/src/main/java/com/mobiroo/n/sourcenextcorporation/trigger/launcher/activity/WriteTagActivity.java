package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.WriteTagFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TagInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import java.io.IOException;
import java.util.Locale;

public class WriteTagActivity extends AppCompatActivity {

    private Task[]      mIncomingTags;
    private NdefMessage mMessage;
    private boolean     mLockTag;
    private boolean     mWriteName;
    private boolean     mForceMapped;
    private boolean     mPluginInstalled;
    private boolean     mFlagBuildForSwitch = true;
    private boolean     mEncodeInUtf8 = true;

    private WriteTagFragment mWriteFragment;



    private class ErrorMessage {
        private boolean mHasError;
        private String mErrorMessage;
        private boolean mMessageHasPriority;
        private boolean mFinishAfterDisplay;
        private boolean mWroteSuccessfully;

        public ErrorMessage() {
            mErrorMessage = "";
            mHasError = false;
            mMessageHasPriority = false;
            mFinishAfterDisplay = false;
            mWroteSuccessfully = true;
        }

        @SuppressWarnings("unused")
        public ErrorMessage(String message) {
            mErrorMessage = message;
            mHasError = false;
            mMessageHasPriority = false;
            mFinishAfterDisplay = false;
        }

        public void setFinishAfterDisplay() {
            mFinishAfterDisplay = true;
        }

        @SuppressWarnings("unused")
        public boolean shouldFinishAfterDisplay() {
            return mFinishAfterDisplay;
        }

        public void giveMessagePriority() {
            mMessageHasPriority = true;
        }

        @SuppressWarnings("unused")
        public boolean doesMessageHavePriority() {
            return mMessageHasPriority;
        }

        public boolean hasError() {
            return mHasError;
        }

        @SuppressWarnings("unused")
        public String getErrorMessage() {
            return mErrorMessage;
        }

        public void setErrorMessage(String message) {
            mErrorMessage = message;
            mHasError = true;
        }

        public void appendErrorMessage(String message) {
            mErrorMessage += message;
        }

        public void setWriteSuccess() {
            mWroteSuccessfully = true;
        }

        public void setWriteFail() {
            mWroteSuccessfully = false;
        }

        public boolean wasWriteSuccessful() {
            return mWroteSuccessfully;
        }
    }

    private WriteTagFragment getWriteFragment() {
        WriteTagFragment fragment = (WriteTagFragment) getSupportFragmentManager().findFragmentByTag(WriteTagFragment.FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new WriteTagFragment();
        }
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_finish_task);

        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        bar.setTitle(R.string.menu_done);

        bar.inflateMenu(R.menu.menu_done_only);
        bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_options:
                        showOptions();
                        return true;
                    default:
                        return false;
                }
            }
        });

        bar.setNavigationIcon(R.drawable.ic_action_accept);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        
        SettingsHelper.loadPreferences(WriteTagActivity.this);
        mLockTag = false;
        mWriteName = true;

        mForceMapped = getIntent().getBooleanExtra(Constants.EXTRA_FORCE_MAPPED, false);


 
        if (savedState == null) {
            mWriteFragment = getWriteFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content, mWriteFragment, WriteTagFragment.FRAGMENT_TAG);
            transaction.commit();
        } 

        mPluginInstalled = Utils.isReusePluginInstalled(this);
        
        Parcelable[] incomingTags = getIntent().getParcelableArrayExtra(Constants.EXTRA_SAVED_TAG);
        if (incomingTags != null)
        {
            mIncomingTags = new Task[incomingTags.length];
            for (int i=0; i<incomingTags.length; i++) {
                Task tag = (Task) incomingTags[i];
                mIncomingTags[i] = tag;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            Intent intent = new Intent(this, this.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            adapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Logger.d("Discovered tag with to write");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        writeTag(tag);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_done_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_options:
                showOptions();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showOptions() {
        SimpleDialogFragment dialog = new SimpleDialogFragment();

        View view = View.inflate(this, R.layout.dialog_write_options, null);
        CheckBox lock = (CheckBox) view.findViewById(R.id.prefLock);
        lock.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLockTag = isChecked;
                showWarningDialog();
            }
        });
        
        CheckBox name = (CheckBox) view.findViewById(R.id.prefWriteName);
        name.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWriteName = isChecked;
            }
        });
        

        dialog.setChildView(view);

        dialog.setTitle(getString(R.string.write_options_title));
        dialog.setPositiveButton(getString(R.string.dialogOK), null);
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "options_dialog");

    }
    
    private void showWarningDialog() {
        final SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.setMessage(getString(R.string.readOnlyWarning));
        dialog.setTitle(getString(R.string.warning));
        dialog.setPositiveButton(getString(R.string.dialogOK), null);
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "read_only_warning");
    }

    public void finishAndLoadShop(View v) {
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_FLAG_EXIT, true);
        intent.putExtra(Constants.EXTRA_LOAD_SHOP_FRAGMENT, true);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }

    public void finishMe() {
        Intent finish = new Intent();
        finish.putExtra(Constants.EXTRA_FLAG_EXIT, true);
        setResult(RESULT_OK, finish);
        finish();
    }


    public void writeTag(Tag tag) {
        if (mIncomingTags != null) {
            if (mIncomingTags.length == 1)
            {
                /* This is a standard Task tag */
                mMessage = new NdefMessage(NFCUtil.buildNdefRecord(getBaseContext(), getPackageName(), mIncomingTags[0].buildPayloadString(mWriteName, !mFlagBuildForSwitch), Locale.ENGLISH, mEncodeInUtf8));
            }
            else if (mIncomingTags.length > 1){
                /* Here we have a switch.  Currently two tags, can be more in the future */
                mMessage = new NdefMessage(NFCUtil.buildNdefRecord(getBaseContext(), getPackageName(), NFCUtil.buildSwitchPayload(getBaseContext(), mWriteName, mIncomingTags), Locale.ENGLISH, mEncodeInUtf8));
            }
        }

        /* Ensure we have a non null message */
        if (mMessage != null) {
            int messageSize = mMessage.toByteArray().length;
            Logger.d(Constants.TAG, "Final size is " + messageSize);

            TagInfo tagInfo = NFCUtil.getTagInfo(tag);
            ErrorMessage error = writeMessageToTag(tag, tagInfo);

            doSound();
            doVibrate();

            
            (findViewById(R.id.write_status)).setVisibility(View.VISIBLE);

            if (error.wasWriteSuccessful()) {
                TextView writeStatus = (TextView) findViewById(R.id.write_status);
                writeStatus.setText(getString(R.string.tag_write_success_message));
                writeStatus.setGravity(Gravity.CENTER);

                TextView writeResult = (TextView) findViewById(R.id.write_result);
                writeResult.setVisibility(View.VISIBLE);
                writeResult.setText(getString(R.string.writeSuccessfulWrite));
                (findViewById(R.id.write_status_container)).setBackgroundColor(getResources().getColor(R.color.write_success));
                
            } else {
                TextView writeStatus = (TextView) findViewById(R.id.write_status);
                writeStatus.setVisibility(View.VISIBLE);
                writeStatus.setText((mPluginInstalled) ?  getString(R.string.tag_write_failed_message) : getString(R.string.tag_write_failed_no_plugin));
                writeStatus.setGravity(Gravity.LEFT);

                TextView writeResult = (TextView) findViewById(R.id.write_result);
                writeResult.setVisibility(View.VISIBLE);
                writeResult.setText(getString(R.string.tag_write_failed));
                (findViewById(R.id.write_status_container)).setBackgroundColor((mPluginInstalled) ? getResources().getColor(R.color.write_warning) : getResources().getColor(R.color.write_failed));
            }


            if (error.hasError()) {
                Logger.d("Exception writing tag " + error.mErrorMessage);
            }
        }


    }

    private void writeMappedTag() {

    }
    public ErrorMessage writeMessageToTag(Tag tag, TagInfo tagInfo)
    {
        Logger.d("Trying to write");
        ErrorMessage error = new ErrorMessage();
        int messageSize = mMessage.toByteArray().length;

        if (mForceMapped) {
            Logger.d("Forcing mapped payload");
            mMessage = new NdefMessage(NFCUtil.createSmallRecord(getBaseContext(), getPackageName(), true, 49));
        }

        boolean saveToCloud = false;
        
        if (NFCUtil.hasTech(tag, Ndef.class)) {

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                error.setErrorMessage("Could not get tag");
                return error;
            }
            int maxSize = ndef.getMaxSize();
            Logger.d("Detected NDEF TAG with size of " + maxSize);

            try {
                ndef.connect();
            } catch (IOException e1) {
                error.setErrorMessage("Error connecting to tag");
            } catch (NullPointerException e1) {
                error.setErrorMessage("Error connecting to tag");
            }

            if (ndef.isConnected()) {
                if (ndef.isWritable()) {
                    boolean tryLock = true;
                    try {
                        ndef.writeNdefMessage(mMessage);
                    } catch (Exception e) {
                            /* Save local mapping here */
                        Logger.d("Failed writing to tag.  Using a local mapping.");
                        saveToCloud = true;
                        error.giveMessagePriority();
                        error.setFinishAfterDisplay();
                        error.setErrorMessage("Exception writing " + e);
                        error.setWriteFail();
                        try {
                            Logger.d("Trying to write small message");
                            error.setWriteSuccess();
                            NdefMessage msg = new NdefMessage(NFCUtil.createSmallRecord(getBaseContext(), getPackageName(), true, ndef.getMaxSize()));
                            Logger.d("Message size is " + msg.toByteArray().length);
                            ndef.writeNdefMessage(msg);

                        } catch (Exception ex) {
                            tryLock = false;
                            error.setWriteFail();
                        }
                    }


                    if ((mLockTag) && (tryLock)) {
                        try {
                            ndef.makeReadOnly();
                        } catch (IOException e) {
                            if ((mLockTag) && (tagInfo.getType().equals("Classic 1K"))) {
                                error.setErrorMessage("IO Error making read only");
                            } else if (ndef.getMaxSize() < messageSize) {
                                error.setErrorMessage("IO Error making read only, message too large");
                            } else {
                                if (tagInfo.getType().equals("Classic 1K")) {
                                    error.setErrorMessage("IO Error making 1k tag read only");
                                } else {
                                    error.setErrorMessage("IO Error making ro");
                                }
                            }
                            error.appendErrorMessage("\n" + "Message is too large for tag " + tagInfo.getType());
                            e.printStackTrace();
                        }
                    }
                } else {
                    Logger.d("Tag is read only, saving local mapping");
                    saveToCloud = true;
                    error.giveMessagePriority();
                    error.setFinishAfterDisplay();
                    error.setWriteFail();
                    error.setErrorMessage("Tag is read-only");
                }
                
                saveTagMapping(tag, mForceMapped || saveToCloud);
            }
            else {
                error.setErrorMessage("Could not connect");
            }

            if (ndef.isConnected()) {
                try { ndef.close(); }
                catch (IOException e) { /* Fail silently */ }
            }
        } else if (NFCUtil.hasTech(tag, NdefFormatable.class)) {
            NdefFormatable ndef = NdefFormatable.get(tag);

            try {
                ndef.connect();
            } catch (IOException e) {
                error.setErrorMessage("Could not connect to Formatable tag");
            }

            if (ndef.isConnected()) {
                try {
                    if (!mLockTag) {
                        error.setWriteSuccess();
                        ndef.format(mMessage);
                    }
                    else {
                        error.setWriteSuccess();
                        ndef.formatReadOnly(mMessage);
                    }
                } catch (Exception e) {
                    Logger.d("Failed writing to tag.  Using a local mapping.");
                    saveToCloud = true;
                    error.giveMessagePriority();
                    error.setFinishAfterDisplay();
                    error.setErrorMessage("Exception writing NDEF Message to Formatable");
                    error.setWriteFail();
                    /* Try to write > 48 byte record here */
                    try { 
                        Logger.d("Trying to write small message");
                        if (!mLockTag) {
                            error.setWriteSuccess();
                            ndef.format(new NdefMessage(NFCUtil.createSmallRecord(getBaseContext(), getPackageName(), true, 50)));
                        }
                        else {
                            error.setWriteSuccess();
                            ndef.formatReadOnly(new NdefMessage(NFCUtil.createSmallRecord(getBaseContext(), getPackageName(), true, 50)));
                        }
                    } catch (Exception ex) {
                        error.setWriteFail();
                        try { 
                            if (!mLockTag) {
                                error.setWriteSuccess();
                                ndef.format(new NdefMessage(NFCUtil.createSmallRecord(getBaseContext(), getPackageName(), true, 48))); 
                            } else {
                                error.setWriteSuccess();
                                ndef.formatReadOnly(new NdefMessage(NFCUtil.createSmallRecord(getBaseContext(), getPackageName(), true, 48)));
                            }

                        } catch (Exception ex2) { 
                            error.setWriteFail();
                        }
                    }

                }
                saveTagMapping(tag, mForceMapped || saveToCloud);
            }

            if (ndef.isConnected()) {
                try { ndef.close(); } 
                catch (IOException e) { /* Fail silently */ }
            }

        }
        else {
            //error.setErrorMessage(getString(R.string.writeTagUnsupported));
            saveTagMapping(tag, true);
            error.giveMessagePriority();
            error.setFinishAfterDisplay();
            error.setErrorMessage("Tag is not ndef/ndefformatable - saving locally");
            error.setWriteFail();
        }

        return error;
    }
    
    private void saveTagMapping(Tag tag, boolean saveToCloud) {

        String payload = "";
        if (mIncomingTags.length == 1) {
            payload = mIncomingTags[0].buildPayloadString(mWriteName, !mFlagBuildForSwitch);
        } else {
            payload = NFCUtil.buildSwitchPayload(getBaseContext(), mWriteName, mIncomingTags);
        }

        Logger.d("Saving " + payload);

        if (!payload.isEmpty() && tag != null) {
            String tagId = NFCUtil.getTagUuidAsString(tag);
            Logger.d("Tag ID is " + tagId);

            if (!tagId.isEmpty()) {
                if (saveToCloud) {
                    TagstandManager.putTag(getBaseContext(), tagId, payload);
                }

                /* Save Payload with this UUID */
                ContentValues values = new ContentValues();
                values.put("Payload", payload);

                int rowsAffected = getContentResolver().update(TaskProvider.Contract.LOCAL_MAPPING, values, "UUID=?", new String[] {tagId});
                if (rowsAffected == 0) {
                    values.put("UUID", tagId);
                    getContentResolver().insert(TaskProvider.Contract.LOCAL_MAPPING, values);
                }

                if (mIncomingTags.length == 1) {
                    /* Also add this UUID to the Task tag */
                    values = new ContentValues();
                    values.put("ShareId", tagId);
                    getContentResolver().update(TaskProvider.Contract.TASKS, values, "ID=?", new String[]{mIncomingTags[0].getId()});
               }
            }
        }
    }


    public void doSound() {
        if (SettingsHelper.shouldPlaySound(getBaseContext())) {
            final String uriString = SettingsHelper.getPrefString(getBaseContext(), "prefNotificationURI");
            try {
                Uri alert = Uri.parse(uriString);
                MediaPlayer mp = MediaPlayer.create(WriteTagActivity.this, alert);
                mp.start();
            } catch (Exception e) { 
                /* Fail silently if we can't play a sound */ 
                Logger.e("Could not play sound for " + uriString);
            }

        }
    }

    public void doVibrate() {
       Vibrator v = (Vibrator) WriteTagActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
       v.vibrate(Constants.VIBRATE_LENGTH);
    }
    
    
}
