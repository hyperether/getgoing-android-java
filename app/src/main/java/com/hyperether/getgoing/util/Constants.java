package com.hyperether.getgoing.util;

/**
 * Created by nikola on 10/07/17.
 */

public class Constants {

    // Global constants
    /*
     * Define a request code to send to Google Play services
	 * This code is returned in Activity.onActivityResult
	 */
    public final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // The fastest update frequency, in seconds
    public static final int FASTEST_INTERVAL_IN_SECONDS = 5;

    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Preference file
    public static final String PREF_FILE = "CBUserDataPref.txt";

    public static final int NODE_ADD_DISTANCE = 10;

    public static final int REQUEST_RESOLVE_ERROR = 1001;

    //Permission request TAG
    public static final int TAG_CODE_PERMISSION_LOCATION = 1;

    public static final int RESULT_REQUESTED = 1;

    public static final int METRIC = 0;

    // Number picker
    public static final int NUMBER_PICKER_MAX_VALUE = 150;
    public static final int NUMBER_PICKER_DEFAULT_WEIGHT = 60;
    public static final int NUMBER_PICKER_DEFAULT_AGE = 20;
    public static final int NUMBER_PICKER_MIN_VALUE = 0;
    public static final int NUMBER_PICKER_VALUE_SIZE = 151;

    // Requets TAG
    public static final int REQUEST_GPS_SETTINGS = 100;
}
