package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import android.content.Context;

public class ShopInfo {
    public static final String shopUrl= Constants.SHOP_URL_BASE + "/checkout/shipping_tax?app=launcher";
    
    public ShippingOptions shipping;
    public TaxRates tax;
 
    
    public ShopInfo(Context context) {
        tax = new TaxRates();
        shipping = new ShippingOptions();
    }
    
    public void loadDataFromResponse(JSONObject object) {
        
        if (object.has("shipping")) {
            loadShippingInfo(object);
        }
        
        if (object.has("taxes")) {
            loadTaxInfo(object);
        }
        
    }
    
    private void loadTaxInfo(JSONObject object) {
        TaxRate[] rates = new TaxRate[0];
        
        
        try {
            JSONArray options = object.getJSONArray("taxes");
            rates = new TaxRate[options.length()];
            
            for (int i=0; i< options.length(); i++) { 
                JSONObject current = (JSONObject) options.get(i);
                rates[i] = new TaxRate(current.getString("region"), current.getString("country"), current.getDouble("rate"));
            }
            
        } catch (JSONException e) {
            Logger.e("Exception getting tax info " + e);
        }
        if (rates.length > 0) {
            tax.setTaxRates(rates);
        } 
           
        
        
        
    }
    
    private void loadShippingInfo(JSONObject object) {
        ShippingOption[] shipping_options = new ShippingOption[0];
        try {
            JSONArray options = object.getJSONArray("shipping");
            shipping_options = new ShippingOption[options.length()];
            for (int i=0; i< options.length(); i++) {
                JSONObject current = (JSONObject) options.get(i);
                HashMap<String, Integer> excludedItems = new HashMap<String, Integer>();
                JSONArray excluded = current.getJSONArray("exclude");
                if(excluded.length() > 0) {
                    for (int j=0; j < excluded.length(); j++) {
                        excludedItems.put(excluded.getString(j), 1);
                    }
                }
                shipping_options[i] = new ShippingOption(current.getString("name"), current.getDouble("price"), current.getString("country"), excludedItems);
            }
            
        } catch (JSONException e) {
            Logger.e("Exception getting shipping info " + e);
        }
        
        if (shipping_options.length > 0) {
            shipping.setShippingOptions(shipping_options);
        }
            
        
        
    }
    
    
    
}
