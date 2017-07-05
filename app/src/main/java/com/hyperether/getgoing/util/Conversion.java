package com.hyperether.getgoing.util;

public class Conversion {
    /**
     * Convert number of milliseconds into form HH:mm:ss
     *
     * @param seconds elapsed time
     * */
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
     * */
    public static String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
