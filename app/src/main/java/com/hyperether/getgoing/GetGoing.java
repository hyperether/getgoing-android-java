package com.hyperether.getgoing;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.crashlytics.android.Crashlytics;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;
import com.hyperether.getgoing.location.ShowData;
import com.hyperether.getgoing.location.ShowLocation;
import com.hyperether.getgoing.settings.SettingsActivity;
import com.hyperether.getgoing.util.FragmentDialog;

import io.fabric.sdk.android.Fabric;
import java.util.ArrayList;
import java.util.List;

public class GetGoing extends Activity {
    private static final int WALK_ID = 1;
    private static final int RUN_ID = 2;
    private static final int RIDE_ID = 3;
    private static final int METRIC = 0;
    //private static final int IMPERIAL = 1;
    //private static final int US = 2;
    private static final int RESULT_REQUESTED = 1;
    public static final String PREF_FILE = "CBUserDataPref.txt";
    private CBDataFrame cbDataFrameLocal;

    // Database access variables
    private GetGoingDataSource datasource;
    private List<DbRoute> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        addButtonListener();

        cbDataFrameLocal = new CBDataFrame();

        SharedPreferences currentSettings = getSharedPreferences(PREF_FILE, 0);

		/*
         * default value is metric
		 */
        int measureUnitId = currentSettings.getInt("measurementSystemId", METRIC);
        cbDataFrameLocal.setMeasurementSystemId(measureUnitId);
        int age = currentSettings.getInt("age", 0);
        cbDataFrameLocal.setAge(age);
        int weight = currentSettings.getInt("weight", 0);
        cbDataFrameLocal.setWeight(weight);
    }

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
        getMenuInflater().inflate(R.menu.get_going, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            callSettingsActivity();
            return true;
        } else if (itemId == R.id.action_stats) {
            routes = new ArrayList<DbRoute>();
            // Initialize database connection
            datasource = new GetGoingDataSource(this);
            datasource.open();
            routes = datasource.getAllRoutes(); // Get the list of all routes from database
            datasource.close();

            if (routes.isEmpty()) {
                FragmentDialog dialog = new FragmentDialog();
                dialog.show(getFragmentManager(), "Data set empty");
            } else {
                Intent intent = new Intent(GetGoing.this, ShowData.class);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (RESULT_REQUESTED):
                if (resultCode == Activity.RESULT_OK) {
                    if (data.hasExtra("dataKey")) {
                        this.cbDataFrameLocal = data.getParcelableExtra("dataKey");
                        SharedPreferences settings = getSharedPreferences(
                                PREF_FILE, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("measurementSystemId",
                                this.cbDataFrameLocal.getMeasurementSystemId());
                        editor.putInt("age", this.cbDataFrameLocal.getAge());
                        editor.putInt("weight", this.cbDataFrameLocal.getWeight());
                        editor.commit();

                        int id = settings.getInt("meteringActivityRequestedId", 0);
                        if (id > 0) {
                            callMeteringActivity(id);
                        }
                    }
                } else { // back option button pressed

                }
                break;
        }
    }

    /*
     * true: parameters are set false: settings required
     */
    private boolean getParametersStatus(CBDataFrame cbDataFrameLocal) {
        if ((cbDataFrameLocal.getAge() == 0)
                || (cbDataFrameLocal.getWeight() == 0)) {
            return false;
        }
        return true;
    }

    private void callSettingsActivity() {
        Intent intent = new Intent(GetGoing.this, SettingsActivity.class);
        intent.putExtra("searchKey", this.cbDataFrameLocal);
        startActivityForResult(intent, RESULT_REQUESTED);
    }

    private void callMeteringActivity(int id) {
        if (getParametersStatus(this.cbDataFrameLocal)) {
            setMeteringActivityRequested(0);
            this.cbDataFrameLocal.setProfileId(id);
            Intent intent = new Intent(GetGoing.this, ShowLocation.class);
            intent.putExtra("searchKey", this.cbDataFrameLocal);
            startActivity(intent);
        } else {
            setMeteringActivityRequested(id);
            callSettingsActivity();
        }
    }

    private void setMeteringActivityRequested(int id) {
        SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("meteringActivityRequestedId", id);
        editor.commit();
    }
}
