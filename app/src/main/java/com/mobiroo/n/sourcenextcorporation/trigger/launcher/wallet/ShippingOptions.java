package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import java.util.ArrayList;
import java.util.HashMap;

public class ShippingOptions {

    private String mUS = "US";
    private String mOther = "OTHER";
    
    private ShippingOption mUSFirstClass = new ShippingOption("USPS First Class", 198, mUS, new HashMap<String, Integer>());
    private ShippingOption mUSPriority = new ShippingOption("USPS Priority", 775, mUS, new HashMap<String, Integer>());
    private ShippingOption mInternational = new ShippingOption("FedEx International Priority", 1900, mOther, new HashMap<String, Integer>());
    
    private ShippingOption[] mDefaultShippingOptions = new ShippingOption[] {
            mUSFirstClass,
            mUSPriority,
            mInternational
    };

    private ShippingOption[] mShippingOptions;
    
    public ShippingOptions() {
        mShippingOptions = new ShippingOption[0];
    }
    
    public ShippingOption[] getShippingOptions(String country) {
        ShippingOption[] options = getValidShippingOptions(country, "");
        
        if (options.length > 0) {
            return options;
        } else  {
            return getValidShippingOptions(mOther, "");
        }
    }
    
    public void setShippingOptions(ShippingOption[] options) {
        mShippingOptions = options;
    }
    
    public ShippingOption[] getValidShippingOptions(String country, String item) {
        ArrayList<ShippingOption> validOptions = new ArrayList<ShippingOption>();
        ShippingOption[] allOptions = (mShippingOptions == null) ? mDefaultShippingOptions : mShippingOptions; 
        for (int i=0; i< allOptions.length; i++) {
            if (allOptions[i].getCountry().equalsIgnoreCase(country)) {
                if (!item.isEmpty()) {
                    if (!allOptions[i].isItemExcluded(item)) {
                        validOptions.add(allOptions[i]);
                    }
                } else {
                    validOptions.add(allOptions[i]);
                }
            }
        }
        
        if (validOptions.size() == 0) {
            if (country.equals(mUS)) {
                return new ShippingOption[0];
                //return new ShippingOption[] { mUSFirstClass };
            } else {
                return new ShippingOption[0];
                //return new ShippingOption[] { mUSFirstClass };
            }
        }
        
        return validOptions.toArray(new ShippingOption[validOptions.size()]);
    }
    
}
