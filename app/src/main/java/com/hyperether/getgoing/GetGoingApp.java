package com.hyperether.getgoing;

import android.app.Application;


/**
 * Created by nikola on 07/07/17.
 */

public class GetGoingApp extends Application {

    private static GetGoingApp instance = null;

    public static GetGoingApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}