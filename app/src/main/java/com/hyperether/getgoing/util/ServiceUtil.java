package com.hyperether.getgoing.util;

import android.app.ActivityManager;
import android.content.Context;

import com.hyperether.getgoing.service.GPSTrackingService;

import java.util.List;

public class ServiceUtil {
    public static boolean isServiceActive(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(GPSTrackingService.class.getName())) {
                return true;
            }
        }
        return false;
    }
}
