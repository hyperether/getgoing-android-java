package com.hyperether.getgoing.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.db.AppDatabase;
import com.hyperether.getgoing.db.DbHelper;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.service.GPSTrackingService;
import com.hyperether.getgoing.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

public class ShowLocationActivity extends Activity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        OnMapReadyCallback {

    private GoogleMap mMap;

    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;

    private final ConnectionResult connectionResult = new ConnectionResult(0, null);

    private GoogleApiClient mGoogleApiClient;
    //private LocationClient mLocationClient;
    private boolean connectionEstablished = false;

    private boolean mUpdatesRequested;
    private boolean mProgramRunning = false;
    private boolean mRouteAlreadySaved = false;

    private SharedPreferences mPrefs;
    private Editor mEditor;
    // to store the current settings
    private CBDataFrame cbDataFrameLocal;

    // U/I variables
    private Button button_start, button_pause, button_rst, button_save;
    private Chronometer showTime, showCalories, showDistance;
    private Chronometer showVelocity, showVelocityAvg;

    // timer for data show
    private Timer timer;
    long timeWhenStopped = 0;

    // Route storage variables
    private GetGoingDataSource datasource;
    private SimpleDateFormat sdf;
    private String currentDateandTime;
    private boolean timeFlg = true;
    private boolean mResolvingError = false;

    private GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

    private boolean notificationExist = false;
    private boolean backPressed = true;
    private static final int notificationID = 0;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Keep screen on all the time
        setContentView(R.layout.show_location);

        mRouteAlreadySaved = true;

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

        // Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        // Start with updates turned off
        mUpdatesRequested = false;
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.apply();

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

        initLayoutDinamically();

        datasource = new GetGoingDataSource(this);
        datasource.open();
        sdf = new SimpleDateFormat("dd.MM.yyyy.' 'HH:mm:ss", Locale.ENGLISH);

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

        if (mProgramRunning && backPressed) {
            backgroundTrackNotification(notificationID);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        /*
         * Get any previous setting for location updates
		 * Gets "false" if an error occurs
		 */
        if (mProgramRunning) {
            mMap.clear();
            drawRoute(CacheManager.getInstance().getmRoute());
        }

        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
            // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }

        deleteNotification(notificationID);
        backPressed = true;

        datasource.open();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCacheData();
        stopService(new Intent(this, GPSTrackingService.class));
        deleteNotification(notificationID);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        savedInstanceState.putBoolean("mProgramRunning", mProgramRunning);
        savedInstanceState.putString("currentDateandTime", currentDateandTime);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        mProgramRunning = savedInstanceState.getBoolean("mProgramRunning");
        currentDateandTime = savedInstanceState.getString("currentDateandTime");
    }

    /**
     * This method handle button click on Start button.
     */
    private final OnClickListener mButtonStartListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startTracking();
            if (timeFlg) {
                // Get date and time on which the tracking started
                currentDateandTime = sdf.format(new Date());
                timeFlg = false;
            }
        }
    };

    /**
     * This method handle button click on pause button.
     */
    private final OnClickListener mButtonPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            stopTracking();
        }
    };

    /**
     * This method handle button click on save button.
     */
    private final OnClickListener mButtonSaveListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setCancelable(false);
            dialog.setTitle(R.string.alert_dialog_title_save_btn);
            dialog.setMessage(getString(R.string.alert_dialog_message_save_btn));
            dialog.setPositiveButton(R.string.alert_dialog_positive_button_save_btn, new
                    DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            // Save the current route in DB*/
                            if (!CacheManager.getInstance().getmRoute().isEmpty()) {
                                // Save the current route in DB*/
                                roomStore(CacheManager.getInstance().getmRoute());
                            } else {
                                CacheManager.getInstance().setKcalCumulative(0.0);
                                CacheManager.getInstance().setDistanceCumulative(0.0);
                                CacheManager.getInstance().setVelocity(0.0);
                                CacheManager.getInstance().setVelocityAvg(0.0);

                                List<DbNode> tmpRoute = new ArrayList<>();
                                DbNode tmpNode = new DbNode(0, 0, 0, 0, 0, 0);
                                tmpRoute.add(tmpNode);
                                roomStore(tmpRoute);
                            }

                            mRouteAlreadySaved = true;
                        }
                    });

            dialog.setNegativeButton(getString(R.string.alert_dialog_negative_button_save_btn),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                        }
                    });

            dialog.show();
        }
    };

    /**
     * This method handle button click on reset button.
     */
    private final OnClickListener mButtonResetListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setCancelable(false);
            dialog.setTitle(R.string.alert_dialog_title_reset_btn);
            dialog.setMessage(getString(R.string.alert_dialog_message_reset_btn));
            dialog.setPositiveButton(R.string.alert_dialog_positive_reset_save_btn, new
                    DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            if (CacheManager.getInstance().getmRoute() != null) {
                                clearCacheData();
                            }

                            if (mMap != null) mMap.clear();

                            showTime.setBase(SystemClock.elapsedRealtime());
                            timeWhenStopped = 0;
                            stopTracking();

                            timeFlg = true; // ready for the new round
                            clearData();
                        }
                    });

            dialog.setNegativeButton(getString(R.string.alert_dialog_negative_reset_save_btn),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                        }
                    });

            dialog.show();
        }
    };

    private void clearCacheData() {
        CacheManager.getInstance().setTimeCumulative(0);
        CacheManager.getInstance().clearmRoute();
        CacheManager.getInstance().setDistanceCumulative(0.0);
        CacheManager.getInstance().setVelocity(0.0);
        CacheManager.getInstance().setVelocityAvg(0.0);
        CacheManager.getInstance().setKcalCumulative(0.0);
    }

    /**
     * Via this method recorded data is clear..
     */
    private void clearData() {
        showData(0, 0, 0, 0);
    }

    /**
     * This method starts timer and enable visibility of pause button.
     */
    private void startTracking() {
        startService(new Intent(this, GPSTrackingService.class));

        showTime.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        showTime.start();

        timer = new Timer();
        timer.schedule(new RefreshData(), 0, 1000);

        button_start.setVisibility(View.GONE);
        button_pause.setVisibility(View.VISIBLE);

        startUpdates();

        mProgramRunning = true;
        mRouteAlreadySaved = false;
    }

    /**
     * This method starts timer and enable visibility of start button.
     */
    private void stopTracking() {
        stopService(new Intent(this, GPSTrackingService.class));

        timeWhenStopped = showTime.getBase() - SystemClock.elapsedRealtime();
        showTime.stop();

        button_start.setVisibility(View.VISIBLE);
        button_pause.setVisibility(View.GONE);

        stopUpdates();
        mProgramRunning = false;
        mRouteAlreadySaved = false;

		/*
         * TODO: ovo treba jos testirati. Razlog: ako se pauzira merenje i
		 * ode daleko pa ponovo ukljuci, nastace greska.
		 */
        /*
         * ovo bi trebalo da ga natera da ponovo inicijalizuje lat_old i da ignorise racunanje
		 * posle prvog update-a. treba probati.
		 */
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, Constants.REQUEST_RESOLVE_ERROR);
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
            ShowLocationActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    CacheManager.getInstance().setTimeCumulative(timeWhenStopped);
                    mMap.clear();
                    drawRoute(CacheManager.getInstance().getmRoute());

                    if (CacheManager.getInstance().getVelocity() != null) {
                        showData(CacheManager.getInstance().getDistanceCumulative(),
                                CacheManager.getInstance().getKcalCumulative(), CacheManager
                                        .getInstance().getVelocity(), CacheManager.getInstance()
                                        .getVelocityAvg());
                    }
                }
            });
        }
    }

    /**
     * This method show measured data.
     *
     * @param distance passed distance
     * @param kcal calories burned
     * @param vel average velocity
     */
    private void showData(double distance, double kcal, double vel,
                          double velAvg) {
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

    /**
     * Show a dialog returned by Google Play services for the connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        googleApiAvailability.getErrorDialog(this, errorCode,
                Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
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
            case Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST:
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
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
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
            googleApiAvailability.getErrorDialog(this, errorCode,
                    Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.show_map_page);
        mapFragment.getMapAsync(this);

        // If Google Play Services is available
        if (servicesConnected()) {
            connectionEstablished = true;

            if (mProgramRunning)
                Toast.makeText(this, "Welcome back", Toast.LENGTH_SHORT).show();
            else {
                mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
                if (mUpdatesRequested)
                    startUpdates();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.mMap = googleMap;
            initMapComponents(googleMap);

            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);

            boolean gpsEnabled;

            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!gpsEnabled) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setCancelable(false);
                dialog.setTitle(R.string.alert_dialog_title);
                dialog.setMessage(getString(R.string.alert_dialog_message));
                dialog.setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        openGPSSettings();
                    }
                });

                dialog.setNegativeButton(R.string.alert_dialog_negative_button, new
                        DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int
                                    paramInt) {
                                // TODO Auto-generated method stub
                                finish();
                            }
                        });

                dialog.show();
            }

            Criteria criteria = new Criteria();

            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String bestProvider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(bestProvider);

            zoomOverCurrentLocation(mMap, location);

            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    zoomOverCurrentLocation(mMap, location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10,
                    locationListener);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        backPressed = false;
        if (mProgramRunning || !mRouteAlreadySaved) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setTitle(R.string.alert_dialog_title_back_pressed);
            dialog.setMessage(getString(R.string.alert_dialog_message_back_pressed));
            dialog.setPositiveButton(R.string.alert_dialog_positive_back_pressed, new
                    DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            stopService(new Intent(GetGoingApp.getInstance()
                                    .getApplicationContext(),
                                    GPSTrackingService.class));
                            clearCacheData();
                            finish();
                        }
                    });

            dialog.setNegativeButton(getString(R.string.alert_dialog_negative_back_pressed),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        }
                    });

            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * This method is used for init of map components
     *
     * @param googleMap google map v2
     **/
    private void initMapComponents(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            googleMap.setMyLocationEnabled(true);
            googleMap.setTrafficEnabled(true);
            googleMap.setIndoorEnabled(true);
            googleMap.setBuildingsEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    /**
     * This method is used for zooming over user current location or last known location.
     *
     * @param googleMap google map v2
     **/
    private void zoomOverCurrentLocation(GoogleMap googleMap, Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            googleMap.moveCamera(center);
            googleMap.animateCamera(zoom);
        }
    }

    /**
     * This method get lat and lng form Location object
     *
     * @param loc Location location
     */
    private LatLng locationToLatLng(Location loc) {
        if (loc != null)
            return new LatLng(loc.getLatitude(), loc.getLongitude());
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        connectionEstablished = false;
    }

    @Override
    public void onLocationChanged(Location currentLocation) {

    }

    /**
     * Sends a request to start location updates
     */
    public void startUpdates() {
        mUpdatesRequested = true;
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();

        if (connectionEstablished) {
            // mLocationClient.requestLocationUpdates(mLocationRequest, this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                            .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi
                        .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
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
            // mLocationClient.removeLocationUpdates(this);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                            .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            }
        }
    }

    /**
     * This method draws a route.
     *
     * @param mRoute list of nodes
     */
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

    /**
     * This method draws a segment of the route and coloring it in accordance with the speed
     *
     * @param firstNode first point of the rout
     * @param secondNode second point of the rout
     */
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

    /**
     * Method for storing tracking data into DB.
     *
     * @param nodeList tracked rout
     */
    private void dbStore(List<DbNode> nodeList) {
        DbNode currentNode = null;

		/*
         * Store the general route data in the DB
		 * */
        DbRoute route = datasource
                .createRoute(timeWhenStopped, CacheManager.getInstance().getKcalCumulative(),
                        CacheManager.getInstance().getDistanceCumulative(), currentDateandTime,
                        CacheManager.getInstance().getVelocityAvg(), cbDataFrameLocal
                                .getProfileId());

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
        if (route != null) {
            Iterator<DbNode> it = nodeList.iterator();
            while (it.hasNext()) {
                currentNode = it.next();
                datasource.createNode(currentNode.getLatitude(), currentNode.getLongitude(),
                        currentNode.getVelocity(), currentNode.getIndex(), route.getId());
            }
        }
    }

    /*
         * Store the every node in the RoomDB
		 * */
    private void roomStore(List<DbNode> nodeList) {
        // TODO: execute this on new background thread/handler
        // Then remove flowable/reactive parts
//        DbNode currentNode = null;

        DbRoute dbRoute = new DbRoute(0, timeWhenStopped,
                CacheManager.getInstance().getKcalCumulative(),
                CacheManager.getInstance().getDistanceCumulative(), currentDateandTime,
                CacheManager.getInstance().getVelocityAvg(), cbDataFrameLocal
                .getProfileId());
        DbHelper.getInstance(getApplicationContext()).insertRoute(dbRoute, nodeList);
//
//
//        long routeId = dbRoom.dbRouteDao().insertRoute(
//                new DbRoute(0, timeWhenStopped, CacheManager.getInstance().getKcalCumulative(),
//                        CacheManager.getInstance().getDistanceCumulative(), currentDateandTime,
//                        CacheManager.getInstance().getVelocityAvg(), cbDataFrameLocal
//                        .getProfileId()));
//        Flowable<DbRoute> route = dbRoom.dbRouteDao().getRouteById(routeId);
//
//        if (route != null) {
//            Iterator<DbNode> it = nodeList.iterator();
//            while (it.hasNext()) {
//                currentNode = it.next();
//                dbRoom.dbNodeDao().insertNode(
//                        new DbNode(0, currentNode.getLatitude(), currentNode.getLongitude(),
//                                currentNode.getVelocity(), currentNode.getIndex(), routeId));
//            }
//        }
    }

    /**
     * Method for initiating layout.
     */
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
    }

    /**
     * Method to open settings.
     */
    public void openGPSSettings() {
        Intent i = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(i, Constants.REQUEST_GPS_SETTINGS);
    }

    private void backgroundTrackNotification(int notificationID) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launher);
        builder.setContentTitle(getString(R.string.notification_title));
        builder.setContentText(getString(R.string.notification_text));
        builder.setAutoCancel(true);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, this.getIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationID, builder.build());
        notificationExist = true;
    }

    private void deleteNotification(int notificationID) {
        if (notificationExist) {
            notificationManager.cancel(notificationID);
            notificationExist = false;
        }
    }
}
