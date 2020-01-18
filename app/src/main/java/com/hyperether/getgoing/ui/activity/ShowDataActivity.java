package com.hyperether.getgoing.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.databinding.ShowDataBinding;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.adapter.DbRecyclerAdapter;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.ProgressBarBitmap;
import com.hyperether.getgoing.viewmodel.RouteViewModel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static com.hyperether.getgoing.util.Constants.BUNDLE_PARCELABLE;
import static com.hyperether.getgoing.util.Constants.DATA_DETAILS_LABEL;


// TODO remove unused code
public class ShowDataActivity extends AppCompatActivity
        implements DbHelper.OnDataLoadListener, OnListItemClick, OnMapReadyCallback {

    private ShowDataBinding binding;

    private GoogleMap mMap;

    private final List<DbRoute> routes = new ArrayList<>();
    private RecyclerView.Adapter recyclerAdapter;
//    private RecyclerView recyclerView;
//    private ProgressBar progress;

    private String dataLabel;
//    private TextView tvLabel;
//    private ImageButton btnBack, btnToggleMap;
//    private ConstraintLayout mapView, chartView;

    private boolean mapToogleDown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.show_data);
//        getSupportActionBar().show();

        dataLabel = getIntent().getStringExtra(DATA_DETAILS_LABEL);

        initializeViews();
        populateListView();
//        populateChart();

//        DbHelper.getInstance(this).populateRoutes(routes, this);

        RouteViewModel model = ViewModelProviders.of(this).get(RouteViewModel.class);
        model.getRouteList().observe(this, routeList -> {
            // update UI
            routes.clear();
            // TODO implement size check, or method for getting last 7 records from Db - Ivana
//            if(routeList.size() >= 8) {
//                routes.addAll(routeList.subList(routeList.size() - 8, routeList.size() - 1));
//            } else {
                routes.addAll(routeList);
//            }

            binding.setVar(routes.get(0));
            Bitmap bm = ProgressBarBitmap.getWidgetBitmap(getApplicationContext(), 50, 800, 800, 50, 0);
            binding.progress.setImageBitmap(bm);
            // TODO if getting all records, remove 0th Node - Ivana
            routes.remove(0);
            recyclerAdapter.notifyDataSetChanged();
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.sd_map_view);
        mapFragment.getMapAsync(this);


    }


    private void initializeViews() {
//        progress = findViewById(R.id.progress);
//        binding.progress.setVisibility(View.GONE);
//        tvLabel = findViewById(R.id.tv_sd_label);
//        btnBack = findViewById(R.id.ib_sd_back_btn);
//        btnToggleMap = findViewById(R.id.btn_toggle_map);
//        mapView = findViewById(R.id.sd_map_bg);
//        chartView = findViewById(R.id.sd_chart_layout);

        binding.tvSdLabel.setText(dataLabel);
        binding.ibSdBackBtn.setOnClickListener(v -> onBackPressed());
        binding.btnToggleMap.setOnClickListener(v -> toogleMap());

        binding.mapFragmentHolder.animate().scaleYBy(-1);

    }



    // TODO translation values for different screen sizes??
    private void toogleMap() {

        if(!mapToogleDown) {
//            binding.btnToggleMap.setImageResource(R.drawable.ic_gray_angle_down_icon);
            binding.btnToggleMap.animate().rotationBy(180).setDuration(500);
            mapToogleDown = true;
            binding.mapFragmentHolder.animate().scaleYBy(1).setStartDelay(200).setDuration(500);
            binding.displayMap.animate().y((int)((70*(Resources.getSystem().getDisplayMetrics().density)))).setDuration(500);
//            binding.progress.animate().alpha(0).setDuration(800);
//            binding.mapFragmentHolder.animate().translationYBy(-150).setDuration(500);
//            binding.mapBg.animate().translationYBy(-150).setDuration(500);
        } else {
//            binding.btnToggleMap.setImageResource(R.drawable.ic_gray_angle_up_icon);
            binding.btnToggleMap.animate().rotationBy(180).setDuration(500);
            mapToogleDown = false;
            binding.mapFragmentHolder.animate().scaleYBy(-1).setDuration(500);
            binding.displayMap.animate().translationY(0).setDuration(500);
//            binding.progress.animate().alpha(1).setDuration(400);
//            binding.mapFragmentHolder.animate().translationY(0).setDuration(500);
//            binding.mapBg.animate().translationY(0).setDuration(500);
        }
    }

    /**
     * This method is for populating list view
     */
    private void populateListView() {
//        recyclerView = findViewById(R.id.recyclerList);
        recyclerAdapter = new DbRecyclerAdapter(this, routes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerList.setLayoutManager(linearLayoutManager);
        binding.recyclerList.setAdapter(recyclerAdapter);
    }

    @Override
    public void onLoad() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!routes.isEmpty()) {
                    populateListView();
//                    populateChart();
//                    binding.progress.setVisibility(View.GONE);
                } else {
                    new AlertDialog.Builder(ShowDataActivity.this)
                            .setTitle(R.string.alert_dialog_empty_title)
                            .setPositiveButton(R.string.confirm,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            finish();
                                        }
                                    }).create().show();
                }

            }
        });
    }

//    private ArrayList<String> xValuesChart() {
//        LinkedHashSet<String> charDatesSet = new LinkedHashSet<>();
//        ArrayList<String> charDates = new ArrayList<>();
//        for (int i = 0; i < routes.size(); i++) {
//            charDatesSet.add(routes.get(i).getDate().substring(0, 10));
//        }
//        charDates.addAll(charDatesSet);
//        return charDates;
//    }

    private ArrayList<Map<String, Float>> collectData() {
        ArrayList<Map<String, Float>> collectedData = new ArrayList<>();
        Map<String, Float> mapY1 = new LinkedHashMap<>();
        Map<String, Float> mapY2 = new LinkedHashMap<>();
        Map<String, Float> mapY3 = new LinkedHashMap<>();

        for (DbRoute item : routes) {
            if (mapY1.containsKey(item.getDate().substring(0, 10)) && item.getActivity_id() == 1) {
                float q = mapY1.get(item.getDate().substring(0, 10)) + (float) item.getEnergy();
                mapY1.put(item.getDate().substring(0, 10), q);
            } else if (item.getActivity_id() == 1) {
                mapY1.put(item.getDate().substring(0, 10), (float) item.getEnergy());
            } else if (mapY2.containsKey(item.getDate().substring(0, 10)) &&
                    item.getActivity_id() == 2) {
                float q = mapY2.get(item.getDate().substring(0, 10)) + (float) item.getEnergy();
                mapY2.put(item.getDate().substring(0, 10), q);
            } else if (item.getActivity_id() == 2) {
                mapY2.put(item.getDate().substring(0, 10), (float) item.getEnergy());
            } else if (mapY3.containsKey(item.getDate().substring(0, 10)) &&
                    item.getActivity_id() == 3) {
                float q = mapY3.get(item.getDate().substring(0, 10)) + (float) item.getEnergy();
                mapY3.put(item.getDate().substring(0, 10), q);
            } else if (item.getActivity_id() == 3) {
                mapY3.put(item.getDate().substring(0, 10), (float) item.getEnergy());
            }
        }
        collectedData.add(mapY1);
        collectedData.add(mapY2);
        collectedData.add(mapY3);
        return collectedData;
    }

//    private ArrayList<IBarDataSet> prepareData() {
//        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
//        ArrayList<BarEntry> yVal1 = new ArrayList<>();
//        ArrayList<BarEntry> yVal2 = new ArrayList<>();
//        ArrayList<BarEntry> yVal3 = new ArrayList<>();
//        ArrayList<String> x = xValuesChart();
//        Map<String, Float> y1 = collectData().get(0);
//        Map<String, Float> y2 = collectData().get(1);
//        Map<String, Float> y3 = collectData().get(2);
//        String xVal;
//        float emptyValue = 0;
//
//        for (int i = 0; i < x.size(); i++) {
//            xVal = x.get(i);
//            if (y1.containsKey(xVal)) {
//                yVal1.add(new BarEntry(y1.get(xVal), i));
//            } else {
//                yVal1.add(new BarEntry(emptyValue, i));
//            }
//            if (y2.containsKey(xVal)) {
//                yVal2.add(new BarEntry(y2.get(xVal), i));
//            } else {
//                yVal2.add(new BarEntry(emptyValue, i));
//            }
//            if (y3.containsKey(xVal)) {
//                yVal3.add(new BarEntry(y3.get(xVal), i));
//            } else {
//                yVal3.add(new BarEntry(emptyValue, i));
//            }
//        }
//
//        BarDataSet set1 = new BarDataSet(yVal1, getString(R.string.chart_label_walk));
//        BarDataSet set2 = new BarDataSet(yVal2, getString(R.string.chart_label_run));
//        BarDataSet set3 = new BarDataSet(yVal3, getString(R.string.chart_label_ride));
//        set1.setColor(getResources().getColor(R.color.walk_graph_color));
//        set1.setBarShadowColor(getResources().getColor(R.color.walk_shadow));
//        set1.setValueTextSize(10f);
//        set2.setColor(getResources().getColor(R.color.run_graph_color));
//        set2.setBarShadowColor(getResources().getColor(R.color.run_shadow));
//        set2.setValueTextSize(10f);
//        set3.setColor(getResources().getColor(R.color.ride_graph_color));
//        set3.setBarShadowColor(getResources().getColor(R.color.ride_shadow));
//        set3.setValueTextSize(10f);
//        dataSets.add(set1);
//        dataSets.add(set2);
//        dataSets.add(set3);
//
//        return dataSets;
//    }

    @Override
    public void onListItemClick(Bundle bundle) {
        DbRoute route = bundle.getParcelable(BUNDLE_PARCELABLE);
        binding.setVar(route);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.mMap = googleMap;
            initMapComponents(googleMap);

            LocationManager locationManager = (LocationManager) getSystemService(
                    Context.LOCATION_SERVICE);

            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setCancelable(false);
                dialog.setTitle(R.string.alert_dialog_title);
                dialog.setMessage(getString(R.string.alert_dialog_message));
                dialog.setPositiveButton(R.string.alert_dialog_positive_button, new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub
                        openGPSSettings();
                    }
                });

                dialog.setNegativeButton(R.string.alert_dialog_negative_button, (paramDialogInterface, paramInt) -> finish());

                dialog.show();
            }

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String bestProvider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            zoomOverCurrentLocation(mMap, location);
        } else {
            finish();
        }
    }
    /**
     * This method is used for init of map components
     *
     * @param googleMap google map v2
     **/
    private void initMapComponents(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                        .ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            googleMap.setMyLocationEnabled(true);
            googleMap.setTrafficEnabled(false);
            googleMap.setIndoorEnabled(true);
            googleMap.setBuildingsEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    /**
     * This method is used for zooming over user current location or last known location.
     *
     * @param googleMap google map v2
     **/
    private void zoomOverCurrentLocation(GoogleMap googleMap, Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    /**
     * Method to open settings.
     */
    public void openGPSSettings() {
        Intent i = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(i, Constants.REQUEST_GPS_SETTINGS);
    }


    /* delete this */
//    private void populateChart() {
//        BarData data = new BarData(xValuesChart(), prepareData());
//        chart.setData(data);
//        chart.moveViewToX(xValuesChart().size());
//        chart.setDescription("");
//        chart.getAxisRight().setEnabled(false);
//        chart.getAxisLeft().setDrawGridLines(false);
//        chart.animateY(2000, Easing.EasingOption.Linear);
//        chart.setVisibleXRangeMaximum(8);
//        chart.setDrawBarShadow(true);
//        chart.getAxisLeft().setAxisMinValue(0f);
//    }

}
