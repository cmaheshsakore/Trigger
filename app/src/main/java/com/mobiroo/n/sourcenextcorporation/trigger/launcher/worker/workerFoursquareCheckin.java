package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class workerFoursquareCheckin extends LocationActivity {

    public static final String EXTRA_VENUE_ID = "foursquare_venue_id_extra";
    public static final String EXTRA_ACCESS_TOKEN = "foursquare_access_token";

    protected String mVenueId;
    protected String mAccessToken;

    protected double mLatitude;
    protected double mLongitude;
    protected float mAccuracy;
    protected double mAltitude;

    private boolean mHasPosted = false;

    private WebView mAuthView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.foursquare_auth_request);
        SettingsHelper.loadPreferences(this);
        mAuthView = (WebView) findViewById(R.id.authWebView);
        mAuthView.setVisibility(View.VISIBLE);
        SettingsHelper.loadPreferences(this);

        mVenueId = getIntent().getStringExtra(EXTRA_VENUE_ID);

        mNumUpdatesDesired = 2;
        mHandleUpdateWhenReady = false;
        mNumUpdatesReceived = 0;

        mVenueId = getIntent().getStringExtra(EXTRA_VENUE_ID);
        mAccessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);

        if (mAccessToken == null) {
            getAuthData();
        }
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
                    for (int i = 0; i < values.length; i++) {
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

        Logger.d("doCheckin called");
        mNumUpdatesDesired = 2;
        mHandleUpdateWhenReady = true;
        mNumUpdatesReceived = 0;
        mFinishWhenComplete = true;
        connect();
    }

    @Override
    protected void updateLocation(double latitude, double longitude, float accuracy, double altitude) {
        mLatitude = latitude;
        mLongitude = longitude;
        mAccuracy = accuracy;
        mAltitude = altitude;

        mNumUpdatesReceived++;

        Logger.d("FOURESQUARE: Received update for " + latitude + ", " + longitude);

        if (!mHasPosted) {
            mHasPosted = true;

            Logger.d("Foursquare: Posting Foursquare location");

            JSONObject data = new JSONObject();
            try {
                data.put("latitude", "" + mLatitude + "");
                data.put("longitude", "" + mLongitude + "");
                data.put("accuracy", "" + mAccuracy + "");

            } catch (JSONException ex) {
                Log.e("NFCT", "Foursquare: Excetion adding data");
            }

            // Need to check in via API now
            String baseURL = "https://api.foursquare.com/v2/checkins/add";

            String urlSuffix = "?oauth_token=" + mAccessToken;

            String url = baseURL + urlSuffix;
            url += "&venueId=" + mVenueId;
            url += "&ll=" + mLatitude + "," + mLongitude;
            url += "&llAcc=" + mAccuracy;
            Calendar cal = Calendar.getInstance();
            String year = String.valueOf(cal.get(Calendar.YEAR));
            String month = String.valueOf((cal.get(Calendar.MONTH) + 1));
            if (month.length() < 2)
                month = "0" + month;
            String day = String.valueOf(cal.get(Calendar.DAY_OF_YEAR));
            if (day.length() < 2)
                day = "0" + day;

            url += "&v=" + year + month + day;

            // Need to POST
            HttpClient httpclient = new DefaultHttpClient();

            try {
                HttpPost httppost = new HttpPost(url);
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outstream);
                byte[] responseBody = outstream.toByteArray();
                Logger.d("NFCT", new String(responseBody));

                // TODO : Check for error and show user a toast if code is not 200

            } catch (Exception e) {
                Logger.e(Constants.TAG, "Foursquare: Exception posting Foursquare checkin", e);
            }
            resumeProcessing(RESULT_OK);
        }
    }
}

