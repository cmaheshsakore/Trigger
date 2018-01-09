package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends BaseWorkerActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks {
    protected GoogleApiClient mLocationClient;
    protected Location              mLocation;
    protected ConnectionResult      mConnectionResult;
    protected int                   mNumUpdatesReceived = 0;
    protected int                   mNumUpdatesDesired = 3;
    protected boolean               mHandleUpdateWhenReady = false;
    protected boolean               mFinishWhenComplete = true;
    protected int                   mUpdateInterval = 5000;
    private boolean                 mRequestUpdatesWhenConnected = true;
    private boolean                 mIsPlayServicesAvailable;
    private boolean                 mHaveUpdatedLocation = false;

    /* Included for devices that do not have Play Services available */
    private LocationManager         mLocationManager;
    private LegacyLocationListener  mListenerNetwork;
    private LegacyLocationListener  mListenerGps;
    private final long              mLegacyWaitTime = 3000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsPlayServicesAvailable = 
                    (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) 
                    ? true 
                    : false;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        connect();
    }

    protected void connect() {
        Logger.d("LOCATION: Connecting to client");
        if (mIsPlayServicesAvailable) {
            connectClient();
        } else {
            setupLegacyLocation();
        }
    }

    protected void disconnect() {
        Logger.d("LOCATION: Disconnecting from client");
        if (mIsPlayServicesAvailable) {
            disconnectClient();
        } else {
            disconnectLegacyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsPlayServicesAvailable) {
            if (mLocationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            }
        } else {
            removeLegacyUpdates();
        }
    }

    protected void removeLegacyUpdates() {
        try {
            mLocationManager.removeUpdates(mListenerNetwork);
            mLocationManager.removeUpdates(mListenerGps);
        } catch (Exception e) {
            /* Ignore any exception */
        }
    }
    
    protected void setupLegacyLocation() {
        Logger.d("Play services is not available, using legacy location providers");
        
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        
        mListenerNetwork = new LegacyLocationListener(LocationManager.NETWORK_PROVIDER);
        mListenerGps = new LegacyLocationListener(LocationManager.GPS_PROVIDER);
        
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mListenerNetwork);
        } catch (Exception e) {
            Logger.e("Location: Could not set up Network location provider");
        }
        
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mListenerGps);
        }  catch (Exception e) {
            Logger.e("Location: Could not set up Gps location provider");
        }
        
        /* Setup a timer to fire after polling */
        final Handler handler = new Handler();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (mHandleUpdateWhenReady) {
                            new UpdateLocationTask().execute(getLegacyLocation());
                            removeLegacyUpdates();
                        }
                    }
                });
            }
        }, mLegacyWaitTime);
    }
    
    protected Location getLegacyLocation() {
        Location best = null;
        Location network = mListenerNetwork.getLocation(mLocationManager);
        if (network != null) {
            /* Default to network */
            best = network;
        }

        Location gps = mListenerGps.getLocation(mLocationManager);
        if (gps != null) {
            if (best == null) {
                best = gps;
            } else {
                if (gps.getAccuracy() < best.getAccuracy()) {
                    best = gps;
                }
            }
        }
        
        mLocation = best;
        return best;
    }
    
    protected void connectClient() {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mLocationClient.connect();
    }

    protected void disconnectClient() {
        if (mLocationClient.isConnected()) {
            mLocationClient.disconnect();
        }
    }

    protected void disconnectLegacyLocation() {
        removeLegacyUpdates();
    }

    protected Location getLastLocation() {
        return (mIsPlayServicesAvailable) ? LocationServices.FusedLocationApi.getLastLocation(mLocationClient) : getLegacyLocation();
    }

    protected void locationUpdated() {
        if (mHandleUpdateWhenReady) {
            mNumUpdatesReceived++;
            Logger.d("Location: " + mNumUpdatesReceived + " updates, " + 
                    mLocation.getLatitude() + ", " + mLocation.getLongitude() + 
                    ", " + mLocation.getAccuracy());

            if (mNumUpdatesReceived >= mNumUpdatesDesired) {
                if (!mHaveUpdatedLocation) {
                    mHaveUpdatedLocation = true;
                    new UpdateLocationTask().execute(mLocation);
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            }
        }
    }

    private class UpdateLocationTask extends AsyncTask<Location, Void, Void> {

        @Override
        protected Void doInBackground(Location... locations) {
            Location location = locations[0];
            if (location != null) {
                updateLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getAltitude());
            }
            return null;
        }

        @Override
        public void onPostExecute(Void unused) {
            if (mFinishWhenComplete) {
                resumeProcessing(RESULT_OK);
            }
        }
    }

    @Override
    protected void resumeProcessing(int result) {
        try { LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);}
        catch (Exception e) { Logger.d("Error removing location updates: " + e); }
        super.resumeProcessing(result);
    }

    protected void updateLocation(double latitude, double longitude, float accuracy, double altitude) throws UnsupportedOperationException {
        Logger.i("ERROR: APP MUST IMPLEMENT updateLocation");
        throw new UnsupportedOperationException();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLocation == null) {
            mLocation = location;
        } else if (location.getAccuracy() < mLocation.getAccuracy()) {
            mLocation = location;
        }

        locationUpdated();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Logger.d("Connected");
        if (mLocationClient.isConnected()) {
            if (mRequestUpdatesWhenConnected) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient,
                        new LocationRequest()
                        .setInterval(mUpdateInterval)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(mNumUpdatesDesired),
                        this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    
    private class LegacyLocationListener implements android.location.LocationListener {

        private String mProvider;
        private Location mLocation;

        
        public LegacyLocationListener(String provider) {
            mProvider = provider;
        }
        
        public Location getLocation(LocationManager manager) {
            if (mLocation == null) {
                try {
                    mLocation = manager.getLastKnownLocation(mProvider);
                } catch (Exception e) {
                    Logger.e("Exception pulling last known location from " + mProvider);
                }
            }
            
            return mLocation;
        }
        
        @SuppressWarnings("unused")
        public Location getLocation() {
            return mLocation;
        }
        
        @SuppressWarnings("unused")
        public String getProvider() {
            return mProvider;
        }
        
        @Override
        public void onLocationChanged(Location location) {
            if (mLocation == null) {
                mLocation = location;
            } else {
                if (location.getAccuracy() < mLocation.getAccuracy()) {
                    mLocation = location;
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            
        }

        @Override
        public void onProviderEnabled(String provider) {
            
        }

        @Override
        public void onProviderDisabled(String provider) {
            
        }
        
    };

}
