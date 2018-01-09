package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;

public class workerTwitterAuthRequest extends BaseWorkerActivity {

    private String mAuthUrl;
    private WebView mAuthView;

    private OAuthConsumer mTwitterConsumer;
    private OAuthProvider mTwitterProvider;

    public static final String EXTRA_SKIP_MESSAGE = "com.trigger.launcher.extra_skip_twitter_message";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_auth_request);
        getAuthData();
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        getAuthData();
    }

    public void getAuthData() {
        SettingsHelper.loadPreferences(this);
        Logger.d("No credentials found - requesting authorization");
        try {
            mTwitterConsumer = new DefaultOAuthConsumer(OAuthConstants.TWITTER_CLIENT_ID, OAuthConstants.TWITTER_CLIENT_SECRET);
            mTwitterProvider = new DefaultOAuthProvider(OAuthConstants.TWITTER_REQUEST_TOKEN_URL, OAuthConstants.TWITTER_ACCESS_TOKEN_URL, OAuthConstants.TWITTER_AUTHORIZE_TOKEN_URL);

            mAuthView = (WebView) findViewById(R.id.twitterAuthWebView);
            mAuthView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Logger.i("Calling processURI");
                    Uri uri = Uri.parse(url);
                    if ((uri != null) && (uri.getScheme().equals(OAuthConstants.TWITTER_CALLBACK_SCHEME))) {
                        new processURIInBG().execute(uri);
                        mAuthView.setVisibility(View.GONE);
                    }
                    return false;
                }
            });

            TwitterAuthRequest request = new TwitterAuthRequest(mAuthView);
            request.execute();
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Error creating consumer / provider", e);
        }
    }

    private class processURIInBG extends AsyncTask<Uri, Void, Void> {

        protected Void doInBackground(final Uri... args) {
            processURI(args[0]);
            return null;
        }

        protected void onPostExecute(final Void unused) {
            resumeProcessing(RESULT_OK);
        }
    }

    public void processURI(Uri uri) {
        Logger.d("In process URI for " + uri);
        if (uri != null && uri.getScheme().equals(OAuthConstants.TWITTER_CALLBACK_SCHEME)) {
            try {
                // Log.i(TAG, "Callback received : " + uri);
                Logger.d("Retrieving Access Token");

                @SuppressWarnings("unused")
                final String access_token = uri.getQueryParameter("oauth_token");
                String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

                if (mTwitterProvider == null) {
                    mTwitterProvider = new DefaultOAuthProvider(OAuthConstants.TWITTER_REQUEST_TOKEN_URL, OAuthConstants.TWITTER_ACCESS_TOKEN_URL, OAuthConstants.TWITTER_AUTHORIZE_TOKEN_URL);
                }

                if (mTwitterConsumer == null) {
                    mTwitterConsumer = new DefaultOAuthConsumer(OAuthConstants.TWITTER_CLIENT_ID, OAuthConstants.TWITTER_CLIENT_SECRET);
                }

                mTwitterProvider.retrieveAccessToken(mTwitterConsumer, oauth_verifier);

                String token = mTwitterConsumer.getToken();
                String secret = mTwitterConsumer.getTokenSecret();
                SettingsHelper.setPrefString(this, OAuthConstants.TWITTER_TOKEN_PREF, token);
                SettingsHelper.setPrefString(this, OAuthConstants.TWITTER_SECRET_PREF, secret);

                mTwitterConsumer.setTokenWithSecret(token, secret);
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Exception thrown processing secret", e);
            }
        }

    }

    private class TwitterAuthRequest extends AsyncTask<Context, Void, Void> {
        private WebView aView;
        @SuppressWarnings("unused")
        private String authURL;

        public TwitterAuthRequest(WebView aView) {
            this.aView = aView;
        }

        @Override
        protected Void doInBackground(Context... arg0) {
            try {
                mAuthUrl = mTwitterProvider.retrieveRequestToken(mTwitterConsumer, OAuthConstants.TWITTER_CALLBACK_URL);
            } catch (Exception e) {
                Logger.e(Constants.TAG,"Exception creating twitter auth URL", e);
            }
            return null;
        }

        protected void onPostExecute(final Void unused) {
            Logger.d("Loading URL " + mAuthUrl);
            aView.loadUrl(mAuthUrl);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Twitter Auth: Returned with " + requestCode + ", result=" + resultCode);
        switch (requestCode) {
            case 1:
                resumeProcessing(RESULT_OK);
        }

    }

}
