package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ExampleTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.TextRecord;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.GeofenceTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NdefMessageParser;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.ParsedNdefRecord;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import static com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem.TaskTypeNfc;

public class ImportTagActivity extends BaseNfcActivity {
    
    private Tag mTag;
    
    private ArrayList<SavedAction> mActionsOne;
    private ArrayList<SavedAction> mActionsTwo;
    private String mTagNameOne;
    private String mTagNameTwo;
    
    private int mNumTags = 1;
    
    private int mType = 0;
    private String mCondition;
    private String mKey1;
    private String mKey2;

    private ExampleTask mExample;
    private Trigger         mTrigger;
    
    @Override
    public void onCreate(Bundle savedState) {
        super.hideMenu();
        super.onCreate(savedState);
        mContext = this;

        /* Check if we are receiving an import request from a share */
        if (getIntent().hasExtra(Constants.EXTRA_SHARED_TAG_PAYLOAD)) {
            
            // This is an import request from a shared payload.  Pull from intent and parse all details
            
            String payload = getIntent().getStringExtra(Constants.EXTRA_SHARED_TAG_PAYLOAD);
            
            // Check if this is a shared Trigger task
            
            if (payload.contains(",")) {
                String[] payloadArr = payload.split(",");
                if (payloadArr.length >=4) {
                    String tempPayload = "";
                    if (payloadArr.length > 4) {
                        for (int i=4; i < payloadArr.length; i++) {
                            tempPayload += payloadArr[i];
                        }
                    } else {
                        tempPayload = payloadArr[4];
                    }
                    
                    mCondition = payloadArr[0];
                    mType = Integer.parseInt(payloadArr[1]);
                    mKey1 = payloadArr[2];
                    mKey2 = payloadArr[3];
                    payload = tempPayload;
                    Logger.i("Setting payload to " + payload);
                }
            }
            
            if (!payload.equals(TagstandManager.REQUEST_FAILED)) {
                NdefMessage message = new NdefMessage(NFCUtil.buildNdefRecord(ImportTagActivity.this, getPackageName(), payload, Locale.ENGLISH, true));
                parseMessages(new NdefMessage[] {message});
            } else {
                Toast.makeText(ImportTagActivity.this, getString(R.string.no_task), Toast.LENGTH_LONG).show();
                this.finish();
            }
        } else if (getIntent().hasExtra(ExampleTask.EXTRA_EXAMPLE_TASK)) {
            
            mExample = (ExampleTask) getIntent().getParcelableExtra(ExampleTask.EXTRA_EXAMPLE_TASK);
            
            // Check for data on an incoming trigger from an example
            if (getIntent().hasExtra(Trigger.EXTRA_TRIGGER)) {
                mTrigger = (Trigger) getIntent().getParcelableExtra(Trigger.EXTRA_TRIGGER);
            }
            
            // We need both an example and a trigger to proceed
            if (mTrigger == null) {
                mTrigger = new Trigger(TaskTypeItem.TASK_TYPE_NFC, DatabaseHelper.TRIGGER_NO_CONDITION, null, null);
            }
            
            String payload = mExample.getPayload();
            mType = mTrigger.getType();
            mKey1 = mTrigger.getExtra(1);
            mKey2 = mTrigger.getExtra(2);
            mCondition = mTrigger.getCondition();
            
            Logger.i("set name to " + mExample.getName(this));
            
            // Build an encoded NDEF message from the payload to send to parser to be broken down into task actions
            NdefMessage message = new NdefMessage(NFCUtil.buildNdefRecord(ImportTagActivity.this, getPackageName(), payload, Locale.ENGLISH, true));
            
            parseMessages(new NdefMessage[] {message});
            
        } else {
            /* Set  up NFC adapter and listen for tag */
            try {
                mAdapter = NfcAdapter.getDefaultAdapter(mContext);
            } catch (Exception e) {
                Logger.e("NFCT", "Error getting NFC Adapter " + e);
            }

            setContentView(R.layout.tag_import);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow_back_black);
            toolbar.setTitle(getString(R.string.importTitle));

            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.setResult(RESULT_OK);
        this.finish();
    }

    @Override
    public void onNewIntent(Intent intent) {

        mTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] msgs;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
            // Check if these are the message we care about!
            if ((msgs != null) && (msgs.length > 0)) {
                Logger.d("Found messages, parsing");
                parseMessages(msgs);
            }

        } else {
            // Check to see if this is a read-only tag that doesn't have a payload but is mapped on the server
            Logger.d("No NDEF Messages, checking web mapping");
            new TagLoader().execute(NFCUtil.getTagUuidAsString(mTag));
        }

    }

    private void initNewTag() {
        mActionsOne = Lists.newArrayList();
        mTagNameOne = "";
        mActionsTwo = Lists.newArrayList();
        mTagNameTwo = "";
    }
    
    private boolean foundTag() {
        return ((!mTagNameOne.isEmpty())|| (mActionsOne.size() > 0));
    }
    
    private void parseMessages(NdefMessage[] msgs) {
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();
        initNewTag();
        // 1+ NDEF messages have been found, parse and see if they have content
        // we care about
        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            if (record.getClass().getName() == TextRecord.class.getName()) {
                // Got a text tag here - parse it and see if it has what we are
                // looking for!
                TextRecord tr = (TextRecord) record;
                String recordText = tr.getText();
                Logger.d(Constants.TAG, "Found " + recordText);
                processMessage(recordText);
            }
        }
        
        if (!foundTag()) {
            // This means we've found one or more Ndef Messages but the parse didn't yield anything useful. 
            // Query web service to see if we have a mapped payload
            new TagLoader().execute(NFCUtil.getTagUuidAsString(mTag));
            
        } else {
            dispatchTag();
        }
        
    }
    
    private void dispatchTag() {


        Task tagOne = null;
        Task tagTwo = null;
       
        if ((mTagNameOne == null) || (mTagNameOne.isEmpty())){
            if (mExample ==  null) {
                mTagNameOne = Utils.getNextTagName(ImportTagActivity.this);
            } else {
                mTagNameOne = mExample.getName(this);
            }
        }
        
        tagOne = new Task(null, mTagNameOne, "", mActionsOne);
        
        TaskSet set = new TaskSet();
        if (mType != 0) {
            set.addTrigger(new Trigger(mType, mCondition, mKey1, mKey2));
        } else {
            set.addTrigger(Trigger.getNfcTrigger());
        }
        
        set.addTask(tagOne);
        
        /* Save examples directly */
        if (mExample != null) {
            String id = DatabaseHelper.saveTask(this, tagOne, mType, mCondition, mKey1, mKey2, false);
            for (Trigger trigger: set.getTriggers()) {
                DatabaseHelper.saveTrigger(this, trigger, id);
            }

            Usage.logMixpanelEvent(null, "example created", true, new String[] { "name", tagOne.getName()});
            Utils.checkReceivers(this);
            for (Trigger trigger: set.getTriggers()) {
                switch (trigger.getType()) {
                    case TaskTypeItem.TASK_TYPE_GEOFENCE:
                        GeofenceTrigger.registerGeofences(this);
                        break;
                    case TaskTypeItem.TASK_TYPE_TIME:
                        TimeTrigger.scheduleTimeTasks(this);
                        break;
                    case TaskTypeItem.TASK_TYPE_NFC:
                        Intent intent = new Intent(this, WriteTagActivity.class);
                        intent.putExtra(Constants.EXTRA_SAVED_TAG, new Task[] { tagOne });
                        startActivity(intent);
                        break;
                }
            }

            Toast.makeText(this, String.format(getString(R.string.example_created), tagOne.getName()), Toast.LENGTH_LONG).show(); 
            setResult(RESULT_OK, new Intent().putExtra("show_tasks_fragment", true));
            finish();
            return;
            
        }
        
        
        if (mNumTags > 1) {
            if ((mTagNameTwo == null) || (mTagNameTwo.isEmpty())){
                mTagNameTwo = Utils.getNextTagName(ImportTagActivity.this);
            }
            tagTwo = new Task(null, mTagNameTwo, "", mActionsTwo);
            set.addTask(tagTwo);
        }
        
        Intent intent = new Intent(ImportTagActivity.this, TaskWizardActivity.class);
        intent.putExtra(Trigger.EXTRA_TRIGGER, set.getTriggers());
        intent.putExtra(TaskSet.EXTRA_TASK, set);
        startActivityForResult(intent, 1);

    }
    
    private void processMessage(String message) {
     // Check if this is a switch or task tag
        if (message.length() > 0) {
            if (message.substring(0, 1).equals(Constants.COMMAND_TOGGLE_PROFILE)) {
                Logger.d("Found a switch tag");
                message = message.substring(0);
                String[] messages = message.split(Constants.SWITCH_SEPARATOR);
                mNumTags = 2;
                processMessage(messages[0], 1);
                processMessage(Constants.COMMAND_TOGGLE_PROFILE + ":" + messages[1], 2);
            } else {
                Logger.d("Found a task tag");
                mNumTags = 1;
                processMessage(message, 1);
            }
        }
    }
    private void processMessage(String message, int tagNum) {
  
        ArrayList<SavedAction> actions = Lists.newArrayList();
        String name = "";
        
        String[] commands = message.split(";");
        Logger.d("Broken into " + commands.length + " commands");
        String displayText = "";
        
        for (String c : commands) {
            // Look at each command
            String command = c;
            Logger.d("Checking " + command);
            String[] commandArgs = command.split(":");
            if (commandArgs[0].equals(Constants.COMMAND_TAG_ID)) {
                // This is Command:ID:Name for Task Tags
                if (commandArgs.length > 2) {
                    name = commandArgs[2];
                }
            } else if (commandArgs[0].equals(Constants.COMMAND_TOGGLE_PROFILE)) {
                // This is part of a switch tag
                if (commandArgs.length > 3) {
                    name = commandArgs[3];
                }
                    
                    int newLength = commandArgs.length - 4;
                    command = "";
                    if (newLength > 0) {
                        String[] temp = new String[newLength];
                        for (int i=0; i< newLength; i++) {
                            temp[i] = commandArgs[4 + i];
                            if (!command.isEmpty()) {
                                command += ":";
                            }
                            command += commandArgs[4+i];
                        }
                        commandArgs = temp;
                    }
            } else if ((commandArgs.length > 2) && commandArgs[1].equals(Constants.COMMAND_TAG_NAME)) {
                if (commandArgs.length > 1) {
                    name = commandArgs[2];
                    
                    // Strip off ID:command:Name
                    int newLength = commandArgs.length - 3;
                    command = "";
                    if (newLength > 0) {
                        String[] temp = new String[newLength];
                        for (int i=0; i< newLength; i++) {
                            temp[i] = commandArgs[3 + i];
                            if (!command.isEmpty()) {
                                command += ":";
                            }
                            command += commandArgs[3+i];
                        }
                        
                        commandArgs = temp;
                    }
                }
            } 
            
            if ((commandArgs[0].equals(Constants.COMMAND_ENABLE)) || (commandArgs[0].equals(Constants.COMMAND_DISABLE)) || (commandArgs[0].equals(Constants.COMMAND_TOGGLE))) {
                String[] args;

                if ((commandArgs.length - 2) > 0) {
                    args = new String[commandArgs.length - 2];
                    for (int j = 2; j < commandArgs.length; j++) {
                        Logger.d("Adding " + commandArgs[j] + " to " + (j - 1));
                        args[j - 2] = commandArgs[j];
                    }

                } else
                    args = new String[0];

                displayText = Utils.getDisplayTextFromAction(commandArgs[0], commandArgs[1], args, this);
                
            } else {
                String[] args;

                if ((commandArgs.length - 1) > 0) {
                    args = new String[commandArgs.length - 1];
                    for (int j = 1; j < commandArgs.length; j++) {
                        Logger.d("Adding " + commandArgs[j] + " to " + (j - 1));
                        args[j - 1] = commandArgs[j];
                    }

                } else
                    args = new String[0];

                displayText = Utils.getDisplayTextFromAction("", commandArgs[0], args, this);

            }

            if (!displayText.equals("")) {
                Logger.d("Adding " + command + " to actions");
                actions.add(new SavedAction(command, displayText, "", ""));
            }
        }
        
        if (tagNum == 1) {
            mTagNameOne = name;
            mActionsOne = actions;
        } else {
            mTagNameTwo = name;
            mActionsTwo = actions;
        }
    } 
    
    private class TagLoader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            return TagstandManager.getTag(ImportTagActivity.this, args[0], null);
        }
        
        protected void onPostExecute(String payload) {
            if (!payload.isEmpty() && !payload.equals(TagstandManager.REQUEST_FAILED) && (!payload.equals(TagstandManager.REQUEST_NOT_MAPPED))) {
                initNewTag();
                processMessage(payload);
                if (foundTag()) {
                    dispatchTag();
                }
            } else {
                /* Prompt user to create a tag using this NFC tag */
                AlertDialog.Builder builder = new AlertDialog.Builder(ImportTagActivity.this);
                builder.setMessage(getString(R.string.import_create_tag));
                builder.setPositiveButton(getString(R.string.dialogYes), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadCreateTag();
                    }
                    
                });
                builder.setNegativeButton(getString(R.string.dialogNo), null);
                if (!this.isCancelled()) {
                    builder.create().show();
                    Toast.makeText(ImportTagActivity.this, "No Data Found", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    
    private void loadCreateTag() {
        Intent intent = new Intent(this, TaskTypeNfc.getActivityClass());
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeNfc.getExtraValue());
        intent.putExtra(TaskTypeItem.EXTRA_LAYOUT_ID, TaskTypeNfc.getLayoutId());
        startActivity(intent);
    }
}
