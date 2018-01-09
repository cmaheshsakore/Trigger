package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.FoursquareVenue;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerFoursquareCheckin;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;

public class FoursquareCheckinVenueAction extends BaseAction {

    public static final int REQUEST_FOURSQUARE_CHECKIN = 7;
    
    private static AlertDialog mResultsDialog;
    
    private static TextView mFoursquareVenueName;
    private static TextView mFoursquareVenueId;
    private static String mResultVenueName;
    private static String mResultVenueId;
    
    private static String mSearch;
    private static String mNear;
    private String mVenueId;

    private SearchForVenue mVenueSearch;

    @Override
    public String getCommand() {
        return Constants.COMMAND_FOURSQUARE;
    }

    @Override
    public String getCode() {
        return Codes.CHECKIN_FOURSQUARE;
    }

    @Override
    public View getView(Context context, CommandArguments arguments) {
        LayoutInflater inflater = getLayoutInflater(context);
        View dialogView = inflater.inflate(R.layout.configuration_dialog_option028, null, false);

        mFoursquareVenueName = (TextView) dialogView.findViewById(R.id.foursquareVenueName);
        mFoursquareVenueId = (TextView) dialogView.findViewById(R.id.foursquareVenueID);
        
        ImageButton searchButton = (ImageButton) dialogView.findViewById(R.id.foursquareSearchButton);
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // Initialize search values for search

                // perform a list search
                if ((mVenueId == null) || (mVenueId.equals(""))) {
                    mVenueSearch = new SearchForVenue(v.getContext());
                    mVenueSearch.execute(mSearch);
                } else {
                    GetVenueTask venueTask = new GetVenueTask(v.getContext());
                    venueTask.execute(mVenueId);
                }
            }

        });
        searchButton.setSelected(true);

        EditText searchTextBox = (EditText) dialogView.findViewById(R.id.foursquareVenue);
        searchTextBox.setSelected(false);
        searchTextBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){
                mSearch = s.toString();
            }
        });


        EditText nearTextBox = (EditText) dialogView.findViewById(R.id.foursquareNear);
        nearTextBox.setSelected(false);
        nearTextBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){
                mNear = s.toString();
            }
        });

        EditText venueIdTextBox = (EditText) dialogView.findViewById(R.id.foursquareSearchVenueID);
        venueIdTextBox.setSelected(false);
        venueIdTextBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){
                mVenueId = s.toString();
            }
        });
        
        if (hasArgument(arguments, CommandArguments.OPTION_EXTRA_FLAG_ONE)) {
            String id = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_ONE);
            String name = arguments.getValue(CommandArguments.OPTION_EXTRA_FLAG_TWO);
            ((TextView) dialogView.findViewById(R.id.foursquareVenueID)).setText(id);
            ((TextView) dialogView.findViewById(R.id.foursquareVenueName)).setText((name.isEmpty()) ? id : name);
        }
        
        return dialogView;
        
    }

    @Override
    public String getName() {
         return "Foursquare Checkin";
    }

    @Override
    public String[] buildAction(View actionView, Context context) {
        TextView idText = (TextView) actionView.findViewById(R.id.foursquareVenueID);
        TextView nameText = (TextView) actionView.findViewById(R.id.foursquareVenueName);

        String venueId = idText.getText().toString();
        String name = nameText.getText().toString();
        Logger.d("Venue ID is " + venueId);
        if (!venueId.isEmpty() && (!venueId.equals(context.getString(R.string.optionsSocialFoursquareVenueEmpty)))) {
            String message = Constants.COMMAND_FOURSQUARE + ":" + venueId + ":" + name;
            return new String[] { message, context.getString(R.string.listFoursquareVenueText), name };
        } else {
            return new String[] { "" };
        }
    }


    @Override
    public int getMinArgLength() {
        return 3;
    }

    @Override
    public String getDisplayFromMessage(String command, String[] args, Context context) {
        String display = context.getString(R.string.listFoursquareVenueText);
        try {
            display += " " + args[0];
        } catch (Exception e) {
            display += " venue";
        }
        return display;
    }

    @Override
    public CommandArguments getArgumentsFromAction(String action) {
        String[] args = action.split(":");
        return new CommandArguments(
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_ONE, Utils.tryParseString(args, 1, "")),
                new BasicNameValuePair(CommandArguments.OPTION_EXTRA_FLAG_TWO, Utils.tryParseString(args, 2, ""))
                );
    }
    
    private static class GetVenueTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog pDialog;
        private Context mContext;

        public GetVenueTask(Context context) {
            this.mContext = context;
        }

        protected void onPreExecute() {
            pDialog = ProgressDialog.show(mContext, "", " " + mContext.getString(R.string.optionsSocialFoursquareSearching), true, false);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return getSingleVenueInfo(mContext, params[0]);
        }
        
        
        protected void onPostExecute(Boolean gotData) {
            if (gotData) {
                setVenueInfo(mResultVenueName, mResultVenueId);
            }
            pDialog.dismiss();
        }

    }
    
    private static boolean getSingleVenueInfo(Context context, String venue) {
        
        boolean gotData = true;
        
        try {
            FoursquareApi fsAPI = new FoursquareApi(OAuthConstants.FOURSQUARE_CLIENT_ID, OAuthConstants.FOURSQUARE_CLIENT_SECRET, "nfctl://foursquare");
            String venueIdText = venue;
            if (venueIdText != null) {
                Result<CompleteVenue> venueResult = fsAPI.venue(venueIdText);
                if (venueResult.getMeta().getCode() == 200) {
                    CompleteVenue fVenue = venueResult.getResult();
                    if (fVenue != null) {
                        mResultVenueId = fVenue.getId();
                        mResultVenueName = fVenue.getName();
                    }

                }
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception getting venue name ", e);
            gotData = false;
        }
        return gotData;
    }

    private static void setVenueInfo(String name, String Id) {
        mFoursquareVenueName.setText(name);
        mFoursquareVenueId.setText(Id);
    }

    private static class SearchForVenue extends AsyncTask<String, Void, ArrayList<FoursquareVenue>> {
        private Context mContext;
        private ProgressDialog pDialog;

        private Location _bestLoc = null;
        private Location _bestLocGPS = null;
        private LocationManager _locationManager;
        private LocationListener _locationListener;
        private LocationListener _locationListenerGPS;

        private double dLat;
        private double dLong;

        public SearchForVenue(Context context) {
            this.mContext = context;

        }

        protected void onPreExecute() {
            pDialog = ProgressDialog.show(mContext, "", " " + mContext.getString(R.string.optionsSocialFoursquareSearching), true, false);
            try {
                _locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                try {
                    _bestLoc = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Error getting coordinates from Network Provider", e);
                }

                try {
                    _bestLocGPS = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Error getting coordinates from GPS", e);
                }

                // Define a listener that responds to location updates
                _locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        if (_bestLoc == null)
                            _bestLoc = location;
                        int accDelta = (int) (location.getAccuracy() - _bestLoc.getAccuracy());
                        if (accDelta <= 0)
                            _bestLoc = location;
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };

                _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, _locationListener);
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Error setting location listener for Network", e);
                e.printStackTrace();
            }

            try {
                _locationListenerGPS = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network
                        // location provider.
                        if (_bestLocGPS == null)
                            _bestLocGPS = location;
                        int accDelta = (int) (location.getAccuracy() - _bestLocGPS.getAccuracy());
                        if (accDelta <= 0)
                            _bestLocGPS = location;
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };

                _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, _locationListenerGPS);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Error setting location listener for GPS");
                e.printStackTrace();
            }

            // Get Location info
            if (_bestLoc == null) {
                _bestLoc = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (_bestLocGPS == null) {
                _bestLocGPS = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            // Unregister listener
            _locationManager.removeUpdates(_locationListener);
            _locationManager.removeUpdates(_locationListenerGPS);

            dLat = 0;
            dLong = 0;

            try {
                Logger.d(Constants.TAG, "Using Network for location");
                dLat = _bestLoc.getLatitude();
                dLong = _bestLoc.getLongitude();
            } catch (Exception e) {
                Logger.e(Constants.TAG, "Cound not get location from network");
            }

            try {
                if (_bestLocGPS != null) {
                    Logger.d(Constants.TAG, "Using GPS for location");
                    try {
                        if ((dLat == 0) || (dLong == 0)) {
                            dLat = _bestLocGPS.getLatitude();
                            dLong = _bestLocGPS.getLongitude();
                        } else {
                            // try to test accuracy assuming both are present
                            float GPSAccuracy = _bestLocGPS.getAccuracy();
                            float NetworkAccuracy = 0;
                            if (_bestLoc != null) {
                                try {
                                    NetworkAccuracy = _bestLoc.getAccuracy();
                                } catch (Exception ignored) {}
                            }
                            if (NetworkAccuracy == 0) {
                                dLat = _bestLocGPS.getLatitude();
                                dLong = _bestLocGPS.getLongitude();
                            } else if (GPSAccuracy < NetworkAccuracy) {
                                dLat = _bestLocGPS.getLatitude();
                                dLong = _bestLocGPS.getLongitude();
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(Constants.TAG, "Cound not get location from GPS");
                    }
                }
            } catch (Exception ignored) {}
            if ((dLat == 0) && (dLong == 0)) {
                Toast.makeText(mContext, "Error getting location.", Toast.LENGTH_LONG).show();
            }
            Logger.d("NFCT", "dLat = " + dLat + ", dLong = " + dLong);
        }

        @Override
        protected ArrayList<FoursquareVenue> doInBackground(String... params) {
            return startSearch(mContext, dLat, dLong);
        }

        protected void onPostExecute(ArrayList<FoursquareVenue> venueList) {
            if ((pDialog != null)  && (pDialog.isShowing())) {
                pDialog.cancel();
            }
            if (mContext != null) {
                showSearchResultsDialog(mContext, venueList);
            }
        }

    }

    private static ArrayList<FoursquareVenue> startSearch(Context context, double dLat, double dLong) {
        ArrayList<FoursquareVenue> venueList = new ArrayList<FoursquareVenue>();
        try {
            FoursquareApi fsAPI = new FoursquareApi(OAuthConstants.FOURSQUARE_CLIENT_ID, OAuthConstants.FOURSQUARE_CLIENT_SECRET, "nfctl://foursquare");
            Result<VenuesSearchResult> result;

            String nearText = mNear;
            String searchText = mSearch;

            if ((nearText == null) || (nearText.equals(context.getString(R.string.optionsFoursquareSearchCity)))) {
                result = fsAPI.venuesSearch(dLat + "," + dLong, null, null, null, searchText, 50, null, null, null, null, null, null);
            } else {
                result = fsAPI.venuesSearch(null, null, null, null, searchText, 50, null, null, null, null, null, nearText);
            }

            if (result.getMeta().getCode() == 200) {
                venueList = new ArrayList<FoursquareVenue>();
                for (CompactVenue venue : result.getResult().getVenues()) {
                    // Populate an array with this data to pass into the dialog
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

                    venueList.add(fVenue);
                }
            } else {
                Logger.d(Constants.TAG, "Error occured: ");
                Logger.d(Constants.TAG, "  code: " + result.getMeta().getCode());
                Logger.d(Constants.TAG, "  type: " + result.getMeta().getErrorType());
                Logger.d(Constants.TAG, "  detail: " + result.getMeta().getErrorDetail());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return venueList;
    }

    private static void showSearchResultsDialog(Context context, ArrayList<FoursquareVenue> venueList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.optionsSocialFoursquareSearchTitle));

        final LayoutInflater _mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v2 = _mInflater.inflate(R.layout.dialog_foursquare_search, null);

        ListView venues = (ListView) v2.findViewById(R.id.venueList);
        if ((venueList != null) && (venueList.size() > 0)) {
            // Populate list
            venues.setAdapter(new ArrayAdapter<FoursquareVenue>(context, R.layout.list_item_venue, venueList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View row;

                    if (null == convertView) {
                        row = _mInflater.inflate(R.layout.list_item_venue, null);
                    } else {
                        row = convertView;
                    }
                    row.setBackgroundResource(R.drawable.list_selector);
                    FoursquareVenue v = getItem(position);

                    TextView tv = (TextView) row.findViewById(R.id.row1Text);
                    tv.setText(v.VenueName);

                    TextView tvSub = (TextView) row.findViewById(R.id.row1SubText);
                    tvSub.setText(v.VenueAddress);

                    TextView tvDistance = (TextView) row.findViewById(R.id.row1SubDistance);
                    tvDistance.setText("" + v.VenueDistance + " meters");

                    TextView tvVenue = (TextView) row.findViewById(R.id.row1Venue);
                    tvVenue.setText(v.VenueID);

                    return row;
                }
            });

            venues.setOnItemClickListener(new OnItemClickListener() {

                public void onItemClick(AdapterView<?> adapter, View parent, int position, long id) {
                    TextView name = (TextView) parent.findViewById(R.id.row1Text);
                    TextView venue = (TextView) parent.findViewById(R.id.row1Venue);

                    setVenueInfo((name != null) ? name.getText().toString() : "", (venue != null) ?  venue.getText().toString() : "");
                    mResultsDialog.cancel();
                }

            });

        }

        builder.setView(v2);
        builder.setNegativeButton(context.getString(R.string.dialogCancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        mResultsDialog = builder.create();
        mResultsDialog.show();

    }

    @Override
    public void performAction(Context context, int operation, String[] args, int currentIndex) {
        if (Utils.isConnectedToNetwork(context)) {
            String venue = Utils.tryParseEncodedString(args, 1, "");
            Logger.d("Check in to Foursquare " + venue);
            if (!venue.isEmpty()) {
                setAutoRestart(currentIndex + 1);
                Logger.d("Starting check in");
                Intent intent = new Intent(context, workerFoursquareCheckin.class);
                //Intent intent = new Intent(context, FoursquareCheckinService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(workerFoursquareCheckin.EXTRA_VENUE_ID, venue);
                intent = buildReturnIntent(intent);
                context.startActivity(intent);
                //context.startService(intent);
            }
        } else {
            Logger.d("Network connection not available, skipping check in");
        }
    }

    @Override
    public String getWidgetText(Context context, int operation) {
        return getBaseWidgetCheckinText(context, operation, context.getString(R.string.social_foursquare_venue));
    }

    @Override
    public String getNotificationText(Context context, int operation) {
        return getBaseActionCheckinText(context, operation, context.getString(R.string.social_foursquare_venue));
    }
    
}
