package com.hyperether.getgoing.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.ui.activity.ShowLocationActivity;
import com.hyperether.getgoing.util.CaloriesCalculation;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.Conversion;
import com.hyperether.getgoing.util.KalmanLatLong;
import com.hyperether.toolbox.HyperNotification;
import com.hyperether.toolbox.location.HyperLocationService;


/**
 * Created by nikola on 11/07/17.
 */

public class GPSTrackingService extends HyperLocationService {

    private static final String TAG = GPSTrackingService.class.getSimpleName();

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
    //it is automatically set to zero by default
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

    private CaloriesCalculation calcCal = new CaloriesCalculation();
    private CBDataFrame cbDataFrameLocal;    // to store the current settings

    @Override
    public void onCreate() {
        SharedPreferences settings = getSharedPreferences(Constants.PREF_FILE, 0);
        weight = settings.getInt("weight", 0);

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

        super.onCreate();
    }

    @Override
    protected void startForeground() {
        super.startForeground();

        Intent intent = new Intent(this, ShowLocationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        startForeground(1123, HyperNotification.getInstance().getForegroundServiceNotification(this,
                getString(R.string.notification_title),
                getString(R.string.notification_text),
                R.drawable.ic_logo_light,
                R.mipmap.ic_logo,
                pendingIntent));
    }

    @Override
    protected void onLocationUpdate(Location location) {
        double dLat, dLong;
        double distance = 0;
        // Every time we press Start on the app, it calls this method.
        // Current time in milliseconds. OldTime calls for a startup time update
        time = System.currentTimeMillis() - oldTime;
        timeCumulative += System.currentTimeMillis() - oldTime;
        secondsCumulative = (int) timeCumulative / 1000;
        oldTime = System.currentTimeMillis();
        //When this method is called, the current location is passed
        mCurrentLocation = location;

        if (mCurrentLocation != null) {
            //It takes coordinates, if not at null
            dLat = mCurrentLocation.getLatitude();
            dLong = mCurrentLocation.getLongitude();
            //FirstPass whether the first entry in the database
            if (firstPass) {
                latitude = latitude_old = dLat;
                longitude = longitude_old = dLong;
                //It should just pass the first time and then never again
                firstPass = false;
             //DbNode creates a new object, sets the id to 0 (first entry), nodeIndex ++ uses the value, so increase it;
                DbNode tmp = new DbNode(0, latitude, longitude, 0, nodeIndex++, CacheManager.getInstance().getCurrentRouteId());
                //
                CacheManager.getInstance().addRouteNode(tmp);
                //daoInsertNode specifically inserts into the database
                GgRepository.getInstance().daoInsertNode(tmp);
                //If the second entry is, then Else
            } else {
                latitude_old = latitude;
                longitude_old = longitude;
                //current values
                latitude = dLat;
                longitude = dLong;
            }
           //If an error occurs, it will not be set to true, then it will not enter if
            actualPositionValid = true; // put up a flag for the algorithm
        }

        if (actualPositionValid) {
            actualPositionValid = false; // reset the flag
            double dLate = latitude - latitude_old;
            double dLon = longitude - longitude_old;

            if ((dLate != 0) || (dLon != 0)) {
                // Carry out the path filtering
                if (!isKalmanStateSet) {
                    //the beginning, when it starts must do this part
                    kalman.SetState(latitude,
                            longitude,
                            mCurrentLocation != null ? mCurrentLocation.getAccuracy() : 0,
                            timeCumulative);
                    isKalmanStateSet = true;
                }
                //continues later on to this part
                kalman.Process(latitude,
                        longitude,
                        mCurrentLocation != null ? mCurrentLocation.getAccuracy() : 0,
                        timeCumulative);
                latitude = kalman.get_lat();
                longitude = kalman.get_lng();

                distance =
                        Conversion.gps2m(latitude, longitude, latitude_old, longitude_old);
                //If it is not equal to null, it will enter if
                if (!Double.isNaN(distance)) {
                    distanceCumulative += distance;
                    distanceDelta += distance;

                    velocityAvg = distanceCumulative / secondsCumulative;

                    //speed is the mean value of the measured and read speed
                    velocity = (mCurrentLocation.getSpeed() + (distance / time)) / 2;
                    if (velocity < 30) {
                        CacheManager.getInstance().setVelocity(velocity);
                    }

                    if (CacheManager.getInstance().getObDataFrameGlobal() != null) {
                        cbDataFrameLocal = CacheManager.getInstance().getObDataFrameGlobal();
                        kcalCurrent = calcCal
                                .calculate(distance, velocity, cbDataFrameLocal,
                                        weight);
                        kcalCumulative += kcalCurrent;
                        if (velocity < 30) {
                            CacheManager.getInstance().setKcalCumulative(kcalCumulative);
                        }
                    }

                    //Checks if this distance is greater than 10
                    if (distanceDelta > Constants.NODE_ADD_DISTANCE) {
                        distanceDelta = 0;
                        // add new point to the route
                        // node and route database _ids are intentionally 0
                        DbNode tmp = new DbNode(0, latitude, longitude, (float) velocity,
                                nodeIndex++, CacheManager.getInstance().getCurrentRouteId());
                        if (velocity < 30) {
                            CacheManager.getInstance().addRouteNode(tmp);
                            //here adds data to the database
                            GgRepository.getInstance().daoInsertNode(tmp);
                        }
                    }
                    //If the distance is null, then Else goes
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
         //Traversed path
        if (velocity < 30) {
            CacheManager.getInstance().setDistanceCumulative(distanceCumulative);
        }
    }
}