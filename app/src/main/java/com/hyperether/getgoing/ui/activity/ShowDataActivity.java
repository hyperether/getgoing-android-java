package com.hyperether.getgoing.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.databinding.ShowDataBinding;
import com.hyperether.getgoing.listeners.GgOnClickListener;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.adapter.DbRecyclerAdapter;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.util.ProgressBarBitmap;
import com.hyperether.getgoing.viewmodel.NodeListViewModel;
import com.hyperether.getgoing.viewmodel.RouteViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.BUNDLE_PARCELABLE;
import static com.hyperether.getgoing.util.Constants.DATA_DETAILS_LABEL;


public class ShowDataActivity extends AppCompatActivity
        implements GgOnClickListener, OnMapReadyCallback {

    private ShowDataBinding binding;

    private GoogleMap mMap;
    private LatLngBounds mapBounds;

    private final List<DbRoute> routes = new ArrayList<>();
    private RecyclerView.Adapter recyclerAdapter;

    private String dataLabel;
    private boolean mapToogleDown;
    private int activityId;
    private RouteViewModel routeViewModel;
    private NodeListViewModel nodesViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.show_data);

        dataLabel = getIntent().getStringExtra(DATA_DETAILS_LABEL);

        if (getResources().getString(R.string.walking).equals(dataLabel)) {
            activityId = ACTIVITY_WALK_ID;
        } else if (getResources().getString(R.string.running).equals(dataLabel)) {
            activityId = ACTIVITY_RUN_ID;
        } else {
            activityId = ACTIVITY_RIDE_ID;
        }

        initializeViews();
        populateListView();
        initializeViewModel();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.sd_map_view);
        mapFragment.getMapAsync(this);

    }


    private void initializeViewModel() {
        routeViewModel = ViewModelProviders.of(this).get(RouteViewModel.class);
        routeViewModel.getRouteList().observe(this, routeList -> {
            routes.clear();

            Bitmap bm = null;
            if (routeList.size() > 1) {
                routeList.remove(0); // remove 0th node

                for (DbRoute route : routeList) {
                    if (route.getActivity_id() == activityId) {
                        routes.add(route);
                    }
                }

                bm = ProgressBarBitmap.getWidgetBitmap(getApplicationContext(), routes.get(0).getGoal(), routes.get(0).getLength(), 400, 400, 160, 220, 20, 0);

            } else {
                new AlertDialog.Builder(ShowDataActivity.this)
                        .setTitle(R.string.alert_dialog_empty_title)
                        .setPositiveButton(R.string.confirm,
                                (DialogInterface dialog, int whichButton) -> finish())
                        .create()
                        .show();

                bm = ProgressBarBitmap.getWidgetBitmap(getApplicationContext(), 0, 0, 400, 400, 160, 220, 20, 0);

            }

            binding.setVar(routes.get(0));
            binding.progress.setImageBitmap(bm);
            recyclerAdapter.notifyDataSetChanged();
        });
    }


    private void initializeViews() {
        binding.tvSdLabel.setText(dataLabel);
        binding.ibSdBackBtn.setOnClickListener(v -> onBackPressed());
        binding.btnToggleMap.setOnClickListener(v -> toogleMap());
        binding.mapFragmentHolder.animate().scaleYBy(-1);
    }


    private void toogleMap() {

        if (!mapToogleDown) {
            binding.btnToggleMap.animate().rotationBy(180).setDuration(500);
            mapToogleDown = true;
            binding.mapFragmentHolder.animate().scaleYBy(1).setStartDelay(200).setDuration(500);
            binding.displayMap.animate().y((int) ((70 * (Resources.getSystem().getDisplayMetrics().density)))).setDuration(500);
            drawSavedRoute();
        } else {
            binding.btnToggleMap.animate().rotationBy(180).setDuration(500);
            mapToogleDown = false;
            binding.mapFragmentHolder.animate().scaleYBy(-1).setDuration(500);
            binding.displayMap.animate().translationY(0).setStartDelay(200).setDuration(500);
        }
    }

    private void drawSavedRoute() {

        mMap.clear();

        DbRoute route = binding.getVar();

        nodesViewModel = ViewModelProviders.of(this).get(NodeListViewModel.class);

        nodesViewModel.getNodeListById(route.getId())
                .observe(this, dbNodes -> {

                    PolylineOptions pOptions = new PolylineOptions();
                    pOptions.width(10)
                            .color(getResources().getColor(R.color.light_theme_accent))
                            .geodesic(true);

                    if (!dbNodes.isEmpty()) {

                        for (DbNode node : dbNodes) {
                            pOptions.add(new LatLng(node.getLatitude(), node.getLongitude()));
                        }

                        mMap.addPolyline(pOptions);
                        mMap.addCircle(new CircleOptions()
                                .center(new LatLng(dbNodes.get(0).getLatitude(), dbNodes.get(0).getLongitude()))
                                .radius(5)
                                .fillColor(getResources().getColor(R.color.light_theme_accent))
                                .strokeColor(getResources().getColor(R.color.transparent_light_theme_accent))
                                .strokeWidth(20));
                        mMap.addCircle(new CircleOptions()
                                .center(new LatLng(dbNodes.get(dbNodes.size()-1).getLatitude(), dbNodes.get(dbNodes.size()-1).getLongitude()))
                                .radius(5)
                                .fillColor(getResources().getColor(R.color.light_theme_accent))
                                .strokeColor(getResources().getColor(R.color.transparent_light_theme_accent))
                                .strokeWidth(20));



                        setCameraView(dbNodes);
                    }

                });


    }

    private void setCameraView(List<DbNode> routeNodes) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (DbNode node : routeNodes) {
            builder.include(new LatLng(node.getLatitude(), node.getLongitude()));
        }
        mapBounds = builder.build();

        // TODO - add padding/offset to bounds - Ivana
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngBounds(mapBounds, 1));


    }

    /**
     * This method is for populating list view
     */
    private void populateListView() {
        recyclerAdapter = new DbRecyclerAdapter(this, routes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerList.setLayoutManager(linearLayoutManager);
        binding.recyclerList.setAdapter(recyclerAdapter);
    }


    @Override
    public void onClick(Bundle bundle) {
        DbRoute route = bundle.getParcelable(BUNDLE_PARCELABLE);
        binding.setVar(route);

        if (route != null) {
            Bitmap bm = ProgressBarBitmap.getWidgetBitmap(getApplicationContext(), route.getGoal(), route.getLength(), 400, 400, 160, 220, 20, 0);
            binding.progress.setImageBitmap(bm);
            drawSavedRoute();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    this.mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

}
