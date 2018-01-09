package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.NetComm;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;

public class FoursquareCheckinService extends Service {

    public static final String EXTRA_VENUE_ID = "foursquare_venue_id_extra";
    public static final String EXTRA_ACCESS_TOKEN = "foursquare_access_token";

    protected String mVenueId;
    protected String mAccessToken;

    protected double mLatitude;
    protected double mLongitude;
    protected float mAccuracy;
    protected double mAltitude;

    private boolean mHasPosted = false;


    public FoursquareCheckinService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SettingsHelper.loadPreferences(this);

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        mVenueId = intent.getStringExtra(EXTRA_VENUE_ID);
        mAccessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN);

        new CheckinTask().execute();

        return Service.START_NOT_STICKY;
    }

    private class CheckinTask extends AsyncTask<String, NetComm.Response, NetComm.Response> {

        @Override
        protected NetComm.Response doInBackground(String... params) {
            return checkAuth();
        }

        @Override
        protected void onPostExecute(NetComm.Response response) {
            Logger.d("Response is: " + response.getCode() + " , " + response.getBody());
            if (response.getCode() == 200) {
                // Check

            }
        }
    }

    private NetComm.Response checkAuth() {
        String authUrl = "https://foursquare.com/oauth2/authenticate?" +
                "client_id=" + OAuthConstants.FOURSQUARE_CLIENT_ID +
                "&response_type=token" +
                "&redirect_uri=" + Uri.encode("http://nfctl-foursquare");

        return NetComm.getHttpsResponse(this, authUrl, NetComm.METHOD_GET, null, null);
    }
}
