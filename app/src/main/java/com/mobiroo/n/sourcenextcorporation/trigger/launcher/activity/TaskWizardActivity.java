package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.AlertDialog;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.CalendarTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.ChargingTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.TagBuilderFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.TriggerFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.AgentTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BluetoothTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.HeadsetTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.TimeTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.WifiTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TaskDataChangedListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TaskStateListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.StepPagerStrip;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krohnjw
 *
 */
public class TaskWizardActivity extends FragmentActivity implements ResultCallback<Status>, TaskDataChangedListener {

    public static final String EXTRA_TRIGGER = "com.trigger.extra_trigger";

    public static final String EXTRA_TAG_ID         = "com.trigger.launcher.EXTRA_TAG_ID";
    public static final String EXTRA_TAG_TWO_ID     = "com.trigger.launcher.EXTRA_TAG_TWO_ID";
    public static final String EXTRA_TAG_NAME       = "com.trigger.launcher.EXTRA_TAG_NAME";
    public static final String EXTRA_TAG_TWO_NAME   = "com.trigger.launcher.EXTRA_TAG_TWO_NAME";
    public static final String EXTRA_TASK_IS_NEW    = "com.trigger.launcher.EXTRA_TAG_IS_NEW";
    public static final String EXTRA_SAVED_TASK     = "com.trigger.launcher.EXTRA_SAVED_TASK";
    public static final String EXTRA_PRELOADED_NAME = "com.trigger.launcher.EXTRA_PRE_LOADED_NAME";
    public static final String EXTRA_IS_VALID       = "com.trigger.launcher.EXTRA_IS_VALID";
    public static final String EXTRA_FRAGMENT_NUM   = "com.trigger.launcher.EXTRA_FRAGMENT_NUM";


    public static final int REQUEST_ADD_ACTION      = 1;
    public static final int REQUEST_WRITE_TAG       = 2;
    public static final int REQUEST_SHOW_INTRO      = 3;
    public static final int REQUEST_EDIT_ACTIONS    = 4;
    public static final int REQUEST_SELECT_TRIGGER  = 5;
    public static final int REQUEST_EDIT_TRIGGER    = 6;
    
    
    public static final int RESULT_ADD_CLICKED =    1000;
    public static final int RESULT_WRITE_CLICKED =  1001;
    public static final int RESULT_BACK_PRESSED =   1002;

    private final String    HELP_URL = "http://sites.google.com/site/triggerwiki/home";

    @SuppressWarnings("unused")
    private boolean                 mIsPhoneLayout;
    private int                     mWidthDp;
    private int                     mHeightDp;

    private Toolbar               mBar;
    private boolean                 mIsNewTask;

    private Bundle                  mBundle_1;
    private Bundle                  mBundle_2;
    private Bundle                  mTriggerBundle;
    
    private FragmentAdapter         mPagerAdapter;
    private ViewPager               mPager;

    private final int               mPositionTriggerFragment         = 0;
    private final int               mPositionTaskBuilderPrimary      = 1;
    private final int               mPositionTaskBuilderSecondary    = 2;

    private TaskSet mTaskSet;
    
    private Task mTask1;
    private Task    mTask2;
    
    private ArrayList<Trigger> mTriggers;

    private Button    mButtonPrevious;
    private Button    mButtonNext;

    private SparseArray<TaskStateListener> mPendingListeners;
    
    @SuppressWarnings("unused")
    private GoogleApiClient         mLocationClient;
    private String                  mPendingGeoLatitude;
    private String                  mPendingGeoLongitude;
    private String                  mPendingGeoRadius;
    private String                  mPendingGeoId;
    private int                     mPendingGeoTransition;
    private GeofenceClient mGeoClient;
    private List<Geofence>          mFences;
    
    private boolean                 mSavingTriggers;
    private boolean                 mFinishWhenCompleted;
    private boolean                 mIsDirty = false;
    private boolean                 mHasNfcTrigger = false;
    
    private TriggerFragment         mTriggerFragment;
    private TagBuilderFragment      mTask1Fragment;
    private TagBuilderFragment      mTask2Fragment;

    private Menu                    mMenu;

    private int                     mCurrentPage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_wizard);
        
        mButtonPrevious = (Button) findViewById(R.id.button_previous);
        mButtonPrevious.setVisibility(View.INVISIBLE); // Hide on first screen

        mButtonNext = (Button) findViewById(R.id.button_next);

        setupActionBar();
        setupRenderedLayoutDetails();
        setupUsageLogging();
        
        if (savedInstanceState == null) {
            loadDataFromIntent();
            setupTaskBundles();
        }
        
        buildLayout(savedInstanceState);
        
        updateStepPager();
        
        checkNfcTriggerPresent();
        
        mIsDirty = false;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_tag_builder_icons_only, mMenu);
        mMenu.findItem(R.id.menu_remove).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMenu == null)
            mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.add_action:
                addClicked();
                return true;
            case R.id.menu_help:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(HELP_URL));
                startActivity(intent);
                return true;
            case R.id.menu_remove:
                clearSwitchFragment();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (!mIsDirty) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        
        new AlertDialog.Builder(this)
        .setMessage(R.string.tag_unsaved_changes)
        .setPositiveButton(R.string.discard_changes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                finish();
                TaskWizardActivity.super.onBackPressed();

            }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
        
    }
    
    private void updateStepPager() {
        ((StepPagerStrip) findViewById(R.id.strip)).setPageCount(mPagerAdapter.getCount());
    }
    
    public void addListener(TaskStateListener listener, int tag) {
        Logger.d("Adding a listener for " + tag);
        if (mPendingListeners == null) {
            mPendingListeners = new SparseArray<TaskStateListener>();
        }
        mPendingListeners.put(tag, listener);
    }
    
    private void addClicked() {
        if (mPendingListeners != null){
            try {
                mPendingListeners.get(mCurrentPage).addClicked(mPager.getCurrentItem());
            } catch (Exception e) {
                Logger.e("Exception passing clicked event on: " + e, e);
            }
        } else {
            Logger.d("No registered listeners");
        }
    }
    
    public void buttonClicked(View v) {
        switch (v.getId()) {
            case R.id.button_previous:
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                break;
            case R.id.button_next:
                if (mPager.getCurrentItem() == (mPagerAdapter.getCount() - 1)) {
                    finishTaskBuilder();                            
                } else {
/*                    Usage.logEvent(
                            Usage.getAnalyticsObject(TaskWizardActivity.this),
                            "Task next clicked",
                            false
                    );*/
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
                break;
        }
    }
    
    private void setupActionBar() {
        mBar = (Toolbar) findViewById(R.id.toolbar);
        mBar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_white);
        mBar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //mBar.setTitle(R.string.title_configure_triggers);

    }

    private void setupRenderedLayoutDetails() {
        mWidthDp = Utils.getWidthInDp(this);
        mHeightDp = Utils.getHeightInDp(this);
        mIsPhoneLayout = ((mHeightDp > 1200) || (mWidthDp > 1200))? false : true;
    }

    private void setupUsageLogging() {
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        mIsNewTask = intent.getBooleanExtra(EXTRA_TASK_IS_NEW, true);

        if (intent.hasExtra(TaskSet.EXTRA_TASK)) {
            Logger.d("Importing task set");
            
            mTaskSet = intent.getParcelableExtra(TaskSet.EXTRA_TASK);
            ArrayList<Task> tasks = mTaskSet.getTasks();
            if (tasks != null) {
                Logger.d("Received " + tasks.size());
                if (tasks.size() > 1) {
                    mTask2 = tasks.get(1);
                    if ((mTask2.getActions() == null) || (mTask2.getActions().size() == 0)) {
                        try { mTask2.loadActions(this); }
                        catch (Exception e) {}
                    }
                }
                
                if (tasks.size() > 0) {
                    mTask1 = tasks.get(0);
                    if ((mTask1.getActions() == null) || (mTask1.getActions().size() == 0)) {
                        try { mTask1.loadActions(this); }
                        catch (Exception e) {}
                    }
                } 
            }
            
            mTriggers = mTaskSet.getTriggers();
            if (mTriggers.size() > 0) {
                for (Trigger t: mTriggers) {
                    t.loadConstraints(this);
                }
            }
            
        }
        
        if (mIsNewTask) {
            Usage.logEvent(null, "Start New Task", false);
        }
    }

    private void setupTaskBundles() {

        mTriggerBundle = new Bundle();
        if (mTaskSet != null) {
            mTriggerBundle.putParcelableArrayList(TaskSet.EXTRA_TRIGGERS, mTaskSet.getTriggers());
        }
        
        mBundle_1 = new Bundle();

        if (mTaskSet != null) {
            if (mTaskSet.getTask(0) != null) {
                mBundle_1.putParcelable(Constants.EXTRA_IMPORTED_TAG, mTaskSet.getTask(0));
            }
        }
        
        /* Setup bundle for primary task */
        if (getIntent().hasExtra(EXTRA_TAG_ID)) {
            mBundle_1.putString(TaskWizardActivity.EXTRA_TAG_ID,
                    (String) getIntent().getStringExtra(EXTRA_TAG_ID));
        } else if (getIntent().hasExtra(Constants.EXTRA_IMPORTED_TAG_ONE)) {
            mBundle_1.putParcelable(Constants.EXTRA_IMPORTED_TAG,
                    getIntent().getParcelableExtra(Constants.EXTRA_IMPORTED_TAG_ONE));
        } else if (getIntent().hasExtra(Constants.EXTRA_SAVED_TAG_REUSE)) {
            mBundle_1.putParcelable(Constants.EXTRA_IMPORTED_TAG, 
                    getIntent().getParcelableExtra(Constants.EXTRA_SAVED_TAG_REUSE));
        } else if (getIntent().hasExtra(Constants.EXTRA_PRELOAD_NAME)){
            mBundle_1.putString(Constants.EXTRA_PRELOAD_NAME, 
                    getIntent().getStringExtra(Constants.EXTRA_PRELOAD_NAME));
        }

        if (getIntent().hasExtra(Constants.EXTRA_PRELOADED_ACTIONS)) {
            /* Check for pre-loaded codes here, launch Activity picker if they exist */
            Parcelable[] actions = getIntent().getParcelableArrayExtra(Constants.EXTRA_PRELOADED_ACTIONS);
            if (actions != null) {
                mBundle_1.putParcelableArray(Constants.EXTRA_PRELOADED_ACTIONS, actions);
            }
        }

        mBundle_1.putBoolean(EXTRA_IS_VALID, true);
        mBundle_1.putInt(EXTRA_FRAGMENT_NUM, 1);


        mBundle_2 = new Bundle();

        if (getIntent().hasExtra(EXTRA_TAG_TWO_ID)) {
            mBundle_2.putString(EXTRA_TAG_TWO_ID, 
                    (String) getIntent().getStringExtra(EXTRA_TAG_TWO_ID));
            mBundle_2.putString(EXTRA_TAG_TWO_NAME, 
                    (String) getIntent().getStringExtra(EXTRA_TAG_TWO_NAME));
            mBundle_2.putBoolean(EXTRA_IS_VALID, true);

        }  else if (getIntent().hasExtra(Constants.EXTRA_IMPORTED_TAG_TWO)) {
            mBundle_2.putParcelable(Constants.EXTRA_IMPORTED_TAG,
                    getIntent().getParcelableExtra(Constants.EXTRA_IMPORTED_TAG_TWO));
            mBundle_2.putBoolean(EXTRA_IS_VALID, true);

        } else {
            mBundle_2.putBoolean(EXTRA_IS_VALID, false);
        }
        
        if (mTaskSet != null) {
            if (mTaskSet.getTask(1) != null) {
                mBundle_2.putParcelable(Constants.EXTRA_IMPORTED_TAG, mTaskSet.getTask(1));
                mBundle_2.putBoolean(EXTRA_IS_VALID, true);
            }
        }
        
        mBundle_2.putInt(EXTRA_FRAGMENT_NUM, 2);

    }

    private void setActionBarTitle(int position) {
        switch(position) {
            case 0:
                //mBar.setTitle(R.string.title_configure_triggers);
                break;
            case 1:
                //mBar.setTitle(R.string.title_configure_task);
                break;
            case 2:
                //mBar.setTitle(R.string.title_configure_switch);
                break;
        }
    }
    
    private void buildLayout(Bundle state) {

        Logger.d("BuildLayout called");
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(2);

        mPagerAdapter = new FragmentAdapter(getSupportFragmentManager(), this, mPager);
       

        checkNfcTriggerPresent();

        mTriggerFragment = new TriggerFragment();
        mTriggerFragment.setArguments(mTriggerBundle);
        mTriggerFragment.setTag(mPositionTriggerFragment);

        mTask1Fragment = new TagBuilderFragment();
        mTask1Fragment.setArguments(mBundle_1);
        mTask1Fragment.setTag(mPositionTaskBuilderPrimary);

        if (mHasNfcTrigger) {
            mTask2Fragment = new TagBuilderFragment();
            mTask2Fragment.setArguments(mBundle_2);
            mTask2Fragment.setTag(mPositionTaskBuilderSecondary);
        }


        mPagerAdapter.addFragment(mTriggerFragment, "triggers");
        mPagerAdapter.addFragment(mTask1Fragment, "task 1");
        if (mTask2Fragment != null) {
            mPagerAdapter.addFragment(mTask2Fragment, "task 2");
        }

        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int position, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                mButtonPrevious.setEnabled((position == 0) ? false : true);
                setButtonTitles(position);
                setActionBarTitle(position);

                ((StepPagerStrip) findViewById(R.id.strip)).setCurrentPage(position);

                if(position != 0) {
                    FragmentLifecycle fragmentToShow = (FragmentLifecycle) mPagerAdapter.getItem(position);
                    fragmentToShow.onResumeFragment();
                }


                mCurrentPage = position;

                try {
                    mMenu.findItem(R.id.menu_remove).setVisible((position > 1) ? true : false);
                } catch (Exception e) {
                    /* OK if this fails */
                }
            }

        });
        mPager.setAdapter(mPagerAdapter);
    }

    private void setButtonTitles(int position) {

        mButtonPrevious.setVisibility((position == 0) ? View.INVISIBLE : View.VISIBLE);

        if ((!mHasNfcTrigger) && (position == 1) ||  (position > 1)) {
            mButtonNext.setText(R.string.menu_done);
            
        } else {
            mButtonNext.setText(R.string.next);
        }
    }



    private static class FragmentAdapter extends BetterFragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm, FragmentActivity activity, ViewPager pager) {
            super(fm);
        }
    }

    private void checkNfcTriggerPresent() {
        mHasNfcTrigger = false;
        if (mTriggers != null) {
            for (Trigger t: mTriggers) {
                mHasNfcTrigger |= ((t.getType() == TaskTypeItem.TASK_TYPE_NFC) || (t.getType() == TaskTypeItem.TASK_TYPE_SWITCH));
            }
        }
        
        if ((!mHasNfcTrigger) && (mPagerAdapter.getCount() > 2)) {
           removeSwitchFragment();
        } else if ((mHasNfcTrigger) && (mPagerAdapter.getCount() == 2)) {
            addSwitchFragment();
        }
    }

    private void removeSwitchFragment() {
        mPagerAdapter.removeFragment(2);
        mPagerAdapter.notifyDataSetChanged();
        updateStepPager();
    }

    private void addSwitchFragment() {
        setupTaskBundles();
        mTask2Fragment = new TagBuilderFragment();
        mTask2Fragment.setArguments(mBundle_2);
        mTask2Fragment.setTag(mPositionTaskBuilderSecondary);
        mPagerAdapter.addFragment(mTask2Fragment, "task 2");
        mPagerAdapter.notifyDataSetChanged();
        updateStepPager();
    }

    private void clearSwitchFragment() {
        Logger.d("Calling clear");
        mTask2Fragment.showIntro();
    }

    /* Listener implementation */
    @Override
    public void triggersChanged(ArrayList<Trigger> triggers) {
        mIsDirty = true;
        mTriggers = triggers;
        checkNfcTriggerPresent();
    }

    @Override
    public void taskChanged(Task task, int tag) {
        mIsDirty = true;
        switch (tag) {
            case mPositionTaskBuilderPrimary:
                mTask1 = task;
                break;
            case mPositionTaskBuilderSecondary:
                mTask2 = task;
                break;
        }

    }
    
    @Override
    public void signalLoadFinished() {
        mIsDirty = false;
    }
    
    private void finishTaskBuilder() {
        
        if ((mTask1 == null)) {
            // This *shouldn't* happen
            Logger.d("Task1 is null");
            return;
        }
        
        if (mTask1.getName().length() < 1) {
            Toast.makeText(this, R.string.builderInvalidName, Toast.LENGTH_SHORT).show();
            mPager.setCurrentItem(1);
            return;
        }
     
        if ((mTask2 != null) && (mTask2.getName().length() < 1)) {
            Toast.makeText(this, R.string.builderInvalidName, Toast.LENGTH_SHORT).show();
            mPager.setCurrentItem(2);
            return;
        }
        
        if ((mTriggers == null) || (mTriggers.size() == 0)) {
            Toast.makeText(this, R.string.no_triggers, Toast.LENGTH_SHORT).show();
            mPager.setCurrentItem(0);
            return;
        }
        
        if (mTask1.getActions().size() == 0) {
            SimpleDialogFragment dialog = new SimpleDialogFragment();
            dialog.setTitle(getString(R.string.no_actions_found));
            dialog.setMessage(String.format(getString(R.string.no_actions_found_message), mTask1.getName()));
            dialog.setPositiveButton(getString(R.string._continue), new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mIsNewTask) { 
                        Usage.logEvent(null, "Saved New Task", false);
                    }
                    
                    new SaveAndFinishTask(TaskWizardActivity.this, mTriggers, mTask1, mTask2).execute();

                }
                
            });
            
            dialog.setNegativeButton(getString(R.string.go_back), null);
            dialog.show(getSupportFragmentManager(), "no_actions_dialog");
        } else {
            // We have a task with a valid name, proceed
            
            if (mIsNewTask) { 
                Usage.logEvent(null, "Saved New Task", false);
            }
            
            new SaveAndFinishTask(this, mTriggers, mTask1, mTask2).execute();
        }
    }
   
    
    private class SaveAndFinishTask extends AsyncTask<Void, Void, String[]> {
        private ArrayList<Trigger> mTriggers;
        private Task[] mTasks;
        private Context mContext;
        
        public SaveAndFinishTask(Context context, ArrayList<Trigger> triggers, Task... tasks) {
            mContext = context;
            mTriggers = triggers;
            mTasks = tasks;
        }
        
        private String saveTask(Task... tasks) {
            if ((tasks.length == 2) && (tasks[1] != null) && (mTask2Fragment != null) && (mTask2Fragment.mIsValid)){
                Logger.d("Saving switch");
                return DatabaseHelper.saveTask(mContext, tasks[0], tasks[1], false);
            } else {
                Logger.d("Saving task");
                if (tasks.length == 2) {
                    if (tasks[1] != null) {
                        Logger.d("Found second task but has been marked invalid - Removing " + tasks[1].getId());
                        tasks[1].delete(TaskWizardActivity.this);
                    }
                    tasks[0].setSecondaryId(null);
                    tasks[0].setSecondaryName(null);
                }
                return DatabaseHelper.saveTask(mContext, tasks[0], null, false);
            }
        }
        
        private String saveTrigger(Trigger trigger, String taskId) {
            return DatabaseHelper.saveTrigger(TaskWizardActivity.this, trigger, taskId);
        }
        
        @Override
        protected String[] doInBackground(Void... params) {
 
            String id = (mTasks.length > 1) ? saveTask(mTask1, mTask2) : saveTask(mTask1);
            
            // Delete all existing triggers and constraints for this task
            DatabaseHelper.deleteTriggersForTask(TaskWizardActivity.this, id);
            
            // Use ID to save triggers
            for (Trigger trigger: mTriggers) {
                String triggerId = saveTrigger(trigger, id);
                trigger.setId(triggerId);
            }
            
            BackupManager.dataChanged(getPackageName());
            String account = SettingsHelper.getPrefString(TaskWizardActivity.this, OAuthConstants.SSO_OAUTH_ACCOUNT_NAME,"");

            if (!account.isEmpty()) {
                int count = DatabaseHelper.getTaskCount(TaskWizardActivity.this);
                Usage.logUserProperty(TaskWizardActivity.this, "tasks_created", count);
            }

            return new String[] { id };
        }
        
        @Override
        public void onPostExecute(String[] results) {
            // Check triggers and do updates as necessary (save geofence for example)
            mSavingTriggers = true;
            mFinishWhenCompleted = true;
            
            for (Trigger trigger: mTriggers) {
                switch (trigger.getType()) {
                    case TaskTypeItem.TASK_TYPE_NFC:
                    case TaskTypeItem.TASK_TYPE_SWITCH:
                        Intent intent = new Intent(TaskWizardActivity.this, WriteTagActivity.class);
                        intent.putExtra(Constants.EXTRA_FORCE_MAPPED, (trigger.getConstraints().size() > 0));
                        if ((mTask1 != null) && (mTask2 != null)) {
                            intent.putExtra(Constants.EXTRA_ACTION_NAME, Constants.TAG_PROFILE);
                            intent.putExtra(Constants.EXTRA_SAVED_TAG, new Task[] { mTask1, mTask2});
                        } else {
                            intent.putExtra(Constants.EXTRA_ACTION_NAME, Constants.TAG_ACTION);
                            intent.putExtra(Constants.EXTRA_SAVED_TAG_NAME, mTask1.getName());
                            intent.putExtra(Constants.EXTRA_TAG_ID,         results[0]);
                            intent.putExtra(Constants.EXTRA_SAVED_TAG, new Task[] { mTask1 });
                        }
                        mFinishWhenCompleted = false;
                        startActivityForResult(intent, REQUEST_WRITE_TAG);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case TaskTypeItem.TASK_TYPE_BLUETOOTH:
                        BluetoothTrigger.enable(getApplicationContext());
                        break;
                    case TaskTypeItem.TASK_TYPE_WIFI:
                        WifiTrigger.enable(getApplicationContext());
                        break;
                    case TaskTypeItem.TASK_TYPE_GEOFENCE:
                        mFinishWhenCompleted = false;
                        addGeofence(results[0], trigger);
                        break;
                    case TaskTypeItem.TASK_TYPE_BATTERY:
                        BatteryTrigger.enable(getApplicationContext());
                        break;
                    case TaskTypeItem.TASK_TYPE_TIME:
                        TimeTrigger.scheduleTimeTask(getApplicationContext(), trigger.getId(), trigger.getExtra(1), trigger.getExtra(2), results[0]);
                        break;
                    case TaskTypeItem.TASK_TYPE_CALENDAR:
                        CalendarTrigger.scheduleNextEvent(getApplicationContext());
                        break;
                    case TaskTypeItem.TASK_TYPE_HEADSET:
                        HeadsetTrigger.enable(getApplicationContext());
                        break;
                    case TaskTypeItem.TASK_TYPE_CHARGER:
                        ChargingTrigger.enable(getApplicationContext());
                        break;
                    case TaskTypeItem.TASK_TYPE_AGENT:
                        AgentTrigger.enable(getApplicationContext());
                        break;

                }
            }

            Utils.signalTasksChanged(TaskWizardActivity.this);

            mSavingTriggers = false;
            
            if (mFinishWhenCompleted) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
     
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_WRITE_TAG:
                setResult(RESULT_OK, data);
                finish();
                break;
            default:
                if (mPendingListeners != null){
                    Logger.d("Passing on request code " + requestCode);
                    try { mPendingListeners.get(mCurrentPage).notifyResult(requestCode, resultCode, data); }
                    catch (Exception e) { Logger.e("Exception notifying current page " + mCurrentPage + ": e", e); }
                } else {
                    Logger.d("Listeners list is null");
                }
                break;
        } 
    }
    
    public void addGeofence(String tagId, Trigger trigger) {
        
        String[] location = trigger.getExtra(1).split(",");
        if (location.length < 2) {
            Logger.d("Cannot add this fence, did not get a valid location: " + trigger.getExtra(1));
            return;
        }
        
        float radius = Float.parseFloat(trigger.getExtra(2));
        Logger.d("Setting radius to " + radius);
        
        if (BuildConfiguration.USE_GEOFENCES) {
            Geofence.Builder builder = new Geofence.Builder();

            mPendingGeoLatitude = location[0];
            mPendingGeoLongitude = location[1];
            mPendingGeoRadius = trigger.getExtra(2);
            mPendingGeoId = trigger.getId();
    
            Logger.i("TaskCondition = " + trigger.getCondition());
            mPendingGeoTransition = (DatabaseHelper.TRIGGER_ENTER.equals(trigger.getCondition())) ? Geofence.GEOFENCE_TRANSITION_DWELL : Geofence.GEOFENCE_TRANSITION_EXIT;
    
            Logger.d("Adding new Geofence for " +
                    trigger.getId() + " at " +
                    mPendingGeoLatitude + "," +
                    mPendingGeoLongitude + " : " + 
                    mPendingGeoRadius + " m on: " +
                    mPendingGeoTransition);
    
            builder.setCircularRegion(
                    Double.parseDouble(location[0]), 
                    Double.parseDouble(location[1]), 
                    radius)
                    .setNotificationResponsiveness(5000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(GeofenceClient.DWELL_DURATION)
                    .setRequestId(trigger.getId())
                    .setTransitionTypes(mPendingGeoTransition);
    
         
            Geofence newFence = builder.build();
            mFences = DatabaseHelper.getGeofences(this);
            boolean isNew = true;
            /* See if our new fence is already in the list */
            for (int i=0; i<mFences.size(); i++) {
                Geofence fence = mFences.get(i);
                if (fence.getRequestId().equals(newFence.getRequestId())) {
                    Logger.d("Fence exists, removing");
                    mFences.remove(i);
                }
            }
    
            if (isNew) {
                mFences.add(newFence);
            }
    
            mGeoClient = new GeofenceClient(this);
            mGeoClient.setGeofences(mFences);
            mGeoClient.setResultCallback(this);
            mGeoClient.connectAndSave();
        }
    }



    @Override
    public void onResult(Status status) {

        Logger.d("Got Geofence result " + status.isSuccess());

        if (status.isSuccess()) {
            DatabaseHelper.saveGeofence(this,
                    mPendingGeoId,
                    mPendingGeoLatitude,
                    mPendingGeoLongitude,
                    mPendingGeoRadius,
                    mPendingGeoTransition);
            setResult(RESULT_OK);
            finish();
        }

        else {
            Logger.d("Error saving geofence");
        }

        if (!mSavingTriggers) {
            setResult(RESULT_OK);
            finish();
        } else {
            mFinishWhenCompleted = true;
        }
    }


    
    public void loadShop(View v) {
        setResult(RESULT_OK, new Intent().putExtra(Constants.EXTRA_LOAD_SHOP_FRAGMENT, true));
        finish();
    }
}
