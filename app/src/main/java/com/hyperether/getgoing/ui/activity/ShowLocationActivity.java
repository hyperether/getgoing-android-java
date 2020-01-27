package com.hyperether.getgoing.ui.activity;

import android.annotation.SuppressLint;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

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
import com.hyperether.getgoing.repository.room.DbRouteAddedCallback;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.service.GPSTrackingService;
import com.hyperether.getgoing.ui.fragment.ActivitiesFragment;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.viewmodel.NodeListViewModel;
import com.hyperether.toolbox.HyperConst;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_LOCATION_ACT;
import static com.hyperether.getgoing.util.Constants.PREF_RIDE_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_RUN_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_WALK_ROUTE_EXISTING;

public class ShowLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final float LOCATION_DISTANCE = 5;

    private GoogleMap mMap;

    private boolean mLocTrackingRunning = false;
    private boolean mRouteAlreadySaved = false;

    private SharedPreferences mPrefs;
    private SharedPreferences cbPrefs;
    private Editor mEditor;
    // to store the current settings
    private CBDataFrame cbDataFrameLocal;
    private NodeListViewModel nodeListViewModel;

    // U/I variables
    private Button set_goal;
    private TextView activity_id, labelDuration, labelVelocity, labelCalories;
    private ImageView button_start, button_pause;
    private ImageButton button_rst, button_save, button_back;
    private Chronometer showTime, showCalories, showDistance, showVelocity;

    private List<DbNode> nodeList;

    // timer for data show
    private Timer timer;
    long timeWhenStopped = 0;
    long timeWhenStopped4Storage = 0;

    // Route storage variables
    private SimpleDateFormat sdf;
    private String currentDateandTime;
    private boolean timeFlg = true;

    private long goalStore;

    //    private View toInflate;
    private Context classContext;

    private DbRoute updatedRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Keep screen on all the time
        setContentView(R.layout.activity_location);

        mRouteAlreadySaved = true;

        // Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        // Get user data shared prefs
        cbPrefs = getSharedPreferences("CBUserDataPref.txt", Context.MODE_PRIVATE);

        cbDataFrameLocal = new CBDataFrame();
        Bundle b = getIntent().getExtras();
        cbDataFrameLocal = b.getParcelable("searchKey");

        nodeList = new ArrayList<>();
        nodeListViewModel = ViewModelProviders.of(this).get(NodeListViewModel.class);

        classContext = this;

        initLayoutDinamically();
        setActivityLabel();
        setVisibilities();

        sdf = new SimpleDateFormat("dd.MM.yyyy.' 'HH:mm:ss", Locale.ENGLISH);

        clearData();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mLocTrackingRunning = mPrefs.getBoolean("KEY_UPDATES_ON", false);
            // Otherwise, turn off location updates
        } else {
            mLocTrackingRunning = false;
            mEditor.putBoolean("KEY_UPDATES_ON", mLocTrackingRunning);
            mEditor.apply();
        }

//        toInflate = getLayoutInflater().inflate(R.layout.alertdialog_goal, null, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearCacheData();
        stopService(new Intent(this, GPSTrackingService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cbPrefs.registerOnSharedPreferenceChangeListener(sharedPrefsListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cbPrefs.unregisterOnSharedPreferenceChangeListener(sharedPrefsListener);
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

    private void setupVMObserver() {
        nodeListViewModel.getNodeListById(CacheManager.getInstance().getCurrentRouteId()).observe(this, dbNodes -> {
            if (CacheManager.getInstance().getCurrentRouteId() != 0) {

                mMap.clear();
                drawRoute(dbNodes);

                CacheManager cacheMngr = CacheManager.getInstance();
                cacheMngr.setTimeCumulative(timeWhenStopped4Storage);

                if (cacheMngr.getVelocity() != null) {
                    showData(cacheMngr.getDistanceCumulative(), cacheMngr.getKcalCumulative(),
                            cacheMngr.getVelocity());
                }
            }
        });
    }

    private void setActivityLabel() {
        int id = cbDataFrameLocal.getProfileId();

        switch (id) {
            case 1: {
                activity_id.setText(getString(R.string.walking));
                break;
            }
            case 2: {
                activity_id.setText(getString(R.string.running));
                break;
            }
            case 3: {
                activity_id.setText(getString(R.string.cycling));
                break;
            }
        }
    }

    private void setVisibilities() {
        if (!cbPrefs.contains("goal")) {
            set_goal.setVisibility(View.VISIBLE);
            button_save.setVisibility(View.GONE);
            button_rst.setVisibility(View.GONE);
            button_start.setClickable(false);
            showDistance.setVisibility(View.GONE);
            showTime.setVisibility(View.GONE);
            showCalories.setVisibility(View.GONE);
            showVelocity.setVisibility(View.GONE);
            labelDuration.setVisibility(View.GONE);
            labelVelocity.setVisibility(View.GONE);
            labelCalories.setVisibility(View.GONE);
        } else {
            goalStore = cbPrefs.getInt("goal", 0);
            set_goal.setVisibility(View.GONE);
            button_save.setVisibility(View.VISIBLE);
            button_save.setClickable(false);
            button_rst.setVisibility(View.VISIBLE);
            button_rst.setClickable(false);
            button_start.setClickable(true);
            showDistance.setVisibility(View.VISIBLE);
            showTime.setVisibility(View.VISIBLE);
            showCalories.setVisibility(View.VISIBLE);
            showVelocity.setVisibility(View.VISIBLE);
            labelDuration.setVisibility(View.VISIBLE);
            labelVelocity.setVisibility(View.VISIBLE);
            labelCalories.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method handle button click on Start button.
     */
    private final OnClickListener mButtonStartListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (goalStore > 0) {
                startTracking(classContext);
                if (timeFlg) {
                    // Get date and time on which the tracking started
                    currentDateandTime = sdf.format(new Date());
                    timeFlg = false;
                }
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
            button_save.setClickable(true);
            button_save.setImageDrawable(getDrawable(R.drawable.ic_light_save_icon));
            button_rst.setClickable(true);
            button_rst.setImageDrawable(getDrawable(R.drawable.ic_light_replay_icon));
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
            dialog.setMessage(getString(R.string.alert_dialog_message_save_btn));
            dialog.setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            CacheManager cm = CacheManager.getInstance();

                            mRouteAlreadySaved = true;

                            updatedRoute = new DbRoute(cm.getCurrentRouteId(), timeWhenStopped4Storage,
                                    cm.getKcalCumulative(), cm.getDistanceCumulative(), currentDateandTime,
                                    cm.getVelocityAvg(), cbDataFrameLocal.getProfileId(), goalStore);

                            GgRepository.getInstance().updateRoute(updatedRoute);
                            CacheManager.getInstance().setCurrentRouteId(0);

                            Editor editor = cbPrefs.edit();
                            if (updatedRoute.getActivity_id() == ACTIVITY_WALK_ID &&
                                    !cbPrefs.getBoolean(PREF_WALK_ROUTE_EXISTING, false)) {
                                editor.putBoolean(PREF_WALK_ROUTE_EXISTING, true);
                                editor.apply();
                            } else if (updatedRoute.getActivity_id() == ACTIVITY_RUN_ID &&
                                    !cbPrefs.getBoolean(PREF_RUN_ROUTE_EXISTING, false)) {
                                editor.putBoolean(PREF_RUN_ROUTE_EXISTING, true);
                                editor.apply();
                            } else if (updatedRoute.getActivity_id() == ACTIVITY_RIDE_ID &&
                                    !cbPrefs.getBoolean(PREF_RIDE_ROUTE_EXISTING, false)) {
                                editor.putBoolean(PREF_RIDE_ROUTE_EXISTING, true);
                                editor.apply();
                            }

                            button_save.setClickable(false);
                            button_save.setImageDrawable(getDrawable(R.drawable.ic_light_save_icon_disabled));
                            Toast.makeText(classContext, getString(R.string.alert_dialog_route_saved), Toast.LENGTH_SHORT).show();
                        }
                    });

            dialog.setNegativeButton(getString(R.string.alert_dialog_negative_button_save_btn),
                    (paramDialogInterface, paramInt) -> {
                        mRouteAlreadySaved = false;
                        GgRepository.getInstance().deleteRouteById(CacheManager.getInstance().getCurrentRouteId());
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

                            GgRepository.getInstance().deleteRouteById(CacheManager.getInstance().getCurrentRouteId());
                            CacheManager.getInstance().setCurrentRouteId(0);

                            button_save.setClickable(false);
                            button_save.setImageDrawable(getDrawable(R.drawable.ic_light_save_icon_disabled));
                            button_rst.setClickable(false);
                            button_rst.setImageDrawable(getDrawable(R.drawable.ic_light_replay_icon_disabled));
                            mRouteAlreadySaved = true;
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

    private final OnClickListener mButtonBackListener = v -> onAnyBackButtonPressed();

    private final OnClickListener mButtonSetGoalListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivitiesFragment activitiesFragment = ActivitiesFragment.newInstance(null, OPENED_FROM_LOCATION_ACT);
            activitiesFragment.show(getSupportFragmentManager(), "ActivitiesFragment");
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
        showData(0, 0, 0);
    }

    /**
     * This method starts timer and enable visibility of pause button.
     */
    private void startTracking(Context context) {
        GgRepository.getInstance().insertRoute(new DbRoute(0, 0, 0, 0, "", 0, 0, 0), new DbRouteAddedCallback() {
            @Override
            public void onRouteAdded(long currentid) {
                CacheManager.getInstance().setCurrentRouteId(currentid);

                Intent intent = new Intent(context, GPSTrackingService.class);
                intent.putExtra(HyperConst.LOC_INTERVAL, UPDATE_INTERVAL_IN_MILLISECONDS);
                intent.putExtra(HyperConst.LOC_FASTEST_INTERVAL, FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
                intent.putExtra(HyperConst.LOC_DISTANCE, LOCATION_DISTANCE);
                startService(intent);

                runOnUiThread(() -> {
                    setupVMObserver();

                    showTime.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    showTime.start();

                    button_start.setVisibility(View.GONE);
                    button_pause.setVisibility(View.VISIBLE);
                });

                mLocTrackingRunning = true;
                mRouteAlreadySaved = false;
                mEditor.putBoolean("KEY_UPDATES_ON", mLocTrackingRunning);
                mEditor.apply();
            }
        });
    }

    /**
     * This method stops timer and disables visibility of start button.
     */
    private void stopTracking() {
        stopService(new Intent(this, GPSTrackingService.class));

        timeWhenStopped4Storage = SystemClock.elapsedRealtime() - showTime.getBase();
        timeWhenStopped = showTime.getBase() - SystemClock.elapsedRealtime();
        showTime.stop();

        button_start.setVisibility(View.VISIBLE);
        button_pause.setVisibility(View.GONE);

        mLocTrackingRunning = false;
        mRouteAlreadySaved = false;
        mEditor.putBoolean("KEY_UPDATES_ON", mLocTrackingRunning);
        mEditor.apply();
    }

    /**
     * This method show measured data.
     *
     * @param distance passed distance
     * @param kcal     calories burned
     * @param vel      average velocity
     */
    @SuppressLint("DefaultLocale")
    private void showData(double distance, double kcal, double vel) {
        showCalories.setText(String.format("%.02f kcal", kcal));
        if (cbDataFrameLocal.getMeasurementSystemId() == 1 ||
                cbDataFrameLocal.getMeasurementSystemId() == 2)
            showDistance
                    .setText(String.format("%.02f ft", distance * 3.281)); // present data in feet
        else
            showDistance.setText(String.format("%.02f m", distance));

        showVelocity.setText(String.format("%.02f m/s", vel));
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

                dialog.setNegativeButton(R.string.alert_dialog_negative_button, (paramDialogInterface, paramInt) -> finish());

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
        onAnyBackButtonPressed();
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
            googleMap.setTrafficEnabled(false);
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
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
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
     * @param firstNode  first point of the rout
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
     * Method for initiating layout.
     */
    private void initLayoutDinamically() {
        button_back = findViewById(R.id.ib_al_backbutton);
        button_back.setOnClickListener(mButtonBackListener);
        button_start = findViewById(R.id.al_btn_start);
        button_start.setOnClickListener(mButtonStartListener);
        button_pause = findViewById(R.id.al_btn_pause);
        button_pause.setOnClickListener(mButtonPauseListener);
        button_rst = findViewById(R.id.ib_al_reset);
        button_rst.setOnClickListener(mButtonResetListener);
        button_save = findViewById(R.id.ib_al_save);
        button_save.setOnClickListener(mButtonSaveListener);

        showTime = findViewById(R.id.chr_al_duration);
        showCalories = findViewById(R.id.chr_al_kcal);
        showDistance = findViewById(R.id.chr_al_meters);
        showVelocity = findViewById(R.id.chr_al_speed);

        activity_id = findViewById(R.id.tv_al_activity);
        labelCalories = findViewById(R.id.tv_al_kcal);
        labelDuration = findViewById(R.id.tv_al_duration);
        labelVelocity = findViewById(R.id.tv_al_speed);

        set_goal = findViewById(R.id.al_btn_setgoal);
        set_goal.setOnClickListener(mButtonSetGoalListener);
    }

    /**
     * Method to open settings.
     */
    public void openGPSSettings() {
        Intent i = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(i, Constants.REQUEST_GPS_SETTINGS);
    }

    private void onAnyBackButtonPressed() {
        if (mLocTrackingRunning || !mRouteAlreadySaved) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setCancelable(false);
            dialog.setTitle(R.string.alert_dialog_title_back_pressed);
            dialog.setMessage(getString(R.string.alert_dialog_message_back_pressed));
            dialog.setPositiveButton(R.string.alert_dialog_positive_back_pressed, (paramDialogInterface, paramInt) -> {
                stopService(new Intent(GetGoingApp.getInstance().getApplicationContext(),
                        GPSTrackingService.class));
                clearCacheData();
                // TODO remove temporary route from database
                finish();
            });

            dialog.setNegativeButton(getString(R.string.alert_dialog_negative_back_pressed),
                    (paramDialogInterface, paramInt) -> {
                    });

            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsListener =
            (SharedPreferences sharedPreferences, String key) -> setVisibilities();


}
