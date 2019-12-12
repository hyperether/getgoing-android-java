package com.hyperether.getgoing.util;

import android.content.Context;

public class Conversion {
    /**
     * Convert number of milliseconds into form HH:mm:ss
     *
     * @param seconds elapsed time
     */
    public static String getDurationString(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " +
                twoDigitString(seconds);
    }

    /**
     * Formatting method for time output
     *
     * @param number time
     */
    public static String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
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
    public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
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

    public static int convertDpToPixel(float dp, Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int convertPixelToDp(Context context, float pxValue) {
        final float scale =  context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
