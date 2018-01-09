package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import java.util.HashMap;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItem;

import android.view.View;

public class ShippingOption implements ListItem {
    private String mName;
    private double mRate;
    private  String mCountry;
    private HashMap<String, Integer> mExcludedItems;
    
    public ShippingOption(String name, double rate, String country, HashMap<String, Integer> excluded) {
        mName = name;
        mRate = rate;
        mCountry = country;
        mExcludedItems = excluded;
    }
    
    public String getName() {
        return mName;
    }
    
    public double getRate() {
        return mRate;
    }
    
    public double getRateInDollars() {
        return mRate / 100;
    }
    
    public String getCountry() {
        return mCountry;
    }
    
    public boolean isItemExcluded(String item) {
        return mExcludedItems.containsKey(item); 
    }
   
    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        // Unused
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
