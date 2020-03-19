package com.hyperether.getgoing.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.ui.activity.NavigationActivity;
import com.hyperether.getgoing.util.CaloriesCalculation;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.Conversion;
import com.hyperether.getgoing.util.KalmanLatLong;
import com.hyperether.toolbox.HyperNotification;
import com.hyperether.toolbox.location.HyperLocationService;

import static com.hyperether.getgoing.util.Constants.TRACKING_ACTIVITY_KEY;


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
    private int nodeIndex;

    private int profileID;

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

    private SharedPreferences settings;

    private CaloriesCalculation calcCal = new CaloriesCalculation();

    @Override
    public void onCreate() {
        settings = getSharedPreferences(Constants.PREF_FILE, 0);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        profileID = intent.getIntExtra(TRACKING_ACTIVITY_KEY, -1);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void startForeground() {
        super.startForeground();

        Intent intent = new Intent(this, NavigationActivity.class);
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

        time = System.currentTimeMillis() - oldTime;
        timeCumulative += System.currentTimeMillis() - oldTime;
        secondsCumulative = (int) timeCumulative / 1000;
        oldTime = System.currentTimeMillis();

        mCurrentLocation = location;

        if (mCurrentLocation != null) {
            dLat = mCurrentLocation.getLatitude();
            dLong = mCurrentLocation.getLongitude();

            if (firstPass) {
                latitude = latitude_old = dLat;
                longitude = longitude_old = dLong;
                firstPass = false;

                DbNode tmp = new DbNode(0, latitude, longitude, 0, nodeIndex++, CacheManager.getInstance().getCurrentRouteId());
                CacheManager.getInstance().addRouteNode(tmp);
                GgRepository.getInstance().daoInsertNode(tmp);
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
                        Conversion.gps2m(latitude, longitude, latitude_old, longitude_old);
                if (!Double.isNaN(distance)) {
                    distanceCumulative += distance;
                    distanceDelta += distance;

                    velocityAvg = distanceCumulative / secondsCumulative;

                    //brzina je srednja vrednost izmerene i ocitane brzine
                    velocity = (mCurrentLocation.getSpeed() + (distance / time)) / 2;
                    if (velocity < 30) {
                        CacheManager.getInstance().setVelocity(velocity);
                    }

                    kcalCurrent = calcCal
                            .calculate(distance, velocity, profileID, settings,
                                    weight);
                    kcalCumulative += kcalCurrent;
                    if (velocity < 30) {
                        CacheManager.getInstance().setKcalCumulative(kcalCumulative);
                    }

                    if (distanceDelta > Constants.NODE_ADD_DISTANCE) {
                        distanceDelta = 0;
                        // add new point to the route
                        // node and route database _ids are intentionally 0
                        DbNode tmp = new DbNode(0, latitude, longitude, (float) velocity,
                                nodeIndex++, CacheManager.getInstance().getCurrentRouteId());
                        if (velocity < 30) {
                            CacheManager.getInstance().addRouteNode(tmp);
                            GgRepository.getInstance().daoInsertNode(tmp);
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
}