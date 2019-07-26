package com.hyperether.getgoing.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.crashlytics.android.Crashlytics;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.manager.CacheManager;
import com.hyperether.getgoing.ui.fragment.SettingsFragment;
import com.hyperether.getgoing.util.Constants;

import io.fabric.sdk.android.Fabric;

import static com.hyperether.getgoing.ui.fragment.SettingsFragment.DATA_KEY;

public class GetGoingActivity extends Activity {

    private static final int WALK_ID = 1;
    private static final int RUN_ID = 2;
    private static final int RIDE_ID = 3;
    //private static final int IMPERIAL = 1;
    //private static final int US = 2;

    private CBDataFrame cbDataFrameLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        if (getActionBar() != null) {
            getActionBar().setTitle("");
        }
        addButtonListener();

        cbDataFrameLocal = new CBDataFrame();

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, Constants
                .TAG_CODE_PERMISSION_LOCATION);

        SharedPreferences currentSettings = getSharedPreferences(Constants.PREF_FILE, 0);

        /*
         * default value is metric
         */
        int measureUnitId = currentSettings.getInt("measurementSystemId", Constants.METRIC);
        cbDataFrameLocal.setMeasurementSystemId(measureUnitId);
        int age = currentSettings.getInt("age", 0);
        cbDataFrameLocal.setAge(age);
        int weight = currentSettings.getInt("weight", 0);
        cbDataFrameLocal.setWeight(weight);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.TAG_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                break;
        }
    }

    /**
     * This method is used for handling button clicks.
     */
    public void addButtonListener() {
        ImageButton buttonWalk;
        buttonWalk = (ImageButton) findViewById(R.id.walk_button);
        buttonWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callMeteringActivity(WALK_ID);
            }
        });

        ImageButton buttonRide;
        buttonRide = (ImageButton) findViewById(R.id.ride_button);
        buttonRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callMeteringActivity(RIDE_ID);
            }
        });

        ImageButton buttonRun;
        buttonRun = (ImageButton) findViewById(R.id.run_button);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callMeteringActivity(RUN_ID);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {
            callSettingsFragment();
            return true;
        } else if (itemId == R.id.action_stats) {
            Intent intent = new Intent(GetGoingActivity.this, ShowDataActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (Constants.RESULT_REQUESTED):
                if (resultCode == Activity.RESULT_OK) {
                    if (data.hasExtra(DATA_KEY)) {
                        this.cbDataFrameLocal = data.getParcelableExtra(DATA_KEY);
                        SharedPreferences settings = getSharedPreferences(
                                Constants.PREF_FILE, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("measurementSystemId",
                                this.cbDataFrameLocal.getMeasurementSystemId());
                        editor.putInt("age", this.cbDataFrameLocal.getAge());
                        editor.putInt("weight", this.cbDataFrameLocal.getWeight());
                        editor.apply();

                        int id = settings.getInt("meteringActivityRequestedId", 0);
                        if (id > 0) {
                            callMeteringActivity(id);
                        }
                    }
                }
                break;
        }
    }

    /*
     * true: parameters are set false: settings required
     */
    private boolean getParametersStatus(CBDataFrame cbDataFrameLocal) {
        return !((cbDataFrameLocal.getAge() == 0)
                || (cbDataFrameLocal.getWeight() == 0));
    }

    /**
     * This method starts SettingsFragment
     */
    private void callSettingsFragment() {
        SettingsFragment settingsFragment = SettingsFragment.newInstance(this.cbDataFrameLocal);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_main, settingsFragment)
                .commit();
//        Intent intent = new Intent(GetGoingActivity.this, SettingsFragment.class);
//        intent.putExtra("searchKey", this.cbDataFrameLocal);
//        startActivityForResult(intent, Constants.RESULT_REQUESTED);
    }

    /**
     * This method starts SettingsFragment
     *
     * @param id mode id
     */
    private void callMeteringActivity(int id) {
        if (getParametersStatus(this.cbDataFrameLocal)) {
            setMeteringActivityRequested(0);
            this.cbDataFrameLocal.setProfileId(id);
            Intent intent = new Intent(GetGoingActivity.this, ShowLocationActivity.class);
            CacheManager.getInstance().setObDataFrameLocal(this.cbDataFrameLocal);
            intent.putExtra("searchKey", this.cbDataFrameLocal);
            startActivity(intent);
        } else {
            setMeteringActivityRequested(id);
            callSettingsFragment();
        }
    }

    /**
     * This method set Metering Activity request.
     *
     * @param id mode id
     */
    private void setMeteringActivityRequested(int id) {
        SharedPreferences settings = getSharedPreferences(Constants.PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("meteringActivityRequestedId", id);
        editor.apply();
    }
}
