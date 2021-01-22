package com.hyperether.getgoing;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by nikola on 07/07/17.
 */

public class GetGoingApp extends Application {

    private static GetGoingApp instance = null;
    private Handler handler = null;

    public static GetGoingApp getInstance() {
        return instance;
    }

    public Handler getHandler() {
        if (handler == null) {
            HandlerThread thread = new HandlerThread("db-thread");
            thread.start();
            handler = new Handler(thread.getLooper());
        }
        return handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}