package com.hyperether.getgoing.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
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
import com.hyperether.getgoing.databinding.ShowDataBinding;
import com.hyperether.getgoing.listeners.GgOnClickListener;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.entity.DbNode;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.ui.activity.ShowDataActivity;
import com.hyperether.getgoing.ui.adapter.DbRecyclerAdapter;
import com.hyperether.getgoing.util.ProgressBarBitmap;
import com.hyperether.getgoing.viewmodel.RouteViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.hyperether.getgoing.util.Constants.ACTIVITY_RIDE_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_RUN_ID;
import static com.hyperether.getgoing.util.Constants.ACTIVITY_WALK_ID;
import static com.hyperether.getgoing.util.Constants.BUNDLE_PARCELABLE;
import static com.hyperether.getgoing.util.Constants.PREF_FILE;
import static com.hyperether.getgoing.util.Constants.PREF_RIDE_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_RUN_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_WALK_ROUTE_EXISTING;

public class ShowDataFragment extends DialogFragment implements GgOnClickListener, OnMapReadyCallback {

    public static final String DATA_LABEL = "data_label";
    private ShowDataBinding binding;

    private GoogleMap mMap;

    private final List<DbRoute> routes = new ArrayList<>();
    private RecyclerView.Adapter recyclerAdapter;

    private String dataLabel;
    private boolean mapToogleDown;
    private int activityId;
    private RouteViewModel routeViewModel;

    private View rootView;
    private MapFragment mapFragment;


    public static ShowDataFragment newInstance(String dataLabel) {
        ShowDataFragment showDataFragment = new ShowDataFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_LABEL, dataLabel);
        showDataFragment.setArguments(bundle);
        return showDataFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        dataLabel = getArguments().getString(DATA_LABEL);

        if (getResources().getString(R.string.walking).equals(dataLabel)) {
            activityId = ACTIVITY_WALK_ID;
        } else if (getResources().getString(R.string.running).equals(dataLabel)) {
            activityId = ACTIVITY_RUN_ID;
        } else {
            activityId = ACTIVITY_RIDE_ID;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.show_data, container, false);
        rootView = binding.getRoot();

        populateListView();

        initializeViewModel();
        initializeViews();


        mapFragment = (MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.sd_map_view);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapFragment != null) {
            getActivity().getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
    }

    private void initializeViewModel() {
        routeViewModel = ViewModelProviders.of(getActivity()).get(RouteViewModel.class);
        routeViewModel.getRouteList().observe(getActivity(), routeList -> {
            routes.clear();
            if (routeList.size() > 1) {
                routeList.remove(0); // remove 0th node
                for (DbRoute route : routeList) {
                    if (route.getActivity_id() == activityId) {
                        routes.add(route);
                    }
                }
            }

            if (routes.size() == 0) {
                showNoRoutesDialog();
            } else {
                Bitmap bm = ProgressBarBitmap.getWidgetBitmap(getActivity().getApplicationContext(), routes.get(routes.size() - 1).getGoal(), routes.get(0).getLength(), 400, 400, 160, 220, 20, 0);
                binding.setVar(routes.get(routes.size() - 1));
                binding.progress.setImageBitmap(bm);
                binding.recyclerList.smoothScrollToPosition(routes.size() - 1);
            }

            recyclerAdapter.notifyDataSetChanged();
        });
    }

    private void initializeViews() {
        binding.tvSdLabel.setText(dataLabel);
        binding.ibSdBackBtn.setOnClickListener(v -> this.dismiss());
        binding.btnToggleMap.setOnClickListener(v -> toogleMap());
        binding.mapFragmentHolder.animate().scaleYBy(-1);
        binding.ibSdDeleteBtn.setOnClickListener(v -> deleteRoute());
    }

    private void deleteRoute() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.alert_dialog_delete_route));
        dialog.setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                (DialogInterface paramDialogInterface, int paramInt) -> {
                    routeViewModel.removeRouteById(binding.getVar().getId());
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
                            SharedPreferences prefs = getActivity().getSharedPreferences(PREF_FILE, getActivity().MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            switch (activityId) {
                                case ACTIVITY_WALK_ID:
                                    editor.putBoolean(PREF_WALK_ROUTE_EXISTING, false);
                                    break;
                                case ACTIVITY_RUN_ID:
                                    editor.putBoolean(PREF_RUN_ROUTE_EXISTING, false);
                                    break;
                                case ACTIVITY_RIDE_ID:
                                    editor.putBoolean(PREF_RIDE_ROUTE_EXISTING, false);
                                    break;
                            }
                            editor.apply();

                            this.dismiss();
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
        DbRoute route = binding.getVar();
        routeViewModel.getNodeListById(route.getId())
                .observe(getActivity(), dbNodes -> {

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
        recyclerAdapter = new DbRecyclerAdapter(getActivity(), routes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerList.setLayoutManager(linearLayoutManager);
        binding.recyclerList.setAdapter(recyclerAdapter);
    }


    @Override
    public void onClick(Bundle bundle) {
        DbRoute route = bundle.getParcelable(BUNDLE_PARCELABLE);
        binding.setVar(route);

        if (route != null) {
            Bitmap bm = ProgressBarBitmap.getWidgetBitmap(getActivity().getApplicationContext(), route.getGoal(), route.getLength(), 400, 400, 160, 220, 20, 0);
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