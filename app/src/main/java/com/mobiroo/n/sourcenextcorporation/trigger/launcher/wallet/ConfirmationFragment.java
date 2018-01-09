package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.wallet.Address;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ShopItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.util.NetworkUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfirmationFragment extends BaseWalletFragment implements OnClickListener {


    private MaskedWallet mMaskedWallet;
    private FullWallet mFullWallet;

    private Button mChangeShippingButton;

    private TextView mShipping;
    private TextView mTax;
    private TextView mTotal;

    private LinearLayout mShippingDetails;
    private LinearLayout mShippingProgress;

    private ShopItem mItem;
    private TaxRate mTaxes;
    private ShopInfo mShop;

    private double mTaxAmount;
    private double mTotalAmount;
    private int mSelectedShippingOption = 0;

    private String mOrderNumber;
    @SuppressWarnings("unused")
    private boolean mOrderPaid;
    private String mError;

    private String mBillingPhone;
    private String mShippingPhone;

    private boolean mUpdateShippingWhenReady;
    private Intent mIntent;

    // No. of times to retry loadFullWallet on receiving a ConnectionResult.INTERNAL_ERROR
    private static final int MAX_FULL_WALLET_RETRIES = 1;
    private int mRetryLoadFullWalletCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);


        mShop = new ShopInfo(getActivity());

        mIntent = getActivity().getIntent();
        if (mIntent != null) {
            processIntent(mIntent);
        }

        if (savedInstanceState == null) {
            Usage.logPurchaseEvent(null, "Purchase confirmation displayed", mItem, true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        new RequestShopData(ShopInfo.shopUrl, false).execute();
        mUpdateShippingWhenReady = true;
    }

    @Override 
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class RequestShopData extends AsyncTask<String, String, String> {

        private String mCountryKey = "shipping_country=";
        private String mRegionKey = "shipping_region=";
        private String mCityKey = "shipping_city=";
        private String mPostcodeKey = "shipping_postcode=";
        private String mUrl = "";

        private boolean mShowDialog = false;
        private ProgressDialog mPd;

        public RequestShopData(String url, boolean showProgressDialog) {
            mUrl = url;
            mShowDialog = showProgressDialog;
        }
        @Override
        protected void onPreExecute() {
            if (mShowDialog) {
                mPd = ProgressDialog.show(getActivity(), "", "Loading");
            }
            if (mMaskedWallet != null) {

                try { 
                    mUrl += "&calculated=1";
                    mUrl += "&" + mCountryKey + URLEncoder.encode(mMaskedWallet.getShippingAddress().getCountryCode(), "UTF-8");
                    mUrl += "&" + mRegionKey + URLEncoder.encode(mMaskedWallet.getShippingAddress().getState(), "UTF-8");
                    mUrl += "&" + mCityKey + URLEncoder.encode(mMaskedWallet.getShippingAddress().getCity(), "UTF-8");
                    mUrl += "&" + mPostcodeKey + URLEncoder.encode(mMaskedWallet.getShippingAddress().getPostalCode(), "UTF-8"); 
                }

                catch (Exception e) { Logger.e("Exception encoding shipping URL ", e); }
            }

            Logger.d("Shipping URL is " + mUrl);
        }
        @Override
        protected String doInBackground(String... params) {

            mUpdateShippingWhenReady = false;
            try {
                NetworkUtil.Response r = NetworkUtil.getHttpResponse(mUrl, NetworkUtil.METHOD_GET);
                mShop.loadDataFromResponse(r.getJson());

            } catch (Exception e) { }
            return null;
        }

        protected void onPostExecute(final String unused) {
            if (mShowDialog && (mPd != null)) {
                mPd.dismiss();
            }

            updateUiForNewMaskedWallet();

        }

    }
    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initializeProgressDialog();

        View view = inflater.inflate(R.layout.fragment_confirmation_wallet, container, false);

        ((TextView) view.findViewById(R.id.text_item_name)).setText(mItem.getName());;
        ((TextView) view.findViewById(R.id.text_item_description)).setText(mItem.getDescription());
        ((TextView) view.findViewById(R.id.text_item_price)).setText(NumberFormat.getCurrencyInstance(Locale.US).format(mItem.getPriceInDollars()));


        TextView shippingLabel = (TextView) view.findViewById(R.id.text_shipping);
        shippingLabel.setText(R.string.shipping);

        mShipping = (TextView) view.findViewById(R.id.text_shipping_price);
        mTax = (TextView) view.findViewById(R.id.text_tax_price);
        mTotal = (TextView) view.findViewById(R.id.text_total_price);

        updateUiForNewMaskedWallet();


        mChangeShippingButton = (Button) view.findViewById(R.id.button_change_shipping_method);
        mChangeShippingButton.setOnClickListener(this);

        mShippingDetails = (LinearLayout) view.findViewById(R.id.shipping_details);
        mShippingProgress = (LinearLayout) view.findViewById(R.id.shipping_progress);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm_button) {
            confirmPurchase();
        } else if (v == mChangeShippingButton) {
            final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
            dialog.setListAdapter(new ShippingOptionAdapter(getActivity(), mShop.shipping.getValidShippingOptions(mMaskedWallet.getShippingAddress().getCountryCode(), mItem.getId())));
            dialog.setTitle(getString(R.string.choose_shipping_method));
            dialog.setListOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                    mSelectedShippingOption = position;
                    updateUiForNewMaskedWallet();
                    dialog.dismiss();
                }

            });
            dialog.show(getFragmentManager(), "shipping-dialog");
        }
    }

    /**
     * Helper method to retrieve relevant data out of an intent.  If there is new data, the member
     * fields will be updated.
     *
     * @param intent The intent to retrieve data from.
     * @return {@code true} if the given {@code Intent} contained new data.
     */
    protected boolean processIntent(Intent intent) {
        // the masked wallet contains the customer's payment info and should be displayed on the
        // confirmation page
        mItem = (ShopItem) intent.getParcelableExtra(RequestConstants.EXTRA_ITEM);
        MaskedWallet maskedWallet = intent.getParcelableExtra(RequestConstants.EXTRA_MASKED_WALLET);

        if (maskedWallet != null) {
            mMaskedWallet = maskedWallet;
            return true;
        } else {
            return false;
        }
    }

    protected void onNewIntent(Intent intent) {
        if (processIntent(intent)) {
            updateUiForNewMaskedWallet();
        }
    }

    private double calculateTotal() {
        ShippingOption[] options = mShop.shipping.getValidShippingOptions(mMaskedWallet.getShippingAddress().getCountryCode(), mItem.getId());
        double shipping = options[mSelectedShippingOption].getRateInDollars(); 

        // User may have changed state of billing address.
        String state = mMaskedWallet.getShippingAddress().getState();
        String country = mMaskedWallet.getShippingAddress().getCountryCode();
        mTaxes = mShop.tax.getTaxRateFor(state, country);

        // Price is in cents
        mTaxAmount = mItem.getPriceInDollars() * (mTaxes.getRate() / 100);

        mTotalAmount = mItem.getPriceInDollars() + mTaxAmount + shipping;

        return mTotalAmount;
    }



    private void fetchTransactionStatus(FullWallet fullWallet, int status) {
        // Send back details such as fullWallet.getProxyCard() and fullWallet.getBillingAddress()
        // and get back success or failure

        Logger.d("Order Completed");

        try {mProgressDialog.dismiss();}
        catch (Exception e) { }

        // Send results to Google
        Wallet.Payments.notifyTransactionStatus(mGoogleApiClient,
                WalletUtil.createNotifyTransactionStatusRequest(fullWallet.getGoogleTransactionId(),status));


        // If Successful exit to a success message

        Usage.logPurchaseEvent(null, "Purchase order completed", mItem, true);

        Intent intent = new Intent(getActivity(), OrderCompleteActivity.class);
        intent.putExtra(RequestConstants.EXTRA_FULL_WALLET, mFullWallet);
        if (mOrderNumber != null) {
            intent.putExtra(OrderCompleteActivity.EXTRA_ORDER_NUMBER, mOrderNumber);
        }
        if (mError != null) {
            intent.putExtra(OrderCompleteActivity.EXTRA_ERROR_MESSAGE, mError);
        }

        startActivity(intent);

        Intent data = new Intent();
        data.putExtra(Constants.EXTRA_WALLET_PURCHASE_COMPLETED, true);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();


    }


    /**
     * Handles updating the UI when the masked wallet has changed.  This can happen if the customer
     * decides to change their payment instrument or their shipping address.
     * {@link ConfirmationFragment#mMaskedWallet} is expected to be updated and not {@code null}
     * before this method is called.
     */
    private void updateUiForNewMaskedWallet() {

        if (mMaskedWallet.getBillingAddress() == null) {
            /* we have no billing address, this should never happen! */
            getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra("error", RequestConstants.WALLET_ERROR_NO_BILLING_ADDRESS));
            getActivity().finish();
        } else if (mMaskedWallet.getShippingAddress() == null) {
            /* we have no billing address, this should never happen! */
            getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra("error", RequestConstants.WALLET_ERROR_NO_SHIPPING_ADDRESS));
            getActivity().finish();
        } else {
            /* We have valid billing and shipping addresses, proceed */
            ShippingOption[] options = mShop.shipping.getValidShippingOptions(mMaskedWallet.getShippingAddress().getCountryCode(), mItem.getId()); 

            if (mUpdateShippingWhenReady || (options == null || options.length == 0)) {
                Logger.d("Updating shipping info");
                mUpdateShippingWhenReady = false;
                new RequestShopData(ShopInfo.shopUrl, true).execute();
            } else {
                // email may change if the user changes to a different Wallet account

                mBillingPhone = mMaskedWallet.getBillingAddress().getPhoneNumber();
                mShippingPhone = mMaskedWallet.getShippingAddress().getPhoneNumber();

                // calculating exact shipping and tax should now be possible because there is a shipping
                // address in mMaskedWallet

                double shipping = options[mSelectedShippingOption].getRateInDollars();
                mShipping.setText(NumberFormat.getCurrencyInstance(Locale.US).format(shipping)); 

                // User may have changed state of billing address.
                String state = mMaskedWallet.getShippingAddress().getState();
                String country = mMaskedWallet.getShippingAddress().getCountryCode();
                mTaxes = mShop.tax.getTaxRateFor(state, country);

                // Price is in cents
                mTaxAmount = mItem.getPriceInDollars() * (mTaxes.getRate() / 100);
                mTax.setText(NumberFormat.getCurrencyInstance(Locale.US).format(mTaxAmount));

                mTotalAmount = mItem.getPriceInDollars() + mTaxAmount + shipping; 
                mTotal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(mTotalAmount));

                String address_text = WalletUtil.formatAddress(getActivity(),
                        mMaskedWallet.getShippingAddress());

                if (address_text.isEmpty()) {
                    handleUnrecoverableGoogleWalletError(9010);
                    return;
                }


                mShippingProgress.setVisibility(View.GONE);
                mShippingDetails.setVisibility(View.VISIBLE);
            }
        }

    }

    private final String PAYMENT_URL = RequestConstants.getPaymentUrl();
    private final String PAYMENT_FULL_NAME = "name";
    //private final String PAYMENT_LAST_NAME = "last_name";
    private final String PAYMENT_EMAIL = "email";
    //private final String PAYMENT_SHIPPING_NAME = "shipping_name";
    private final String PAYMENT_SHIPPING_COMPANY = "shipping_company";
    private final String PAYMENT_SHIPPING_ADDRESS_1 = "shipping_addr1";
    private final String PAYMENT_SHIPPING_ADDRESS_2 = "shipping_addr2";
    private final String PAYMENT_SHIPPING_ADDRESS_3 = "shipping_addr3";
    private final String PAYMENT_SHIPPING_CITY = "shipping_city";
    private final String PAYMENT_SHIPPING_REGION = "shipping_region";
    private final String PAYMENT_SHIPPING_POSTCODE = "shipping_postcode";
    private final String PAYMENT_SHIPPING_COUNTRY = "shipping_country";
    private final String PAYMENT_SHIPPING_PHONE = "shipping_phone";

    private final String PAYMENT_BILLING_NAME = "billing_name";
    private final String PAYMENT_BILLING_COMPANY = "billing_company";
    private final String PAYMENT_BILLING_ADDRESS_1 = "billing_addr1";
    private final String PAYMENT_BILLING_ADDRESS_2 = "billing_addr2";
    private final String PAYMENT_BILLING_ADDRESS_3 = "billing_addr3";
    private final String PAYMENT_BILLING_CITY = "billing_city";
    private final String PAYMENT_BILLING_REGION = "billing_region";
    private final String PAYMENT_BILLING_POSTCODE = "billing_postcode";
    private final String PAYMENT_BILLING_COUNTRY = "billing_country";
    private final String PAYMENT_BILLING_PHONE = "billing_phone";
    private final String PAYMENT_CART_CONTENT = "cart_contents"; // Comma delimited string.  Quantity,Item,Price in cents
    private final String PAYMENT_SUBTOTAL = "subtotal"; // Int in cents
    private final String PAYMENT_TAX = "tax"; // Int in cents
    private final String PAYMENT_SHIPPING_PRICE = "shipping_price"; // Int in cents
    private final String PAYMENT_TOTAL_PRICE = "total_price"; // Int in cents
    private final String PAYMENT_GOOGLE_TRANS_ID = "google_transaction_id";
    private final String PAYMENT_MERCHANT_TRANS_ID = "merchant_transaction_id";
    private final String PAYMENT_CC_NUMBER = "cc_number"; // All digits
    private final String PAYMENT_CC_EXPIRATION = "cc_expiration"; // YYYY-MM
    private final String PAYMENT_CC_CVV = "cc_cvv"; // All digits
    private final String PAYMENT_SHIPPING_METHOD = "shipping_method";


    private void processPayment(FullWallet fullWallet) {
        mFullWallet = fullWallet;

        //  the full wallet can now be used to process the customer's payment
        // send the wallet info up to server to process, and to get the jwt to notify
        // the transaction status

        String accountNumber  =  mFullWallet.getProxyCard().getPan();
        String securityCvv  = mFullWallet.getProxyCard().getCvn();
        int expirationYear = mFullWallet.getProxyCard().getExpirationYear();
        int expirationMonth = mFullWallet.getProxyCard().getExpirationMonth();
        Address billingAddress = mFullWallet.getBillingAddress();
        Address shippingAddress = mFullWallet.getShippingAddress();
        String googleTransId = mFullWallet.getGoogleTransactionId();
        String merchantTransId = mFullWallet.getMerchantTransactionId();

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PAYMENT_FULL_NAME, billingAddress.getName()));
        params.add(new BasicNameValuePair(PAYMENT_EMAIL, mFullWallet.getEmail()));

        // Billing Info
        params.add(new BasicNameValuePair(PAYMENT_BILLING_NAME, billingAddress.getName()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_COMPANY, billingAddress.getCompanyName()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_ADDRESS_1, billingAddress.getAddress1()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_ADDRESS_2, billingAddress.getAddress2()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_ADDRESS_3, billingAddress.getAddress3()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_CITY, billingAddress.getCity()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_REGION, billingAddress.getState()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_POSTCODE, billingAddress.getPostalCode()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_COUNTRY, billingAddress.getCountryCode()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_PHONE, mBillingPhone));

        // Shipping Info
        //params.add(new BasicNameValuePair(PAYMENT_SHIPPING_NAME, shippingAddress.getName()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_COMPANY, shippingAddress.getCompanyName()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_ADDRESS_1, shippingAddress.getAddress1()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_ADDRESS_2, shippingAddress.getAddress2()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_ADDRESS_3, shippingAddress.getAddress3()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_CITY, shippingAddress.getCity()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_REGION, shippingAddress.getState()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_POSTCODE, shippingAddress.getPostalCode()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_COUNTRY, shippingAddress.getCountryCode()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_PHONE, mShippingPhone));


        ShippingOption[] options = mShop.shipping.getValidShippingOptions(mMaskedWallet.getBillingAddress().getCountryCode(), mItem.getId());  
        int shipping = (int) Math.round(options[mSelectedShippingOption].getRate());
        int price = (int) Math.round(mItem.getPrice());
        int tax = (int) Math.round(mItem.getPrice() * (mTaxes.getRate() / 100));
        int total = price + shipping + tax;

        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_METHOD, options[mSelectedShippingOption].getName()));

        params.add(new BasicNameValuePair(PAYMENT_CART_CONTENT, "1," + mItem.getId() + "," + price));
        params.add(new BasicNameValuePair(PAYMENT_SUBTOTAL, Integer.toString(price)));
        params.add(new BasicNameValuePair(PAYMENT_TAX, Integer.toString(tax)));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_PRICE, Integer.toString(shipping)));
        params.add(new BasicNameValuePair(PAYMENT_TOTAL_PRICE, Integer.toString(total)));

        params.add(new BasicNameValuePair(PAYMENT_GOOGLE_TRANS_ID, googleTransId));
        params.add(new BasicNameValuePair(PAYMENT_MERCHANT_TRANS_ID, merchantTransId)); 

        params.add(new BasicNameValuePair(PAYMENT_CC_NUMBER, accountNumber));
        params.add(new BasicNameValuePair(PAYMENT_CC_CVV, securityCvv));

        if (expirationMonth < 10) {
            params.add(new BasicNameValuePair(PAYMENT_CC_EXPIRATION, expirationYear + "-0" + expirationMonth + "-01"));
        } else {
            params.add(new BasicNameValuePair(PAYMENT_CC_EXPIRATION, expirationYear + "-" + expirationMonth + "-01"));
        }

        PostPayment process = new PostPayment(params);
        process.execute(PAYMENT_URL);

    }

    private class PostPayment extends AsyncTask <String, String, NetworkUtil.Response> {

        private List<NameValuePair> mParams;

        public PostPayment(List<NameValuePair> params) {
            mParams = params;
        }

        @Override
        protected NetworkUtil.Response doInBackground(String... args) {
            Logger.d("Posting payment to " + args[0]);
            try {
                return NetworkUtil.getHttpsResponse(args[0], NetworkUtil.METHOD_POST, mParams);
            } catch (Exception e) { 
                Logger.e(Constants.TAG, "Exception processing payment", e); 
            }

            return null;
        }

        protected void onPostExecute(final NetworkUtil.Response response) {

            int status = NotifyTransactionStatusRequest.Status.Error.UNKNOWN;

            Logger.d("payment response = " + response.getBody());

            JSONObject obj = null;
            try {
                obj = response.getJson();
            } catch (JSONException e) {
                Logger.e(Constants.TAG, "Exception parsing server response", e);
            }

            if (obj != null) {
                if (obj.has("success")) {
                    // Get Order #
                    mOrderPaid = true;
                    try { 
                        mOrderNumber = obj.getString("order_number");
                        status = NotifyTransactionStatusRequest.Status.SUCCESS;
                    } 
                    catch (JSONException e) { Logger.e(Constants.TAG, "Error parsing order number", e); }
                } else {
                    mOrderPaid = false;
                    try { mError = obj.getString("error"); }
                    catch (Exception e) { Logger.e(Constants.TAG, "Error parsing err message", e); }
                }

                fetchTransactionStatus(mFullWallet, status);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mProgressDialog.hide();

        // retrieve the error code, if available
        int errorCode = -1;
        if (data != null) {
            errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
        }

        switch (requestCode) {
            case REQUEST_CODE_RESOLVE_ERR:
                if (resultCode == Activity.RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    handleUnrecoverableGoogleWalletError(errorCode);
                }
                break;
            case REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mMaskedWallet = data.getParcelableExtra(
                                WalletConstants.EXTRA_MASKED_WALLET);

                        updateUiForNewMaskedWallet();
                        break;
                    case Activity.RESULT_CANCELED:
                        // no action needed
                        break;
                    default:
                        handleError(errorCode);
                }
                break;
            case REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if(data.hasExtra(WalletConstants.EXTRA_FULL_WALLET)) {
                            mFullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);

                            // the full wallet can now be used to process the customer's payment
                            // send the wallet info up to server to process, and to get the result
                            // for sending a transaction status

                            processPayment(mFullWallet);
                        } else if (data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
                            mMaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                            updateUiForNewMaskedWallet();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // no action needed
                        break;
                    default:
                        handleError(errorCode);
                }
                break;
            default:
                break;
        }
    }



    public void changeMaskedWallet(MaskedWallet wallet) {
        mMaskedWallet = wallet;
        mHandleMaskedWalletWhenReady = true;
    }

    public void confirmPurchase() {
        // the user needs to resolve an issue before WalletClient can connect
        if (mConnectionResult != null &&
                mRequestCode == REQUEST_CODE_RESOLVE_ERR) {
            resolveUnsuccessfulConnectionResult();
        } else {
            getFullWallet();
            mProgressDialog.setMessage(getString(R.string.finalize_payment));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            mHandleFullWalletWhenReady = true;
        } 
    }

    private void getFullWallet() {
        Logger.d("Getting Full Wallet");
        /* Get full shipping info */
        ShippingOption[] options = mShop.shipping.getValidShippingOptions(mMaskedWallet.getBillingAddress().getCountryCode(), mItem.getId());
        double shipping = options[mSelectedShippingOption].getRateInDollars(); 

        String state = mMaskedWallet.getShippingAddress().getState();
        String country = mMaskedWallet.getShippingAddress().getCountryCode();
        mTaxes = mShop.tax.getTaxRateFor(state, country);


        Wallet.Payments.loadFullWallet(mGoogleApiClient,
                WalletUtil.createFullWalletRequest(
                        getActivity(), 
                        mItem,
                        mMaskedWallet.getGoogleTransactionId(), 
                        shipping, 
                        mTaxes, 
                        calculateTotal()
                        ),
                        REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET
                );
    }

}
