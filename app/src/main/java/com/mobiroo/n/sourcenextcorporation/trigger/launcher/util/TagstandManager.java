package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 
 * Utility class to pushing tag data to the Tagstand Cloud and retrieving payloads from the cloud
 * @author krohnjw
 */
public class TagstandManager {
    
    private static final String mURLBase = "http://tags.to";
    private static final String mURLSuffix = "ntl";
    private static final String mUUIDKey = "uuid";
    private static final String mShareUUIDKey = "uid";
    private static final String mURLSecret = "xyz321=1";
    
    private static final String TRIAL_SIGNUP_URL = "http://tags.to/ntl_free_trial";
    private static final String mTrialKey = "email";
    
    public static final String REQUEST_FAILED = "request_failed";
    public static final String REQUEST_NOT_MAPPED = "request_empty";
    public static final String RESULT_EMPTY_PAYLOAD = "{\"payload\":\"\"}";
    
    public static final String EXTRA_PAYLOAD = "tagstand_manager_payload";
    /**
     * Used to push tag content to the cloud.  Uses TagPutRequest by default.  Runs asynchronously.
     * If you want to retrieve the short url from the push extend TagPutRequest.
     * @param context Application Context
     * @param UUID Tag UUID.  Can be a hardware UUID or other generated combination
     * @param payload Tag payload to save
     * @return Always returns false
     * 
     */
    public static boolean putTag(Context context, String UUID, String payload) {
        String postURL = generatePostURL(UUID);
        TagPutRequest request = new TagPutRequest(postURL, payload);
        request.execute();
        return false;
    }
    
    /**
     * Publicly available AsyncTask for pushing content to the Tagstand Cloud service.
     * Only defines doInBackground.  Returns a String URL that represents a shortened URL.
     * 
     * @author krohnjw
     *
     */
    public static class TagPutRequest extends AsyncTask<String, Void, String> {

        private String mPostURL;
        private String mPayload;
        private String mKey1;
        private String mKey2;
        private String mCondition;
        private int mType;
        public TagPutRequest(String postURL, String payload) {
            //Logger.d("Setting post URL to " + postURL.replace(mURLSecret, ""));
            mPostURL = postURL;
            mPayload = payload;
        }
        
        public TagPutRequest(String postURL, String payload, int type, String key1, String key2, String condition) {
            mPostURL = postURL;
            mPayload = payload;
            mKey1 = key1;
            mKey2 = key2;
            mType = type;
            mCondition = condition;
        }
        
        @Override
        protected String doInBackground(String... params) {
            JSONObject tagData = new JSONObject();
            
            try {
                tagData.put("payload", mPayload);
                if (null != mKey1) {
                    tagData.put("key1", mKey1);
                }
                if (null != mKey2) {
                    tagData.put("key2", mKey2);
                }
                if (null != mCondition) {
                    tagData.put("condition", mCondition);
                    tagData.put("type", mType);
                }
                
            } catch (JSONException e) {
                Logger.e(Constants.TAG, "Exception adding data to tag for saving", e);
            }

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(mPostURL);
            httppost.setHeader("content-type", "application/json");

            try {
                ByteArrayEntity entity = new ByteArrayEntity(tagData.toString().getBytes());
                httppost.setEntity(entity);
                
                HttpResponse response = httpclient.execute(httppost);
                
                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outstream);
                byte[] responseBody = outstream.toByteArray();
                String responseString = new String(responseBody);
                Logger.d("Put response = " + responseString);
                
                // Parse response into JSON object to get shortened URL
                JSONTokener tokener = new JSONTokener(new String(responseBody));
                try {
                    JSONObject obj = new JSONObject(tokener);
                    if (obj.has("success")) {
                        if (obj.has("short_url")) {
                            return obj.getString("short_url");
                        }
                        
                    } else {
                        return "";
                    }
                }
                catch (Exception e) { }
                
            } catch (UnsupportedEncodingException e) {
                Logger.e(Constants.TAG, "Unsupported encoding exception setting POST data", e);
            } catch (ClientProtocolException e) {
                Logger.e(Constants.TAG, "Client protocol exception setting POST data", e);
            } catch (IOException e) {
                Logger.e(Constants.TAG, "IOException setting POST data", e);
            }
            
            return "";
        }
        
    }

    /**
     * Goes to Tagstand cloud service to retrieve a previously saved tag payload based off of a 
     * unique identifier.  Either URL or UUID must be non null.  Do not run on UI thread.
     * @param context
     * @param UUID A unique tag identifier (can be null if url is populated)
     * @param url Shortened URL to get tag from (can be null if UUID is populated)
     * @return A tag payload if present or an empty string
     * @author krohnjw
     */
    public static String getTag(Context context, String UUID, String url) {
        String payload = TagstandManager.REQUEST_NOT_MAPPED;
        
        String getUrl = "";
        if (url == null) {
            getUrl = generateGetURL(UUID);
            Logger.d("Getting tag from " + getUrl.replace(mURLSecret, ""));
        } else {
            Logger.d("Getting tag from " + url);
            getUrl = url + "?" + mURLSecret;
        }

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,  5000);
        HttpClient httpclient = new DefaultHttpClient(httpParams);
        
        HttpGet httpget = new HttpGet(getUrl);
        try {
            HttpResponse response = httpclient.execute(httpget);
            
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            response.getEntity().writeTo(outstream);
            byte[] responseBody = outstream.toByteArray();
            
            Logger.d(Constants.TAG, new String(responseBody));
            
            /* Parse response into JSON doc */

            JSONTokener tokener = new JSONTokener(new String(responseBody));
            try {
                JSONObject obj = new JSONObject(tokener);
                String uuid = UUID;
                if (obj.has("payload")) {
                    
                    payload = obj.getString("payload");
                    
                    if ((uuid != null) && (payload != null) && (!uuid.isEmpty()) && (!payload.isEmpty())) {
                        /* We have non empty data, save locally */
                        ContentValues values = new ContentValues();
                        values.put("Payload", payload);
                        
                        
                            Logger.i("Missing condition");
                            int rowsAffected = 0;
                            try {
                                rowsAffected = context.getContentResolver().update(TaskProvider.Contract.LOCAL_MAPPING, values, "UUID=?", new String[]{uuid});
                            } catch (Exception e) {
                                Logger.e("Exception updating local mapping", e);
                                rowsAffected = 0;
                            }
                            
                            if (rowsAffected == 0) {
                                values.put("UUID", uuid);
                                try {
                                    context.getContentResolver().insert(TaskProvider.Contract.LOCAL_MAPPING, values);
                                } catch (Exception e) {
                                    Logger.e("Exception inserting task to local mapping", e);
                                }
                            }
                          
                    } else {
                        if (obj.has("condition")) {
                            // This is a trigger based tag  prepend the payload with condition, key1, key2, 
                            String condition = (String) obj.get("condition");
                            int type = (Integer) obj.get("type");
                            String key1 = (obj.has("key1")) ? (String) obj.get("key1") : "X";
                            String key2 = (obj.has("key2")) ? (String) obj.get("key2") : "X";
                            payload = condition + "," + type + "," + key1 + "," + key2 + "," + payload;
                        } 
                    }
                    
                } else if (obj.has("error")) {
                    payload = TagstandManager.REQUEST_NOT_MAPPED;
                }

            } catch (JSONException e) {
               Logger.e(Constants.TAG, "Exception parsing response from server", e);
               payload = TagstandManager.REQUEST_FAILED;
            }

        } catch (ClientProtocolException e) {
            Logger.e(Constants.TAG, "ClientProtocolException requesting tag data", e);
            payload = TagstandManager.REQUEST_FAILED;
        } catch (IOException e) {
            Logger.e(Constants.TAG, "IOException requesting tag data", e);
            payload = TagstandManager.REQUEST_FAILED;
        }

        return payload;
    }

    
    
    /**
     * Generates a URL for retrieving a previously stored tag payload
     * @param UUID A unique identifier for a specific tag.  This can be a hardware UUID or other unique combination.
     * @return A URL suitable for querying the Tagstand Cloud service to get a tag payload
     * @author krohnjw
     */
    public static String generateGetURL(String UUID) {
        return mURLBase + "/" + mURLSuffix + "?" + mUUIDKey + "=" + UUID + "&" + mURLSecret;
    }
    
    
    /**
     * Generates a URL suitable for posting against to save a payload to the Tagstand cloud.
     * @param UUID A unique identifier for a specific tag.  This can be a hardware UUID or other unique combination.
     * @return A URL suitable for posting
     * @author krohnjw
     */
    public static String generatePostURL(String UUID) {
        return mURLBase + "/" + mURLSuffix + "?" + mUUIDKey + "=" + UUID + "&" + mURLSecret;
    }
    
    
    /**
     * Returns a default share URL.  This should only be used as a fallback if the shortened URL generated from the push request is not returned
     * or if querying directly from a tag read (Hardware UUID).  Do not use for direct sharing as a primary option.
     * @param UUID A unique identifier for a specific tag.  This can be a hardware UUID or other unique combination.
     * @return A URL suitable for sharing a tag between devices (publicly).  Cannot be used to query directly
     * @author krohnjw
     */
    public static String generateShareURL(String UUID) {
        // Query URL shortener
        return mURLBase + "/" + mURLSuffix + "?" + mShareUUIDKey + "=" + UUID; 
    }
    
    private static class TrialPush extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams,  5000);
            HttpClient httpclient = new DefaultHttpClient(httpParams);
      
            try {
                HttpPost httppost = new HttpPost(TRIAL_SIGNUP_URL + "?" + mTrialKey + "=" + URLEncoder.encode(params[0], "UTF-8"));
                Logger.d("Posting to " + TRIAL_SIGNUP_URL + "?" + mTrialKey + "=" + URLEncoder.encode(params[0], "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                
                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outstream);
                byte[] responseBody = outstream.toByteArray();
                
                Logger.d(Constants.TAG, new String(responseBody));
            } catch (Exception e) {
                Logger.e("Exception posting trial signup: " + e, e);
            }
            return null;
        }
        
    }
    public static void putTrial(String email) {        
        new TrialPush().execute(email);

    }

}

