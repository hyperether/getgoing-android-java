package com.hyperether.getgoing.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.repository.room.DbRouteAddedCallback;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.service.GPSTrackingService;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.viewmodel.NodeListViewModel;
import com.hyperether.getgoing.viewmodel.RouteViewModel;
import com.hyperether.toolbox.HyperConst;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_KEY;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_LOCATION_ACT;
import static com.hyperether.getgoing.util.Constants.TRACKING_ACTIVITY_KEY;


public class TrackingFragment extends Fragment implements OnMapReadyCallback {

    private NavController navigationController;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final float LOCATION_DISTANCE = 5;

    private GoogleMap mMap;

    private boolean mLocTrackingRunning = false;
    private boolean mRouteAlreadySaved = true;
    private boolean trackingStarted = false;

    // U/I variables
    private Button set_goal;
    private TextView activity_id, labelDuration, labelVelocity, labelCalories;
    private ImageView button_start, button_pause;
    private ImageButton button_rst, button_save, button_back;
    private Chronometer showTime, showCalories, showDistance, showVelocity;

    // timer for data show
    private Timer timer;
    long timeWhenStopped = 0;
    long timeWhenStopped4Storage = 0;

    // Route storage variables
    private SimpleDateFormat sdf;

    private long goalStore;
    private int profileID;

    //    private View toInflate;
    private Context classContext;

    MapFragment mapFragment;

    private long currentRouteID;
    private RouteViewModel routeViewModel;
    private NodeListViewModel nodeListViewModel;

    public TrackingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                onAnyBackButtonPressed();
            }
        };
        profileID = getArguments().getInt(TRACKING_ACTIVITY_KEY);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
        nodeListViewModel = new ViewModelProvider(this).get(NodeListViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigationController = Navigation.findNavController(view);

        classContext = getActivity();

        setupVMObserver();
        initLayoutDinamically();
        setActivityLabel();
        setVisibilities();

        sdf = new SimpleDateFormat("dd.MM.yyyy.' 'HH:mm:ss", Locale.ENGLISH);

        clearData();

        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(new Intent(getActivity(), GPSTrackingService.class));
        if (!mRouteAlreadySaved) {
            routeViewModel.removeRouteById(currentRouteID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapFragment != null) {
            getActivity().getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
    }

    private void setupVMObserver() {
        nodeListViewModel.getNodeListById(currentRouteID).observe(getViewLifecycleOwner(), new Observer<List<DbNode>>() {
            @Override
            public void onChanged(List<DbNode> dbNodes) {
                mMap.clear();
                drawRoute(dbNodes);
            }
        });

        routeViewModel.getRouteByIdAsLiveData(currentRouteID).observe(getViewLifecycleOwner(), new Observer<DbRoute>() {
            @Override
            public void onChanged(DbRoute dbRoute) {
                if (dbRoute != null) {
                    showData(dbRoute.getLength(), dbRoute.getEnergy(), dbRoute.getCurrentSpeed());
                }
            }
        });
    }

    private void setActivityLabel() {
        switch (profileID) {
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
        if (!SharedPref.isGoalSet()) {
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
            goalStore = SharedPref.getGoal();
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
    private final View.OnClickListener mButtonStartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (goalStore > 0) {
                startTracking(classContext);
            }
        }
    };

    /**
     * This method handle button click on pause button.
     */
    private final View.OnClickListener mButtonPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopTracking();
            button_save.setClickable(true);
            button_save.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_save_icon));
            button_rst.setClickable(true);
            button_rst.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_replay_icon));
        }
    };

    /**
     * This method handle button click on save button.
     */
    private final View.OnClickListener mButtonSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.alert_dialog_message_save_btn));
            dialog.setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            mRouteAlreadySaved = true;

                            if (profileID == ACTIVITY_WALK_ID &&
                                    !SharedPref.doesWalkRouteExist()) {
                                SharedPref.setWalkRouteExisting(true);
                            } else if (profileID == ACTIVITY_RUN_ID &&
                                    !SharedPref.doesRunRouteExist()) {
                                SharedPref.setRunRouteExisting(true);
                            } else if (profileID == ACTIVITY_RIDE_ID &&
                                    !SharedPref.doesRideRouteExist()) {
                                SharedPref.setRideRouteExisting(true);
                            }

                            button_save.setClickable(false);
                            button_save.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_save_icon_disabled));
                            Toast.makeText(classContext, getString(R.string.alert_dialog_route_saved), Toast.LENGTH_SHORT).show();
                        }
                    });

            dialog.setNegativeButton(getString(R.string.alert_dialog_negative_button_save_btn),
                    (paramDialogInterface, paramInt) -> {
                        // TODO delete current route and nodes? - Ivana
                    });

            dialog.show();
        }
    };

    /**
     * This method handle button click on reset button.
     */
    private final View.OnClickListener mButtonResetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.alert_dialog_message_reset_btn));
            dialog.setPositiveButton(R.string.alert_dialog_positive_reset_save_btn,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            if (mMap != null)
                                mMap.clear();

                            showTime.setBase(SystemClock.elapsedRealtime());
                            timeWhenStopped = 0;

                            clearData();
                            if (!mRouteAlreadySaved) {
                                routeViewModel.removeRouteById(currentRouteID);
                            }

                            mRouteAlreadySaved = true;
                            trackingStarted = false;

                            button_save.setClickable(false);
                            button_save.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_save_icon_disabled));
                            button_rst.setClickable(false);
                            button_rst.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_replay_icon_disabled));
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

    private final View.OnClickListener mButtonBackListener = v -> getActivity().onBackPressed(); //onAnyBackButtonPressed();

    private final View.OnClickListener mButtonSetGoalListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt(OPENED_FROM_KEY, OPENED_FROM_LOCATION_ACT);
            navigationController.navigate(R.id.action_trackingFragment_to_activitiesFragment, bundle);
        }
    };

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
        if (!trackingStarted) {
            trackingStarted = true;
            GgRepository.getInstance().insertRoute(new DbRoute(0, 0, 0, 0, sdf.format(new Date()), 0, 0, profileID, goalStore), new DbRouteAddedCallback() {
                @Override
                public void onRouteAdded(long currentid) {
                    currentRouteID = currentid;
                    getActivity().runOnUiThread(() -> {
                        nodeListViewModel.setRouteID(currentid);
                        routeViewModel.setRouteID(currentid);
                    });
                    startTrackingService(context);
                }
            });
        } else {
            startTrackingService(context);
        }

    }

    private void startTrackingService(Context context) {
        Intent intent = new Intent(context, GPSTrackingService.class);
        intent.putExtra(HyperConst.LOC_INTERVAL, UPDATE_INTERVAL_IN_MILLISECONDS);
        intent.putExtra(HyperConst.LOC_FASTEST_INTERVAL, FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        intent.putExtra(HyperConst.LOC_DISTANCE, LOCATION_DISTANCE);
        getActivity().startService(intent);

        getActivity().runOnUiThread(() -> {

            showTime.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            showTime.start();

            button_start.setVisibility(View.GONE);
            button_pause.setVisibility(View.VISIBLE);
            if (mLocTrackingRunning) {
                button_save.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_save_icon_disabled));
                button_save.setClickable(false);
                button_rst.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_replay_icon_disabled));
                button_rst.setClickable(false);
            }
        });

        mLocTrackingRunning = true;
        mRouteAlreadySaved = false;
    }


    /**
     * This method stops timer and disables visibility of start button.
     */
    private void stopTracking() {
        getActivity().stopService(new Intent(getActivity(), GPSTrackingService.class));

        timeWhenStopped4Storage = SystemClock.elapsedRealtime() - showTime.getBase();
        timeWhenStopped = showTime.getBase() - SystemClock.elapsedRealtime();
        showTime.stop();

        button_start.setVisibility(View.VISIBLE);
        button_pause.setVisibility(View.GONE);

        mLocTrackingRunning = false;
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
        if (SharedPref.getMeasurementSystemId() == 1 ||
                SharedPref.getMeasurementSystemId() == 2)
            showDistance
                    .setText(String.format("%.02f ft", distance * 3.281)); // present data in feet
        else
            showDistance.setText(String.format("%.02f m", distance));

        showVelocity.setText(String.format("%.02f m/s", vel));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission
                        .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.mMap = googleMap;
            initMapComponents(googleMap);

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(
                    Context.LOCATION_SERVICE);

            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setCancelable(false);
                dialog.setTitle(R.string.alert_dialog_title);
                dialog.setMessage(getString(R.string.alert_dialog_message));
                dialog.setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        openGPSSettings();
                    }
                });

                dialog.setNegativeButton(R.string.alert_dialog_negative_button, (paramDialogInterface, paramInt) -> getActivity().finish());

                dialog.show();
            }

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String bestProvider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            zoomOverCurrentLocation(mMap, location);
        } else {
            getActivity().finish();
        }
    }


    /**
     * This method is used for init of map components
     *
     * @param googleMap google map v2
     **/
    private void initMapComponents(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission
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

            if (firstNode.isLast()) {
                Log.i("LAST", "" + firstNode.isLast());
                continue;
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
        button_back = getView().findViewById(R.id.ib_al_backbutton);
        button_back.setOnClickListener(mButtonBackListener);
        button_start = getView().findViewById(R.id.al_btn_start);
        button_start.setOnClickListener(mButtonStartListener);
        button_pause = getView().findViewById(R.id.al_btn_pause);
        button_pause.setOnClickListener(mButtonPauseListener);
        button_rst = getView().findViewById(R.id.ib_al_reset);
        button_rst.setOnClickListener(mButtonResetListener);
        button_save = getView().findViewById(R.id.ib_al_save);
        button_save.setOnClickListener(mButtonSaveListener);

        showTime = getView().findViewById(R.id.chr_al_duration);
        showCalories = getView().findViewById(R.id.chr_al_kcal);
        showDistance = getView().findViewById(R.id.chr_al_meters);
        showVelocity = getView().findViewById(R.id.chr_al_speed);

        activity_id = getView().findViewById(R.id.tv_al_activity);
        labelCalories = getView().findViewById(R.id.tv_al_kcal);
        labelDuration = getView().findViewById(R.id.tv_al_duration);
        labelVelocity = getView().findViewById(R.id.tv_al_speed);

        set_goal = getView().findViewById(R.id.al_btn_setgoal);
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
            getActivity().moveTaskToBack(true);
        } else {
            navigationController.popBackStack();
        }
    }
}
