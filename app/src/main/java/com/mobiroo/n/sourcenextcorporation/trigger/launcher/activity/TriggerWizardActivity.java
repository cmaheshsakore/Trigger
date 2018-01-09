package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.BaseFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger.TriggerConstraintFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ExampleTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TriggerDataChangedListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.StepPagerStrip;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterFragmentPagerAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.listener.TriggerStateListener;

import java.util.ArrayList;

public class TriggerWizardActivity extends FragmentActivity {

    public static final String  EXTRA_SSID                  = "com.trigger.launcher.EXTRA_SSID";
    public static final String  EXTRA_BT_NAME               = "com.trigger.launcher.EXTRA_BT_NAME";
    public static final String  EXTRA_BT_MAC                = "com.trigger.launcher.EXTRA_BT_MAC";
    public static final String  EXTRA_TRIGGER_CONDITION     = "com.trigger.launcher.EXTRA_TRIGGER_CONDITION";
    public static final String  EXTRA_TITLE                 = "com.trigger.launcher.EXTRA_TITLE";
    public static final String  EXTRA_TRIGGER               = "com.trigger.launcher.EXTRA_TRIGGER";
        
    private int             mTaskType;
    private Trigger mTrigger;
    private ViewPager       mPager;
    private FragmentAdapter mPagerAdapter;
    private BaseFragment mTriggerFragment;
    private TriggerConstraintFragment mConstraintFragment;
    
    private Bundle          mTriggerBundle;
    private ExampleTask mExample;
    private TextView        mButtonPrevious;
    private TextView        mButtonNext;
    private TriggerDataChangedListener mDataChangedListener;
    private boolean         mTaskIsNew;
    private String          mTitle;
    private SparseArray<TriggerStateListener> mListeners;
    
    public class DataChangedListener implements TriggerDataChangedListener {

        @Override
        public void TriggerUpdated(Trigger trigger) {
            mTrigger = trigger;
            
        }

        @Override
        public void ConstraintsUpdated(ArrayList<Constraint> constraints) {
            mTrigger.setConstraints(constraints);
        }
        
    }
    
    public void addListener(TriggerStateListener listener, int position) {
        if (mListeners == null) {
            mListeners = new SparseArray<TriggerStateListener>();
        }
        mListeners.put(position, listener);
    }
    
    private static class FragmentAdapter extends BetterFragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm, FragmentActivity activity, ViewPager pager) {
            super(fm);
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDataChangedListener = new DataChangedListener();
        
        // Passed in from an edit
        mTrigger =  getIntent().getParcelableExtra(Trigger.EXTRA_TRIGGER);

        int page = getIntent().getIntExtra(Trigger.EXTRA_PAGE, 0);

        if (mTrigger == null) {
            mTaskIsNew = true;
        } else {
            mTaskIsNew = false;
        }
        
        setContentView(R.layout.activity_trigger_wizard);
        
        mButtonPrevious = (TextView) findViewById(R.id.button_previous);
        mButtonNext = (TextView) findViewById(R.id.button_next);
        
        getDataFromIntent(getIntent());

        mTriggerFragment = TaskTypeItem.getFragmentFromType(mTrigger.getType());
        if ((mTriggerFragment == null) || (mTrigger == null)) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new FragmentAdapter(getSupportFragmentManager(), this, mPager);
        
        mTriggerBundle = new Bundle();
        mTriggerBundle.putParcelable(Trigger.EXTRA_TRIGGER, mTrigger);
        mTriggerFragment.setArguments(mTriggerBundle);
        mTriggerFragment.setListener(mDataChangedListener);
        mPagerAdapter.addFragment(mTriggerFragment, "trigger");

        if (mTriggerFragment.useConstraints()) {
            mConstraintFragment = new TriggerConstraintFragment();
            mConstraintFragment.setArguments(mTriggerBundle);
            mConstraintFragment.setListener(mDataChangedListener);
            mPagerAdapter.addFragment(mConstraintFragment, "constraint");
        } else {
            mButtonNext.setText(R.string.menu_done);
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
                setButtonTitles(position);
                setCustomTitle(position);
                
                ((StepPagerStrip) findViewById(R.id.strip)).setCurrentPage(position);
            }

        });
        mPager.setAdapter(mPagerAdapter);
        ((StepPagerStrip) findViewById(R.id.strip)).setPageCount(mPagerAdapter.getCount());
        
        /* Set title */
        mTitle = (getIntent().hasExtra(EXTRA_TITLE)) ? getIntent().getStringExtra(EXTRA_TITLE) : getCustomTitle();
        
        setCustomTitle(0);
        setButtonTitles(0);
        setupOnClickListeners();

        mPager.setCurrentItem(page);

    }

    private void setCustomTitle(int position) {
        ((TextView) findViewById(android.R.id.title)).setText((position == 0) ? mTitle : getString(R.string.use_this_task));
        ((TextView) findViewById(android.R.id.title)).setTextColor(Color.WHITE);
        ((TextView) findViewById(android.R.id.title)).setBackgroundColor(getResources().getColor(R.color.highlight_green));
        (findViewById(R.id.titleDivider)).setBackgroundColor(getResources().getColor(R.color.title_spacer_colored));
    }
    
    private void setButtonTitles(int position) {
        if ((position > 0)) {
            mButtonNext.setText(R.string.menu_done);
            mButtonPrevious.setText(R.string.previous);
        } else {
            mButtonNext.setText(R.string.next);
            mButtonPrevious.setText(R.string.dialogCancel);
        }
    }
    
    private void setupOnClickListeners() {
        (findViewById(R.id.button_next)).setOnClickListener(buttonClicked);

        (findViewById(R.id.button_previous)).setOnClickListener(buttonClicked);

    }

    private OnClickListener buttonClicked = new OnClickListener() {
        @Override public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_previous:
                    if (mPager.getCurrentItem() == 0) {
                        setResult(RESULT_CANCELED);
                        finish();
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                    }
                    break;
                case R.id.button_next:
                    if (mPager.getCurrentItem() == (mPagerAdapter.getCount() - 1)) {
                        /*Usage.logEvent(
                                Usage.getAnalyticsObject(TriggerWizardActivity.this),
                                "Trigger config done",
                                false
                        );*/
                        finishWizard();                          
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                    break;
            }
        }
    };
    
    private void finishWizard() {
        Intent intent = new Intent();
        
        // Call to listeners to pull newest data
        mTrigger = mListeners.get(0).getUpdatedTrigger();

        if (mTriggerFragment.useConstraints()) {
            mTrigger.setConstraints(mListeners.get(1).getUpdatedConstraints());
        }

        if (mExample != null) {
            intent.putExtra(ExampleTask.EXTRA_EXAMPLE_TASK, mExample);
        }
        
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, mTrigger.getType());
        intent.putExtra(TaskWizardActivity.EXTRA_TASK_IS_NEW, mTaskIsNew);
        intent.putExtra(Trigger.EXTRA_TRIGGER, mTrigger);
   
        setResult(RESULT_OK, intent);
        finish();
    }
    
    private void getDataFromIntent(Intent intent) {

        // sent from an example task
        if (getIntent().hasExtra(ExampleTask.EXTRA_EXAMPLE_TASK)) {
            mExample = (ExampleTask) getIntent().getParcelableExtra(ExampleTask.EXTRA_EXAMPLE_TASK);
        }
        
        // Sent in via an edit
        if (intent.hasExtra(EXTRA_TRIGGER)) {
            mTrigger = intent.getParcelableExtra(EXTRA_TRIGGER);
            mTaskType = mTrigger.getType();
        }
             
        if (intent.hasExtra(TaskTypeItem.EXTRA_TASK_TYPE)) {
            mTaskType = intent.getIntExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_BLUETOOTH);
            if (mTrigger == null) {
                mTrigger = new Trigger(mTaskType, null, null, null);
            }
        } 
    }


    private String getCustomTitle() {
        return String.format(getString(R.string.configure_connection_task_title), TaskTypeItem.getTaskName(this, mTrigger.getType()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }
}
