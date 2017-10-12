package com.hyperether.getgoing.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.location.KalmanLatLong;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.util.CaloriesCalculation;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.LogUtil;


/**
 * Created by nikola on 11/07/17.
 */

public class GPSTrackingService extends Service {

    private static final String TAG = "GPSTracker";

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final float LOCATION_DISTANCE = 5;

    // filter for GPS data smoothing
    // Initialise Kalman filter
    private KalmanLatLong kalman = new KalmanLatLong(3);

    private double latitude, longitude, latitude_old, longitude_old;
    private boolean firstPass = true;
    private boolean actualPositionValid = false;

    private boolean isKalmanStateSet = false;
    // Global variable to hold the current location
    private Location mCurrentLocation;
    private long timeCumulative = 0;
    private int nodeIndex;

    private int secondsCumulative = 0;
    private long time = 0; // time between to position updates

    private double kcalCumulative = 0;
    private double kcalCurrent;
    private double distanceCumulative = 0;
    private double distanceDelta = 0;
    private double velocity = 0;
    private double velocityAvg = 0;
    private double weight = 0;
    private long oldTime = 0;

    private String timeString;    // current duration of a walk

    private CaloriesCalculation calcCal = new CaloriesCalculation();
    private CBDataFrame cbDataFrameLocal;    // to store the current settings

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        SharedPreferences settings = getSharedPreferences(Constants.PREF_FILE, 0);
        weight = settings.getInt("weight", 0);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();

        if (CacheManager.getInstance().getDistanceCumulative() != null) {
            distanceCumulative = CacheManager.getInstance().getDistanceCumulative();
        }

        if (CacheManager.getInstance().getKcalCumulative() != null) {
            kcalCumulative = CacheManager.getInstance().getKcalCumulative();
        }

        if (CacheManager.getInstance().getVelocity() != null) {
            velocity = CacheManager.getInstance().getVelocity();
        }

        if (CacheManager.getInstance().getVelocityAvg() != null) {
            velocityAvg = CacheManager.getInstance().getVelocityAvg();
        }

        timeCumulative = CacheManager.getInstance().getTimeCumulative();
        secondsCumulative = CacheManager.getInstance().getSecondsCumulative();
        time = CacheManager.getInstance().getTime();

        oldTime = System.currentTimeMillis();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            try {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            } catch (SecurityException ex) {

            }
        }
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(LOCATION_DISTANCE);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "location: " + locationResult.getLastLocation());

                LogUtil.getInstance().add(LogUtil.INFO, TAG, "current_loc: " + locationResult.getLastLocation(), new Exception());

                double dLat, dLong;
                double distance = 0;

                time = System.currentTimeMillis() - oldTime;
                timeCumulative += System.currentTimeMillis() - oldTime;
                secondsCumulative = (int) timeCumulative / 1000;
                oldTime = System.currentTimeMillis();

                mCurrentLocation = locationResult.getLastLocation();

                if (mCurrentLocation != null) {
                    dLat = mCurrentLocation.getLatitude();
                    dLong = mCurrentLocation.getLongitude();

                    if (firstPass) {
                        latitude = latitude_old = dLat;
                        longitude = longitude_old = dLong;
                        firstPass = false;

                        DbNode tmp = new DbNode(0, latitude, longitude, 0, nodeIndex++, 0);
                        CacheManager.getInstance().addRouteNode(tmp);
                    } else {
                        latitude_old = latitude;
                        longitude_old = longitude;
                        latitude = dLat;
                        longitude = dLong;
                    }

                    actualPositionValid = true; // put up a flag for the algorithm
                }

                if (actualPositionValid) {
                    actualPositionValid = false; // reset the flag
                    double dLate = latitude - latitude_old;
                    double dLon = longitude - longitude_old;

                    if ((dLate != 0) || (dLon != 0)) {
                        // Carry out the path filtering
                        if (!isKalmanStateSet) {
                            kalman.SetState(latitude,
                                    longitude,
                                    mCurrentLocation != null ? mCurrentLocation.getAccuracy() : 0,
                                    timeCumulative);
                            isKalmanStateSet = true;
                        }

                        kalman.Process(latitude,
                                longitude,
                                mCurrentLocation != null ? mCurrentLocation.getAccuracy() : 0,
                                timeCumulative);
                        latitude = kalman.get_lat();
                        longitude = kalman.get_lng();

                        distance =
                                gps2m(latitude, longitude, latitude_old, longitude_old);
                        if (!Double.isNaN(distance)) {
                            distanceCumulative += distance;
                            distanceDelta += distance;

                            velocityAvg = distanceCumulative / secondsCumulative;

                            //brzina je srednja vrednost izmerene i ocitane brzine
                            velocity = (mCurrentLocation.getSpeed() + (distance / time)) / 2;
                            if (velocity < 30) {
                                CacheManager.getInstance().setVelocity(velocity);
                            }

                            if (CacheManager.getInstance().getObDataFrameLocal() != null) {
                                cbDataFrameLocal = CacheManager.getInstance().getObDataFrameLocal();
                                kcalCurrent = calcCal.calculate(distance, velocity, cbDataFrameLocal,
                                        weight);
                                kcalCumulative += kcalCurrent;
                                if (velocity < 30) {
                                    CacheManager.getInstance().setKcalCumulative(kcalCumulative);
                                }
                            }

                            if (distanceDelta > Constants.NODE_ADD_DISTANCE) {
                                distanceDelta = 0;
                                // add new point to the route
                                // node and route database _ids are intentionally 0
                                DbNode tmp = new DbNode(0, latitude, longitude, (float) velocity,
                                        nodeIndex++, 0);
                                if (velocity < 30) {
                                    CacheManager.getInstance().addRouteNode(tmp);
                                }
                            }
                        } else {
                            velocity = mCurrentLocation.getSpeed();
                            if (velocity < 30) {
                                CacheManager.getInstance().setVelocity(velocity);
                            }
                        }
                    }

                    if (velocity < 30) {
                        CacheManager.getInstance().setDistanceCumulative(distanceCumulative);
                        CacheManager.getInstance().setKcalCumulative(kcalCumulative);
                        CacheManager.getInstance().setVelocity(velocity);
                        CacheManager.getInstance().setVelocityAvg(velocityAvg);
                    }

                    time = 0; // reset the second counter for calculating velocity
                } else {
                    // is connection broken???
                }

                if (velocity < 30) {
                    CacheManager.getInstance().setDistanceCumulative(distanceCumulative);
                }
            }
        };
    }

    /**
     * This method only works if the points are close enough that you can omit that
     * earth is not regular shape
     *
     * @param lat_a first point lat
     * @param lng_a first point lng
     * @param lat_b second point lat
     * @param lng_b second point lng
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
}