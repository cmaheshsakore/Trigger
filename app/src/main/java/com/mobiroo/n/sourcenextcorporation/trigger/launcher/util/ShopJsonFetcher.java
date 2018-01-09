package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.mobiroo.n.sourcenextcorporation.trigger.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShopJsonFetcher {

    private static final String CATALOG_URL  = Constants.SHOP_URL_BASE + "/checkout/catalog.json?app=launcher&v=2";
    private Context mContext;
    
    public static final String PREFS_CACHE = "shop_cache_prefs";
    public static final String KEY_SHOP_CACHE = "shop_cache_5";
    public static final String KEY_CACHE_DATE = "shop_cache_date_5";
    public static final String KEY_SIG = "shop_signature_5";
    
    public static final boolean CACHE_RESPONSE = true;
    public static final boolean SKIP_CACHE_RESPONSE = false;
    
    private boolean mSkipCache = false;
    
    
    public ShopJsonFetcher(Context context) {
        mContext = context;
    }
    
    public String getShopUrl() {
        return CATALOG_URL;
    }
    
    public String loadData(boolean cache_response) throws Exception {
        
        String cache = loadFromCache();
        
        if (!cache.isEmpty() && !mSkipCache) {
            return cache;
        }
        
        Logger.d("Querying new shop data");
        NetworkUtil.Response r = NetworkUtil.getHttpResponse(getShopUrl(), NetworkUtil.METHOD_GET);

        if (r.getCode() != 200) {
            throw new Exception(String.format("%s %s", r.getCode(), r.getMessage()));
        }

        if (cache_response) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_CACHE, 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_SHOP_CACHE, r.getBody());
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            editor.putString(KEY_CACHE_DATE, sdf.format(new Date()));
            editor.putString(KEY_SIG, Utils.md5(r.getBody()));
            editor.commit();
        }
        
        return r.getBody();
    }
    
    private String loadFromCache() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_CACHE, 0);
        String cached_date = prefs.getString(KEY_CACHE_DATE, "");
        String now = sdf.format(new Date());
        if (cached_date.equals(now)) {

            String data = prefs.getString(KEY_SHOP_CACHE, "");
            if (!data.isEmpty()) {
                if (Utils.md5(data).equals(prefs.getString(KEY_SIG, ""))) {
                    return data;
                } else {
                    return "";
                }
            }
        }
        
        return "";
    }
    
    public JSONArray createArrayFromData(String data) throws Exception {
        JSONTokener tokener = new JSONTokener(data); 
        return new JSONArray(tokener);
    }
}
