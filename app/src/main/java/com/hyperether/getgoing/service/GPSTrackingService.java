package com.hyperether.getgoing.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import com.hyperether.getgoing.GetGoingApp;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.Node;
import com.hyperether.getgoing.repository.room.entity.Route;
import com.hyperether.getgoing.ui.activity.NavigationActivity;
import com.hyperether.getgoing.util.CaloriesCalculation;
import com.hyperether.toolbox.HyperNotification;
import com.hyperether.toolbox.location.HyperLocationService;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;


/**
 * @author Slobodan Prijic
 */

public class GPSTrackingService extends HyperLocationService {

    private static final String TAG = GPSTrackingService.class.getSimpleName();
    private static final double ACCURACY_MIN = 20.0;
    private int nodeIndex;
    private int profileID;
    private long routeID;
    private double weight = 0;
    private Route currentRoute;
    private Location previousLocation;
    private long previousTimestamp = 0;
    private long timeCumulative = 0;
    private int secondsCumulative = 0;
    private double kcalCumulative = 0;
    private double distanceCumulative = 0;
    private double velocityAvg = 0;
    private CaloriesCalculation calcCal = new CaloriesCalculation();

    @Override
    public void onCreate() {
        super.onCreate();
        weight = SharedPref.getWeight();
        previousTimestamp = System.currentTimeMillis();

        GetGoingApp.getInstance().getHandler().post(() -> {
            currentRoute = GgRepository.getInstance().getLastRoute();

            routeID = currentRoute.getId();
            profileID = currentRoute.getActivity_id();
            distanceCumulative = currentRoute.getLength();
            kcalCumulative = currentRoute.getEnergy();
            velocityAvg = currentRoute.getAvgSpeed();
            timeCumulative = currentRoute.getDuration();
            secondsCumulative = (int) timeCumulative / 1000;
        });
    }

    @Override
    protected void startForeground() {
        super.startForeground();

        Intent intent = new Intent(this, NavigationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1123, HyperNotification.getInstance().getForegroundServiceNotification(this,
                            getString(R.string.notification_title),
                            getString(R.string.notification_text),
                            R.drawable.ic_notification_small,
                            R.drawable.ic_notification,
                            pendingIntent),
                    FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(1123, HyperNotification.getInstance().getForegroundServiceNotification(this,
                    getString(R.string.notification_title),
                    getString(R.string.notification_text),
                    R.drawable.ic_notification_small,
                    R.drawable.ic_notification,
                    pendingIntent));
        }
    }

    @Override
    protected void onLocationUpdate(Location location) {
        if (location != null && location.getAccuracy() < ACCURACY_MIN) {
            if (previousLocation == null) {
                previousTimestamp = System.currentTimeMillis();
                GgRepository.getInstance().daoInsertNode(createNode(location));
            } else {
                // time between to position updates
                long elapsedTime = System.currentTimeMillis() - previousTimestamp;
                previousTimestamp = System.currentTimeMillis();
                timeCumulative += elapsedTime;
                secondsCumulative = (int) timeCumulative / 1000;

                float distance = location.distanceTo(previousLocation);
                if (distance > 0) {
                    distanceCumulative += distance;
                    velocityAvg = distanceCumulative / secondsCumulative;

                    //speed is average value from gps and calculated value
                    float velocity = (location.getSpeed() + (distance / elapsedTime)) / 2;
                    double kcalCurrent = calcCal.calculate(distance, velocity, profileID, weight);
                    kcalCumulative += kcalCurrent;

                    currentRoute.setLength(distanceCumulative);
                    currentRoute.setEnergy(kcalCumulative);
                    currentRoute.setCurrentSpeed(velocity);
                    currentRoute.setAvgSpeed(velocityAvg);

                    GgRepository.getInstance().daoInsertNode(createNode(location));
                    GgRepository.getInstance().updateRoute(currentRoute);
                }
            }
            previousLocation = location;
        }
    }

    private Node createNode(Location location) {
        // node and route database _ids are intentionally 0
        return new Node(0, location.getLatitude(), location.getLongitude(),
                location.getSpeed(), nodeIndex++, routeID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GgRepository.getInstance().markLastNode();
    }
}