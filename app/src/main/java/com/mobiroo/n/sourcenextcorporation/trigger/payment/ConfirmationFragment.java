package com.mobiroo.n.sourcenextcorporation.trigger.payment;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;


import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.ShippingOptionAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ShopItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.RequestConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.ShippingOption;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.ShopInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.TaxRate;
import com.mobiroo.n.sourcenextcorporation.trigger.util.NetworkUtil;

public class ConfirmationFragment extends Fragment implements OnClickListener{

    final int REQUEST_GET_PAYMENT_INFO = 9;

    private Button mChangeAddressButton;
    private Button mChangeShippingButton;

    private Address mAddressBilling;
    private Address mAddressShipping;
    private CreditCard mCard;

    private TextView mEmail;
    private TextView mShipping;
    private TextView mTax;
    private TextView mTotal;
    private TextView mPaymentDescriptions;
    private TextView mShippingAddress;

    private LinearLayout mShippingDetails;
    private LinearLayout mShippingProgress;
    private TableLayout mWalletDetails;
    private LinearLayout mWalletProgress;

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

    private boolean mUpdateShippingWhenReady;
    private Intent mIntent;


    protected ProgressDialog mProgressDialog;

    private boolean mHasRequestedData;
    
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

        if (!((mCard == null) || (mAddressShipping == null) || (mAddressBilling == null))) {
            new RequestShopData(ShopInfo.shopUrl, false).execute();
            mUpdateShippingWhenReady = true;
        }
    }

    @Override 
    public void onPause() {
        super.onPause();
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
            if (mAddressShipping != null) {

                try { 
                    mUrl += "&calculated=1";
                    mUrl += "&" + mCountryKey + URLEncoder.encode(mAddressShipping.getCountry().getCode(), "UTF-8");
                    mUrl += "&" + mRegionKey + URLEncoder.encode(mAddressShipping.getRegion().toLowerCase(Locale.ENGLISH) , "UTF-8");
                    mUrl += "&" + mCityKey + URLEncoder.encode(mAddressShipping.getCity(), "UTF-8");
                    mUrl += "&" + mPostcodeKey + URLEncoder.encode(mAddressShipping.getPostcode(), "UTF-8"); 
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
            if (mShowDialog) {
                mPd.dismiss();
            }

            updateUi();

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
            }
        });
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initializeProgressDialog();

        View view = inflater.inflate(R.layout.fragment_confirmation, container, false);

        view.findViewById(R.id.wallet_logo).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.text_item_name)).setText(mItem.getName());;
        ((TextView) view.findViewById(R.id.text_item_description)).setText(mItem.getDescription());
        ((TextView) view.findViewById(R.id.text_item_price)).setText(NumberFormat.getCurrencyInstance(Locale.US).format(mItem.getPriceInDollars()));


        TextView shippingLabel = (TextView) view.findViewById(R.id.text_shipping);
        shippingLabel.setText(R.string.shipping);

        mEmail = (TextView) view.findViewById(R.id.text_username);
        mShipping = (TextView) view.findViewById(R.id.text_shipping_price);
        mTax = (TextView) view.findViewById(R.id.text_tax_price);
        mTotal = (TextView) view.findViewById(R.id.text_total_price);
        mPaymentDescriptions = (TextView) view.findViewById(R.id.text_payment_descriptions);
        mShippingAddress = (TextView) view.findViewById(R.id.text_shipping_address);

        updateUi();

        mChangeAddressButton = (Button) view.findViewById(R.id.button_change_shipping_address);
        mChangeAddressButton.setOnClickListener(this);

        mChangeShippingButton = (Button) view.findViewById(R.id.button_change_shipping_method);
        mChangeShippingButton.setOnClickListener(this);

        mWalletDetails = (TableLayout) view.findViewById(R.id.wallet_details);
        mWalletProgress = (LinearLayout) view.findViewById(R.id.wallet_progress);
        mShippingDetails = (LinearLayout) view.findViewById(R.id.shipping_details);
        mShippingProgress = (LinearLayout) view.findViewById(R.id.shipping_progress);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == mChangeAddressButton) {
            changePaymentInfo();
        } else if (v.getId() == R.id.confirm_button) {
            confirmPurchase();
        } else if (v == mChangeShippingButton) {
            final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
            ShippingOptionAdapter adapter = null;

            if (mAddressShipping == null) {
                changePaymentInfo();
            } else {

                if (mShop == null) {
                    mShop = new ShopInfo(getActivity());
                    new RequestShopData(ShopInfo.shopUrl, false).execute();
                }
                
                adapter = new ShippingOptionAdapter(getActivity(), mShop.shipping.getValidShippingOptions(mAddressShipping.getCountry().getCode(), mItem.getId()));
                dialog.setListAdapter(adapter);
                dialog.setTitle(getString(R.string.choose_shipping_method));
                dialog.setListOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                        mSelectedShippingOption = position;
                        updateUi();
                        dialog.dismiss();
                    }

                });
                dialog.show(getFragmentManager(), "shipping-dialog");
            }
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


        mCard = (CreditCard) intent.getParcelableExtra(RequestConstants.EXTRA_CREDIT_CARD);
        mAddressShipping = (Address) intent.getParcelableExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS);
        mAddressBilling = (Address) intent.getParcelableExtra(RequestConstants.EXTRA_BILLING_ADDRESS);

        return true;
    }

    protected void onNewIntent(Intent intent) {
        if (processIntent(intent)) {
            updateUi();
        }
    }

    private void fetchTransactionStatus(int status) {
        // Send back details such as fullWallet.getProxyCard() and fullWallet.getBillingAddress()
        // and get back success or failure

        Logger.d("Order Completed");

        try {mProgressDialog.dismiss();}
        catch (Exception e) { }

        // If Successful exit to a success message

        Usage.logPurchaseEvent(null, "Purchase order completed", mItem, true);

        Intent intent = new Intent(getActivity(), OrderCompleteActivity.class);
        intent.putExtra(OrderCompleteActivity.EXTRA_ADDRESS, mAddressShipping);
        intent.putExtra("hide_wallet", true);
        if (mOrderNumber != null) {
            intent.putExtra(OrderCompleteActivity.EXTRA_ORDER_NUMBER, mOrderNumber);
        }
        if (mError != null) {
            intent.putExtra(OrderCompleteActivity.EXTRA_ERROR_MESSAGE, mError);
        }

        startActivity(intent);

        if (mError == null) {
            Intent data = new Intent();
            data.putExtra(Constants.EXTRA_WALLET_PURCHASE_COMPLETED, true);
            getActivity().setResult(Activity.RESULT_OK, data);
            getActivity().finish();
        }


    }

    private void changePaymentInfo() {
        mHasRequestedData = false;
        Intent intent = new Intent(getActivity(), PaymentInfoActivity.class);
        intent.putExtra(RequestConstants.EXTRA_BILLING_ADDRESS, mAddressBilling);
        intent.putExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS, mAddressShipping);
        intent.putExtra(RequestConstants.EXTRA_CREDIT_CARD, mCard);
        startActivityForResult(intent, REQUEST_GET_PAYMENT_INFO);
    }
    /**
     * Handles updating the UI when the masked wallet has changed.  This can happen if the customer
     * decides to change their payment instrument or their shipping address.
     * {@link ConfirmationFragment#mMaskedWallet} is expected to be updated and not {@code null}
     * before this method is called.
     */
    private void updateUi() {

        if ((mAddressShipping == null) || (mAddressBilling == null) || (mCard == null)) {
            // Pop activity for user to fill in info
            changePaymentInfo();
        } else {
            /* We have valid billing and shipping addresses, proceed */
            ShippingOption[] options = mShop.shipping.getValidShippingOptions(mAddressShipping.getCountry().getCode(), mItem.getId()); 

            if (mUpdateShippingWhenReady || (options == null || options.length == 0)) {
                Logger.d("Updating shipping info");
                mUpdateShippingWhenReady = false;
                if (!mHasRequestedData) {
                    mHasRequestedData = true;
                    new RequestShopData(ShopInfo.shopUrl, true).execute();
                }
            } else {
                // email may change if the user changes to a different Wallet account
                mEmail.setText(mAddressShipping.getEmail());

                // calculating exact shipping and tax should now be possible because there is a shipping
                // address in mMaskedWallet

                double shipping = options[mSelectedShippingOption].getRateInDollars();
                mShipping.setText(NumberFormat.getCurrencyInstance(Locale.US).format(shipping)); 

                // User may have changed state of billing address.
                String state = mAddressShipping.getRegion();
                String country = mAddressShipping.getCountry().getCode();
                Logger.d("State is " + state);
                Logger.d("Country is " + country);
                mTaxes = mShop.tax.getTaxRateFor(state, country);

                // Price is in cents
                mTaxAmount = mItem.getPriceInDollars() * (mTaxes.getRate() / 100);
                mTax.setText(NumberFormat.getCurrencyInstance(Locale.US).format(mTaxAmount));

                mTotalAmount = mItem.getPriceInDollars() + mTaxAmount + shipping; 
                mTotal.setText(NumberFormat.getCurrencyInstance(Locale.US).format(mTotalAmount));

                // display the payment descriptions of all of the payment instruments being used
                mPaymentDescriptions.setText("xxxxxxxxxxxx-" + mCard.getNumber().substring(mCard.getNumber().length() - 4));

                String address_text = Address.formatAddress(getActivity(),
                        mAddressShipping);

                if (address_text.isEmpty()) {
                    changePaymentInfo();
                    return;
                }

                mShippingAddress.setText(address_text);

                mWalletProgress.setVisibility(View.GONE);
                mWalletDetails.setVisibility(View.VISIBLE);
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
    private final String PAYMENT_SHIPPING_CITY = "shipping_city";
    private final String PAYMENT_SHIPPING_REGION = "shipping_region";
    private final String PAYMENT_SHIPPING_POSTCODE = "shipping_postcode";
    private final String PAYMENT_SHIPPING_COUNTRY = "shipping_country";
    private final String PAYMENT_SHIPPING_PHONE = "shipping_phone";

    private final String PAYMENT_BILLING_NAME = "billing_name";
    private final String PAYMENT_BILLING_COMPANY = "billing_company";
    private final String PAYMENT_BILLING_ADDRESS_1 = "billing_addr1";
    private final String PAYMENT_BILLING_ADDRESS_2 = "billing_addr2";
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

    private final String PAYMENT_CC_NUMBER = "cc_number"; // All digits
    private final String PAYMENT_CC_EXPIRATION = "cc_expiration"; // YYYY-MM
    private final String PAYMENT_CC_CVV = "cc_cvv"; // All digits
    private final String PAYMENT_SHIPPING_METHOD = "shipping_method";


    private void processPayment() {

        //  the full wallet can now be used to process the customer's payment
        // send the wallet info up to server to process, and to get the jwt to notify
        // the transaction status

        String accountNumber  =  mCard.getNumber();
        String securityCvv  = mCard.getCvc();
        String expirationYear = mCard.getExpiryYear();
        String expirationMonth = mCard.getExpiryMonth();
        Address billingAddress = mAddressBilling;
        Address shippingAddress = mAddressShipping;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PAYMENT_FULL_NAME, billingAddress.getName()));
        params.add(new BasicNameValuePair(PAYMENT_EMAIL, billingAddress.getEmail()));

        // Billing Info
        params.add(new BasicNameValuePair(PAYMENT_BILLING_NAME, billingAddress.getName()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_COMPANY, billingAddress.getCompanyName()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_ADDRESS_1, billingAddress.getStreetAddress()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_ADDRESS_2, billingAddress.getStreetSecondary()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_CITY, billingAddress.getCity()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_REGION, billingAddress.getRegion()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_POSTCODE, billingAddress.getPostcode()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_COUNTRY, billingAddress.getCountry().getCode()));
        params.add(new BasicNameValuePair(PAYMENT_BILLING_PHONE, billingAddress.getPhone()));

        // Shipping Info
        //params.add(new BasicNameValuePair(PAYMENT_SHIPPING_NAME, shippingAddress.getName()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_COMPANY, shippingAddress.getCompanyName()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_ADDRESS_1, shippingAddress.getStreetAddress()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_ADDRESS_2, shippingAddress.getStreetSecondary()));

        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_CITY, shippingAddress.getCity()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_REGION, shippingAddress.getRegion()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_POSTCODE, shippingAddress.getPostcode()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_COUNTRY, shippingAddress.getCountry().getCode()));
        params.add(new BasicNameValuePair(PAYMENT_SHIPPING_PHONE, shippingAddress.getPhone()));


        ShippingOption[] options = mShop.shipping.getValidShippingOptions(billingAddress.getCountry().getCode(), mItem.getId());  
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

        params.add(new BasicNameValuePair(PAYMENT_CC_NUMBER, accountNumber));
        params.add(new BasicNameValuePair(PAYMENT_CC_CVV, securityCvv));

        if (Integer.parseInt(expirationMonth) < 10) {
            params.add(new BasicNameValuePair(PAYMENT_CC_EXPIRATION, "20" + expirationYear + "-0" + Integer.parseInt(expirationMonth) + "-01"));
        } else {
            params.add(new BasicNameValuePair(PAYMENT_CC_EXPIRATION, "20" + expirationYear + "-" + expirationMonth + "-01"));
        }

        params.add(new BasicNameValuePair("wallet_type", "ntl_custom"));

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

            int status = -1;
            mError = null;

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
                        status = 1;
                    } 
                    catch (JSONException e) { Logger.e(Constants.TAG, "Error parsing order number", e); }
                } else {
                    mOrderPaid = false;
                    try { mError = obj.getString("error"); }
                    catch (Exception e) { Logger.e(Constants.TAG, "Error parsing err message", e); }
                }

                fetchTransactionStatus(status);
            } else {
                /* Handle error with network comm or post here */
                mOrderPaid = false;
                mError = "ERROR : " + response.getCode();
                fetchTransactionStatus(status);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mProgressDialog.hide();

        // retrieve the error code, if available
        requestCode = requestCode >> 16;
        Logger.d("Request code is " + requestCode + " result is " + resultCode + " data is null " + (data == null));
        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra(RequestConstants.EXTRA_CREDIT_CARD)) {
                    mCard = (CreditCard) data.getParcelableExtra(RequestConstants.EXTRA_CREDIT_CARD);
                }

                if (data.hasExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS)) {
                    mAddressShipping = (Address) data.getParcelableExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS);
                }

                if (data.hasExtra(RequestConstants.EXTRA_BILLING_ADDRESS)) {
                    mAddressBilling = (Address) data.getParcelableExtra(RequestConstants.EXTRA_BILLING_ADDRESS);
                }

                updateUi();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if ((mAddressShipping == null) && (mAddressBilling == null) && (mCard == null)) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
        } else {
            if (resultCode == Activity.RESULT_CANCELED) {
                if ((mAddressShipping == null) && (mAddressBilling == null) && (mCard == null)) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
        }
    }

    public void confirmPurchase() {
        mProgressDialog.setMessage(getString(R.string.finalize_payment));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        processPayment();
    }


}
