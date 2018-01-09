package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;


public class TaxRate {
    
    private String mRegion;
    private String mCountry;
    private double mRate;
    
    public TaxRate(String region, String country, double rate) {
        mRegion = region;
        mCountry = country;
        mRate = rate;
    }

    public double getRate() {
        return mRate;
    }
    
    public String getState() {
        return mRegion;
    }
    
    public String getRegion() {
        return mRegion;
    }
    
    public String getCountry() {
        return mCountry;
    }

}
