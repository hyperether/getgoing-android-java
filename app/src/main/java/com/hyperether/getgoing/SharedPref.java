package com.hyperether.getgoing;

import android.content.SharedPreferences;

import com.hyperether.getgoing.util.Constants;

import static com.hyperether.getgoing.util.Constants.PREF_RIDE_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_RUN_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_WALK_ROUTE_EXISTING;

public class SharedPref {

    private static SharedPreferences settings = GetGoingApp.getInstance().getSharedPreferences(Constants.PREF_FILE, 0);

    public static void setGender(int gender) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("gender", gender);
        editor.apply();
    }

    public static int getGender() {
        return settings.getInt("gender", 0);
    }

    public static void setAge(int age) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("age", age);
        editor.apply();
    }

    public static int getAge() {
        return settings.getInt("age", 0);
    }

    public static void setWeight(int weight) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("weight", weight);
        editor.apply();
    }

    public static int getWeight() {
        return settings.getInt("weight", 0);
    }

    public static void setHeight(int height) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("height", height);
        editor.apply();
    }

    public static int getHeight() {
        return settings.getInt("height", 0);
    }

    public static boolean isGenderSet() {
        return settings.contains("gender");
    }

    public static boolean isGoalSet() {
        return settings.contains("goal");
    }

    public static void setGoal(int goal) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("goal", goal);
        editor.apply();
    }

    public static int getGoal() {
        return settings.getInt("goal", 5000);
    }

    public static void setWalkRouteExisting(boolean walk) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_WALK_ROUTE_EXISTING, walk);
        editor.apply();
    }

    public static boolean doesWalkRouteExist() {
        return settings.getBoolean(PREF_WALK_ROUTE_EXISTING, false);
    }

    public static void setRunRouteExisting(boolean run) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_RUN_ROUTE_EXISTING, run);
        editor.apply();
    }

    public static boolean doesRunRouteExist() {
        return settings.getBoolean(PREF_RUN_ROUTE_EXISTING, false);
    }

    public static void setRideRouteExisting(boolean ride) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_RIDE_ROUTE_EXISTING, ride);
        editor.apply();
    }

    public static boolean doesRideRouteExist() {
        return settings.getBoolean(PREF_RIDE_ROUTE_EXISTING, false);
    }

    public static boolean isZeroNodeInit() {
        return settings.getBoolean("zeroNode", false);
    }

    public static void setZeroNodeInit(boolean zeroNode) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("zeroNode", zeroNode);
        editor.apply();
    }

    public static int getMeasurementSystemId() {
        return settings.getInt("measurementSystemId", Constants.METRIC);
    }

    public static void setMeasurementSystemId(int id) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("meteringActivityRequestedId", id);
        editor.apply();
    }
}
