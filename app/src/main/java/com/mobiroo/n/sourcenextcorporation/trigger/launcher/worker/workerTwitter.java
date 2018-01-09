package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

public class workerTwitter extends BaseWorkerActivity {

    private static String mMessage;
    private final int REQUEST_TWITTER_AUTH = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_empty);
        mMessage = getIntent().getStringExtra(Constants.EXTRA_TWITTER_MESSAGE);
    }

    @Override
    public void onResume() {
        super.onResume();
        SettingsHelper.loadPreferences(this);
        // Check our stored preferences
        String uToken = SettingsHelper.getPrefString(this, OAuthConstants.TWITTER_TOKEN_PREF, "");
        String uSecret = SettingsHelper.getPrefString(this, OAuthConstants.TWITTER_SECRET_PREF, "");

        // If we are missing the token or sercret, request
        if ((uToken.equals("")) || (uSecret.equals(""))) {
            Logger.d("No credentials found - requesting authorization");
            Intent intent = new Intent(workerTwitter.this, workerTwitterAuthRequest.class);
            intent.putExtra(Constants.EXTRA_TWITTER_MESSAGE, mMessage);
            startActivityForResult(intent, REQUEST_TWITTER_AUTH);
        } else {
            Logger.d("Found credentials - posting tweet");
            new postMessageTask().execute();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Twitter Auth: Returned with " + requestCode + ", result=" + resultCode);
        resumeProcessing(RESULT_OK);
    }

    public class postMessageTask extends AsyncTask <Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            if ((mMessage != null) && (!mMessage.equals(""))) {

                String uToken = SettingsHelper.getPrefString(workerTwitter.this, OAuthConstants.TWITTER_TOKEN_PREF);
                String uSecret = SettingsHelper.getPrefString(workerTwitter.this, OAuthConstants.TWITTER_SECRET_PREF);

                CommonsHttpOAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(OAuthConstants.TWITTER_CLIENT_ID, OAuthConstants.TWITTER_CLIENT_SECRET);
                postConsumer.setTokenWithSecret(uToken, uSecret);
                HttpClient httpclient = new DefaultHttpClient();
                
                HttpPost httppost;
                try {
                    httppost = new HttpPost("https://api.twitter.com/1.1/statuses/update.json?status=" + URLEncoder.encode(mMessage, "UTF-8"));
                    httppost.setHeader("content-type", "application/json");
                    // SIGN PACKAGE
                    postConsumer.sign(httppost);

                    HttpResponse response = httpclient.execute(httppost);
                    ByteArrayOutputStream outstream = new ByteArrayOutputStream();

                    response.getEntity().writeTo(outstream);
                    byte[] responseBody = outstream.toByteArray();
                    String responseString = new String(outstream.toByteArray());
                    if (responseString.contains("\"error\"")) {
                        
                        try {
                            JSONTokener tokener = new JSONTokener(responseString);
                            JSONObject obj = new JSONObject(tokener);
                            if (obj.has("error")) { 
                                return obj.getString("error");
                                
                            }
                        } catch (Exception e) { Logger.e("Exception parsing twitter status " + e); e.printStackTrace(); }
                    }
                    Logger.d(Constants.TAG, new String(responseBody));

                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception posting tweet", e);
                }

                mMessage = "";
            }
            return "";
        }
        
        @Override
        protected void onPostExecute(String error) {
            if ((error != null) && (!error.isEmpty())) {
                Logger.d("Twitter returned error: " + error);
                Toast.makeText(workerTwitter.this, error, Toast.LENGTH_LONG).show();
            }

            resumeProcessing(RESULT_OK);
    
        }
        
    }

}
