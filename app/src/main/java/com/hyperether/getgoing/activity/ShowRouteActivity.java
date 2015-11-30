package com.hyperether.getgoing.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;

import java.util.Iterator;
import java.util.List;

public class ShowRouteActivity extends FragmentActivity {

    private GoogleMap mMap;

    private EditText showTime, showCalories, showDistance;

    private GetGoingDataSource datasource;

    private List<DbNode> nodes;
    private DbRoute route;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_route);

        datasource = new GetGoingDataSource(this);
        datasource.open();

        showTime = (EditText) findViewById(R.id.showTime);
        showCalories = (EditText) findViewById(R.id.showCalories);
        showDistance = (EditText) findViewById(R.id.showDistance);

        mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.show_map_page)).getMap();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Long route_id =
                    extras.getLong("ROUTE_ID"); // get the id of the route that should be drawn

            route = datasource.getRoute(route_id); // get the route
            nodes = datasource.getRouteNodes(route_id);    // Get all nodes for this route

            // Show the general values for the current route
            showTime.setText(String.format(getDurationString(route.getDuration() / 1000)));
            showCalories.setText(String.format("%.02f kcal", route.getEnergy()));
            showDistance.setText(String.format("%.02f m", route.getLength()));

            // Go to the position of the first node
            if (nodes.size() != 0) { // if list of nodes empty skip the route drawing
                showLocation(nodes.get(0).getLatitude(), nodes.get(0).getLongitude());
                drawRoute(nodes); // draw the route obtained from database
            }
        }
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        datasource.close();
        super.onStop();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    /*
     * This function shows actual coordinates on map
     */
    public void showLocation(double dLat, double dLong) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLat, dLong), 14));
    }

    /*
     * Drawing the route
     * */
    private void drawRoute(List<DbNode> mRoute) {
        boolean drFirstPass = true;
        DbNode firstNode = null;
        DbNode secondNode = null;

        // Redraw the whole route
        Iterator<DbNode> it = mRoute.iterator();
        while (it.hasNext()) {
            if (drFirstPass) {
                firstNode = secondNode = it.next();
                drFirstPass = false;
            } else {
                firstNode = secondNode;
                secondNode = it.next();

            }
            drawSegment(firstNode, secondNode);
        }
    }

    /*
     * Drawing a segment of the route and coloring it in accordance with the speed
     * */
    private void drawSegment(DbNode firstNode, DbNode secondNode) {
        // Different speed spans are represented with different colors: green, yellow, orange, red
        if (secondNode.getVelocity() <= 1) {
            // Drawing the route.
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(0, 255, 0)));  // Green color
        } else if ((secondNode.getVelocity() > 1) && (secondNode.getVelocity() <= 2)) {
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(255, 255, 0))); // Yellow color
        } else if ((secondNode.getVelocity() > 2) && (secondNode.getVelocity() <= 3)) {
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(255, 128, 0))); // Orange color
        } else {
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                    .add(new LatLng(firstNode.getLatitude(), firstNode.getLongitude()))
                    .add(new LatLng(secondNode.getLatitude(), secondNode.getLongitude()))
                    .width(10)
                    .color(Color.rgb(255, 0, 0))); // Red color
        }
    }

    /*
     * Convert number of milliseconds into form HH:mm:ss
     * */
    private String getDurationString(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " +
                twoDigitString(seconds);
    }

    /*
     * Formatting method for time output
     * */
    private String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
