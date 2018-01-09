package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

public class TaxRates {
    
    public static final String[] STATES = {
        "AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY"
    };

    private static TaxRate[] mDefaultTaxRates = new TaxRate[] {
      new TaxRate("IL", "US", 8),
      new TaxRate("CA", "US", 9)
    };
    
    private static TaxRate[] mTaxRates;
    
    public TaxRates() {
        mTaxRates = mDefaultTaxRates;
    }
    
    public void setTaxRates(TaxRate[] rates) {
        mTaxRates = rates;
    }
    
    public TaxRate getTaxRateFor(String state, String country) {
        for (int i=0; i< mTaxRates.length; i++) {
            if (mTaxRates[i].getState().equals(state) && mTaxRates[i].getCountry().equals(country)) {
                return mTaxRates[i];
            }
        }
        return new TaxRate("", "", 0);
    }
    
    public TaxRate[] getTaxInfo() {
        return (mTaxRates == null) ? mDefaultTaxRates : mTaxRates;
    }
    
}
