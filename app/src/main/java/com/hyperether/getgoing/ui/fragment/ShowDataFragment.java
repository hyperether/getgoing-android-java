package com.hyperether.getgoing.ui.fragment;

import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.BUNDLE_PARCELABLE;
import static com.hyperether.getgoing.util.Constants.DATA_DETAILS_LABEL;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.databinding.FragmentShowdataBinding;
import com.hyperether.getgoing.listeners.GgOnClickListener;
import com.hyperether.getgoing.repository.room.GgRepository;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.adapter.DbRecyclerAdapter;
import com.hyperether.getgoing.util.ProgressBarBitmap;
import com.hyperether.getgoing.viewmodel.RouteViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShowDataFragment extends Fragment implements GgOnClickListener, OnMapReadyCallback {
    private FragmentShowdataBinding binding;
    private GoogleMap mMap;
    private final List<DbRoute> routes = new ArrayList<>();
    private RecyclerView.Adapter recyclerAdapter;
    private String dataLabel;
    private boolean mapToogleDown;
    private int activityId;
    private RouteViewModel routeViewModel;
    private View rootView,map_bg;
    private SupportMapFragment mapFragment;

    private ConstraintLayout constraintLayout;

    public ShowDataFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_showdata, container, false);
//        binding.setDbRoute(GgRepository.getInstance().getLastRoute());
        rootView = binding.getRoot();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataLabel = getArguments().getString(DATA_DETAILS_LABEL);

        if (getResources().getString(R.string.walking).equals(dataLabel)) {
            activityId = ACTIVITY_WALK_ID;
        } else if (getResources().getString(R.string.running).equals(dataLabel)) {
            activityId = ACTIVITY_RUN_ID;
        } else {
            activityId = ACTIVITY_RIDE_ID;
        }



        initializeViewModel();
        initializeViews();
        populateListView();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.sd_map_view);
        mapFragment.getMapAsync(this);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mapFragment != null) {
//            getChildFragmentManager().beginTransaction().remove(mapFragment).commit();
//        }
//    }

    private void initializeViewModel() {
        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
        routeViewModel.getAllRoutes().observe(getViewLifecycleOwner(), new Observer<List<DbRoute>>() {
            @Override
            public void onChanged(List<DbRoute> dbRoutes) {
                routes.clear();
                if (dbRoutes.size() > 1) {
                    dbRoutes.remove(0); // remove 0th node
                    for (DbRoute route : dbRoutes) {
                        if (route.getActivity_id() == activityId) {
                            routes.add(route);
                        }
                    }
                }

                if (routes.size() == 0) {
                    showNoRoutesDialog();
                } else {
                    Bitmap bm = ProgressBarBitmap.getWidgetBitmap(getActivity().getApplicationContext(), routes.get(routes.size() - 1).getGoal(), routes.get(0).getLength(), 400, 400, 160, 220, 20, 0);
                    binding.setDbRoute(routes.get(routes.size() - 1));
                    binding.progress.setImageBitmap(bm);
                    binding.recyclerList.smoothScrollToPosition(routes.size() - 1);
                }

                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initializeViews() {
        binding.tvSdLabel.setText(dataLabel);
        binding.ibSdBackBtn.setOnClickListener(v -> getActivity().onBackPressed());
        binding.btnToggleMap.setOnClickListener(v -> toogleMap());
        binding.ibSdDeleteBtn.setOnClickListener(v -> deleteRoute());
        binding.mapFragmentHolder.setVisibility(View.GONE);
    }

    private void deleteRoute() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.alert_dialog_delete_route));
        dialog.setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                (DialogInterface paramDialogInterface, int paramInt) -> {
                    routeViewModel.removeRouteById(binding.getDbRoute().getId());
                    Toast.makeText(getActivity(), "Route deleted", Toast.LENGTH_SHORT).show();
                });

        dialog.setNegativeButton(getString(R.string.alert_dialog_negative_button_save_btn),
                (paramDialogInterface, paramInt) -> {
                });
        dialog.show();
    }

    private void showNoRoutesDialog() {
        new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setMessage(getResources().getString(R.string.alert_dialog_no_routes))
                .setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                        (DialogInterface paramDialogInterface, int paramInt) -> {
                            switch (activityId) {
                                case ACTIVITY_WALK_ID:
                                    SharedPref.setWalkRouteExisting(false);
                                    break;
                                case ACTIVITY_RUN_ID:
                                    SharedPref.setRunRouteExisting(false);
                                    break;
                                case ACTIVITY_RIDE_ID:
                                    SharedPref.setRideRouteExisting(false);
                                    break;
                            }
                            getActivity().onBackPressed();
                        })
                .show();
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
        DbRoute route = binding.getDbRoute();
        Log.d("Provera mapa",route.toString());
        routeViewModel.getNodeListById(route.getId()).observe(getViewLifecycleOwner(), dbNodes -> {
            if (dbNodes != null && !dbNodes.isEmpty()) {
                Iterator<DbNode> it = dbNodes.iterator();
                while (it.hasNext()) {
                    PolylineOptions pOptions = new PolylineOptions();
                    pOptions.width(10)
                            .color(getResources().getColor(R.color.light_theme_accent))
                            .geodesic(true);

                    boolean first = true;
                    DbNode node = null;
                    while (it.hasNext()) {
                        node = it.next();
                        if (first) {
                            mMap.addCircle(new CircleOptions()
                                    .center(new LatLng(node.getLatitude(), node.getLongitude()))
                                    .radius(5)
                                    .fillColor(getResources().getColor(R.color.light_theme_accent))
                                    .strokeColor(getResources().getColor(R.color.transparent_light_theme_accent))
                                    .strokeWidth(20));
                            first = false;
                        }
                        pOptions.add(new LatLng(node.getLatitude(), node.getLongitude()));
                        if (node.isLast()) break;
                    }

                    mMap.addPolyline(pOptions);
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(node.getLatitude(), node.getLongitude()))
                            .radius(5)
                            .fillColor(getResources().getColor(R.color.light_theme_accent))
                            .strokeColor(getResources().getColor(R.color.transparent_light_theme_accent))
                            .strokeWidth(20));
                }

                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(dbNodes.get(0).getLatitude(), dbNodes.get(0).getLongitude()))
                        .radius(5)
                        .fillColor(getResources().getColor(R.color.light_theme_accent))
                        .strokeColor(getResources().getColor(R.color.transparent_light_theme_accent))
                        .strokeWidth(20));
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(dbNodes.get(dbNodes.size() - 1).getLatitude(), dbNodes.get(dbNodes.size() - 1).getLongitude()))
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

        // find route center point
        LatLng center = builder.build().getCenter();
        // zoom over center
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 16));
    }

    /**
     * This method is for populating list view
     */
    private void populateListView() {
        recyclerAdapter = new DbRecyclerAdapter(this, routes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerList.setLayoutManager(linearLayoutManager);
        binding.recyclerList.setAdapter(recyclerAdapter);
    }

    @Override
    public void onClick(Bundle bundle) {
        DbRoute route = bundle.getParcelable(BUNDLE_PARCELABLE);
        binding.setDbRoute(route);
        if (route != null) {
            Bitmap bm = ProgressBarBitmap.getWidgetBitmap(getActivity().getApplicationContext(), route.getGoal(), route.getLength(), 400, 400, 160, 220, 20, 0);
            binding.progress.setImageBitmap(bm);
            drawSavedRoute();
            binding.progress.setVisibility(View.GONE);
            binding.goalImg.setVisibility(View.GONE);
            binding.mapFragmentHolder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
