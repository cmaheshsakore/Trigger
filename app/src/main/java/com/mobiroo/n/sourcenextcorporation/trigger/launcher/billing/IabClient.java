package com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class IabClient {

    public static final String EXTRA_SKU = "sku";
    public static final String EXTRA_ITEM = "item";
    public static final String PREF_NAME   = "IAP_PREFS";
    
    private static final boolean UNLOCK_ALLOW = true;
    
    private final int TRIAL_UNIT    = Calendar.DAY_OF_YEAR;
    private final int TRIAL_VALUE   = 7;
    
    private ArrayList<String> mSkus;
    private IabHelper mHelper;

    private IabCallback mCallback;
    
    private int mItem;
    private String mSku;
    
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, 0);
    }
    
    private void logd(String message) {
        Logger.d("IAB: " + message);
    }
    
    @SuppressWarnings("unused")
    private void logi(String message) {
        Logger.i("IAB: " + message);
    }
    
    private void loge(String message, Exception e) {
        Logger.e("IAB: " + message, e);
    }
    
    @Deprecated
    public IabClient(Context context) {
        mHelper = new IabHelper(context, Constants.getpk());
        mCallback = null;
    }

    public IabClient(Context context, IabCallback callback) {
        mHelper = new IabHelper(context, Constants.getpk());
        mCallback = callback;
    }
    
    public void setCallback(IabCallback callback) {
        mCallback = callback;
    }
    
    public IabHelper getHelper() {
        return mHelper;
    }

    private void buildSkus() {
        mSkus = new ArrayList<String>();
        mSkus.add(Constants.SKU_TRIGGER_GEOFENCE);
        mSkus.add(Constants.SKU_TRIGGER_BATTERY);
        mSkus.add(Constants.SKU_TRIGGER_TIME);
        mSkus.add(Constants.SKU_TRIGGER_UNLOCK);
    }

    public ArrayList<String> getSkuList() {
        if (mSkus == null) {
            buildSkus();
        }
        return mSkus;
    }

    public static boolean grantUnlock(Context context) {

        PackageManager packageManager = context.getPackageManager();
        PackageInfo info = null;
        try { info = packageManager.getPackageInfo("com.trigger.betaunlock", PackageManager.GET_META_DATA);}
        catch (NameNotFoundException e) {}

        if ((info != null) || (BuildConfiguration.getWalletEnvironment() == WalletConstants.ENVIRONMENT_SANDBOX)) {
            return true;
        } else {

            //2_2_18
            //temp making this as true so that the all features are enabled...
            //return false;
            return true;
        }
    }
    
    public static final boolean checkLocalUnlockOrTrial(Context context) {
        //2_2_18
        //return checkLocalUnlock(context) || isUserOnTrial(context) || grantUnlock(context);
        return true;
    }
    
    public static boolean checkLocalUnlock(Context context) {
        /*2_2_18
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        boolean local = settings.getBoolean(Constants.SKU_TRIGGER_BATTERY, false)
                || settings.getBoolean(Constants.SKU_TRIGGER_GEOFENCE, false)
                || settings.getBoolean(Constants.SKU_TRIGGER_TIME, false)
                || settings.getBoolean(Constants.SKU_TRIGGER_UNLOCK, false);
        return local;*/
        return true;
    }


    public boolean isUpgrade(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        return !settings.getBoolean(Constants.SKU_TRIGGER_UNLOCK, false);
    }
    
    public boolean wasUnlockPurchased(Context context) {
        //2_2_18
        /*Inventory inventory = null;
        try {
            inventory = mHelper.queryInventory(false, getSkuList());
        } catch (Exception e) {
            loge("Exception building inventory", e);
        }
        
        if (inventory == null) {
            logd("Received null inventory");
            return false;
        }

        if (inventory.getAllOwnedSkus() != null) {
            logd("All owned skus: " + TextUtils.join(",", inventory.getAllOwnedSkus()));
        } else {
            logd("No skus owned");
        }


        return UNLOCK_ALLOW && (isUserOnTrial(context)
                || inventory.hasPurchase(Constants.SKU_TRIGGER_UNLOCK)
                || inventory.hasPurchase(Constants.SKU_TRIGGER_GEOFENCE)
                || inventory.hasPurchase(Constants.SKU_TRIGGER_BATTERY) 
                || inventory.hasPurchase(Constants.SKU_TRIGGER_TIME));*/

        return true;
    }
    
    public boolean checkUnlock(Context context) {
        //2_2_18
        //return checkLocalUnlock(context) || wasUnlockPurchased(context);
        return true;
    }

    //storing the status of the application purchase...
    public void storePurchase(Context context, String sku, boolean purchased) {
        logd("Storing " + sku + ", " + purchased);

        Editor editor = getPreferences(context).edit();
        editor.putBoolean(sku, purchased);
        editor.commit();
    }


    public static boolean isRestrictedItem(int primary) {
        return primary != TaskTypeItem.TASK_TYPE_BLUETOOTH
                && primary != TaskTypeItem.TASK_TYPE_WIFI
                && primary != TaskTypeItem.TASK_TYPE_NFC;
    }
    
    private final static String USER_IS_ON_TRIAL = "trial_started";
    private final static String TRIAL_EXPIRATION = "trial_expiration";
    private final static String USER_TRIAL_CLAIMED = "trial_claimed";
    
    public static boolean isUserOnTrial(Context context) {

        /*2_2_18
        SharedPreferences prefs = getPreferences(context);
        long stored_end = prefs.getLong(TRIAL_EXPIRATION, 1000);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(stored_end);

        return (getPreferences(context).getBoolean(USER_IS_ON_TRIAL, false)
                && Calendar.getInstance().before(end));
        */

        // user in not on the trail...
        return false;
    }

    //only for the trail version
    public void startTrial(Context context, String account) {
        Editor editor = getPreferences(context).edit();
        
        editor.putBoolean(USER_IS_ON_TRIAL, true);
        editor.putBoolean(USER_TRIAL_CLAIMED, true);
        
        Calendar end = Calendar.getInstance();
        end.add(TRIAL_UNIT, TRIAL_VALUE);
        Logger.d("Trial expires " + new SimpleDateFormat("MM-dd-yy hh:mm").format(end.getTime()));
        editor.putLong(TRIAL_EXPIRATION, end.getTimeInMillis());
        editor.commit();
        
        TagstandManager.putTrial(account);
    }

    public static void endTrial(Context context) {
        Editor editor = getPreferences(context).edit();
        editor.putBoolean(USER_IS_ON_TRIAL, false);
    }

    public static boolean isTrialAvaiable(Context context) {
        return !(getPreferences(context).getBoolean(USER_TRIAL_CLAIMED, false));
    }
    
    public void startSetup() {
        getHelper().startSetup(mIabSetupFinishedListener);
    }
    
    public void startSetup(IabHelper.OnIabSetupFinishedListener listener) {
        getHelper().startSetup(listener);
    }
    
    private IabHelper.OnIabSetupFinishedListener mIabSetupFinishedListener = new IabHelper.OnIabSetupFinishedListener() {
        public void onIabSetupFinished(IabResult result) {
            if (!result.isSuccess()) {
                mCallback.handleIabError(result);
            } else {
                mCallback.handleIabSuccess(result);
                
            }
        }
    };
    
    
    public void startPurchase(Activity activity, int item, String sku) {
        mItem = item;
        mSku = sku;
        getHelper().launchPurchaseFlow(activity, sku, item, mOnIabFinishListener);
    }
    
    
    private IabHelper.OnIabPurchaseFinishedListener mOnIabFinishListener = new OnIabPurchaseFinishedListener() {

        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isSuccess()) {
                Logger.d("IAB purchase finished successfully for " + mItem + " " + mSku);
            } else if (result.isFailure()) {
                Logger.d("IAB purchase failed [" + result.getResponse() + "]");
            }
            
            mCallback.onIabPurchaseFinished(result, info);
        }
    };
    
    public static interface IabCallback {
        public void handleIabError(IabResult result);
        public void handleIabSuccess(IabResult result);
        public void onIabPurchaseFinished(IabResult result, Purchase info);
    }
    
    public static String getSkuFromItem(TaskTypeItem item) {
        return getSkuFromItem(item.getExtraValue());
    }

    public static String getSkuFromItem(int type) {
        return Constants.SKU_TRIGGER_UNLOCK;
    }
}
