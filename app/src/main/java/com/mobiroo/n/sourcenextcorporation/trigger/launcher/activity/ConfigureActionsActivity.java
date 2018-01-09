package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.Action;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.BaseAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.CallAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.EmailAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.PlaySoundAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.RingerSoundAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.SmsAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.UnifiedRemoteAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActionViewConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.PendingAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.NotificationSoundAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.TaskerAction;

import java.util.ArrayList;

public class ConfigureActionsActivity extends AppCompatActivity {
    
    public static final String EXTRA_ACTIONS = "actions";
    public static final String EXTRA_TASK = "task";
    public static final String EXTRA_POSITION = "position";
    
    
    private Context mContext;
    private ArrayList<SavedAction> mActions = new ArrayList<SavedAction>();
    private ArrayList<PendingAction> mPendingActions;
    private int mPendingPosition = -1;
    
    @Override
    public void onCreate(Bundle savedState) {
        mContext = this;
        super.onCreate(savedState);

        setContentView(R.layout.action_configure);

        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        
        Logger.d("AddTagOptions: Configure Actions");

        // Extra containing CSV String of user selected codes from ActionPickerActivity
        Intent intent = getIntent();
        
        String selectedActions = intent.getStringExtra(Constants.EXTRA_PENDING_ACTIONS);

        // Whether or not we are coming in from a pre-loaded tag
        Parcelable[] parcelActions = intent.getParcelableArrayExtra(Constants.EXTRA_PRELOADED_ACTIONS);  

        TextView title = (TextView) findViewById(android.R.id.title);
        title.setText(getTitle());
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(getResources().getColor(R.color.highlight_yellow));
        (findViewById(R.id.titleDivider)).setBackgroundColor(getResources().getColor(R.color.title_spacer_colored));
        
        mPendingActions = new ArrayList<PendingAction>();

        if (parcelActions != null) {
            ActionViewConfiguration[] pendingActionsList = new ActionViewConfiguration[parcelActions.length];
            for (int i=0; i< parcelActions.length; i++) {
                pendingActionsList[i] = (ActionViewConfiguration) parcelActions[i];
            }
            
            // Incoming is a list of codes from ActionPickerActivity
            for (ActionViewConfiguration config: pendingActionsList) {
                addIncomingAction(config.getCode(), config.getArguments());
            }
            showActionConfigLayouts(this);
        } else if (selectedActions != null) {
            String[] actions = selectedActions.split(",");
            for (String action: actions) {
                addIncomingAction(action, new CommandArguments());
            }
            showActionConfigLayouts(this);
        } else if (intent.hasExtra(EXTRA_TASK)) {
            /* This is for editing actions */
            Task task = intent.getParcelableExtra(EXTRA_TASK);
            mPendingPosition = intent.getIntExtra(EXTRA_POSITION, -1);
            if (task != null) {
                if (mPendingPosition < 0) {
                    for (SavedAction action: task.getActions()) {
                        
                        String code = action.getCode();
                        Action actionObject = BaseAction.getAction(code);
                        
                        CommandArguments args = actionObject.getArgumentsFromAction(action.getMessage());
                        
                        addIncomingAction(code, args);
                    }
                } else {
                    SavedAction action = task.getActions().get(mPendingPosition);
                    String code = action.getCode();
                    Action actionObject = BaseAction.getAction(code);
                    
                    CommandArguments args = actionObject.getArgumentsFromAction(action.getMessage());
                    
                    addIncomingAction(code, args);
                }
                showActionConfigLayouts(this);
            }
        }

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (PendingAction a : mPendingActions) {
                    if (a.Action != null) {
                        runClickEvent(a.Action, a.InflatedView, a.Code, mContext);
                    }
                }
                finishMe();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(ConfigureActionsActivity.this)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Usage.canLogData(ConfigureActionsActivity.this)) {
            EasyTracker.getInstance(this).activityStop(this);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }

    private void finishMe() {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_ACTIONS, mActions);
        if (mPendingPosition >= 0) {
            data.putExtra(EXTRA_POSITION, mPendingPosition);
        }
        data.putExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, getIntent().getIntExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, 1));
        this.setResult(RESULT_OK, data);
        this.finish();
    }

    private void addAction(String message, String prettyAction, String prettyName, String code) {
        Logger.d("AddAction: Adding " + message);
        mActions.add(new SavedAction(message, prettyAction, prettyName, code));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("AddTagOptions: Returning from " + requestCode + " with result " + resultCode);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Intent outData = new Intent();
                    this.setResult(RESULT_OK, outData);
                    finishMe();
                }
                break;
            case RingerSoundAction.ACTIVITY_PICK_RINGTONE:
                if (resultCode == RESULT_OK) {

                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Logger.d("URI is " + uri);
                    String ringerName = "";
                    int ringerPosition = -1;

                    Ringtone rt = null;
                    try { rt = RingtoneManager.getRingtone(this, uri);
                    } catch (Exception e) { Logger.e("Exception getting ringtone " + e.toString()); }
                    
                    if (rt != null) {
                        try { ringerName = rt.getTitle(this);
                        } catch (Exception e) {
                            Logger.d("Exception getting title");
                            ringerName = getString(R.string.ringerTypeSilent); 
                        }
                        
                        RingtoneManager rm = new RingtoneManager(mContext);
                        rm.setType(RingtoneManager.TYPE_ALL);
                        ringerPosition = rm.getRingtonePosition(uri);
                    } else {
                        ringerName = getString(R.string.ringerTypeSilent);
                        ringerPosition = -1;
                    }

                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof RingerSoundAction){
                            ((RingerSoundAction) a.Action).setRingtoneData(ringerName, ringerPosition, uri);
                            break;
                        }
                    }
                }
                
                break;
            case NotificationSoundAction.ACTIVITY_PICK_NOTIFICATION:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Logger.d("URI is " + uri);
                    String ringerName = "";
                    int ringerPosition = -1;
                    
                    Ringtone rt = null;
                    try { rt = RingtoneManager.getRingtone(this, uri);
                    } catch (Exception e) { Logger.e("Exception getting ringtone " + e.toString()); }
                    if ((rt != null) && (uri != null)) {
                        
                        try { ringerName = rt.getTitle(this);
                        } catch (Exception e) { ringerName = getString(R.string.ringerTypeSilent); }
                        
                        RingtoneManager rm = new RingtoneManager(this);
                        rm.setType(RingtoneManager.TYPE_NOTIFICATION);
                        ringerPosition = rm.getRingtonePosition(uri);
                    } else {
                        ringerName = getString(R.string.ringerTypeSilent);
                        ringerPosition = -1;
                    }

                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof NotificationSoundAction){
                            ((NotificationSoundAction) a.Action).setRingtoneData(ringerName, ringerPosition, uri);
                            break;
                        }
                    }
                }
                break;
            case PlaySoundAction.ACTIVITY_PICK_SOUND:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    String title = "";

                    Ringtone rt = null;
                    try {
                        rt = RingtoneManager.getRingtone(this, uri);
                    } catch (Exception e) { Logger.e("Exception getting ringtone " + e.toString()); }

                    if ((rt != null) && (uri != null)) {

                        try {
                            title = rt.getTitle(this);
                        } catch (Exception e) {
                            title = getString(R.string.ringerTypeSilent);
                        }

                    } else {
                        title = getString(R.string.ringerTypeSilent);
                    }


                    Logger.d("Data returned " + title + " - " + uri);

                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof PlaySoundAction){
                            ((PlaySoundAction) a.Action).setData(title, RingtoneManager.TYPE_ALL, uri);
                            break;
                        }
                    }
                }
                break;
            case TaskerAction.REQUEST_TASKER_LIST:
                if (resultCode == RESULT_OK) {
                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof TaskerAction){
                            String sTask = data.getDataString();
                            Logger.d("Tasker Task = " + sTask);
                            ((TaskerAction) a.Action).setTaskerTask(sTask);
                            break;
                        }
                    }
                }
                break;
            case SmsAction.REQUEST_PICK_CONTACT:
                if (resultCode == RESULT_OK) {
                    Uri result = data.getData();  
                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof SmsAction) {
                            ((SmsAction) a.Action).setContact(this, a.InflatedView, result);
                        }
                    }
                }
                break;
            case EmailAction.REQUEST_PICK_CONTACT:
                if (resultCode == RESULT_OK) {
                    Uri result = data.getData();  
                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof EmailAction) {
                         ((EmailAction) a.Action).setContact(this, a.InflatedView, result);
                        }
                    }
                }
                break;
            case CallAction.REQUEST_PICK_CONTACT:
                if (resultCode == RESULT_OK) {
                    Uri result = data.getData();  
                    for (PendingAction a: mPendingActions) {
                        if (a.Action instanceof CallAction) {
                            ((CallAction) a.Action).setContact(this, a.InflatedView, result);
                        }
                    }
                }
                break;
            case UnifiedRemoteAction.REQUEST_CODE_UNIFIED_REMOTE:
                    if (resultCode == RESULT_OK) {
                        String uri = data.getStringExtra(UnifiedRemoteAction.EXTRA_URI);
                        for (PendingAction a: mPendingActions) {
                            if (a.Action instanceof UnifiedRemoteAction) {
                                ((UnifiedRemoteAction) a.Action).setUri(uri);
                            }
                        }
                    }
                break;
        }
    }

    private void addIncomingAction(String code, CommandArguments config) {
        Action action = BaseAction.getAction(code);
        if (action.getName() != null) {
            addIncomingAction(action,config);
        } else {
            Logger.d("Did not find a valid action class for " + code);
        }
    }
    private void addIncomingAction(Action action, CommandArguments config) {
        
        if (action.getName() != null) {
            // getName is defined and not the BaseAction class
            addConfiguration(ConfigureActionsActivity.this, action, config, false);
        } 

    }

    private void addActionToList(PendingAction a) {
        if (mPendingActions == null) {
            mPendingActions = new ArrayList<PendingAction>();
        }
        mPendingActions.add(a);
    }
    
    public void addConfiguration(Context context, Action action, CommandArguments config, boolean showDialogWhenFinished) {
        PendingAction a = new PendingAction(action.getView(context, config), action.getCode(), action);
        addActionToList(a);
        if (showDialogWhenFinished) {
            showActionConfigLayouts(context);
        }
    }
    

    private void showActionConfigLayouts(Context context) {
        LinearLayout l = (LinearLayout) findViewById(R.id.pendingActions);
        for (PendingAction a : mPendingActions) {
            l.addView(a.InflatedView);
        }
    }


    private void runClickEvent(Action action, View actionView, String code, Context context) {

        action.logUsage(context, Codes.COMMAND_ADD, 0);
        String[] message = action.buildAction(actionView, context);

        try {

            if (message[Action.INDEX_ACTION].length() >= action.getMinArgLength()) {
                addAction(message[Action.INDEX_ACTION], message[Action.INDEX_MESSAGE_PREFIX], message[Action.INDEX_MESSAGE_SUFFIX], action.getCode());
            }
        }
        catch (Exception e) {
            Logger.e("Could not add message: " + e.toString(), e);
        }
    }
}
