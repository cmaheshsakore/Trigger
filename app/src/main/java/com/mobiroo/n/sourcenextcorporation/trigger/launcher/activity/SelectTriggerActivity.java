package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabResult;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.Purchase;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ExampleTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandWriterLauncher;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListTaskTypeSpacer;

import java.util.ArrayList;

public class SelectTriggerActivity extends AppCompatActivity {

    public static final String  EXTRA_TITLE     = "com.trigger.launcher.EXTRA_TITLE";
    public static final String  EXTRA_PREFERRED = "com.trigger.launcher.EXTRA_PREFERRED";
    public static final String  EXTRA_OPTIONS   = "com.trigger.launcher.EXTRA_OPTIONS";
    
    private final int REQUEST_SHOW_UPGRADE      = 1003;
    private final int REQUEST_CREATE_TAG        = 1001;
    private final int REQUEST_CREATE_EXAMPLE    = 1002;

    private Task mTask;

    private IabClient mClient;
    
    private boolean mHasPurchasedUnlock     = false;

    private ListItemsAdapter mAdapter;

    private int mItemPendingUpgrade;
    private String mPendingSku;
    private TaskTypeItem mPendingItem;

    private boolean mUpgradeInProgress      = false;
    private ListView mListView;

    private boolean mSaveWhenDone           = false;
    private ExampleTask mExample;

    private Intent      mIntent;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);*/
        setContentView(R.layout.activity_select_trigger);
        
        mSaveWhenDone = getIntent().getBooleanExtra(Constants.EXTRA_SAVE_WHEN_DONE, false);
        if (getIntent().hasExtra(ExampleTask.EXTRA_EXAMPLE_TASK)) {
            mExample = (ExampleTask) getIntent().getParcelableExtra(ExampleTask.EXTRA_EXAMPLE_TASK);
        }
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(mListItemClick);


        if (BuildConfiguration.isPlayStoreAvailable()) {
            mClient = new IabClient(this, mCallback);
            
            mHasPurchasedUnlock = IabClient.grantUnlock(this);

            Logger.d("Unlocked = " + mHasPurchasedUnlock);

            if (!mHasPurchasedUnlock) {
                Logger.d("Setting up IAB");
                mClient.startSetup();
            }
        }
        String title = (getIntent().hasExtra(EXTRA_TITLE)) ? getIntent().getStringExtra(EXTRA_TITLE) : (String) getTitle();

        ((TextView) findViewById(android.R.id.title)).setText(title);
        ((TextView) findViewById(android.R.id.title)).setTextColor(Color.WHITE);
        ((TextView) findViewById(android.R.id.title)).setBackgroundColor(getResources().getColor(R.color.highlight_green));
        (findViewById(R.id.titleDivider)).setBackgroundColor(getResources().getColor(R.color.title_spacer_colored));
        mIntent = getIntent();
        if (mIntent.hasExtra(Constants.EXTRA_SAVED_TAG_REUSE)) {
            mTask = mIntent.getParcelableExtra(Constants.EXTRA_SAVED_TAG_REUSE);
        }

        buildAdapter();

        setListAdapter(mAdapter);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (mAdapter.getCount() == 1) {
            TaskTypeItem item = (TaskTypeItem) mAdapter.getItem(0);
            if (!item.isLocked()) {
                launchTask(item);
            }
        }
    }

    private IabClient.IabCallback mCallback = new IabClient.IabCallback() {

        @Override
        public void handleIabError(IabResult result) {
            Logger.d("Problem settings up IAB " + result);
            Logger.d("Querying local storage");
            mHasPurchasedUnlock = IabClient.checkLocalUnlock(SelectTriggerActivity.this);
            updateUi();
        }

        @Override
        public void handleIabSuccess(IabResult result) {
            Logger.d("IAB set up OK");
            mHasPurchasedUnlock = mClient.wasUnlockPurchased(SelectTriggerActivity.this);
            storePurchase(Constants.SKU_TRIGGER_UNLOCK,   mHasPurchasedUnlock);
            updateUi();
        }

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            Logger.d("Purchase finished");
        }
        
    };

    private void buildAdapter() {
        if ((mIntent == null) || (!mIntent.hasExtra(EXTRA_PREFERRED))) {
            mAdapter = new TriggerAdapter(this, TaskTypeItem.getItems(this), mHasPurchasedUnlock);
        } else {
            ArrayList<TaskTypeItem> items = new ArrayList<TaskTypeItem>();
            items.add(TaskTypeItem.getItemFromType(mIntent.getIntExtra(EXTRA_PREFERRED, -1)));
            int[] secondary = mIntent.hasExtra(EXTRA_OPTIONS) ? mIntent.getIntArrayExtra(EXTRA_OPTIONS) : new int[0];
            for (int i=0 ; i< secondary.length; i++) {
                if (i == 0) {
                    items.add(new ListTaskTypeSpacer(R.string.layoutPreferencesTagsMore, -1, false, null));
                }
                items.add(TaskTypeItem.getItemFromType(secondary[i]));
            }
            mAdapter = new TriggerAdapter(this, items.toArray(new TaskTypeItem[items.size()]), mHasPurchasedUnlock);
        }
    }
    
    private void setListAdapter(ListItemsAdapter adapter) {
        mAdapter = adapter;
        mListView.setAdapter(adapter);
    }

    @SuppressWarnings("unused")
    private ListView getListView() {
        return mListView;
    }

    private ListItemsAdapter getListAdapter() {
        return mAdapter;
    }

    private class TriggerAdapter extends ListItemsAdapter {

        private boolean mLockItems;

        public TriggerAdapter(Activity activity, ListItem[] items, boolean lock) {
            super(activity, items);
            mLockItems = lock;
        }

        private void unlockItems() {
            mLockItems = false;
        }

        @SuppressWarnings("unused")
        private void lockItems() {
            mLockItems = true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TaskTypeItem item = (TaskTypeItem) getItem(position);
            if (item instanceof ListTaskTypeSpacer) {
                return ((ListTaskTypeSpacer) item).getView(getListAdapter(), position, convertView);
            } else {
                if (IabClient.isRestrictedItem(item.getExtraValue())) {
                    return item.getView(this, position, convertView, mLockItems);
                } else {
                    return item.getView(this, position, convertView);
                }
            }
        }
    };

    private void updateUi() {       
        /* Rebuild display, unlocking all items */
        buildAdapter();
        setListAdapter(mAdapter);
    }

    private void storePurchase(String sku, boolean purchased) {
        mHasPurchasedUnlock = purchased;
        mClient.storePurchase(this, sku, purchased);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(SelectTriggerActivity.this)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Usage.canLogData(SelectTriggerActivity.this)) {
            EasyTracker.getInstance(this).activityStop(this);
        }

    }


    private void finishTriggerSelection(Intent intent) {
        
        setResult(RESULT_OK, intent);
        finish();
    }
    private void launchTask(TaskTypeItem item) {

        @SuppressWarnings("rawtypes")
        Class klass = item.getActivityClass();
        int code = (mExample != null) ? REQUEST_CREATE_EXAMPLE : REQUEST_CREATE_TAG;

        if (klass != null) {
            Intent intent = new Intent(SelectTriggerActivity.this, klass);
            
            if (klass == TaskWizardActivity.class) {
                // NFC tag, return trigger object for this
                intent = new Intent();
                intent.putExtra(Trigger.EXTRA_TRIGGER, new Trigger(TaskTypeItem.TASK_TYPE_NFC, DatabaseHelper.TRIGGER_NO_CONDITION, "", ""));
                intent.putExtra(ExampleTask.EXTRA_EXAMPLE_TASK, mExample);
                Logger.d("Returning NFC Task");
                finishTriggerSelection(intent);

            } else {

                if ((mExample != null) && (item.getExtraValue() == TaskTypeItem.TASK_TYPE_NFC)) {
                    intent = new Intent(SelectTriggerActivity.this, ImportTagActivity.class);
                    intent.putExtra(TaskTypeItem.EXTRA_TASK_CONDITION, DatabaseHelper.TRIGGER_NO_CONDITION);
                    intent.putExtra(TaskTypeItem.EXTRA_KEY_1_VALUE, "");
                    intent.putExtra(TaskTypeItem.EXTRA_KEY_2_VALUE, "");
                }

                intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, item.getExtraValue());
                intent.putExtra(TaskTypeItem.EXTRA_LAYOUT_ID, item.getLayoutId());

                if (mSaveWhenDone) {
                    intent.putExtra(Constants.EXTRA_SAVE_WHEN_DONE, mSaveWhenDone);
                }

                if (mExample != null) {
                    intent.putExtra(ExampleTask.EXTRA_EXAMPLE_TASK, mExample);
                }

                if (mTask != null) {
                    intent.putExtra(Constants.EXTRA_SAVED_TAG_REUSE, mTask);
                    if ((mTask.getSecondaryId() != null) && (!mTask.getSecondaryId().isEmpty())) {
                        intent.putExtra(TaskWizardActivity.EXTRA_TAG_TWO_ID, mTask.getSecondaryId());
                        intent.putExtra(TaskWizardActivity.EXTRA_TAG_TWO_NAME, mTask.getSecondaryName());
                    } 
                } else {
                    intent.putExtra(TaskWizardActivity.EXTRA_TASK_IS_NEW, true);                
                }
                Logger.d("Starting activity with code " + code);
                startActivityForResult(intent, code);
            }
        } else {
            TagstandWriterLauncher.launch(SelectTriggerActivity.this);
            finish();
        }
    }

    protected OnItemClickListener mListItemClick = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListItem item = getListAdapter().getItem(position);

            if (item instanceof ListTaskTypeSpacer) {
                ListTaskTypeSpacer i = (ListTaskTypeSpacer) item;
                if (i.isEnabled() && i.getClassOnClick() != null) {
                    Intent intent = new Intent(parent.getContext(), i.getClassOnClick());
                    startActivity(intent);
                }
            } else {
                mPendingItem = (TaskTypeItem) item;

                Usage.logMixpanelEvent(
                        Usage.getAnalyticsObject(SelectTriggerActivity.this),
                        "Trigger config clicked",
                        false,
                        new String[] { "type", String.valueOf(mPendingItem.getExtraValue())}
                );

                if (mHasPurchasedUnlock || !mPendingItem.isLocked()) {
                    launchTask(mPendingItem);
                } else {
                    String sku = getSkuFromItem(mPendingItem);
                    if (!sku.isEmpty()) {
                        boolean launchPurchaseFlow = true;

                        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
                        if (status != ConnectionResult.SUCCESS) {
                        /* User does not have play services installed */
                            Logger.d("Play services not available, not launching purchase");
                            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, SelectTriggerActivity.this, 0);
                            dialog.show();
                            launchPurchaseFlow = false;
                        }

                        if (launchPurchaseFlow) {
                            Logger.d("Starting purchase for SKU " + sku);
                            if (!mUpgradeInProgress) {
                                mUpgradeInProgress = true;
                                mPendingSku = sku;
                                mItemPendingUpgrade = mPendingItem.getExtraValue();
                                Intent intent = new Intent(SelectTriggerActivity.this, TrialDialogActivity.class);
                                intent.putExtra(TrialDialogActivity.EXTRA_ITEM, mItemPendingUpgrade);
                                intent.putExtra(TrialDialogActivity.EXTRA_SKU, IabClient.getSkuFromItem(mItemPendingUpgrade));
                                startActivityForResult(intent, REQUEST_SHOW_UPGRADE);
                            } else {
                                Logger.d("Upgrade already in progress for " + mItemPendingUpgrade);
                            }
                        }
                    } else {
                        Logger.d("Item returned an empty SKU " + mPendingItem.getExtraValue());
                    }
                }
            }
        }

    };


    private String getSkuFromItem(TaskTypeItem item) {
        return getSkuFromItem(item.getExtraValue());
    }

    private String getSkuFromItem(int type) {
        return Constants.SKU_TRIGGER_UNLOCK;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("In onResult " + resultCode + " from " + requestCode);

        try {
            if (mClient.getHelper().handleActivityResult(requestCode, resultCode, data)) {
                Logger.d("onActivityResult handled by IABUtil.");
                return;
            }
        } catch (Exception e) {}


        switch (requestCode) {
            case REQUEST_CREATE_TAG:
            case REQUEST_CREATE_EXAMPLE:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            case REQUEST_SHOW_UPGRADE:
                mUpgradeInProgress = false;
                if (resultCode == RESULT_OK) {
                    // OK is only returned from a successful purchase
                    mHasPurchasedUnlock = mClient.checkUnlock(this);
                    updateUi();
                } else {
                    if (IabClient.isUserOnTrial(this)) {
                        mHasPurchasedUnlock = true;
                        Logger.d("User is on trial, unlocking");
                        for (int i=0; i< mAdapter.getCount(); i++) {
                            ((TaskTypeItem) mAdapter.getItem(i)).setLocked(false);
                        }
                        ((TriggerAdapter) mAdapter).unlockItems();
                        updateUi();
                    }
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
                break;
            
                

        }

    }

    private void unLockPurchase() {
        mHasPurchasedUnlock = true;
        for (int i=0; i< mAdapter.getCount(); i++) {
            ((TaskTypeItem) mAdapter.getItem(i)).setLocked(false);
        }
        ((TriggerAdapter) mAdapter).unlockItems();
        updateUi();
    }

    IabHelper.OnIabPurchaseFinishedListener mOnIabFinishListener = new OnIabPurchaseFinishedListener() {

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            mUpgradeInProgress = false;
            mItemPendingUpgrade = -1;

            if (result.isSuccess()) {
                Logger.d("IAB purchase finished successfully for " + mItemPendingUpgrade + " " + mPendingSku);
                Usage.logEvent(null, "Upgrade purchased", true);
                unLockPurchase();

                String sku = mPendingSku;
                if (!sku.isEmpty()) {
                    storePurchase(Constants.SKU_TRIGGER_UNLOCK, true);
                } else {
                    Logger.d("SKU returned was empty");
                }
            } else if (result.isFailure()) {
                Logger.d("IAB purchase failed [" + result.getResponse() + "]");
                switch(result.getResponse()) {
                    case 1:
                        /* Back pressed, do nothing */
                        break;
                    case 3:
                        /* Billing API version is not supported */
                        Toast.makeText(
                                SelectTriggerActivity.this,
                                "You were not charged.  Billing API unavailable. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                    case 6:
                        // product not available for purchase
                        Toast.makeText(
                                SelectTriggerActivity.this,
                                "You were not charged.  Product not available. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                        break;
                    case 5:
                    case 8:
                        // invalid arguments passed to API
                        // signing problem also possible
                        Toast.makeText(
                                SelectTriggerActivity.this,
                                "You were not charged.  Product not available. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                        break;
                    case 7:
                        // invalid arguments passed to API
                        // signing problem also possible
                        mPendingItem.setLocked(false);
                        unLockPurchase();
                        storePurchase(Constants.SKU_TRIGGER_UNLOCK, true);
                        launchTask(mPendingItem);
                        /*
                        Toast.makeText(
                                SelectTagTypeActivity.this,
                                "You were not charged.  You already own this. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                         */
                        break;
                }
            }

            updateUi();
        }
    };
}
