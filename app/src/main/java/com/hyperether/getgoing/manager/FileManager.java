package com.hyperether.getgoing.manager;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Class for file write / read operations required for app features
 *
 * @author prijic
 * @version 1.0 11/03/2015
 */
public class FileManager {

    private static final String TAG = "FileManager";

    public static String getFileStoragePath(Context context) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File mediaStorageDir = context.getExternalFilesDir(null);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        if (mediaStorageDir != null) {
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            return mediaStorageDir.getAbsolutePath();
        }
        return null;
    }
}
