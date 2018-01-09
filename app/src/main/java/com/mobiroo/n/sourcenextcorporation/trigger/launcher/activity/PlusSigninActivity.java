package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.PlusSigninClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

public class PlusSigninActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_DELETE_CREDENTIALS = "com.trigger.launcher.delete_credentials";
    
    protected final int REQUEST_CODE_RESOLVE_ERR = 1001;
    
    protected String                mWalletScope;
    protected final String          mProfileScope = "https://www.googleapis.com/auth/userinfo.profile";
    protected final String          mEmailScope = "https://www.googleapis.com/auth/userinfo.email";
    protected final String          mPlusScope = "https://www.googleapis.com/auth/plus.login";
    protected final String          mWalletScopeSandbox = "https://www.googleapis.com/auth/paymentssandbox.make_payments";
    protected final String          mWalletScopeProduction = "https://www.googleapis.com/auth/payments.make_payments";
    
    protected PlusSigninClient mPlusClient;
    protected ConnectionResult          mConnectionResult;
    protected ProgressDialog            mProgressDialog;
    protected String                    mAccountName;
    protected String                    mId;
    protected boolean                   mDisconnect = false;
    protected boolean                   mHandleConnectWhenReady = false;
    protected boolean                   mShowDialogOnLoad = true;
    protected boolean                   mResolveWhenReady = true;
    
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState); 
        
        mWalletScope =(BuildConfiguration.getWalletEnvironment() == WalletConstants.ENVIRONMENT_PRODUCTION) ? mWalletScopeProduction : mWalletScopeSandbox;
        initializeProgressDialog();
        
        if (savedState == null) {
            loadCachedUserName();
            Logger.i("OnCreate: Building plus client");
            mPlusClient = buildPlusClient();
            mHandleConnectWhenReady = true;
        }
        
        if (getIntent().hasExtra(EXTRA_DELETE_CREDENTIALS)) {
            mDisconnect = true;
        }
        
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (mHandleConnectWhenReady) {
            Logger.d("Calling connect");
            connect();
        }
        
    }
    
    @Override
    public void onStop() {
        super.onStop();
        mResolveWhenReady = false;
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    
    protected void connect() {
        Logger.d("PlusClient: Connecting");
        getPlusClient().connect();
        
        if (mProgressDialog == null) {
            initializeProgressDialog();
        }
        if (mShowDialogOnLoad) {
            mProgressDialog.show();
        }
        
        mHandleConnectWhenReady = false;
    }
    
    protected ProgressDialog initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.loading));
        return mProgressDialog;
    
    }
    
    protected PlusSigninClient buildPlusClient() {
        Logger.d("BuildPlusClient: Building");

        return new PlusSigninClient(this, this, this);
    }

    protected void loadCachedUserName() {
        mAccountName = SettingsHelper.getPrefString(this, OAuthConstants.SSO_OAUTH_WALLET_ACCOUNT_NAME, "");
    }

    protected void logIn() {
        Logger.i("PlusClient: Login called");
        
        if (getPlusClient().isConnected()) {
            PlusSigninClient.ProfileInfo i = getPlusClient().getProfileInfo(null);
            mAccountName = i.email;
            Person person = getPlusClient().getPerson();
            Logger.d("PlusClient: Connected");
            if (person != null) {
                mId = person.getId();
                cacheLogin();
                Logger.d("PlusClient: Login Connected " + mAccountName);
                if (Usage.canLogData(this)) {
                    Usage.logProfileData(this, person, mAccountName);
                }
                runAfterConnected();
            } else {
                /* Couldn't get a Google+ Person, back out */
                SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutBasic);
                dialog.setMessage("Couldn't get Google+ user");
                dialog.setPositiveButton(getString(R.string.dialogOK), new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                    
                });
                if (!isFinishing()) {
                    dialog.show(getSupportFragmentManager(), "plus-dialog-failure");
                }
            }
            
            
            
        } else if (mConnectionResult == null) {
            Logger.d("PlusClient: No connection result found.  Calling connect");
            getPlusClient().connect();
        } else if (mConnectionResult.hasResolution()) {
            Logger.d("PlusClient: Login has resolution");
            try {
                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (SendIntentException e) {
               // Try connecting again.
                mConnectionResult = null;
                getPlusClient().connect();
            }
        } else {
            Logger.i("PlusClient: Login has no resolution");
        }
    }
    
    protected void runAfterConnected() {
        /* Override in extended activities */
    }
    
    protected void disconnectAndRevoke() {
        Plus.AccountApi.revokeAccessAndDisconnect(getPlusClient().getGoogleApiClient());
        SettingsHelper.setPrefBool(this, Constants.PREF_SSO_CHANGED, true);
        SettingsHelper.setPrefString(this, OAuthConstants.SSO_OAUTH_TOKEN_PREF, "");
        SettingsHelper.setPrefString(this, OAuthConstants.SSO_OAUTH_ACCOUNT_NAME, "");
        SettingsHelper.setPrefString(this, OAuthConstants.SSO_OAUTH_WALLET_ACCOUNT_NAME, "");
        finish();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("PlusClient: OnActivityResult: " + requestCode);
        switch (requestCode) {
            case REQUEST_CODE_RESOLVE_ERR:
                if (resultCode == Activity.RESULT_OK) {
                    mConnectionResult = null;
                    Logger.d("PlusClient: OnActivityResult Reconnecting");
                    mHandleConnectWhenReady = true;
                    if (mPlusClient == null) {
                        mPlusClient = buildPlusClient();
                    }
                    if (!getPlusClient().isConnected()) {
                        getPlusClient().connect();
                    }
                } else {
                    setResult(RESULT_CANCELED);
                    if (mPlusClient != null) {
                        getPlusClient().disconnect();
                    }
                    finish();
                }
                    
                break;
            default:
                if (mPlusClient != null) {
                    getPlusClient().disconnect();
                }
                this.setResult(RESULT_OK, new Intent());
                this.finish();
                break;
        }
    }
    
    protected final Handler OnError = new Handler() {
    };
    
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Logger.d("PlusClient: OnConnectionFailed with " + result.getErrorCode());
        mConnectionResult = result;
        
        if (mProgressDialog.isShowing()) {
            // The user clicked the sign-in button already.
            // Dismiss the progress dialog and start to resolve connection errors.
            mProgressDialog.dismiss();
        }
        
        if (mResolveWhenReady) {
            if (result.isSuccess()) {
                Logger.d("PlusClient: OnConnectionFailed: Calling Login");
                logIn();
            } else if (result.hasResolution()) {
                resolveConnection();
            }
        }
    }

    protected void resolveConnection() {
        Logger.d("PlusClient: OnConnnectionFailed: Resolving connection error");
        try {
                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
        } catch (SendIntentException e) {
                getPlusClient().connect();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        Logger.i("PlusClient: OnConnected");
        if (!mDisconnect) {
            Logger.d("Calling log in");
            logIn();
        } else {
            Logger.d("Calling disconnect and revoke");
            disconnectAndRevoke();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.d("PlusClient: connection suspended " + i);
    }
    
    protected void cacheLogin() {
        PlusSigninClient.ProfileInfo i = getPlusClient().getProfileInfo(null);
        String accountName = i.email;
        Person person = getPlusClient().getPerson();

        mAccountName = accountName;
        mId = person.getId();
        
        SettingsHelper.setPrefBool(this, Constants.PREF_SSO_CHANGED, true);
        SettingsHelper.setPrefString(this, OAuthConstants.SSO_OAUTH_TOKEN_PREF, mId);
        SettingsHelper.setPrefString(this, OAuthConstants.SSO_OAUTH_ACCOUNT_NAME, mAccountName);
        SettingsHelper.setPrefString(this, OAuthConstants.SSO_OAUTH_WALLET_ACCOUNT_NAME, mAccountName);
    }
    
    protected PlusSigninClient getPlusClient() {
        if (mPlusClient == null) {
            mPlusClient = buildPlusClient();
        }

        return mPlusClient;
    }
}
