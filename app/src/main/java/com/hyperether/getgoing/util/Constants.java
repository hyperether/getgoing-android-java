package com.hyperether.getgoing.util;

/**
 * Created by nikola on 10/07/17.
 */

public class Constants {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 1;

    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;

    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    public static final float LOCATION_DISTANCE = 5;

    // Preference file
    public static final String PREF_FILE = "CBUserDataPref.txt";
    public static final String PREF_WALK_ROUTE_EXISTING = "walk_route_existing";
    public static final String PREF_RUN_ROUTE_EXISTING = "run_route_existing";
    public static final String PREF_RIDE_ROUTE_EXISTING = "ride_route_existing";

    public static final int NODE_ADD_DISTANCE = 10;

    //Permission request TAG
    public static final int TAG_CODE_PERMISSION_LOCATION = 1;

    public static final int METRIC = 0;

    // Requets TAG
    public static final int REQUEST_GPS_SETTINGS = 100;

    //ActivitiesFragment
    public static final float AVG_SPEED_WALK = (float) 1.5;
    public static final float AVG_SPEED_RUN = (float) 2.5;
    public static final float AVG_SPEED_CYCLING = 5;

    public static final int CONST_LOW_DIST = 2500;
    public static final int CONST_MEDIUM_DIST = 5000;
    public static final int CONST_HIGH_DIST = 7500;

    //ProfileFragment
    public enum gender {Male, Female, Other}

    // Intent extras
    public static final String DATA_DETAILS_LABEL = "data_details_label";
    public static final String BUNDLE_PARCELABLE = "bundle_parcelable";

    // activities IDs
    public static final String TRACKING_ACTIVITY_KEY = "tracking_activity";
    public static final int ACTIVITY_WALK_ID = 1;
    public static final int ACTIVITY_RUN_ID = 2;
    public static final int ACTIVITY_RIDE_ID = 3;

    public static final int OPENED_FROM_LOCATION_ACT = 501;
    public static final int OPENED_FROM_GG_ACT = 502;
    public static final String OPENED_FROM_KEY = "from";
}
