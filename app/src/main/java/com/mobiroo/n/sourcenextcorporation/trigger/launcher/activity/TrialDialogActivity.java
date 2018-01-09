package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabResult;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.Purchase;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

public class TrialDialogActivity extends Activity implements OnClickListener {

    public static final String EXTRA_ITEM       = "extra_item";
    public static final String EXTRA_SKU        = "extra_sku";
    public static final String EXTRA_TITLE      = "extra_title";
    public static final String EXTRA_HEADING    = "extra_heading";
    public static final String EXTRA_MESSAGE    = "extra_message";

    private final int   REQUEST_LOG_IN = 9;

    private IabClient mClient;
    
    private boolean mHasPurchasedUnlock     = false;


    private int     mItem;
    private String  mSku;

    private boolean mUpgradeInProgress = false;
    
    private int mResult;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_trial_dialog);

        String title = getString(R.string.pro_feature);
        String heading = getString(R.string.upgrade_heading);
        String message = getString(R.string.sign_in_trial);

        if (getIntent() != null) {
            mSku = getIntent().hasExtra(EXTRA_SKU) ? getIntent().getStringExtra(EXTRA_SKU) : "";
            mItem = getIntent().getIntExtra(EXTRA_ITEM, -1);

            if (getIntent().hasExtra(EXTRA_TITLE)) {
                title = getIntent().getStringExtra(EXTRA_TITLE);
            }

            if (getIntent().hasExtra(EXTRA_HEADING)) {
                heading = getIntent().getStringExtra(EXTRA_HEADING);
            }

            if (getIntent().hasExtra(EXTRA_MESSAGE)) {
                message = getIntent().getStringExtra(EXTRA_MESSAGE);
            }
        }


        ((TextView) findViewById(android.R.id.title)).setText(title);
        ((TextView) findViewById(R.id.upgrade_heading)).setText(heading);

        mClient = new IabClient(this, mCallback);
        mClient.startSetup();

        
        if (!IabClient.isTrialAvaiable(this)) {
            (findViewById(R.id.trial_button)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.upgrade_text)).setText(R.string.trial_upgrade_only);
        } else {
            (findViewById(R.id.trial_button)).setOnClickListener(this);
        }
        (findViewById(R.id.upgrade_button)).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upgrade_button:
                // Launch purchase flow
                if (!mUpgradeInProgress) {
                    launchPurchase();
                }
                break;
            case R.id.trial_button:
                startActivityForResult(new Intent(this, GoogleSignInExplanationActivity.class), REQUEST_LOG_IN);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOG_IN:
                if (resultCode == RESULT_OK) {
                    // Start trial
                    if (data.hasExtra(GoogleSigninActivity.EXTRA_USER_ACCOUNT)) {
                        String account = data.getStringExtra(GoogleSigninActivity.EXTRA_USER_ACCOUNT);
                        Logger.d("TRIAL: User is logged in");
                        if (!account.isEmpty()) {
                            Usage.logMixpanelEvent(null, "Trial started", true, new String[] { "account", account});
                            mClient.startTrial(this, account);
                        }
                    }
                }
                setResult(RESULT_CANCELED);
                Logger.d("TRIAL: Finishing from G+");
                finish();
        }
    }
    
    private IabClient.IabCallback mCallback = new IabClient.IabCallback() {

        @Override
        public void handleIabError(IabResult result) {
            Logger.d("Problem settings up IAB " + result);
            mHasPurchasedUnlock = IabClient.checkLocalUnlock(TrialDialogActivity.this);
            mResult = (mHasPurchasedUnlock) ? Activity.RESULT_OK : Activity.RESULT_CANCELED;
            Toast.makeText(TrialDialogActivity.this, "Could not set up in app billing", Toast.LENGTH_LONG).show();
            finishWithResult();
        }

        @Override
        public void handleIabSuccess(IabResult result) {

            Logger.d("IAB set up OK");
            if (mClient.checkUnlock(TrialDialogActivity.this)) {
                Logger.d("User has already purchased");
                /* User has purchased upgrade already, show toast and exit */
                storePurchase(Constants.SKU_TRIGGER_UNLOCK, true);
                IabClient.endTrial(getApplicationContext());
                Toast.makeText(TrialDialogActivity.this, R.string.upgrade_successful, Toast.LENGTH_LONG).show();
                mResult = Activity.RESULT_OK;
                finishWithResult();
            }

        }

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            Usage.logMixpanelEvent(null, "IAB Finished", true, new String[] { "result", String.valueOf(result.isSuccess())});
            mUpgradeInProgress = false;
            mItem = -1;
            mResult = Activity.RESULT_CANCELED;
            if (result.isSuccess()) {
                Logger.d("Upgrade purchased");
                Usage.logEvent(null, "Upgrade purchased", true);
                storePurchase(Constants.SKU_TRIGGER_UNLOCK, true);
                IabClient.endTrial(getApplicationContext());
                mResult = Activity.RESULT_OK;
            } else if (result.isFailure()) {
                switch(result.getResponse()) {
                    case 1:
                        /* Back pressed, do nothing */
                        break;
                    case 3:
                        /* Billing API version is not supported */
                        Toast.makeText(
                                TrialDialogActivity.this,
                                "You were not charged.  Billing API unavailable. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                    case 6:
                        // product not available for purchase
                        Toast.makeText(
                                TrialDialogActivity.this,
                                "You were not charged.  Product not available. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                        break;
                    case 5:
                    case 8:
                        // invalid arguments passed to API
                        // signing problem also possible
                        Toast.makeText(
                                TrialDialogActivity.this,
                                "You were not charged.  Product not available. ("
                                        + result.getResponse() + ")",
                                        Toast.LENGTH_LONG).show();
                        break;
                    case 7:
                        // Already owned
                        Logger.d("Already owned, unlocking");
                        Usage.logEvent(null, "Upgrade purchased", true);
                        storePurchase(Constants.SKU_TRIGGER_UNLOCK, true);
                        mResult = Activity.RESULT_OK;
                        break;
                }
            }
            finishWithResult();
        }
        
    };
    
    private void storePurchase(String sku, boolean purchased) {
        mHasPurchasedUnlock = purchased;
        mClient.storePurchase(this, sku, purchased);
    }
    
    private void launchPurchase() {
        if (mSku.isEmpty()) {
            mSku = IabClient.getSkuFromItem(mItem);
        }
        mUpgradeInProgress = true;
        mClient.startPurchase(TrialDialogActivity.this, mItem, mSku);
    }
    
    private void finishWithResult() {
        setResult(mResult);
        finish();
    }
    
}
