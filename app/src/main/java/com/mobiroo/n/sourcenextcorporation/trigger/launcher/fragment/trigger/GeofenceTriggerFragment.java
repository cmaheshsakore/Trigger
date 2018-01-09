package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment.trigger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;

import java.util.List;
import java.util.Locale;

public class GeofenceTriggerFragment extends BaseFragment implements LocationListener, GoogleApiClient.ConnectionCallbacks {

    protected GoogleApiClient   mLocationClient;
    protected Location          mLocation;
    protected ConnectionResult  mConnectionResult;
    protected int               mNumUpdatesReceived = 0;
    protected int               mNumUpdatesDesired = 1;
    private SupportMapFragment  mMapFragment;
    private TextView            mResultText;
    private LatLng              mLatLng;
    private EditText            mAddress;
    private String              mLastAddress;
    private LatLng              mLastLocation;
    private addressLookup       mAddressLookup;
    private boolean             mRequestAccuracyOnly;
    public static final int     mMinimumRadius = 80;
    public static final int     mRadiusMedium = 200;
    public static final int     mRadiusLarge = 500;
    public static final int     mRadiusHuge = 2000;
    private Spinner             mRadius;

    private GeoLookup           mGeoLookup;
    private boolean             mIsEditing = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configure_geofence_trigger,  container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mResultText = (TextView) getView().findViewById(R.id.results);

        mAddress = (EditText) getView().findViewById(R.id.address);
        mAddress.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mAddress.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    Logger.d("GEO-T:Launching search");
                    mGeoLookup = new GeoLookup(GeoLookup.TYPE_ADDRESS);
                    mGeoLookup.execute(v.getText().toString());
                }
                return false;
            }

        });

        (getView().findViewById(R.id.use_location)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mGeoLookup = new GeoLookup(GeoLookup.TYPE_ADDRESS);
                mGeoLookup.execute(mAddress.getText().toString());
            }

        });

        //radius selection for the geofence...
        mRadius = (Spinner) getView().findViewById(R.id.radius);
        mRadius.setSelection(1);
        mRadius.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {
                if (mLatLng != null) {
                    updateLocationDisplay(mLatLng);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                
            }
            
        });

        FragmentManager fm = getChildFragmentManager();
        fm.executePendingTransactions();

        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMapClickListener(new OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng latLong) {
                        Logger.d("GEO-T:Map Clicked on " + latLong.latitude + ", " + latLong.longitude);
                        updateLocationDisplay(latLong);
                    }
                });
            }
        });

        if (mMapFragment == null) {
            Logger.d("Map fragment is null");
        }

        if (DatabaseHelper.TRIGGER_EXIT.equals(mTrigger.getCondition())) {
            ((RadioGroup) getView().findViewById(R.id.condition)).check(R.id.radio_exit);
        }

        if (Task.isStringValid(mTrigger.getExtra(2))) {
            
            double radius = mRadiusMedium;
            Logger.d("GEO-T: Radius raw = " + mTrigger.getExtra(2));


            try {
                radius = Double.parseDouble(mTrigger.getExtra(2));
            }
            catch (Exception e) {
                Logger.e("Exception parsing radius: " + e, e);
            }

            Logger.d("GEO-T: Radius is " + radius);
            
            if (radius <= mMinimumRadius) {
                mRadius.setSelection(0);
            } else if (radius <= mRadiusMedium) {
                mRadius.setSelection(1);
            } else if (radius <= mRadiusLarge) {
                mRadius.setSelection(2);
            } else {
                mRadius.setSelection(3);
            }
        }

        if (Task.isStringValid(mTrigger.getExtra(1))) {
            mIsEditing = true;
            ((TextView) getView().findViewById(R.id.results)).setText(mTrigger.getExtra(1));
            (getActivity().findViewById(R.id.button_next)).setEnabled(true);
            String[] coordinates = mTrigger.getExtra(1).split(",");
            if (coordinates.length >= 2) {
                updateLocationDisplay(new LatLng(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
            }
        }
        
        view.findViewById(R.id.warning).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent help = new Intent(Intent.ACTION_VIEW, Uri.parse("http://answers.trigger.com/q/210-faq-geofences"));
                List<ResolveInfo> info = getActivity().getPackageManager().queryIntentActivities(help, PackageManager.MATCH_DEFAULT_ONLY);
                if ((info != null) && (info.size() > 0)) {
                    startActivity(help);
                } else {
                    // Open a new web view ?
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return String.format(getString(R.string.configure_connection_task_title), getString(R.string.geofence));
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestAccuracyOnly = true;
        mLocationClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mLocationClient.connect();
    }

    @Override 
    public void onPause() {
        super.onPause();

        if (mLocationClient != null) {
            if (mLocationClient.isConnected()) {
                mLocationClient.disconnect();
            }
        }

        if (mAddressLookup != null) {
            mAddressLookup.cancel(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGeoLookup != null) {
            mGeoLookup.cancel(true);
        }

        if (mAddressLookup != null) {
            mAddressLookup.cancel(true);
        }
    }

    private class addressLookup extends AsyncTask<Double, Void, List<Address>> {

        @Override
        public void onPreExecute() {
            if (mDialog == null) {
                mDialog = new ProgressDialog(getActivity());
                mDialog.setMessage(getActivity().getString(R.string.loading));
            }
            if (!mDialog.isShowing() && (this.isCancelled())) {
                mDialog.show();
            }
        }

        @Override
        protected List<Address> doInBackground(Double... location) {

            if ((getActivity() != null) && (!getActivity().isFinishing())) {
   
                Geocoder geo = new Geocoder(getActivity(), Locale.getDefault());
                //getting the address with geocode...
                try {
                    Logger.i("ADDRESS: Looking up " + location[0] + " , " + location[1]);
                    return geo.getFromLocation(location[0], location[1], 5);
                } catch (Exception e) {
                    Logger.e("ADDRESS: Exception looking up address: " + e, e);
                    return null;
                }
            } else {
                Logger.d("ADDRESS: Skipping lookup");
                return null;

            }
        }

        @Override
        public void onPostExecute(List<Address> results) {
            if (results != null) {
                if (results.size() > 0) {
                    Logger.d("ADDRESS: Received 1+ results");
                    Address first = results.get(0);
                    updateAddress(first);
                }
            } else {
                Logger.d("ADDRESS: Received no results");
                updateAddress(null);
            }
            mDialog.dismiss();
        }
    }

    private class GeoLookup extends AsyncTask<String, Void, List<Address>> {

        public static final int TYPE_ADDRESS = 1;
        public static final int TYPE_LAT_LNG = 2;

        private int mType;

        public GeoLookup(int type) {
            mType = type;
        }

        @Override
        public void onPreExecute() {
            if (mDialog == null) {
                mDialog = new ProgressDialog(getActivity());
                mDialog.setMessage(getActivity().getString(R.string.loading));
            }
            if (!mDialog.isShowing()) {
                mDialog.show();
            }
        }

        @Override
        protected List<Address> doInBackground(String... params) {
            Geocoder geo = new Geocoder(getActivity());
            if (!params[0].isEmpty()) {
                try {
                    switch (mType) {
                        case TYPE_ADDRESS:
                            return geo.getFromLocationName(params[0], 5);
                        case TYPE_LAT_LNG:
                            return geo.getFromLocation(Double.parseDouble(params[0]), Double.parseDouble(params[1]), 5);
                    }
                } catch (Exception e) {
                    Logger.e("Exception querying address", e);
                    return null;
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(List<Address> results) {

            if (results != null) {
                if (results.size() > 0) {
                    Address first = results.get(0);
                    updateLocationDisplay(new LatLng(first.getLatitude(), first.getLongitude()));
                }
            }
            else {
                mRequestAccuracyOnly = false;
                requestUpdates();
            }
            mDialog.dismiss();
        }

    };

    private void requestUpdates() {
        if ((mLocationClient != null) && (mLocationClient.isConnected())) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient,new LocationRequest().setInterval(5000).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).setNumUpdates(mNumUpdatesDesired), GeofenceTriggerFragment.this);
        } else {
            if (mLocationClient == null) {
                mLocationClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).build();
            }
            mLocationClient.connect();
        }
    }

    private void updateAddress(Address address) {

        String display = "";
        if (address != null) {
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                if (!display.isEmpty()) {
                    display += ",";
                }
                display += address.getAddressLine(i);
            }
        }
        mLastAddress = display;
        mAddress.setText(display);

    }

    private void updateLocationDisplay(LatLng location) {
        if (location != null) {
            Logger.d("GEO-T:Setting latlng to " + location.latitude + ", " + location.longitude);
            mLatLng = location;
        }

        if (mLatLng != null) {
            (getActivity().findViewById(R.id.button_next)).setEnabled(true);

            if ((mAddress == null)
                    || mAddress.getText().toString().isEmpty()
                    || !mAddress.getText().toString().equals(mLastAddress)
                    || (mLatLng.latitude != mLastLocation.latitude) 
                    || (mLatLng.longitude != mLastLocation.longitude)) {

                Logger.d("GEO-T: Executing address lookup");
                mAddressLookup = new addressLookup();
                mAddressLookup.execute(mLatLng.latitude, mLatLng.longitude);
            }

            mLastLocation = mLatLng;

            /* Grab the first result and get lat / long */

            if (mLatLng == null) {
                Logger.d("GEO-T:lat lng is null");
            }
            if (mResultText == null) {
                Logger.d("GEO-T:mResultText is null");
            }
            mResultText.setText(mLatLng.latitude + ", " + mLatLng.longitude);
            drawCircle(mLatLng, getRadius());
            moveToLocation(mLatLng);
        }

    }

    private double getRadius() {

        double radius = mMinimumRadius;
        switch(mRadius.getSelectedItemPosition()) {
            case 0:
                radius = mMinimumRadius;
                break;
            case 1:
                radius = mRadiusMedium;
                break;
            case 2:
                radius = mRadiusLarge;
                break;
            case 3:
                radius = mRadiusHuge;
                break;
        }
        return radius;
    }

    private void moveToLocation(LatLng coordinates) {
        float zoom = mMapFragment.getMap().getCameraPosition().zoom;
        double radius = getRadius();
        
        if (radius == mMinimumRadius) {
            zoom = 17.0f;
        } else if (radius == mRadiusMedium) {
            zoom = 15.5f; 
        } else if (radius == mRadiusLarge) {
            zoom = 14.5f;
        } else if (radius == mRadiusHuge) {
            zoom = 12.5f;
        } else {
            zoom = 17.0f;
        }
        
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coordinates, zoom);
        mMapFragment.getMap().moveCamera(update);

    }

    //drawing the perimeter circle(geofence) on the map...
    private void drawCircle(LatLng coordinates, double radius) {
        CircleOptions circle = new CircleOptions();
        circle.center(coordinates);
        circle.radius(10);
        circle.strokeColor(Color.argb(99, 0, 153, 204));
        circle.fillColor(Color.argb(99, 0, 153, 204));
        
        CircleOptions fence = new CircleOptions();
        fence.center(coordinates);
        fence.radius(getRadius());
        fence.strokeColor(Color.argb(99, 51, 51, 51));
        
        mMapFragment.getMap().clear();
        mMapFragment.getMap().addCircle(circle);
        mMapFragment.getMap().addCircle(fence);
    }

    private void updateAccuracy(double accuracy) {}

    private String getGeofenceLocation() {
        if (mLatLng != null) {
            return mLatLng.latitude + "," + mLatLng.longitude;
        } else if (mLocation != null) {
            return mLocation.getLatitude() + "," + mLocation.getLongitude();
        } else {
            return ((TextView) getView().findViewById(R.id.results)).getText().toString();
        }
    }

    @Override
    protected void updateTrigger() {
        String condition = (((RadioGroup) getView().findViewById(R.id.condition)).getCheckedRadioButtonId() == R.id.radio_exit) ? DatabaseHelper.TRIGGER_EXIT : DatabaseHelper.TRIGGER_ENTER;
        String location = getGeofenceLocation();
        String radius = String.valueOf(getRadius());

        mTrigger.setCondition(condition);
        mTrigger.setType(TaskTypeItem.TASK_TYPE_GEOFENCE);
        mTrigger.setExtra(1, location);
        mTrigger.setExtra(2, radius);
    }

    @Override
    public void onLocationChanged(Location location) {

        if (mLocation == null) {
            mLocation = location;
        } else if (location.getAccuracy() < mLocation.getAccuracy()) {
            mLocation = location;
        }

        mNumUpdatesReceived++;
        Logger.d("GEO-T:Received " + mNumUpdatesReceived);

        if (mNumUpdatesReceived >= mNumUpdatesDesired) {
            if (!mRequestAccuracyOnly) {
                updateLocationDisplay(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
            }
            mNumUpdatesReceived = 0;
            if (mLocationClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
            }
        }
        updateAccuracy(location.getAccuracy());
    }

    @Override
    public void onConnected(Bundle arg0) {

        Logger.d("GEO-T:Connected");
        if ((LocationServices.FusedLocationApi.getLastLocation(mLocationClient) != null) && (!mIsEditing)) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            updateLocationDisplay(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

}
