package com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.FoursquareVenue;

import java.util.ArrayList;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;

public class workerFoursquareVenueSearch extends LocationActivity {
    private ArrayList<FoursquareVenue> mVenueList;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private ProgressDialog mProgressDialog;

    private String mVenueId;
    
    private String mText;
    private Context mContext;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foursquare_venue_search);

        ((Toolbar) findViewById(R.id.toolbar)).setTitle(R.string.listFoursquareVenueText);
        mText = getIntent().getStringExtra("text");
        mFinishWhenComplete = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        mNumUpdatesReceived = 0;
        mNumUpdatesDesired = 1;
        mUpdateInterval = 3000;
        
        mHandleUpdateWhenReady = true;
        mContext = workerFoursquareVenueSearch.this;
        mProgressDialog = ProgressDialog.show(mContext, "", " " + getString(R.string.optionsSocialFoursquareSearching), true, false);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mHandleUpdateWhenReady = true;
        if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    
    @Override
    protected void updateLocation(double latitude, double longitude, float accuracy, double altitude) {
        Logger.d("Calling search");
        new searchForVenue().execute("");
    }


    private class searchForVenue extends AsyncTask<String, Void, Void> {

        private void startSearch() {
            try {
                FoursquareApi fsAPI = new FoursquareApi(OAuthConstants.FOURSQUARE_CLIENT_ID, OAuthConstants.FOURSQUARE_CLIENT_SECRET, "nfctl://foursquare");
                mLatitude = mLocation.getLatitude();
                mLongitude = mLocation.getLongitude();
                
                Result<VenuesSearchResult> result = fsAPI.venuesSearch(mLatitude + "," + mLongitude, null, null, null, "", 50, null, null, null, null, null, null);

                if (result.getMeta().getCode() == 200) {
                    Logger.d("FS Search: Result is 200");
                    mVenueList = new ArrayList<FoursquareVenue>();

                    for (CompactVenue venue : result.getResult().getVenues()) {
                        // Populate an array with this data to pass into the
                        // dialog
                        FoursquareVenue fVenue = new FoursquareVenue();
                        fVenue.VenueID = venue.getId();
                        fVenue.VenueName = venue.getName();
                        if ((venue.getLocation().getAddress() != null) && (venue.getLocation().getAddress().length() > 1)) {
                            fVenue.VenueAddress = venue.getLocation().getAddress();
                        } else {
                            String city = "";
                            String state = "";
                            if (venue.getLocation().getCity() != null)
                                city = venue.getLocation().getCity();
                            if (venue.getLocation().getState() != null)
                                state = venue.getLocation().getState();

                            fVenue.VenueAddress = city + " " + state;
                        }
                        fVenue.VenueDistance = venue.getLocation().getDistance();

                        mVenueList.add(fVenue);
                    }
                } else {
                    Logger.d(Constants.TAG, "Error occurred: ");
                    Logger.d(Constants.TAG, "  code: " + result.getMeta().getCode());
                    Logger.d(Constants.TAG, "  type: " + result.getMeta().getErrorType());
                    Logger.d(Constants.TAG, "  detail: " + result.getMeta().getErrorDetail());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            startSearch();
            return null;
        }

        protected void onPostExecute(final Void unused) {
            if ((mProgressDialog != null) && mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
            
            displayResults();
        }

    }

    private void displayResults() {
        final LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final ListView venues = (ListView) findViewById(R.id.venueList);
        if ((mVenueList != null) && (mVenueList.size() > 0)) {
            venues.setAdapter(new ArrayAdapter<FoursquareVenue>(this, R.layout.list_item_venue, mVenueList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View row;

                    if (null == convertView) {
                        row = mInflater.inflate(R.layout.list_item_venue, null);
                    } else {
                        row = convertView;
                    }

                    row.setBackgroundResource(R.drawable.list_selector);

                    FoursquareVenue v = (FoursquareVenue) getItem(position);

                    TextView tv = (TextView) row.findViewById(R.id.row1Text);
                    tv.setText(v.VenueName);

                    TextView tvSub = (TextView) row.findViewById(R.id.row1SubText);
                    tvSub.setText(v.VenueAddress);

                    TextView tvVenue = (TextView) row.findViewById(R.id.row1Venue);
                    tvVenue.setText(v.VenueID);

                    return row;
                }
            });

            venues.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> adapter, View parent, int position, long id) {
                    TextView venue = (TextView) parent.findViewById(R.id.row1Venue);
                    String venueID = venue.getText().toString();
                    Logger.d("Checking in to " + venueID);
                    mVenueId = venueID;
                    Logger.i("Calling foursquare checkin activity");

                    TextView text = (TextView) findViewById(R.id.checkinText);
                    text.setVisibility(View.VISIBLE);
                    text.setText(mText);
                    venues.setVisibility(View.GONE);
                    Intent intent = new Intent(workerFoursquareVenueSearch.this, workerFoursquareCheckin.class);
                    intent.putExtra(workerFoursquareCheckin.EXTRA_VENUE_ID, mVenueId);
                    startActivityForResult(intent, 1);
                }

            });

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Foursquare Checkin: Returned with " + requestCode + ", result=" + resultCode);
        switch (requestCode) {
            case 1:
                resumeProcessing(RESULT_OK);
        }

    }


}
