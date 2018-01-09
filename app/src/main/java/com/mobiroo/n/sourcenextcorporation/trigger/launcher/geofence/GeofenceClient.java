package com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.GeofenceService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import java.util.List;

public class GeofenceClient implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final int DWELL_DURATION = 60 * 1000; // 1 minute

    private Context                 mContext;
    private Intent                  mIntent;
    private PendingIntent           mPendingIntent;
    private boolean                 mConnectionInProgress;
    protected GoogleApiClient mLocationClient;
    private List<String>            mIds;
    private List<Geofence>          mPendingFences;

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    private GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener;

    private enum REQUEST {ADD, REMOVE_ALL, REMOVE_IDS };
    private REQUEST                         mRequestType;

    public GoogleApiClient getLocationClient() {
        if (mLocationClient == null) {
            setLocationClient(new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mOnConnectionFailedListener)
                    .build());
        }
        
        return mLocationClient;
    }
    
    public void setLocationClient(GoogleApiClient client) {
        mLocationClient = client;
    }
    
    public GeofenceClient(Context context) {
        mContext = context;
        setupIntents(context);
        mConnectionCallbacks = this;
        mOnConnectionFailedListener = this;
    }
    
    private void setupIntents(Context context) {
        mIntent =  new Intent(context, GeofenceService.class);
        mPendingIntent = PendingIntent.getService(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    public void connectAndSave() {
        mLocationClient = getLocationClient();
        mRequestType = REQUEST.ADD;
        if (!mConnectionInProgress) {
            if (!mLocationClient.isConnected()) {
                mLocationClient.connect();
            }
        }
    }
    
    public void connectAndRemoveAll() {
        mLocationClient = getLocationClient();
        mRequestType = REQUEST.REMOVE_ALL;
        if (!mConnectionInProgress) {
            if (!mLocationClient.isConnected()) {
                mLocationClient.connect();
            }
        }
    }
    
    public void connectAndRemoveIds(List<String> ids) {
        mLocationClient = getLocationClient();
        mRequestType = REQUEST.REMOVE_IDS;
        mIds = ids;
        if (!mConnectionInProgress) {
            if (!mLocationClient.isConnected()) {
                mLocationClient.connect();
            }
        }
    }
    
    public void disconnect() {
        mConnectionInProgress = false;
        if ((mLocationClient != null) && (mLocationClient.isConnected())) {
            mLocationClient.disconnect();
        }
    }
    public void setGeofences(List<Geofence> fences) {
        mPendingFences = fences;
    }

    ResultCallback<Status> mCallback;
    public void setResultCallback(ResultCallback<Status> results) {
        mCallback = results;
    }
    
    public void setConnectionComplete() {
        mConnectionInProgress = false;
    }
    
    public void setConnecting() {
        mConnectionInProgress = true;
    }

    @Override
    public void onConnected(Bundle hint) {
        mConnectionInProgress = false;
        
        Logger.d("GEO: Client connected for action " + mRequestType);
        PendingResult<Status> result;
        switch(mRequestType) {
            case ADD:
                if (mPendingFences != null) {
                    // Try removing all fences first
                   // mLocationClient.removeGeofences(mPendingIntent, this);
                    // Then re-add all geofences
                    result = LocationServices.GeofencingApi.addGeofences(getLocationClient(), mPendingFences, mPendingIntent); // TODO: Use Pending result to communicate changes
                    result.setResultCallback(mCallback);
                }
                break;
            case REMOVE_ALL:
                result = LocationServices.GeofencingApi.removeGeofences(getLocationClient(), mPendingIntent); // TODO: Use Pending result to communicate changes
                result.setResultCallback(mCallback);
                break;
            case REMOVE_IDS:
                result = LocationServices.GeofencingApi.removeGeofences(getLocationClient(), mIds); // TODO: Use Pending result to communicate changes
                result.setResultCallback(mCallback);
                break;
        }
        
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.d("GEO: Connection to play services failed " + connectionResult.getErrorCode());
        mConnectionInProgress = false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }
    
    public static void cleanUpGeofences(Context context) {
        context.getContentResolver().delete(Uri.withAppendedPath(TaskProvider.Contract.GEOFENCE, "clean"), null, null);
    }
}
