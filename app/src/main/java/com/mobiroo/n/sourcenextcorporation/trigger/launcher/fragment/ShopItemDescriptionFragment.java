package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.ShopItemDescriptionActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.BaseImageLoader;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.BaseWalletFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.ConfirmationActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.WalletUtil;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.PendingImage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ShopItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.RequestConstants;

import java.text.NumberFormat;
import java.util.Locale;

public class ShopItemDescriptionFragment extends BaseWalletFragment implements OnClickListener  {

    private static final int    REQUEST_USER_LOGIN_WALLET = 1006;
    public static final int     REQUEST_WALLET_PURCHASE = 1007;
    public static final int     RESULT_BUYER_NEEDS_CHECKOUT = 5001;

    private MaskedWallet        mMaskedWallet;
    private ShopItem mItem;

    private MaskedWalletRequest mMaskedWalletRequest;
    private int                 mErrorCode;
    private boolean             mGoogleWalletDisabled = false;

    @SuppressWarnings("unused")
    private boolean             mUserHasPreAuthorized = false;

    @SuppressWarnings("unused")
    private int                 mWidthDp;

    private ImageView    mLargeImage;
    private ImageView    mPreviewImage1;
    private ImageView    mPreviewImage2;
    private ImageView    mPreviewImage3;
    private ProgressBar         mProgressLarge;
    private ProgressBar         mProgressPreviews;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItem = (ShopItem) getArguments().getParcelable(ShopItemDescriptionActivity.EXTRA_ITEM);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandleMaskedWalletWhenReady = false;
    }

    private void updateItemInfo() {
        String price = NumberFormat.getCurrencyInstance(Locale.US).format(mItem.getPrice() / 100.0); /* mBaseView price */
        String originalPrice = NumberFormat.getCurrencyInstance(Locale.US).format(mItem.getOriginalPrice() / 100.0);  /* if not the same as price this indicates it is discounted */

        ((TextView) getView().findViewById(R.id.name)).setText(mItem.getLongName());

        if (!originalPrice.equals(price)) {

            /* Set original price with strikethrough */
            TextView orig = (TextView) getView().findViewById(R.id.original_price);
            orig.setText(originalPrice);
            orig.setVisibility(View.VISIBLE);
            orig.setPaintFlags(orig.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            /* Set current "discounted" price */
            ((TextView) getView().findViewById(R.id.price)).setText(price);

        } else {
            ((TextView) getView().findViewById(R.id.price)).setText(price);
        }
        ((TextView) getView().findViewById(R.id.description)).setText(Html.fromHtml(mItem.getLongDescription()));
        //((ImageView) mView.findViewById(R.id.image_large)).setImageBitmap(mItem.getThumbnail());

    }

    

    @Override
    public void onResume() {
        super.onResume();
        
        getView().findViewById(R.id.buy_button).setOnClickListener(this);
        getView().findViewById(R.id.buy_international).setOnClickListener(this);
        mLargeImage = (ImageView) getView().findViewById(R.id.image_large);
        
        mPreviewImage1 = (ImageView) getView().findViewById(R.id.image_preview_1); 
        mPreviewImage1.setOnClickListener(this);

        mPreviewImage2 = (ImageView) getView().findViewById(R.id.image_preview_2); 
        mPreviewImage2.setOnClickListener(this);
        
        mPreviewImage3 = (ImageView) getView().findViewById(R.id.image_preview_3); 
        mPreviewImage3.setOnClickListener(this);

        mProgressLarge = (ProgressBar) getView().findViewById(R.id.progress_large);
        mProgressPreviews = (ProgressBar) getView().findViewById(R.id.progress_previews);

        /* Load preview Image 1 and 2 from URLs / Cache and push into ImageView */

        
        new ImageLoader(getActivity(), mLargeImage).execute(
                new PendingImage(mPreviewImage1, mItem.getPreviewImageUrl(1)),
                new PendingImage(mPreviewImage2, mItem.getPreviewImageUrl(2)),
                new PendingImage(mPreviewImage3, mItem.getPreviewImageUrl(3)));

        updateItemInfo();

        if (SettingsHelper.getPrefBool(getActivity(), Constants.PREF_SSO_CHANGED, false)) {
            mGoogleApiClient.disconnect();
            // SSO Changed.  connect wallet client
            buildApiClient();
            mGoogleApiClient.connect();
            mHandleMaskedWalletWhenReady = false;
            mMaskedWalletRequest = null;
            mMaskedWallet = null;
            SettingsHelper.setPrefBool(getActivity(), Constants.PREF_SSO_CHANGED, true);
        } else {

            // if there was an error, display it to the user
            if (mErrorCode > 0) {
                handleError(mErrorCode);
                // clear it out so it only gets displayed once
                mErrorCode = 0;
            }
            if (mMaskedWalletRequest == null) {
                createMaskedWalletRequest();
            }
        }
    }

    private class ImageLoader extends BaseImageLoader {

        public ImageLoader(Context context, ImageView largeView) {
            mContext = context;
            mLargeView = largeView;
        }

        @Override
        public void onPostExecute(PendingImage[] images) {
            boolean setLarge = false;
            for (int i=0; i < images.length; i++) {
                PendingImage image = images[i];

                if (!setLarge) {
                    if (image.getBitmap() != null) {
                        setLarge = true;
                        if (mLargeView != null) {
                            mProgressLarge.setVisibility(View.GONE);
                            mLargeView.setImageBitmap(image.getBitmap());
                            mLargeView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                image.loadBitmapIntoView();
            }
            mProgressPreviews.setVisibility(View.GONE);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_item_info, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWidthDp = Utils.getWidthInDp(getActivity());
    }

    private void createMaskedWalletRequest() {
        double mPossibleEstimate = mItem.getPriceInDollars() + mItem.getPriceInDollars() * 0.45;
        mMaskedWalletRequest = WalletUtil.createMaskedWalletRequest(getActivity(), mPossibleEstimate);
    }

    private void resolveUnsuccessfulConnectionResultWrapper() {
        if (mConnectionResult == null) {
            showProgressDialog();
            Logger.d("Loading masked wallet");
            Wallet.Payments.loadMaskedWallet(mGoogleApiClient, mMaskedWalletRequest, REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET);
            mHandleMaskedWalletWhenReady = true;

        } else {
            Logger.d("Connection error code is " + mConnectionResult.getErrorCode());
            if (mConnectionResult.getErrorCode() == WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR) {
                Logger.d("Switching to legacy checkout");
                mGoogleWalletDisabled = true;
                mHandleMaskedWalletWhenReady = false;
                doLegacyCheckout();
            } else {
                resolveUnsuccessfulConnectionResult();
            }
        }
    }

    private void buyWithGoogleWallet() {
        mHandleMaskedWalletWhenReady = true;

        if (mGoogleWalletDisabled) {
            displayGoogleWalletUnavailable(
                    (mConnectionResult != null) ? mConnectionResult.getErrorCode() : 0);
        } else if (mConnectionResult != null) {
            Logger.d("Resolving unsuccessful connection");
            resolveUnsuccessfulConnectionResultWrapper();
            
        } else {
            loadMaskedWallet();
        }
    }

    private void loadMaskedWallet() {
        if (mGoogleApiClient.isConnected()) {
            showProgressDialog();
            createMaskedWalletRequest();
            Wallet.Payments.loadMaskedWallet(mGoogleApiClient, mMaskedWalletRequest,
                    REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET);
        } else {
            if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
            mHandleMaskedWalletWhenReady = true;
        }
    }
    
    private void displayGoogleWalletUnavailable(int errorCode) {
        try {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), REQUEST_CODE_RESOLVE_ERR);
            dialog.show();
        } catch (Exception e) { Logger.e("Exception showing dialog"); }
    }

    private void doLegacyCheckout() {
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        showAddressConfirmation(mItem);
    }

    private void showAddressConfirmation(final ShopItem item) {
        Usage.logPurchaseEvent(null, "Google Checkout started", mItem, false);

        Intent intent = new Intent(getActivity(), com.mobiroo.n.sourcenextcorporation.trigger.payment.ConfirmationActivity.class);
        intent.putExtra(RequestConstants.EXTRA_ITEM, item);
        startActivity(intent);
    }

    private void launchConfirmationPage() {

        getUser();
        Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
        intent.putExtra(RequestConstants.EXTRA_ITEM, mItem);
        intent.putExtra(RequestConstants.EXTRA_MASKED_WALLET, mMaskedWallet);
        getActivity().startActivityForResult(intent, REQUEST_WALLET_PURCHASE);

        mMaskedWallet = null;  // Don't re-use this masked wallet request
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            initializeProgressDialog();
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.buy_button:
                if (BuildConfiguration.isWalletAvailable()) {
                    mHandleMaskedWalletWhenReady = true;
                    
                    
                    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity().getBaseContext()) == 0) {
                        Usage.logPurchaseEvent(null, "Purchase started", mItem, false);
                        buyWithGoogleWallet();
                    } else {
                        Usage.logPurchaseEvent(null, "Purchase Failed to start", mItem, true);
                        displayGoogleWalletUnavailable(mConnectionResult.getErrorCode());
                    }
                } else {
                    showAddressConfirmation(mItem);
                }
                break;
            case R.id.buy_international:
                Usage.logPurchaseEvent(null, "Purchase started", mItem, false);
                showAddressConfirmation(mItem);
                break;
            case R.id.image_preview_1:
                mLargeImage.setImageDrawable(mPreviewImage1.getDrawable());
                break;
            case R.id.image_preview_2:
                mLargeImage.setImageDrawable(mPreviewImage2.getDrawable());
                break;
            case R.id.image_preview_3:
                mLargeImage.setImageDrawable(mPreviewImage3.getDrawable());
                break;
        }

    }

    @Override
    protected void handleError(int errorCode) {
        Logger.d("Error is " + errorCode);
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
                Toast.makeText(getActivity(),
                        getString(R.string.spending_limit_exceeded, errorCode),
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
                mGoogleWalletDisabled = true;
                displayGoogleWalletErrorToast(errorCode);
                break;
        }
    }

    private void displayGoogleWalletErrorToast(int errorCode) {
        if (errorCode == WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR) {
            doLegacyCheckout();
        } else {
            String errorMessage = getString(R.string.google_wallet_unavailable) + "\n" +
                    getString(R.string.error_code, errorCode);
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mMaskedWallet == null) {
            Logger.d("Loading masked wallet request");
            if (mMaskedWalletRequest == null) {
                createMaskedWalletRequest();
            }
            
            if (mHandleMaskedWalletWhenReady) {
                if (mGoogleApiClient.isConnected()) {
                    Wallet.Payments.loadMaskedWallet(mGoogleApiClient, mMaskedWalletRequest, REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET);
                }
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("In ShopFragment onActivityResult with " + requestCode + ", " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_RESOLVE_ERR:
                // call connect regardless of success or failure
                // if the result was success, the connect should succeed
                // if the result was not success, this should get a new connection result
                mGoogleApiClient.connect();
                break;
            case REQUEST_CODE_RESOLVE_LOAD_MASKED_WALLET:
                hideProgressDialog();
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Usage.logPurchaseEvent(null, "Purchase Masked wallet loaded", mItem, true);
                        mMaskedWallet =
                                (MaskedWallet) data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                        launchConfirmationPage();
                        break;
                    case Activity.RESULT_CANCELED:
                        Usage.logPurchaseEvent(null, "Purchase User Canceled", mItem, true);
                        // fetch a new ConnectionResult by calling loadMaskedWallet() again
                        // this is necessary because ConnectionResults cannot be reused

                        /* Removing this call to loadMaskedWallet - it always re-throws the
                         * account chooser when a user cancels. 
                         */
                        //mWalletClient.loadMaskedWallet(mMaskedWalletRequest, getUserBundle(), this);
                        break;
                    default:
                        Usage.logPurchaseEvent(null, "Purchase Masked wallet error", true, new String[]{"wallet_error", String.valueOf(resultCode)});
                        int errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, 0);
                        handleError(errorCode);
                }
                break;
            case REQUEST_USER_LOGIN_WALLET:
                // User successfully logged in, time to continue their checkout flow
                // If the user canceled out of the login screen don't do anything.
                if (resultCode == Activity.RESULT_OK) {
                    // Recreating the menu so it now shows Logout
                    getActivity().invalidateOptionsMenu();
                    buyWithGoogleWallet();
                }
                break;
            case REQUEST_WALLET_PURCHASE:
                Logger.d("Response is " + REQUEST_WALLET_PURCHASE);
                if (resultCode == RESULT_BUYER_NEEDS_CHECKOUT) {
                    showAddressConfirmation(mItem);
                } else {
                    if ((data != null) && (data.hasExtra("error"))) {
                        int error = data.getIntExtra("error", -1);
                        if (error != -1) {
                            /* Show user an error dialog */
                            handleUnrecoverableGoogleWalletError(error);
                        }
                    } else {
                        getActivity().finish();
                    }
                }
            default:
                break;
        }
    }
}
