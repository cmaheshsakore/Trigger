package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;


import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;

public class RequestConstants {

    public static final int WALLET_ENVIRONMENT = BuildConfiguration.WALLET_BUILD_ENVIRONMENT;
    
    
    public static final String MERCHANT_NAME = "Tagstand";

    // Intent extra keys
    public static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";
    public static final String EXTRA_ITEM = "EXTRA_ITEM";
    public static final String EXTRA_MASKED_WALLET = "EXTRA_MASKED_WALLET";
    public static final String EXTRA_FULL_WALLET = "EXTRA_FULL_WALLET";

    public static final String EXTRA_SHIPPING_ADDRESS = "EXTRA_SHIPPING_ADDRESS";
    public static final String EXTRA_BILLING_ADDRESS = "EXTRA_BILLING_ADDRESS";
    public static final String EXTRA_CREDIT_CARD = "EXTRA_CREDIT_CARD";
    
    public static final String CURRENCY_CODE_USD = "USD";


    // values to use with KEY_DESCRIPTION
    public static final String DESCRIPTION_LINE_ITEM_SHIPPING = "Shipping";
    public static final String DESCRIPTION_LINE_ITEM_TAX = "Tax";

    public static final int WALLET_ERROR_NO_BILLING_ADDRESS = 9001;
    public static final int WALLET_ERROR_NO_SHIPPING_ADDRESS = 9002;

    private static final String PAYMENT_URL_DEV = "https://gettrigger.com/checkout/charge_create_order"; //:4500
    private static final String PAYMENT_URL_RELEASE = "https://gettrigger.com/checkout/charge_create_order";

    public static final int getWalletEnvironment() {
        return RequestConstants.WALLET_ENVIRONMENT;
    }
    
    public static final String getPaymentUrl() {
        return (getWalletEnvironment() == WalletConstants.ENVIRONMENT_PRODUCTION) ? PAYMENT_URL_RELEASE : PAYMENT_URL_DEV;
    }

}
