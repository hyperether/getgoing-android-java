package com.hyperether.getgoing.util;

import android.widget.Chronometer;

public class TimeUtils {
    public static long chronometerToMills(Chronometer mChronometer) {
        Integer currentMilliseconds = 0;
        String chronoText = mChronometer.getText().toString();
        String array[] = chronoText.split(":");
        if (array.length == 2) {
            currentMilliseconds = Integer.parseInt(array[0]) * 60 * 1000
                    + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            currentMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
                    + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }
        return currentMilliseconds;
    }

    public static int[] getTimeEstimates(int dist) {
        int[] returnValues = new int[3];

        returnValues[0] = (int) (dist / (Constants.AVG_SPEED_WALK * 60));
        returnValues[1] = (int) (dist / (Constants.AVG_SPEED_RUN * 60));
        returnValues[2] = (int) (dist / (Constants.AVG_SPEED_CYCLING * 60));

        return returnValues;
    }
}
