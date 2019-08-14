package com.hyperether.getgoing.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.service.GPSTrackingService;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.toolbox.HyperConst;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.ActivityCompat;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

public class ShowLocationActivity extends Activity implements OnMapReadyCallback {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final float LOCATION_DISTANCE = 5;

    private GoogleMap mMap;

    private boolean mLocTrackingRunning = false;
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
    private SimpleDateFormat sdf;
    private String currentDateandTime;
    private boolean timeFlg = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Keep screen on all the time
        setContentView(R.layout.show_location);

        mRouteAlreadySaved = true;

        // Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();

        cbDataFrameLocal = new CBDataFrame();
        Bundle b = getIntent().getExtras();
        cbDataFrameLocal = b.getParcelable("searchKey");

        initLayoutDinamically();

        sdf = new SimpleDateFormat("dd.MM.yyyy.' 'HH:mm:ss", Locale.ENGLISH);

        clearData();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.show_map_page);
        mapFragment.getMapAsync(this);

        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mLocTrackingRunning = mPrefs.getBoolean("KEY_UPDATES_ON", false);
            // Otherwise, turn off location updates
        } else {
            mLocTrackingRunning = false;
            mEditor.putBoolean("KEY_UPDATES_ON", mLocTrackingRunning);
            mEditor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mLocTrackingRunning && mMap != null) {
            mMap.clear();
            drawRoute(CacheManager.getInstance().getmRoute());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCacheData();
        stopService(new Intent(this, GPSTrackingService.class));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        savedInstanceState.putBoolean("mLocTrackingRunning", mLocTrackingRunning);
        savedInstanceState.putString("currentDateandTime", currentDateandTime);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        mLocTrackingRunning = savedInstanceState.getBoolean("mLocTrackingRunning");
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
            dialog.setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
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
            dialog.setPositiveButton(R.string.alert_dialog_positive_reset_save_btn,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            if (CacheManager.getInstance().getmRoute() != null) {
                                clearCacheData();
                            }

                            if (mMap != null)
                                mMap.clear();

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
        Intent intent = new Intent(this, GPSTrackingService.class);
        intent.putExtra(HyperConst.LOC_INTERVAL, UPDATE_INTERVAL_IN_MILLISECONDS);
        intent.putExtra(HyperConst.LOC_FASTEST_INTERVAL, FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        intent.putExtra(HyperConst.LOC_DISTANCE, LOCATION_DISTANCE);
        startService(intent);

        showTime.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        showTime.start();

        timer = new Timer();
        timer.schedule(new RefreshData(), 0, 1000);

        button_start.setVisibility(View.GONE);
        button_pause.setVisibility(View.VISIBLE);

        mLocTrackingRunning = true;
        mRouteAlreadySaved = false;
        mEditor.putBoolean("KEY_UPDATES_ON", mLocTrackingRunning);
        mEditor.apply();
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

        mLocTrackingRunning = false;
        mRouteAlreadySaved = false;
        mEditor.putBoolean("KEY_UPDATES_ON", mLocTrackingRunning);
        mEditor.apply();
    }

    class RefreshData extends TimerTask {

        @Override
        public void run() {
            ShowLocationActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    CacheManager cacheMngr = CacheManager.getInstance();
                    cacheMngr.setTimeCumulative(timeWhenStopped);
                    mMap.clear();
                    drawRoute(cacheMngr.getmRoute());

                    if (cacheMngr.getVelocity() != null) {
                        showData(cacheMngr.getDistanceCumulative(), cacheMngr.getKcalCumulative(),
                                cacheMngr.getVelocity(), cacheMngr.getVelocityAvg());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.mMap = googleMap;
            initMapComponents(googleMap);

            LocationManager locationManager = (LocationManager) getSystemService(
                    Context.LOCATION_SERVICE);

            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
//        backPressed = false;
        if (mLocTrackingRunning || !mRouteAlreadySaved) {
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

    /*
     * Store the every node in the RoomDB
     * */
    private void roomStore(List<DbNode> nodeList) {
        DbRoute dbRoute = new DbRoute(0, timeWhenStopped,
                CacheManager.getInstance().getKcalCumulative(),
                CacheManager.getInstance().getDistanceCumulative(), currentDateandTime,
                CacheManager.getInstance().getVelocityAvg(), cbDataFrameLocal
                .getProfileId());
        DbHelper.getInstance(getApplicationContext()).insertRoute(dbRoute, nodeList);
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
}
