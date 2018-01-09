package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.UriRecord;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TextDisplayActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import java.io.IOException;
import java.util.Arrays;

import static com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem.TaskTypeNfc;

public class MappedTagService extends Service  {

    public static final String  EXTRA_UUID  = "com.trigger.launcher.EXTRA_UUID";
    public static final String  EXTRA_TAG   = "com.trigger.launcher.EXTRA_TAG";
    
    private boolean             mIdRequested;

    private final int           FLAG_REQUEST_ONLY = 1;
    private final int           FLAG_LOAD_DATA = 2;
    private final int           FLAG_STANDARD_REQUEST = 3;

    private String              mMime = "";
    private final String        KOVIO_MIME = "application/nfctl";
    private Context             mContext;

    private Tag                 mTag;
    private void logd(String message) {
        Logger.d("MAPPED: " + message);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIdRequested = false;
        mContext = this;
        
        mTag = (Tag) intent.getParcelableExtra(EXTRA_TAG);

        if (intent.hasExtra(EXTRA_UUID)) {
            String tagId = intent.getStringExtra(EXTRA_UUID);
            if (!tagId.equals("0000000000000000")) {
                loadTagData(tagId);
            } else { 
                checkForKnownPayloads(true);
            }
        } else  {
            this.stopSelf();
        }
        
        return Service.START_NOT_STICKY;
    }

    @SuppressLint("InlinedApi") 
    private void requestTagUpdate(String id, int flag) {
        TagPayloadRequest request = new TagPayloadRequest(mContext, id);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) { 
            request.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, flag);
        } else {
            request.execute(flag);
        }
    }
    
    private void loadTagData(String tagId) {

        boolean finishWhenDone = false;

        Cursor c = mContext.getContentResolver().query(TaskProvider.Contract.LOCAL_MAPPING, new String[] { "payload" }, "UUID=?", new String[] { tagId }, null);
        if (c != null && c.moveToFirst()) {
            String payload = c.getString(0);
            logd("Found payload " + payload);
            if (!payload.isEmpty()) {
                String[] actions = payload.split(";");

                String id = "";
                for (String action: actions) {
                    String[] args = action.split(":");
                    logd("Checking action " + action + "(" + args[0] + ") against " + Constants.COMMAND_TAG_ID + " and " + Constants.COMMAND_TOGGLE_PROFILE );
                    if (Constants.COMMAND_TAG_ID.equals(args[0]) || Constants.COMMAND_TOGGLE_PROFILE.equals(args[0])) {
                        id = args[1];
                        break;
                    }
                }

                boolean run = true;

                if (!id.isEmpty()) {
                    logd("Id is " + id);
                    TaskSet set = DatabaseHelper.getNfcTasksForId(this, id);
                    if (set != null) {
                        Trigger trigger = set.getTrigger(0);
                        if (trigger != null) {
                            run = trigger.constraintsSatisfied(MappedTagService.this);
                        }
                    }
                } else {
                    logd("Could not find a valid ID - continuing without loading constraints");
                }

                if (run) {
                    logd("Sending payload to service");
                    runTag(payload);
                }

            }
            
            requestTagUpdate(tagId, FLAG_REQUEST_ONLY);
            
            finishWhenDone = true;

        } else {
            logd("Missed local cache, requesting");

            if (!mIdRequested) {
                /* Request TagID from TagstandManager */
                mIdRequested = true;
                if (isNetworkAvailable()) {
                    requestTagUpdate(tagId, FLAG_STANDARD_REQUEST);

                } else {
                    if (!checkForKovioFreeTag()) {
                        checkForKnownPayloads(true);
                    }
                    Usage.logGeneralTag(mContext);
                }
            } else {
                logd("Missed on request");
                if (!checkForKovioFreeTag()) {
                    checkForKnownPayloads(false);
                }
                /* Tag has been requested already and returned no content - finish */
                Usage.logGeneralTag(mContext);
                finishWhenDone = true;
            }
            
        }

        if (!c.isClosed()) {
            c.close();
        }
        
        if (finishWhenDone) {
            this.stopSelf();
        }

    }
    
    private boolean checkForKovioFreeTag() {
        if (KOVIO_MIME.equals(mMime)) {
          // This is a Kovio tag with no data associated.
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_SHOW_WELCOME, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return false;
    }
    private void checkForKnownPayloads(boolean finishWhenComplete) {

        logd("Checking for known payloads");

        /* Check if it is a type of tag we recognize */
        if (mTag == null) {
            return;
        }
        
        Ndef ndef = Ndef.get(mTag);
        //ndef.getNdefMessage().getRecords()[0].toMimeType()
        if (ndef != null) {
            NdefMessage message = ndef.getCachedNdefMessage();
            if (message == null) { 
                try {
                    if (!ndef.isConnected()) {
                        ndef.connect();
                    }
                    if (ndef.isConnected()) {
                        message = ndef.getNdefMessage();
                    }
                } catch (IOException e) {
                    Logger.e("IO Exception reading tag");
                } catch (FormatException e) {
                    Logger.e("Format Exception reading tag");
                } catch (IllegalStateException e) {
                    Logger.e("Illegal state exception reading tag", e);
                }
            }

            if (message != null) {

                NdefRecord[] records = message.getRecords();

                for (int i = 0; i < records.length; i++) {
                    NdefRecord record = records[i];

                    Short tnf = record.getTnf();
                    if (tnf == NdefRecord.TNF_WELL_KNOWN) {
                        logd("Got TNF WK");
                        byte[] type = record.getType();

                        if (Arrays.equals(type, NdefRecord.RTD_TEXT)) {
                            logd("Text record");
                            try {
                                byte[] payload = record.getPayload();

                                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                                int languageCodeLength = payload[0] & 0077;

                                String payloadString = new String(payload);  // Default to full payload
                                try {
                                    payloadString = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
                                } catch (Exception e) {
                                    /* Fail gracefully */
                                }

                                if (!payloadString.isEmpty()) {
                                    Intent intent = new Intent(mContext, TextDisplayActivity.class);
                                    intent.putExtra(TextDisplayActivity.EXTRA_MESSAGE, payloadString);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finishWhenComplete = false;
                                }
                            } catch (Exception e) { Logger.e(Constants.TAG, "Exception parsing TextRecord", e); }
                        } else if (Arrays.equals(type, NdefRecord.RTD_URI) || Arrays.equals(type,  NdefRecord.RTD_SMART_POSTER)) {
                            logd("URI Record");
                            try {
                                Uri data = null;
                                if (Build.VERSION.SDK_INT >= 16) {
                                    data = record.toUri();
                                } else {
                                    UriRecord uri = UriRecord.parse(record);
                                    data = uri.getUri();

                                }

                                if (data != null) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(data);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finishWhenComplete = false;
                                }
                            } catch (Exception e) { Logger.e(Constants.TAG, "Exception parsing URI", e); }

                        }
                    }
                }
            }

        }

        logd("Exiting");
        if (finishWhenComplete) {
            Usage.logGeneralTag(mContext);
            launchTaskCreation();
        } else {
            this.stopSelf();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    private void runTag(String payload) {
        Usage.logTrigger(mContext, Usage.TRIGGER_NFC);
        Intent service = new Intent(this, ParserService.class);
        service.putExtra(ParserService.EXTRA_PAYLOAD, payload);
        service.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_NFC);
        logd("Starting service");
        startService(service);
    }
    
    private void showErrorMessage(String payload) {
        // Alert user that there was an error
        if (!TagstandManager.REQUEST_NOT_MAPPED.equals(payload)) {
            String message = getString(R.string.loadTagPayloadError);
            if (payload.equals(TagstandManager.REQUEST_NOT_MAPPED)) {
                message = getString(R.string.loadTagNoPayload);
            }
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
        this.stopSelf();
    }

    private class TagPayloadRequest extends AsyncTask<Integer, Void, String> {
        private String mUUID;
        private Context mContext;
        private int mRequestMode;

        public TagPayloadRequest(Context context, String UUID) {
            mUUID = UUID;
            mContext = context;
        }
        @Override

        protected String doInBackground(Integer... params) {
            if (params != null) {
                mRequestMode = params[0];
            }
            return TagstandManager.getTag(mContext, mUUID, null);
        }

        protected void onPostExecute(String result) {

            logd("Checking result " + result + " for " + mRequestMode);
            
            if (mRequestMode == FLAG_LOAD_DATA) {
                // An explicit request to load data has been presented
                logd("Load data for " + mUUID);
                loadTagData(mUUID);
            } else if (mRequestMode == FLAG_STANDARD_REQUEST) {
                // Full request.  Perform all handling of result

                if ((result != null) && (!result.isEmpty()) && (!result.equals(TagstandManager.REQUEST_FAILED)) && (!result.equals(TagstandManager.REQUEST_NOT_MAPPED))) {
                    logd("Loading data for " + mUUID);
                    loadTagData(mUUID); /* This will attempt to load tag data from local mapping
                     * If it exists it will launch parser with payload as arg.  Otherwise
                     * will exit */
                } else if (!result.isEmpty()){
                    /* Empty payloads are from deleted tags currently */
                    if (result.equals(TagstandManager.REQUEST_NOT_MAPPED) || result.equals(TagstandManager.RESULT_EMPTY_PAYLOAD)) {
                        checkForKnownPayloads(true);
                    } else {
                        showErrorMessage(result);
                    }
                } else {
                    checkForKnownPayloads(true);
                }
            } 
        }
    }
    
    private void launchTaskCreation() {
        Intent intent = new Intent(this, TaskTypeNfc.getActivityClass());
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeNfc.getExtraValue());
        intent.putExtra(TaskTypeItem.EXTRA_LAYOUT_ID, TaskTypeNfc.getLayoutId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
