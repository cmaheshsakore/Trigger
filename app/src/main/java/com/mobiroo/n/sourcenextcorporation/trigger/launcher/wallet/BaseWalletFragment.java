/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.lang.ref.WeakReference;

/**
 * Base class for common functionality for Fragments that use {@code WalletClient}.
 */
public abstract class BaseWalletFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Request code used when attempting to resolve issues with connecting to Google Play Services.
     * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
     * android.app.Activity, int)}.
     */
    public static final int REQUEST_CODE_RESOLVE_ERR = 1000;

    /**
     * Request code used when attempting to resolve issues with loading a masked wallet
     * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
     * android.app.Activity, int)}.
     */
    public static final int REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET = 1001;

    /**
     * Request code used when attempting to resolve issues with loading a full wallet
     * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
     * android.app.Activity, int)}.
     */
    public static final int REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET = 1012;

    /**
     * Request code used when attempting to resolve issues with changing a masked wallet
     * Only use this request code when calling {@link ConnectionResult#startResolutionForResult(
     * android.app.Activity, int)}.
     */
    public static final int REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET = 1003;

    // Maximum number of times to try to connect to WalletClient if the connection is failing
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY = 3000;
    private static final int MESSAGE_RETRY_CONNECTION = 1010;

    public static final double mEstimatedPrice = 20.00;
    
    private int mRetryCounter = 0;
    // handler for processing retry attempts
    private RetryHandler mRetryHandler;

    protected ProgressDialog mProgressDialog;
    // whether the user tried to do an action that requires a masked wallet (i.e.: loadMaskedWallet)
    // before a masked wallet was acquired (i.e.: still waiting to fetch the request JWT from the
    // server or waiting for mWalletClient to connect)
    protected boolean mHandleMaskedWalletWhenReady = false;
    // whether the user tried to do an action that requires a full wallet (i.e.: loadFullWallet)
    // before a full wallet was acquired (i.e.: still waiting to fetch the request JWT from the
    // server or waiting for mWalletClient to connect)
    protected boolean mHandleFullWalletWhenReady = false;
    protected int mItemId;

    // cached connection result
    protected ConnectionResult mConnectionResult;
    // result code to use if trying the cached result has a resolution
    protected int mRequestCode;

    // Store a user selected account
    protected String mAccount;
    
    protected boolean mCheckPreAuth = false;


    // NEW
    protected GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        buildApiClient();
        mRetryHandler = new RetryHandler(this);
    }

    
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    public void buildApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .setAccountName(getUser())
                .addConnectionCallbacks(BaseWalletFragment.this)
                .addOnConnectionFailedListener(BaseWalletFragment.this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(RequestConstants.WALLET_ENVIRONMENT)
                        .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                        .build())
                .build();
    }
    @Override
    public void onStop() {
        super.onStop();

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        mGoogleApiClient.disconnect();
        
        mRetryHandler.removeMessages(MESSAGE_RETRY_CONNECTION);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // don't need to do anything here
        // subclasses may override if they need to do anything
    }
    
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Logger.d("Connection failed");
        // Save the result so that it can be processed when the user taps a Google Wallet button
        mConnectionResult = result;
        mRequestCode = REQUEST_CODE_RESOLVE_ERR;

        Logger.d("Result is " + mConnectionResult.getErrorCode());
        // Handle the user's tap by dismissing the progress dialog and attempting to resolve the
        // connection result.
        if (mHandleMaskedWalletWhenReady || mHandleFullWalletWhenReady) {
            mProgressDialog.dismiss();

            resolveUnsuccessfulConnectionResult();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Helper to try to resolve a result that is not successful.
     * <p>
     * If the result has a resolution (i.e. the user must select a payment instrument),
     * {@link ConnectionResult#startResolutionForResult(Activity, int)} will be called
     * to allow the user to enter additional input.  Otherwise, if the error is user recoverable
     * (i.e. the user has an out of date version of Google Play Services installed), an error dialog
     * provided by
     * {@link GooglePlayServicesUtil#getErrorDialog(int, Activity, int, OnCancelListener)}. Finally,
     * if none of the other cases apply, the error will be handled in {@link #handleError(int)}.
     */
    protected void resolveUnsuccessfulConnectionResult() {
        Logger.d("Resolving unsuccessfull connection attempt");
        // Additional user input is needed
        if (mConnectionResult.hasResolution()) {
            try {
                Logger.d("Calling startResolution for  " + mRequestCode);
                mConnectionResult.startResolutionForResult(getActivity(), mRequestCode);
            } catch (SendIntentException e) {
                reconnect();
            }
        } else {
            Logger.d("Has no Resolution");
            int errorCode = mConnectionResult.getErrorCode();
            if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(),
                        REQUEST_CODE_RESOLVE_ERR, new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // get a new connection result
                                mGoogleApiClient.connect();
                            }
                        });

                // the dialog will either be dismissed, which will invoke the OnCancelListener, or
                // the dialog will be addressed, which will result in a callback to
                // OnActivityResult()
                dialog.show();
            } else {
                switch (errorCode) {
                    case ConnectionResult.SERVICE_MISSING:
                    case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    case ConnectionResult.SERVICE_DISABLED:
                        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), REQUEST_CODE_RESOLVE_ERR);
                        dialog.show();
                        break;
                    case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
                    case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
                    case WalletConstants.ERROR_CODE_UNKNOWN:
                        handleUnrecoverableGoogleWalletError(errorCode);
                        break;
                    case ConnectionResult.INTERNAL_ERROR:
                    case ConnectionResult.NETWORK_ERROR:
                        reconnect();
                        break;
                    default:
                        handleError(errorCode);
                }
            }
        } 

        // results are one time use
        mConnectionResult = null;
    }

    private void reconnect() {
        if (mRetryCounter < MAX_RETRIES) {

            try { mProgressDialog.show(); }
            catch (NullPointerException e) { }
            
            Message m = mRetryHandler.obtainMessage(MESSAGE_RETRY_CONNECTION);
            // back off exponentially
            long delay = (long) (INITIAL_RETRY_DELAY * Math.pow(2, mRetryCounter));
            mRetryHandler.sendMessageDelayed(m, delay);
            mRetryCounter++;
        } else {
            handleError(WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * For unrecoverable Google Wallet errors, send the user back to the checkout page to handle the
     * problem.
     *
     * @param errorCode
     */
    protected void handleUnrecoverableGoogleWalletError(int errorCode) {
        
        Logger.d("Handling unrecoverable error " + errorCode);
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        SimpleDialogFragment dialog = new SimpleDialogFragment();
        if (getActivity() != null) {
            dialog.setTitle(getString(R.string.wallet_error_title));
            dialog.setMessage(String.format(getString(R.string.wallet_error_message), Integer.toString(errorCode)));
            dialog.setPositiveButton(getString(R.string.dialogClose), dialog.dismissListener);
            dialog.show(getFragmentManager(), "fail-dialog");
        }

    }

    protected void handleError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
                Toast.makeText(getActivity(), getString(R.string.spending_limit_exceeded, errorCode),
                        Toast.LENGTH_LONG).show();
                break;
            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
            case WalletConstants.ERROR_CODE_UNKNOWN:
            default:
                // unrecoverable error
                String errorMessage = getString(R.string.google_wallet_unavailable) + "\n" +
                        getString(R.string.error_code, errorCode);
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                break;
        }
    }
    

    protected void initializeProgressDialog() {
        initializeProgressDialog(getString(R.string.loading));
    }
    
    protected void initializeProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                //mHandleMaskedWalletWhenReady = false;
                mHandleFullWalletWhenReady = false;
            }
        });
    }

    protected void displayNetworkErrorToast() {
        Toast.makeText(getActivity(), R.string.loadTagPayloadError, Toast.LENGTH_LONG).show();
    }

    private static class RetryHandler extends Handler {

        private WeakReference<BaseWalletFragment> mWeakReference;

        protected RetryHandler(BaseWalletFragment walletFragment) {
            mWeakReference = new WeakReference<BaseWalletFragment>(walletFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RETRY_CONNECTION:
                    BaseWalletFragment walletFragment = mWeakReference.get();
                    if (walletFragment != null) {
                        walletFragment.mGoogleApiClient.connect();
                    }
                    break;
            }
        }
    }
    
    protected String getUser() {
        mAccount = SettingsHelper.getPrefString(getActivity(), OAuthConstants.SSO_OAUTH_WALLET_ACCOUNT_NAME, null);
        return mAccount;
    }
    

    
    @SuppressWarnings("unused")
    private Account getAccountFromName(String name) {
        AccountManager am = AccountManager.get(getActivity());
        final Account[] accounts = am.getAccountsByType("com.google");
        final int size = accounts.length;

        for (int i = 0; i < size; i++) {
            if (accounts[i].name.equals(name)) {
                return accounts[i];
            }
        }
        
        return null;
    }
}
