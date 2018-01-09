package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import android.content.Context;

public class PaymentInfo {
    private double mTaxRate;
    private double mShippingRate;
    private ShippingOption[] mShippingOptions;
    
    
    public PaymentInfo(Context context) {
    }
    
    public double getTaxRate() {
        return mTaxRate;
    }
    
    public double getTaxAmount(double price) {
        return mTaxRate * price;
    }
    
    public double getShippingRate() {
        return mShippingRate;
    }
    public void requestShippingOptions() {
        // Grab shipping options in an async task from server
    }
    
    public ShippingOption[] getShippingOptions() {
        return mShippingOptions;
    }
}
