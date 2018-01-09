package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.BuildWalletConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.FlavorInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.ActivityFeedFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.FeedbackFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.OtherNfcFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.TaskListFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ExampleTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListTabletMenuItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListTabletMenuItemSmall;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.MyRegistrationIntentService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.AppLaunchCountTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabResult;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.Purchase;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.ExamplesFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.PluginRepositoryFragment;
import com.trigger.launcher.fragment.ShopFragmentNew;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.MainSettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.CheckReceiversBackgroundService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import com.mobiroo.drm.MobirooDrm;


public class MainActivity extends FragmentActivity implements ExamplesFragment.Listener {


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    public static final String  EXTRA_SHOW_WELCOME = "MainActivity.show_welcome";
    public static final String  ENTRY_STRING = "main";
    
    private static final int    REQUEST_WELCOME = 1;
    public  static final int    REQUEST_CREATE_TASK = 2;
    private static final int    REQUEST_SSO_SIGNIN = 3;
    public static final int     REQUEST_CREATE_EXAMPLE = 4;
    private static final int    REQUEST_UPGRADE = 5;

    //3_1_18
    //removing the shop from the list...
    //private boolean         mShowShop = true;
    private boolean         mShowShop = false;

    private final String    mTagShop            = "fragment-shop";
    private final String    mTagMyTasks         = "fragment-my-tasks";
    private final String    mTagExamples        = "fragment-examples";
    private final String    mTagStats           = "fragment-stats";
    private final String    mTagFeedback        = "fragment-feedback";
    private final String    mTagSettings        = "fragment-settings";
    private final String    mTagOtherNfc        = "fragment-other-nfc";
    private final String    mTagGoogleSignin    = "google-signing";
    private final String    mTagPlugins         = "fragment-plugins";
    private final String    mTagUpgrade         = "upgrade";
    
    private MenuItemsAdapter        mMenuItemsAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout            mDrawerLayout;
    private CharSequence            mTitle;
    private FragmentManager         mManager;
    
    private String          mCurrentTag;
    private FrameLayout     mDrawerList;
    private int             mWidthDp = 0;
    private boolean         mIsPhoneLayout;
    private ListView        mMenu;
    SharedPreferences       mPrefs;

    private IabClient mClient;
    private boolean         mUpgradeVisible;

    private HashMap <String, Integer> mMenuMap;
    private Menu            mOverflowMenu;

    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    @Override
    public void onNewIntent(Intent intent) {
        Logger.e("Received intent");
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        SettingsHelper.loadPreferences(MainActivity.this);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        //3_1_18
        //mShowShop = BuildTools.shouldShowShop();


        mWidthDp = Utils.getWidthInDp(MainActivity.this);
        mIsPhoneLayout = (mWidthDp < 800) ? true : false;
        mManager = getSupportFragmentManager();

        setupNavDrawer();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTaskClicked(view);
            }
        });

        showStartingFragment(savedInstanceState);

        showDialogIfNeeded();

        startService(new Intent(MainActivity.this, CheckReceiversBackgroundService.class));
        new AppLaunchCountTask(MainActivity.this, AppLaunchCountTask.APP_LAUNCH_TYPE.START).execute();
    }

    private boolean hasPurchased() {
        return true;

        //2_2_18
        //return IabClient.checkLocalUnlock(this) && !IabClient.isUserOnTrial(this);
    }

    private void validate(final Activity myActivity) {

        myActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                MobirooDrm.setDebugLog(true);
                MobirooDrm.validateActivity(myActivity);
            }
        });
    }

    @Override
    public void onResume() {
        validate(MainActivity.this);
        super.onResume();

        mFab.setBackgroundTintList(getResources().getColorStateList(R.color.fab_green));

        registerReceiver(upgradeReceiver, new IntentFilter(Constants.ACTION_UPGRADE_PURCHASED));
        checkPurchaseStatus();

        if ((hasPurchased() && mUpgradeVisible) || (!hasPurchased() && !mUpgradeVisible)) {
            setupNavDrawer();
        }

        updateDrawerContents();
        setTitle(!TextUtils.isEmpty(mTitle) ? mTitle : getString(R.string.app_name));
        toggleFab(mCurrentTag);
        if (Usage.canLogData(MainActivity.this)) {
            Usage.logInstallDate(MainActivity.this);
            Usage.logAppOpened(MainActivity.this);
        }


        Log.i("abc","-"+1);
        // Google Play Servicesの利用可能状態をチェックして問題なければRegister
        if (checkPlayServices()) {
            Log.i("abc",""+0);
            MyRegistrationIntentService.startIntentService(this);
        }

    }

    /**
     * Google Play Servicesの利用可能状態をチェックし、必要ならばダウンロード画面へ誘導する
     * @return
     */
    private boolean checkPlayServices() {

        Log.i("abc",""+1);
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        Log.i("abc",""+2);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i("abc",""+3);
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                Log.i("abc",""+4);
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("abc",""+5);
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            Log.i("abc",""+6);
            return false;
        }
        Log.i("abc",""+7);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        try { unregisterReceiver(upgradeReceiver);}
        catch (Exception ignored) {}
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(MainActivity.this)) {
            Usage.startTracker(this, this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (Usage.canLogData(MainActivity.this)) {
            Usage.stopTracker(this);
        }
    }

    private void toggleFab(String current) {
        if (TextUtils.isEmpty(mCurrentTag) || TextUtils.equals(current, mTagMyTasks) || TextUtils.equals(current, mTagExamples)) {
            mFab.setVisibility(View.VISIBLE);
        } else {
            mFab.setVisibility(View.GONE);
        }
    }

    private void showStartingFragment(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            showFragment(mPrefs.getString(Constants.PREF_MAIN_SELECTED_TAB, mTagMyTasks));
        } else {
            if (getIntent().hasExtra("tag")) {
                // If the intent is explicitly requesting we show a fragment show the requested fragment
                showFragment(getIntent().getStringExtra("tag"));
                return;
            }

            if (!mPrefs.getBoolean(Constants.PREF_HIDE_WELCOME, false) || getIntent().getBooleanExtra(EXTRA_SHOW_WELCOME, false) || !mPrefs.contains(Constants.PREF_MAIN_SELECTED_TAB)) {
                // If we are explicitly requesting to show welcome and don't have a main tab preference set, show examples
                showFragment(mTagExamples);
                mPrefs.edit().putBoolean(Constants.PREF_HIDE_WELCOME, true).commit();
            } else if (!mPrefs.contains(Constants.PREF_MAIN_SELECTED_TAB)) {
                setTitle(getString(R.string.app_name));
                mMenu.setItemChecked(1, true);
                mDrawerLayout.openDrawer(mMenu);
                showFragment(mTagExamples);
                mPrefs.edit().putBoolean(Constants.PREF_HIDE_WELCOME, true).commit();
            } else {
                showFragment(mTagMyTasks);
                mMenu.setItemChecked(0, true);
            }
        }
    }

    private void showDialogIfNeeded() {

        String pref_connect = "has_shown_connect";
        String pref_trial_expred = "upgrade_expired_alert";
        String pref_version = "version_dialog";

        if (BuildWalletConstants.BUILD_ENVIRONMENT == WalletConstants.ENVIRONMENT_SANDBOX) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("THIS IS A DEBUG BUILD FOR WALLET. NEVER PUSH THIS LIVE.");
            builder.setPositiveButton("OK", null);
            builder.create().show();
            return;
        }

        // Check if user trial has expired and alert the user if so
        if (!IabClient.isTrialAvaiable(this) && !IabClient.isUserOnTrial(this) && !IabClient.checkLocalUnlock(this)) {
            // Trial is unavailable (claimed) and user is no longer on trial
            if (!SettingsHelper.getPrefBool(this, pref_trial_expred, false)) {
                SettingsHelper.setPrefBool(this, pref_trial_expred, true);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.trial_expired_title)
                        .setMessage(R.string.trial_expired)
                        .setPositiveButton(R.string.dialogOK, null);
                builder.create().show();
                return;
            }
        }


        // Check if we need to pop a what's new dialog
        if (SettingsHelper.getPrefInt(this, pref_version, 0) < BuildConfiguration.VERSION_DIALOG) {
            SettingsHelper.setPrefInt(this, pref_version, BuildConfiguration.VERSION_DIALOG);
            showWhatsNewDialog();
            return;
        }
    }

    private void checkPurchaseStatus() {
        if (!SettingsHelper.getPrefBool(this, "purchase_checked", false)) {
            Logger.d("Checking purchase");
            if (!IabClient.checkLocalUnlock(this) && (!IabClient.isUserOnTrial(this))) {
                // If user is not on trial and we aren't unlocked perform a one time query against IAB to see if the user has purchased
                // and is moving to a new device / wipe / re-install

                Log.i("karo","in checkPurchaseStatus()");

                mClient = new IabClient(this, new IabClient.IabCallback() {

                    @Override
                    public void handleIabError(IabResult result) {
                    }

                    @Override
                    public void handleIabSuccess(IabResult result) {

                        Log.i("karo","in handleIabSuccess()");

                        // Query IAB to see if we have a successful purchase, store result and mark check as completed
                        SettingsHelper.setPrefBool(MainActivity.this, "purchase_checked", true);
                        boolean purchased = mClient.wasUnlockPurchased(MainActivity.this);
                        mClient.storePurchase(MainActivity.this, Constants.SKU_TRIGGER_UNLOCK, purchased);
                        if (purchased) {

                            Log.i("karo","in if(purchased)");

                            // Hide upgrade status
                            sendBroadcast(new Intent(Constants.ACTION_UPGRADE_PURCHASED));
                        }
                    }

                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                    }

                });
                mClient.startSetup();
            }
        }
    }

    private void showWhatsNewDialog() {
        
        /*SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.setTitle(getString(R.string.whats_new));
        dialog.setMessage(Html.fromHtml(getString(R.string.whats_new_text)));
        dialog.setPositiveButton(getString(R.string.dialogOK), null);
        dialog.show(getSupportFragmentManager(), "whats-new-dialog");*/
    }

    private void showConnectDialog() {
        SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.hideAllTitles();
        dialog.setMessage(Html.fromHtml(getString(R.string.connect_dialog_body)));
        dialog.setNegativeButton(getString(R.string.no_thanks), null);
        dialog.setPositiveButton(getString(R.string.try_connect_now), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=com.trigger.connect"));
                startActivity(intent);
            }
        });
        dialog.show(getSupportFragmentManager(), "connect-dialog");
    }

    @Override
    public void onBackPressed() {
        if (!mTagMyTasks.equals(mCurrentTag)) {
            mPrefs.edit().putString(Constants.PREF_MAIN_SELECTED_TAB, mTagMyTasks).commit();
            toggleFab(mTagMyTasks);
            showFragment(mTagMyTasks);
        } else {
            super.onBackPressed();
            return;
        }
    }
    
    private String getTitleFromTag(String tag) {
        int res = R.string.app_name;
        
        if (mTagExamples.equals(tag)) {
            res = R.string.popular_tags;
        } else if (mTagMyTasks.equals(tag)) {
            res = R.string.my_tags;
        } else if (mTagStats.equals(tag)) {
            res = R.string.activity_feed;
        }

        //4_1_18
        /*else if (mTagFeedback.equals(tag)) {
            res = R.string.title_feedback;
        }*/
         else if (mTagOtherNfc.equals(tag)) {
            res = R.string.other_nfc;
        } else if (mTagShop.equals(tag)) {
            res = R.string.buy_nfc_tags;
        }  else if (mTagPlugins.equals(tag)) {
            res = R.string.plugins;
        }
        
        return getString(res);
    }
    
    private void setupNavDrawer() {
        Logger.d("Setup called");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        /*toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.app_name, R.string.app_name) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                mToolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                mToolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        mMenu = (ListView) findViewById(R.id.left_drawer);
        mMenu.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        buildMenuItems();
        mMenu.setAdapter(mMenuItemsAdapter);
        mMenu.setOnItemClickListener(mNavItemClicked);
        mMenu.setOnItemSelectedListener(mNavItemSelected);
    }
    
    private void updateDrawerContents() {
        buildMenuItems();
        mMenuItemsAdapter.notifyDataSetChanged();
        mMenu.setAdapter(mMenuItemsAdapter);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mToolbar.setTitle(mTitle);
        //getActionBar().setTitle(mTitle);
    }

    //return the initialized fragment manager...
    private FragmentManager getMyFragmentManager() {
        
        if (mManager == null) {
            mManager = getSupportFragmentManager();
        }
        return mManager;
    }

    //select specific fragment by tag to show on the screen...
    private void showFragment(String tag) {

        mCurrentTag = tag;
        if (mTagSettings.equals(tag) && !mIsPhoneLayout) {
            if (!mIsPhoneLayout) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            }
        } else {

            try {

                Fragment f = getFragment(tag);
                Fragment current = getMyFragmentManager().findFragmentById(R.id.pager);
                if ((current == null) || (!f.getClass().equals(current.getClass()))) {
                    FragmentTransaction transaction = getMyFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    transaction.replace(R.id.pager, f);
                    transaction.commitAllowingStateLoss();
                    setTitle(getTitleFromTag(tag));
                }

                if( (mMenuMap != null) && (!mMenuMap.isEmpty()) && (mMenuMap.containsKey(tag))) {
                    mMenu.setItemChecked(mMenuMap.get(tag), true);
                }

            } catch (Exception e) {
                Logger.e("Exception loading fragment " + tag + ": " + e, e);
            }
        }
    }

    //selection of fragment by its tag to show on the screen...
    private Fragment getFragment(String tag) {

        Fragment fragment = getMyFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            return fragment;
        }
        
        if (mTagExamples.equals(tag)) {
            return new ExamplesFragment();
        } else if (mTagMyTasks.equals(tag)) {
            return new TaskListFragment();
        } else if (mTagStats.equals(tag)) {
            return new ActivityFeedFragment();
        } else if (mTagFeedback.equals(tag)) {
            return new FeedbackFragment();
        } else if (mTagOtherNfc.equals(tag)) {
            return new OtherNfcFragment();
        } else if (mTagShop.equals(tag)) {
//            return new ShopFragment();
            return new ShopFragmentNew();
        } else if (mTagSettings.equals(tag)) {
          return new MainSettingsFragment();  
        } else if (mTagPlugins.equals(tag)) {
          return new PluginRepositoryFragment();  
        } else {
            return null;
        }
         
    }
    

    //list adapter for the menu list in the navigation drawer...
    private List<ListItem> mMenuItems;
    public class MenuItemsAdapter extends ListItemsAdapter {
       
        public MenuItemsAdapter(Activity activity, ListItem[] items) {
            super(activity, items);
        }
        
        @Override
        public View getView(int position, View v, ViewGroup parent) {
            ListItem item = getItem(position);
            if (v == null) {
                v = item.getView(this, position, v);
            }
            return v;
        }
        
    }

    private int position = 0;
    private void buildMenuItems() {

        mMenuItems = new ArrayList<ListItem>();
        mMenuMap = new HashMap<String, Integer>();

        /*mMenuItems.add(new ListTabletMenuSpacerItem(this, R.string.layoutPreferencesTagsTitle, "spacer_more"));
        position++;*/
        mMenuItems.add(new ListTabletMenuItem(this, R.drawable.ic_action_home, R.string.my_tags, false, mTagMyTasks));
        addTag(mTagMyTasks, position);

        if (FlavorInfo.BUILD_PROFILE.equals(Constants.BUILD_TAGSTAND)) {
            mMenuItems.add(new ListTabletMenuItem(this, R.drawable.ic_action_bulb, R.string.popular_tags, false, mTagExamples));
            addTag(mTagExamples, position);
        }


        // Add a not clickable spacer
      /*  mMenuItems.add(new ListTabletMenuSpacerItem(this, R.string.layoutPreferencesTagsMore, "spacer_more"));
        position++;*/
        mMenuItems.add(new ListTabletMenuItem(this, R.drawable.ic_action_bargraph, R.string.activity_feed, false, mTagStats));
        addTag(mTagStats, position);

        if (mShowShop) {
            mMenuItems.add(new ListTabletMenuItem(this, R.drawable.ic_action_basket, R.string.buy_tags, false, mTagShop));
            addTag(mTagShop, position);
        }
       
        if (BuildConfiguration.isPlayStoreAvailable()) {
            mMenuItems.add(new ListTabletMenuItem(this, R.drawable.ic_action_plug, R.string.plugins, false, mTagPlugins));
            addTag(mTagPlugins, position);
        }

        if (!BuildConfiguration.BUILD_PROFILE.equals(Constants.BUILD_KOREA)) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
                mMenuItems.add(new ListTabletMenuItemSmall(this, R.drawable.ic_action_tag, R.string.other_nfc, false, mTagOtherNfc, true));
                addTag(mTagOtherNfc, position);
            }
        }

        /*
        mMenuItems.add(new ListTabletMenuItemSmall(this, R.drawable.ic_category_messages, R.string.title_feedback, false, mTagFeedback, true));
        addTag(mTagFeedback, position);*/

        if (FlavorInfo.SHOW_UPGRADE && !hasPurchased()) {
            mUpgradeVisible = true;
            mMenuItems.add(new ListTabletMenuItemSmall(this, R.drawable.ic_category_security, R.string.upgrade, false, mTagUpgrade, false));
            addTag(mTagUpgrade, position);
        }

        mMenuItems.add(new ListTabletMenuItemSmall(this, R.drawable.ic_category_settings, R.string.menu_preferences, false, mTagSettings, false));
        addTag(mTagSettings, position);

        //4_1_18
        /*
        if (BuildConfiguration.isPlayStoreAvailable()) {
            if (!isLoggedIntoGoogle()) {
                mMenuItems.add(new ListTabletMenuItemSmall(this, R.drawable.btn_g_normal, R.string.sign_in_with_google, false, mTagGoogleSignin, false));
            } else {
                mMenuItems.add(new ListTabletMenuItemSmall(this, R.drawable.btn_g_normal, R.string.status_logged_in, false, mTagGoogleSignin, false));
            }
        }*/
        
        mMenuItemsAdapter = new MenuItemsAdapter(this,  mMenuItems.toArray(new ListItem[mMenuItems.size()]));
    }

    private void addTag(String tag, int p) {
        //Logger.d("Adding " + tag + " at " + p);
        mMenuMap.put(tag, p);
        position = p + 1;
    }

    //checks is the user is logged in to the google...
    private boolean isLoggedIntoGoogle() {
        String uToken = SettingsHelper.getPrefString(this, OAuthConstants.SSO_OAUTH_TOKEN_PREF, "");
        String uSecret = SettingsHelper.getPrefString(this, OAuthConstants.SSO_OAUTH_ACCOUNT_NAME, "");

        return (((!uToken.equals("")) || (!uSecret.equals(""))));
    }

    private AdapterView.OnItemSelectedListener mNavItemSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    //on item clicked in the navigator menu...
    private OnItemClickListener mNavItemClicked = new OnItemClickListener() {
        
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

            ListTabletMenuItem item = (ListTabletMenuItem) adapter.getItemAtPosition(position);

            if (item.getTag().isEmpty()) {
                return;
            }

            //signIn button is pressed...
            if (item.getTag().equals(mTagGoogleSignin)) {

                if (!isLoggedIntoGoogle()) {

                    startActivityForResult(new Intent(MainActivity.this, GoogleSignInExplanationActivity.class), REQUEST_SSO_SIGNIN);
                    overridePendingTransition(R.anim.bottom_slide_up, R.anim.bottom_slide_down);

                } else {

                    startActivityForResult(new Intent(MainActivity.this, GoogleSigninActivity.class).putExtra(PlusSigninActivity.EXTRA_DELETE_CREDENTIALS, true), REQUEST_SSO_SIGNIN);
                    overridePendingTransition(R.anim.bottom_slide_up, R.anim.bottom_slide_down);

                }
            }
            else if (item.getTag().equals(mTagSettings)) {

                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);

            } else if (item.getTag().equals(mTagUpgrade)) {

                //2_2_18
                //hiding the upgrade dialog box...
                //showUpgradeDialog();

            } else {

                // this is the common else block for all the othe event in the list
                //eg : My Task , Suggested Task , Activity Log , Trigger Shop, Plugins ...

                mPrefs.edit().putString(Constants.PREF_MAIN_SELECTED_TAB, item.getTag()).commit();

                //2_2_18
                //this is just for checking all the shop contacts...
                Log.i("karo","tag : "+item.getTag());

                toggleFab(item.getTag());
                showFragment(item.getTag());
                setTitle(getString(item.getTextId()));
            }

            mMenu.setItemChecked(position, item.selectInMenu());
            mDrawerLayout.closeDrawers();
            
        }
    };

    //called when the upgrade is successfully completed...
    private BroadcastReceiver upgradeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupNavDrawer();
            Toast.makeText(MainActivity.this, R.string.upgrade_successful, Toast.LENGTH_LONG).show();

        }
    };

    public void shopClicked(View v) {
        showFragment(mTagShop);
    }

    public void newTaskClicked(View v) {
        startActivityForResult(new Intent(this, TaskWizardActivity.class), REQUEST_CREATE_TASK);
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
          }
        
        switch (item.getItemId()) {
            case R.id.new_tag:
                startActivityForResult(new Intent(this, TaskWizardActivity.class)
                , REQUEST_CREATE_TASK);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //called from 3 different locations :
    //ExamplesFragment.java - ln 130
    //TaskListFragment.java - ln 531
    //MainActivity.java - ln 677

    public void showUpgradeDialog() {
        Intent intent = new Intent(this, TrialDialogActivity.class);
        intent.putExtra(TrialDialogActivity.EXTRA_HEADING, getString(R.string.upgrade_general_heading));
        intent.putExtra(TrialDialogActivity.EXTRA_TITLE, getString(R.string.upgrade_general_title));
        startActivityForResult(intent, REQUEST_UPGRADE);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        Logger.d("MAIN: returning for " + requestCode + ", result " + resultCode);
        switch(requestCode) {
            case REQUEST_CREATE_TASK:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Logger.d("Got non null data");
                    }
                    boolean showShop = (data != null) && data.getBooleanExtra(Constants.EXTRA_LOAD_SHOP_FRAGMENT, false);
                    showFragment((showShop) ? mTagShop : mTagMyTasks);
                }
                break;
            case REQUEST_SSO_SIGNIN:
                updateDrawerContents();
                break;
            case REQUEST_CREATE_EXAMPLE:
                if (resultCode == Activity.RESULT_OK) {
                    // See if we have a pending TaskSet as a part of this intent
                    if (data != null) {
                        Logger.d("Data is not null");
                        Logger.d("Has example " + data.hasExtra(ExampleTask.EXTRA_EXAMPLE_TASK));
                        Logger.d("Has Trigger " + data.hasExtra(Trigger.EXTRA_TRIGGER));
                        ExampleTask example = data.getParcelableExtra(ExampleTask.EXTRA_EXAMPLE_TASK);
                        Trigger trigger = data.getParcelableExtra(Trigger.EXTRA_TRIGGER);
                        if ((example != null) && (trigger != null)) {
                            Intent intent = new Intent(MainActivity.this, ImportTagActivity.class);
                            intent.putExtra(ExampleTask.EXTRA_EXAMPLE_TASK, example);
                            intent.putExtra(Trigger.EXTRA_TRIGGER, trigger);
                            startActivityForResult(intent, REQUEST_CREATE_EXAMPLE);
                            overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
                            break;
                        } else if (data.hasExtra("show_tasks_fragment")) {
                            showFragment(mTagMyTasks);
                        }
                    }
                    
                    // We don't have a pending task to insert so go ahead and update the display
                    if (mDrawerList != null) {
                        showFragment(mTagMyTasks);
                    }
                }
                break;
            case REQUEST_UPGRADE:
                setupNavDrawer();
                break;
        }
    }

    @Override
    public void showStore() {
        showFragment(mTagShop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Check Purchase status
        mOverflowMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, mOverflowMenu);

        return true;
    }

}