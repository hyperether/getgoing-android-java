package com.hyperether.getgoing.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.db.DbNode;
import com.hyperether.getgoing.db.DbRoute;
import com.hyperether.getgoing.db.GetGoingDataSource;
import com.hyperether.getgoing.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ShowRouteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText showTime, showCalories, showDistance;

    private GetGoingDataSource datasource;

    private List<DbNode> nodes;
    private DbRoute route;

    private CallbackManager callbackManager;

    private SharePhoto routeDataSnapshot;
    private SharePhoto mapSnapshot;
    private ShareContent shareContent;
    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_route);

        datasource = new GetGoingDataSource(this);
        datasource.open();

        showTime = (EditText) findViewById(R.id.showTime);
        showCalories = (EditText) findViewById(R.id.showCalories);
        showDistance = (EditText) findViewById(R.id.showDistance);

        Button shareButton = (Button) findViewById(R.id.btnShare);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.show_map_page);
        mapFragment.getMapAsync(this);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoggedIn()) {
                    takeMapRouteDataSnapshot();
                } else {
                    callbackManager = CallbackManager.Factory.create();

                    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            takeMapRouteDataSnapshot();
                        }

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onError(FacebookException error) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Long route_id =
                    extras.getLong("ROUTE_ID"); // get the id of the route that should be drawn

            datasource.open();
            route = datasource.getRoute(route_id); // get the route
            nodes = datasource.getRouteNodes(route_id);    // Get all nodes for this route

            // Show the general values for the current route
            showTime.setText(String.format(getDurationString(Math.abs(route.getDuration() / 1000))));
            showCalories.setText(String.format("%.02f kcal", route.getEnergy()));
            showDistance.setText(String.format("%.02f m", route.getLength()));

            SharedPreferences currentSettings = getSharedPreferences(Constants.PREF_FILE, 0);
            int measureUnitId = currentSettings.getInt("measurementSystemId", Constants.METRIC);

            if (measureUnitId == 1 || measureUnitId == 2) {
                // present data in feet
                showDistance.setText(String.format("%.02f ft", route.getLength() * 3.281));
            } else {
                showDistance.setText(String.format("%.02f m", route.getLength()));
            }

            // Go to the position of the first node
            if (nodes.size() != 0) { // if list of nodes empty skip the route drawing
                showLocation(nodes.get(0).getLatitude(), nodes.get(0).getLongitude());
                drawRoute(nodes); // draw the route obtained from database
            }
            datasource.close();
        }
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

    /**
     * This function shows actual coordinates on map.
     */
    public void showLocation(double dLat, double dLong) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLat, dLong), 17));
    }

    /**
     * This method draws a route.
     */
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

    /**
     * Drawing a segment of the route and coloring it in accordance with the speed.
     *
     * @param firstNode first point of the rout
     * @param secondNode second point of the rout
     */
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

    /**
     * Convert number of milliseconds into form HH:mm:ss.
     *
     * @param seconds elapsed time in seconds
     */
    private String getDurationString(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " +
                twoDigitString(seconds);
    }

    /**
     * Formatting method for time output.
     *
     * @param number time
     */
    private String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }

    /**
     * Take snapshot of specific layout.
     *
     * @param layoutName
     */
    private void takeScreenshot(int layoutName) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + "" +
                    ".jpg";

            // create bitmap screen capture
            View v1 = findViewById(layoutName);
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //openScreenshot(imageFile);

            routeDataSnapshot = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Take snapshot of map.
     */
    private void takeMapSnapshot() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {
                    Date now = new Date();
                    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

                    String mPath = Environment.getExternalStorageDirectory().toString() +
                            "/" + "map_" + now + ".jpg";

                    File imageFile = new File(mPath);

                    FileOutputStream out = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();

                    //openScreenshot(imageFile);

                    mapSnapshot = new SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .build();

                    shareContent = new ShareMediaContent.Builder()
                            .addMedium(routeDataSnapshot)
                            .addMedium(mapSnapshot)
                            .build();

                    shareDialog = new ShareDialog(ShowRouteActivity.this);
                    shareDialog.show(shareContent, ShareDialog.Mode.AUTOMATIC);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mMap.snapshot(callback);
    }

    /**
     * Show specific picture in gallery.
     *
     * @param imageFile
     */
    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    /**
     * Take snapshot of map and specific layout.
     */
    private void takeMapRouteDataSnapshot() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                try {
                    View mView = findViewById(R.id.data);
                    mView.setDrawingCacheEnabled(true);

                    Bitmap tmpBitmap = mView.getDrawingCache();
                    Bitmap backBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), (int) (tmpBitmap
                            .getHeight() * 0.74));

                    Bitmap bmOverlay = Bitmap.createBitmap(
                            backBitmap.getWidth(), backBitmap.getHeight(),
                            backBitmap.getConfig());

                    Canvas canvas = new Canvas(bmOverlay);
                    canvas.drawBitmap(snapshot, new Matrix(), null);
                    canvas.drawBitmap(backBitmap, 0, 0, null);

                    FileOutputStream out = new FileOutputStream(
                            Environment.getExternalStorageDirectory()
                                    + "/MapScreenShot"
                                    + System.currentTimeMillis() + ".png");
                    //bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();

                    mapSnapshot = new SharePhoto.Builder()
                            .setBitmap(bmOverlay)
                            .build();

                    shareContent = new ShareMediaContent.Builder()
                            .addMedium(mapSnapshot)
                            .build();

                    shareDialog = new ShareDialog(ShowRouteActivity.this);
                    shareDialog.show(shareContent, ShareDialog.Mode.AUTOMATIC);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mMap.snapshot(callback);
    }

    /**
     * This method check if user is logged in by fb
     */
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
}
