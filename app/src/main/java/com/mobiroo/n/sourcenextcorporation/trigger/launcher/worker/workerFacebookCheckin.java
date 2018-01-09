package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class workerFacebookCheckin extends LocationActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_empty);
        SettingsHelper.loadPreferences(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandleUpdateWhenReady = true;
        mNumUpdatesDesired = 2;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mHandleUpdateWhenReady = false;
    }

    final String BASE_FOURSQUARE_URL = "http://touch.facebook.com/nearby/list/?radius=500&json_location=";

    @Override
    protected void updateLocation(double latitude, double longitude, float accuracy, double altitude) {
        mFinishWhenComplete = true;
       Logger.d("Posting Facebook location");
       Logger.d(Constants.TAG, "Lat = " + latitude + ", Long = " + longitude);
        if ((latitude != 0) && (longitude != 0)) {
            JSONObject data = new JSONObject();
            try {
                data.put("latitude", "" + latitude + "");
                data.put("longitude", "" + longitude + "");
                data.put("accuracy", "" + accuracy + "");

            } catch (JSONException ex) {
                Log.e("NFCT", " Excetion adding data");
            }

            Logger.d("URL is " + BASE_FOURSQUARE_URL + data.toString());
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_FOURSQUARE_URL + data.toString()));
            startActivity(browserIntent);
            Logger.d("Finishing worker Facebook " + RESULT_OK);
        }
    }
}
