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
    //automatski je po defaultu na nuli
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
        // Svaki put kada stisnem Start na app, ona poziva ovu metodu.
        //Trenutno vreme u milisekundama. OldTime poziva na update vreme pokretanja
        time = System.currentTimeMillis() - oldTime;
        timeCumulative += System.currentTimeMillis() - oldTime;
        secondsCumulative = (int) timeCumulative / 1000;
        oldTime = System.currentTimeMillis();
        //Kada se ova metoda poziva, prosledjuje se trenutna lokacija
        mCurrentLocation = location;

        if (mCurrentLocation != null) {
            //uzima koordinate, ako nije na null
            dLat = mCurrentLocation.getLatitude();
            dLong = mCurrentLocation.getLongitude();
            //firstPass da li je prvi unos u bazu
            if (firstPass) {
                latitude = latitude_old = dLat;
                longitude = longitude_old = dLong;
                //Samo bi trebalo prvi put da prodje i onda vise nikad
                firstPass = false;
             //DbNode kreira novi objekat, postavlja id na 0 (prvi unos), nodeIndex++ iskoristi vrednost, pa je uvecaj;
                DbNode tmp = new DbNode(0, latitude, longitude, 0, nodeIndex++, CacheManager.getInstance().getCurrentRouteId());
                //
                CacheManager.getInstance().addRouteNode(tmp);
                //daoInsertNode konkretno ubacuje u bazu
                GgRepository.getInstance().daoInsertNode(tmp);
                //Ukoliko je drugi unos, onda Else
            } else {
                latitude_old = latitude;
                longitude_old = longitude;
                //trenutne vrednosti
                latitude = dLat;
                longitude = dLong;
            }
           //Ako izbije neka greska, nece biti postavljeno na true, onda nece uci u if
            actualPositionValid = true; // put up a flag for the algorithm
        }

        if (actualPositionValid) {
            actualPositionValid = false; // reset the flag
            double dLate = latitude - latitude_old;
            double dLon = longitude - longitude_old;

            if ((dLate != 0) || (dLon != 0)) {
                // Carry out the path filtering
                if (!isKalmanStateSet) {
                    //pocetak, kada startuje mora odraditi ovaj deo
                    kalman.SetState(latitude,
                            longitude,
                            mCurrentLocation != null ? mCurrentLocation.getAccuracy() : 0,
                            timeCumulative);
                    isKalmanStateSet = true;
                }
                //nastavalja kasnije na ovaj deo
                kalman.Process(latitude,
                        longitude,
                        mCurrentLocation != null ? mCurrentLocation.getAccuracy() : 0,
                        timeCumulative);
                latitude = kalman.get_lat();
                longitude = kalman.get_lng();

                distance =
                        Conversion.gps2m(latitude, longitude, latitude_old, longitude_old);
                //Ako nije jednako null, uci ce u if
                if (!Double.isNaN(distance)) {
                    distanceCumulative += distance;
                    distanceDelta += distance;

                    velocityAvg = distanceCumulative / secondsCumulative;

                    //brzina je srednja vrednost izmerene i ocitane brzine
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
                    //Proverava da li je to osvezavanje distance vece od 10
                    if (distanceDelta > Constants.NODE_ADD_DISTANCE) {
                        distanceDelta = 0;
                        // add new point to the route
                        // node and route database _ids are intentionally 0
                        DbNode tmp = new DbNode(0, latitude, longitude, (float) velocity,
                                nodeIndex++, CacheManager.getInstance().getCurrentRouteId());
                        if (velocity < 30) {
                            CacheManager.getInstance().addRouteNode(tmp);
                            //dodaje bas u bazu podatke
                            GgRepository.getInstance().daoInsertNode(tmp);
                        }
                    }
                    //Ako je null distanca, onda ide Else
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
         //Predjenja putanja
        if (velocity < 30) {
            CacheManager.getInstance().setDistanceCumulative(distanceCumulative);
        }
    }
}