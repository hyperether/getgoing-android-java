package com.hyperether.getgoing.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.location.KalmanLatLong;
import com.hyperether.getgoing.util.CaloriesCalculation;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.Conversion;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nikola on 11/07/17.
 */

public class GPSTrackingService extends Service {

    private static final String TAG = "GPSTracker";

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 3000;
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

    private boolean connectionEstablished = false;
    private String timeString;    // current duration of a walk

    private CaloriesCalculation calcCal = new CaloriesCalculation();
    private CBDataFrame cbDataFrameLocal;    // to store the current settings
    private Timer timer;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            double dLat, dLong;
            double distance = 0;

            mCurrentLocation = location;

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

                    distance =
                            gps2m(latitude, longitude, latitude_old, longitude_old);
                    if (!Double.isNaN(distance)) {
                        distanceCumulative += distance;
                        distanceDelta += distance;

                        velocityAvg = distanceCumulative / secondsCumulative;

                        //brzina je srednja vrednost izmerene i ocitane brzine
                        velocity = (mCurrentLocation.getSpeed() + (distance / time)) / 2;
                        CacheManager.getInstance().setVelocity(velocity);

                        if (CacheManager.getInstance().getObDataFrameLocal() != null) {
                            cbDataFrameLocal = CacheManager.getInstance().getObDataFrameLocal();
                            kcalCurrent = calcCal.calculate(distance, velocity, cbDataFrameLocal,
                                    weight);
                            kcalCumulative += kcalCurrent;
                            CacheManager.getInstance().setKcalCumulative(kcalCumulative);
                        }

                        if (distanceDelta > Constants.NODE_ADD_DISTANCE) {
                            distanceDelta = 0;
                            // add new point to the route
                            // node and route database _ids are intentionally 0
                            DbNode tmp = new DbNode(0, latitude, longitude, (float) velocity,
                                    nodeIndex++, 0);
                            CacheManager.getInstance().addRouteNode(tmp);
                        }
                    } else {
                        velocity = mCurrentLocation.getSpeed();
                        CacheManager.getInstance().setVelocity(velocity);
                    }
                }

                CacheManager.getInstance().setDistanceCumulative(distanceCumulative);
                CacheManager.getInstance().setKcalCumulative(kcalCumulative);
                CacheManager.getInstance().setVelocity(velocity);
                CacheManager.getInstance().setVelocityAvg(velocityAvg);

                time = 0; // reset the second counter for calculating velocity
            } else {
                // is connection broken???
            }

            CacheManager.getInstance().setDistanceCumulative(distanceCumulative);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        SharedPreferences settings = getSharedPreferences(Constants.PREF_FILE, 0);
        weight = settings.getInt("weight", 0);

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

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

        timer = new Timer();
        timer.schedule(new UpdateTimeCumulative(), 0, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context
                    .LOCATION_SERVICE);
        }
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

    private class UpdateTimeCumulative extends TimerTask {

        @Override
        public void run() {
            timeCumulative += 1000;
            secondsCumulative = (int) (timeCumulative / 1000);

            timeString = Conversion.getDurationString(secondsCumulative);

            time++;

            CacheManager.getInstance().setTimeCumulative(timeCumulative);
            CacheManager.getInstance().setSecondsCumulative(secondsCumulative);
            CacheManager.getInstance().setTime(time);
        }
    }
}