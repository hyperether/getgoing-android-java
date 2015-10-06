package com.hyperether.getgoing.location;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;
import com.hyperether.getgoing.util.CaloriesCalculation;
import com.hyperether.getgoing.util.Conversion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ShowLocation extends Activity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    // Global constants
    /*
     * Define a request code to send to Google Play services
	 * This code is returned in Activity.onActivityResult
	 */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    // Preference file
    public static final String PREF_FILE = "CBUserDataPref.txt";

    private static final int NODE_ADD_DISTANCE = 10;

    private GoogleMap mMap;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    private final ConnectionResult connectionResult = new ConnectionResult(0, null);

    private GoogleApiClient mGoogleApiClient;
    //private LocationClient mLocationClient;
    private boolean connectionEstablished = false;

    private boolean mUpdatesRequested;
    private boolean mProgramRunning = false;

    // Global variable to hold the current location
    private Location mCurrentLocation;

    private SharedPreferences mPrefs;
    private Editor mEditor;

    private CBDataFrame cbDataFrameLocal;    // to store the current settings

    // U/I variables
    private Button button_start, button_pause, button_rst, button_save;
    private Chronometer showTime, showCalories, showDistance;
    private Chronometer showVelocity, showVelocityAvg;

    // Time variables
    private String timeString;    // current duration of a walk
    private long timeCumulative = 0;
    private int secondsCumulative = 0;
    private long time = 0; // time between to position updates
    private Timer timer;
    private boolean timerStarted = false; // protection for stopping timer

    private double latitude, longitude, latitude_old, longitude_old;
    private boolean firstPass = true;
    private boolean actualPositionValid = false;

    private double kcalCumulative = 0;
    private double kcalCurrent;
    private double distanceCumulative = 0;
    private double distanceDelta = 0;
    private double velocity = 0;
    private double velocityAvg = 0;

    private KalmanLatLong kalman; // Kalman filter for GPS data smoothing
    private boolean isKalmanStateSet = false;

    private CaloriesCalculation calcCal;

    private double weight = 0;

    // Route storage variables
    private List<DbNode> mRoute;
    private long nodeIndex = 0;    // index of the node withing the route
    private GetGoingDataSource datasource;
    private SimpleDateFormat sdf;
    private String currentDateandTime;
    private boolean timeFlg = true;
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    LocationManagerHandler locManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Keep screen on all the time
        setContentView(R.layout.show_location);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        // Start with updates turned off
        mUpdatesRequested = false;
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();

		/*
         * Create a new location client, using the enclosing class to
		 * handle callbacks.
		 */
        //mLocationClient = new LocationClient(this, this, this);

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        cbDataFrameLocal = new CBDataFrame();
        Bundle b = getIntent().getExtras();
        cbDataFrameLocal = b.getParcelable("searchKey");

        kalman = new KalmanLatLong(3); // Initialise Kalman filter
        calcCal = new CaloriesCalculation();

        SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        weight = settings.getInt("weight", 0);

        initLayoutDinamically();

        mRoute = new ArrayList<DbNode>();

        datasource = new GetGoingDataSource(this);
        datasource.open();
        sdf = new SimpleDateFormat("dd.MM.yyyy.' 'HH:mm:ss", Locale.ENGLISH);

        locManager = new LocationManagerHandler(this);

        clearData();
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        //mLocationClient.connect();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // If the client is connected
        if (mGoogleApiClient.isConnected()) {
            stopUpdates();
        }
        /*
         * After disconnect() is called, the client is
		 * considered "dead".
		 */
        //mLocationClient.disconnect();
        mGoogleApiClient.disconnect();
        datasource.close();
        super.onStop();
    }

    @Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        datasource.close();
        super.onPause();
    }

    @Override
    protected void onResume() {
        /*
         * Get any previous setting for location updates
		 * Gets "false" if an error occurs
		 */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
            // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }

        datasource.open();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        savedInstanceState.putBoolean("mProgramRunning", mProgramRunning);
        savedInstanceState.putString("time", timeString);
        savedInstanceState.putDouble("distanceCumulative", distanceCumulative);
        savedInstanceState.putDouble("latitude", latitude);
        savedInstanceState.putDouble("longitude", longitude);
        savedInstanceState.putDouble("kcalCumulative", kcalCumulative);
        savedInstanceState.putLong("timeCumulative", timeCumulative);
        savedInstanceState.putString("currentDateandTime", currentDateandTime);
        savedInstanceState
                .putParcelableArrayList("mRoute", (ArrayList<? extends Parcelable>) mRoute);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        mProgramRunning = savedInstanceState.getBoolean("mProgramRunning");
        timeString = savedInstanceState.getString("time");
        distanceCumulative = savedInstanceState.getDouble("distanceCumulative");
        latitude = savedInstanceState.getDouble("latitude");
        longitude = savedInstanceState.getDouble("longitude");
        kcalCumulative = savedInstanceState.getDouble("kcalCumulative");
        timeCumulative = savedInstanceState.getLong("timeCumulative");
        currentDateandTime = savedInstanceState.getString("currentDateandTime");
        mRoute = savedInstanceState.getParcelableArrayList("mRoute");
    }

    private final OnClickListener mButtonStartListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startTracking();
            if (timeFlg) {
                currentDateandTime =
                        sdf.format(new Date()); // Get date and time on which the tracking started
                timeFlg = false;
            }
        }
    };

    private final OnClickListener mButtonPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            stopTracking();
        }
    };

    private final OnClickListener mButtonSaveListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mRoute.isEmpty())
                dbStore(mRoute); // Save the current route in DB
        }
    };

    private final OnClickListener mButtonResetListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            stopTracking();
            if (!mRoute.isEmpty())
                mRoute.clear(); // Delete the route list
            timeFlg = true; // ready for the new round
            clearData();
        }
    };

    private void clearData() {
        distanceCumulative = 0;
        kcalCumulative = 0;
        timeCumulative = 0;
        velocity = 0;
        velocityAvg = 0;
        mRoute.clear();
        nodeIndex = 0;
        firstPass = true;

        showData("00 : 00 : 00", 0, 0, 0, 0);
    }

    private void startTracking() {
        timer = new Timer();
        timer.schedule(new RefreshData(), 0, 1000);
        timerStarted = true;

        button_start.setVisibility(View.GONE);
        button_pause.setVisibility(View.VISIBLE);

        startUpdates();

        mProgramRunning = true;
    }

    private void stopTracking() {
        if (timerStarted) {
            timer.cancel();
            timer.purge();
            timerStarted = false;
        }

        button_start.setVisibility(View.VISIBLE);
        button_pause.setVisibility(View.GONE);

        stopUpdates();
        mProgramRunning = false;

		/*
         * TODO: ovo treba jos testirati. Razlog: ako se pauzira merenje i
		 * ode daleko pa ponovo ukljuci, nastace greska.
		 */
        /*
         * ovo bi trebalo da ga natera da ponovo inicijalizuje lat_old i da ignorise racunanje
		 * posle prvog update-a. treba probati.
		 */
        nodeIndex = 0;
        actualPositionValid = false;
        firstPass = true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    class RefreshData extends TimerTask {

        @Override
        public void run() {
            ShowLocation.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    timeCumulative += 1000;
                    secondsCumulative = (int) (timeCumulative / 1000);

                    timeString = Conversion.getDurationString(secondsCumulative);

                    time++;

                    if (actualPositionValid) {
                        actualPositionValid = false; // reset the flag
                        double dLat = latitude - latitude_old;
                        double dLon = longitude - longitude_old;

                        if ((dLat != 0) || (dLon != 0)) {
                            // Carry out the path filtering
                            if (!isKalmanStateSet) {
                                kalman.SetState(latitude,
                                        longitude,
                                        mCurrentLocation.getAccuracy(),
                                        timeCumulative);
                                isKalmanStateSet = true;
                            }

                            kalman.Process(latitude,
                                    longitude,
                                    mCurrentLocation.getAccuracy(),
                                    timeCumulative);
                            latitude = kalman.get_lat();
                            longitude = kalman.get_lng();

                            double distance =
                                    gps2m(latitude, longitude, latitude_old, longitude_old);
                            if (!Double.isNaN(distance)) {
                                distanceCumulative += distance;
                                distanceDelta += distance;

                                velocityAvg = distanceCumulative / secondsCumulative;

                                //brzina je srednja vrednost izmerene i ocitane brzine
                                velocity = (mCurrentLocation.getSpeed() + (distance / time)) / 2;

                                kcalCurrent =
                                        calcCal.calculate(distance, velocity, cbDataFrameLocal,
                                                weight);
                                kcalCumulative += kcalCurrent;

                                if (distanceDelta > NODE_ADD_DISTANCE) {
                                    distanceDelta = 0;
                                    // add new point to the route
                                    // node and route database _ids are intentionally 0
                                    DbNode tmp =
                                            new DbNode(0, latitude, longitude, (float) velocity,
                                                    nodeIndex++, 0);
                                    mRoute.add(tmp); // add the initial location to the route
                                }
                            } else {
                                velocity = mCurrentLocation.getSpeed();
                            }

                            drawRoute(mRoute);
                        }
                        time = 0; // reset the second counter for calculating velocity
                    } else {
                        // is connection broken?
                        if (!connectionEstablished) {
                            //mLocationClient.connect();
                            if (servicesConnected()) {
                                mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
                                if (mUpdatesRequested)
                                    startUpdates();
                            }
                        }
                    }

                    showData(timeString, distanceCumulative, kcalCumulative, velocity, velocityAvg);
                }
            });
        }
    }

    private void showData(String timeString, double distance, double kcal, double vel,
                          double velAvg) {
        showTime.setText(timeString);
        showCalories.setText(String.format("%.02f kcal", kcal));
        if (cbDataFrameLocal.getMeasurementSystemId() == 1 ||
                cbDataFrameLocal.getMeasurementSystemId() == 2)
            showDistance
                    .setText(String.format("%.02f ft", distance * 3.281)); // present data in feet
        else
            showDistance.setText(String.format("%.02f m", distance));

        showVelocity.setText(String.format("%.02f m/s", vel));
        showVelocityAvg.setText(String.format("%.02f m/s", velAvg));
    }

    /*
     * This only works if the points are close enough that you can omit that
     * earth is not regular shape
     */
    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = 180 / 3.14169;

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    /**
     * Show a dialog returned by Google Play services for the connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), "Error!");
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }

        @Override
        public void dismiss() {
            super.dismiss();
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
            /*
             * If the result code is Activity.RESULT_OK, try
			 * to connect again
			 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                /*
                 * Try the request again
				 */
                        break;
                }
        }
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(), "Location Updates");
            }
            return false;
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.show_map_page)).getMap();

        // If Google Play Services is available
        if (servicesConnected()) {
            connectionEstablished = true;

            // Get the current location
            Location currentLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (currentLocation != null)
                showLocation(currentLocation.getLatitude(), currentLocation.getLongitude());

            if (mProgramRunning)
                startTracking();
            else {
                mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
                if (mUpdatesRequested)
                    startUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        connectionEstablished = false;
    }

//    @Override
//    public void onDisconnected() {
//        // Display the connection status
//        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
//        connectionEstablished = false;
//    }

    @Override
    public void onLocationChanged(Location currentLocation) {

        double dLat, dLong;

        mCurrentLocation = currentLocation;
        /* alternativni pristup
        if (connectionEstablished) {
			mCurrentLocation = mLocationClient.getLastLocation();
			if(mCurrentLocation != null)
			{
		 */
        if (currentLocation != null) {
            dLat = currentLocation.getLatitude();
            dLong = currentLocation.getLongitude();

            if (firstPass) {
                latitude = latitude_old = dLat;
                longitude = longitude_old = dLong;
                firstPass = false;

                DbNode tmp = new DbNode(0, latitude, longitude, 0, nodeIndex++, 0);
                mRoute.add(tmp); // add the initial location to the route
            } else {
                latitude_old = latitude;
                longitude_old = longitude;
                latitude = dLat;
                longitude = dLong;
            }

            actualPositionValid = true; // put up a flag for the algorithm
            showLocation(dLat, dLong);
        }
    }

    /*
     * This function shows actual coordinates on map
     */
    public void showLocation(double dLat, double dLong) {
        mMap.clear();
        MarkerOptions mOpt = new MarkerOptions()
                .position(new LatLng(dLat, dLong))
                .icon(BitmapDescriptorFactory
                        .fromResource(
                                R.drawable.marker))        // Here is the map marker image set up
                .snippet("Your current location").title("You are here");
        mMap.addMarker(mOpt);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                dLat, dLong), 14));

        Toast.makeText(
                getApplicationContext(),
                "Your Location is - \nLat: " + dLat + "\nLong: "
                        + dLong, Toast.LENGTH_LONG).show();
    }

    /**
     * Sends a request to start location updates
     */
    public void startUpdates() {
        mUpdatesRequested = true;
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();

        if (connectionEstablished) {
//            mLocationClient.requestLocationUpdates(mLocationRequest, this);
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Sends a request to remove location updates
     */
    public void stopUpdates() {
        mUpdatesRequested = false;
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();

        if (connectionEstablished) {
//            mLocationClient.removeLocationUpdates(this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    // Drawing the route
    private void drawRoute(List<DbNode> mRoute) {
        boolean drFirstPass = true;
        DbNode firstNode = null;
        DbNode secondNode = null;

        // Redraw the whole route
        Iterator<DbNode> it = mRoute.iterator();
        while (it.hasNext()) {
            if (drFirstPass) {
                firstNode = secondNode = it.next();
                drFirstPass = false;
            } else {
                firstNode = secondNode;
                secondNode = it.next();
            }
            drawSegment(firstNode, secondNode);
        }
    }

    // Drawing a segment of the route and coloring it in accordance with the speed
    private void drawSegment(DbNode firstNode, DbNode secondNode) {

        // Different speed spans are represented with different colors: green, yellow, orange, red
        if (secondNode.getVelocity() <= 1) {
            // Drawing the route.
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(0, 255, 0)));  // Green color
        } else if ((secondNode.getVelocity() > 1) && (secondNode.getVelocity() <= 2)) {
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(255, 255, 0))); // Yellow color
        } else if ((secondNode.getVelocity() > 2) && (secondNode.getVelocity() <= 3)) {
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(255, 128, 0))); // Orange color
        } else {
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(255, 0, 0))); // Red color
        }
    }

    /*
     * Method for storing tracking data into DB
     * */
    private void dbStore(List<DbNode> nodeList) {
        DbRoute route = null;
        DbNode currentNode = null;

		/*
		 * Store the general route data in the DB
		 * */
        route = datasource
                .createRoute(timeCumulative, kcalCumulative, distanceCumulative, currentDateandTime,
                        velocityAvg, cbDataFrameLocal.getProfileId());

		/*
		 * Debugging only!!!
		 * 
		 * */
		/*
		datasource.createNode(50.78007792, 6.15212939, (float) 1.5, 0, route.getId());
		datasource.createNode(50.78009774, 6.15212161, (float) 2.5, 1, route.getId());
		datasource.createNode(50.78011194, 6.15201426, (float) 3.5, 2, route.getId());
		 */

		/*
		 * Store the every node in the DB
		 * */
        Iterator<DbNode> it = nodeList.iterator();
        while (it.hasNext()) {
            currentNode = it.next();
            datasource.createNode(currentNode.getLatitude(), currentNode.getLongitude(),
                    currentNode.getVelocity(), currentNode.getIndex(), route.getId());
        }
    }

    private void initLayoutDinamically() {
        button_start = (Button) findViewById(R.id.start_button);
        button_start.setOnClickListener(mButtonStartListener);
        button_pause = (Button) findViewById(R.id.end_button);
        button_pause.setOnClickListener(mButtonPauseListener);
        button_rst = (Button) findViewById(R.id.refresh_button);
        button_rst.setOnClickListener(mButtonResetListener);
        button_save = (Button) findViewById(R.id.save_button);
        button_save.setOnClickListener(mButtonSaveListener);

        showTime = (Chronometer) findViewById(R.id.showTime);
        showCalories = (Chronometer) findViewById(R.id.showCalories);
        showDistance = (Chronometer) findViewById(R.id.showDistance);
        showVelocity = (Chronometer) findViewById(R.id.showVelocity);
        showVelocityAvg = (Chronometer) findViewById(R.id.showVelocityAvg);

//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        if (metrics.widthPixels < metrics.heightPixels) {
//            button_start.setWidth(metrics.widthPixels / 3);
//            button_pause.setWidth(metrics.widthPixels / 3);
//            button_rst.setWidth(metrics.widthPixels / 3);
//            button_save.setWidth(metrics.widthPixels / 3);
//        } else {
//            button_start.setHeight(metrics.heightPixels / 3);
//            button_pause.setHeight(metrics.heightPixels / 3);
//            button_rst.setHeight(metrics.heightPixels / 3);
//            button_save.setHeight(metrics.heightPixels / 3);
//        }
    }
}
