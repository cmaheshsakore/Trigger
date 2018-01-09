package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;

public class workerFoursquareAuthRequest extends BaseWorkerActivity {

    private WebView mAuthView;
    private String mVenueId;
    private String mAccessToken;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foursquare_auth_request);
        SettingsHelper.loadPreferences(this);
        mAuthView = (WebView) findViewById(R.id.authWebView);
        
        mVenueId = getIntent().hasExtra(workerFoursquareCheckin.EXTRA_VENUE_ID) ? getIntent().getStringExtra(workerFoursquareCheckin.EXTRA_VENUE_ID) : "";
    }

    @Override
    public void onResume() {
        super.onResume();
        mVenueId = getIntent().getStringExtra(workerFoursquareCheckin.EXTRA_VENUE_ID);
        getAuthData();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    public void getAuthData() {
        Logger.d("Called getAuthData");
        
        try {
            String authUrl = "https://foursquare.com/oauth2/authenticate?" +
                    "client_id=" + OAuthConstants.FOURSQUARE_CLIENT_ID +
                    "&response_type=token" + 
                    "&redirect_uri=" + Uri.encode("http://nfctl-foursquare");
            Logger.d("Going to: " + authUrl);

            mAuthView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("http://nfctl-")) {
                        Logger.d("Calling processURI on " + url);
                        mAuthView.setVisibility(View.GONE);
                        processURI(Uri.parse(url));
                    }

                    return false;
                }
            });

            mAuthView.getSettings().setJavaScriptEnabled(true);
            mAuthView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mAuthView.getSettings().setSaveFormData(false);
            mAuthView.getSettings().setSavePassword(false);
            mAuthView.getSettings().setBuiltInZoomControls(true);
            mAuthView.setInitialScale(50);
            mAuthView.loadUrl(authUrl);
            mAuthView.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processURI(Uri uri) {

        mAccessToken = "";

        if (uri != null) {
            Logger.d("Uri is " + uri.toString());
            if (uri.toString().contains("#access_token")) {

                String u = uri.toString();
                String[] values = u.split("=");
                if (values.length == 2) {
                    mAccessToken = values[1];
                } else {
                    for (int i=0; i< values.length; i++) {
                        if (values[i].endsWith("access_token")) {
                            mAccessToken = values[i + 1];
                        }
                    }
                }

                Logger.d("Received access token " + mAccessToken);
                SettingsHelper.setPrefString(this, OAuthConstants.FOURSQUARE_ACCESS_TOKEN_PREF, mAccessToken);
                doCheckin();
            }
        } else {
            Logger.d("Uri is null");
        }

    }

    protected void doCheckin() {
        Logger.d("Foursquare Auth: Passing token to checkin activity");
        Intent intent = new Intent();
        intent.putExtra(workerFoursquareCheckin.EXTRA_ACCESS_TOKEN, mAccessToken);
        intent.putExtra(workerFoursquareCheckin.EXTRA_VENUE_ID, mVenueId);
        setResult(RESULT_OK, intent);
        finish();
    }

}