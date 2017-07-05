package com.hyperether.getgoing.location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class LocationManagerHandler implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    public boolean locationUpdated = false;
    private boolean userCancelledGPS = false;

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public LocationManagerHandler(Context context) {
        this.mContext = context;
        this.canGetLocation = getLocationServicesInfo();
    }

    public boolean getLocationServicesInfo() {
        boolean locationServicesAvailable = false;
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            if (!userCancelledGPS) {
                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);
            }

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                locationServicesAvailable = true;
            }

            // if GPS Enabled get lat/long using GPS Services
            if (!userCancelledGPS) {
                if (!isGPSEnabled) {
                    this.showSettingsAlert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return locationServicesAvailable;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show Location service alert dialog On pressing Settings button will lunch
     * Settings Options On pressing Enter location will lunch input Text dialog
     */
    public void showSettingsAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Location service");

        alertDialog.setIcon(null);
        // alertDialog.setView(getCurrentFocus());
        // Setting Dialog Message
        alertDialog
                .setMessage(
                        "Looks like you have GPS turned off. Turn it on in Settings, or cancel.");// or
        // enter
        // location.");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // On pressing Enter location button
        /*
         * alertDialog.setNeutralButton("Enter location", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog,int which) {
		 * //String value = input.getText().toString(); }
		 * 
		 * });
		 */
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userCancelledGPS = true;
                        isGPSEnabled = false;
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        locationUpdated = true;
    }
}

