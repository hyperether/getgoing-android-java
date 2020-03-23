package com.hyperether.getgoing.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.Share;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.SharedPref;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.viewmodel.RouteViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;

import static com.hyperether.getgoing.ui.fragment.GetGoingFragment.ratio;
import static com.hyperether.getgoing.util.Constants.DATA_DETAILS_LABEL;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_KEY;
import static com.hyperether.getgoing.util.Constants.OPENED_FROM_LOCATION_ACT;
import static com.hyperether.getgoing.util.Constants.PREF_RIDE_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_RUN_ROUTE_EXISTING;
import static com.hyperether.getgoing.util.Constants.PREF_WALK_ROUTE_EXISTING;


public class ActivitiesFragment extends Fragment {

    private NavController navigationController;

    private View whiteView;
    private TextView goal, walkingLabel;
    private SeekBar seekBar;
    private TextView low, medium, high;
    private TextView minutesRunning, minutesWalking, minutesCycling, kcal;
    private TextView mileageWalk, mileageRun, mileageRide;
    private ImageButton backBtn;
    private ProgressBar prbWalk, prbRun, prbRide;
    private Button saveChanges;

    private int openedFrom;
    private RouteViewModel routeViewModel;

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            openedFrom = getArguments().getInt(OPENED_FROM_KEY);
        }
        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigationController = Navigation.findNavController(view);

        seekBar = getView().findViewById(R.id.seekBar);
        goal = getView().findViewById(R.id.tv_fa_goal);
        minutesWalking = getView().findViewById(R.id.tv_fa_minutes);
        minutesRunning = getView().findViewById(R.id.tv_fa_min2);
        minutesCycling = getView().findViewById(R.id.tv_fa_min3);
        kcal = getView().findViewById(R.id.tv_fa_kcal);

        prbWalk = getView().findViewById(R.id.progressBar);
        prbRun = getView().findViewById(R.id.progressBar2);
        prbRide = getView().findViewById(R.id.progressBar3);

        seekBar.incrementProgressBy(10);

        mileageWalk = getView().findViewById(R.id.tv_fa_pb_mileage_walk);
        mileageRun = getView().findViewById(R.id.tv_fa_pb_mileage_run);
        mileageRide = getView().findViewById(R.id.tv_fa_pb_mileage_ride);

        initScreenDimen();
        initLabels();
        initProgressStringColor();
        initListeners();
        routeViewModel.getAllRoutes().observe(getViewLifecycleOwner(), new Observer<List<DbRoute>>() {
            @Override
            public void onChanged(List<DbRoute> dbRoutes) {
                fillProgressBars(dbRoutes);
            }
        });

    }

    private void initScreenDimen() {
        if (ratio >= 1.8) {
            whiteView = getView().findViewById(R.id.view);
            goal = getView().findViewById(R.id.tv_fa_goal);
            walkingLabel = getView().findViewById(R.id.tv_fa_pb_walking);

            whiteView.getLayoutParams().height = 900;

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) goal.getLayoutParams();
            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) seekBar.getLayoutParams();
            ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) walkingLabel.getLayoutParams();

            params.topMargin = 90;
            params1.topMargin = 70;
            params2.topMargin = 80;

            walkingLabel.setLayoutParams(params2);
            goal.setLayoutParams(params);
            seekBar.setLayoutParams(params1);
        }
    }

    private void initProgressStringColor() {
        int progress = seekBar.getProgress();

        low = getView().findViewById(R.id.tv_fa_low);
        medium = getView().findViewById(R.id.tv_fa_medium);
        high = getView().findViewById(R.id.tv_fa_high);

        if (progress >= 0 && progress <= 3333) {
            low.setTextColor(getResources().getColor(R.color.light_theme_accent));
            medium.setTextColor(getResources().getColor(R.color.mat_gray));
            high.setTextColor(getResources().getColor(R.color.mat_gray));
        } else if (progress > 3333 && progress <= 6666) {
            low.setTextColor(getResources().getColor(R.color.mat_gray));
            medium.setTextColor(getResources().getColor(R.color.light_theme_accent));
            high.setTextColor(getResources().getColor(R.color.mat_gray));
        } else if (progress > 6666 && progress <= 10000) {
            low.setTextColor(getResources().getColor(R.color.mat_gray));
            medium.setTextColor(getResources().getColor(R.color.mat_gray));
            high.setTextColor(getResources().getColor(R.color.light_theme_accent));
        }
    }

    @SuppressLint("SetTextI18n")
    private void initListeners() {
        backBtn = getView().findViewById(R.id.ib_fa_back);
        saveChanges = getView().findViewById(R.id.b_fa_save);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                /* TODO
                    this is changing already stored value in shared preferences
                    ex. when we store 2390 -> next time activity is displayed, it will show some increased value
                 */
//                i = i / 10;         /* arithmetic for setting value right */
//                i = i * 10;

                goal.setText(Integer.toString(i));

                if (i >= 0 && i <= 3333) {
                    low.setTextColor(getResources().getColor(R.color.light_theme_accent));
                    medium.setTextColor(getResources().getColor(R.color.mat_gray));
                    high.setTextColor(getResources().getColor(R.color.mat_gray));
                } else if (i > 3333 && i <= 6666) {
                    low.setTextColor(getResources().getColor(R.color.mat_gray));
                    medium.setTextColor(getResources().getColor(R.color.light_theme_accent));
                    high.setTextColor(getResources().getColor(R.color.mat_gray));
                } else if (i > 6666 && i <= 10000) {
                    low.setTextColor(getResources().getColor(R.color.mat_gray));
                    medium.setTextColor(getResources().getColor(R.color.mat_gray));
                    high.setTextColor(getResources().getColor(R.color.light_theme_accent));
                }

                int[] timeEstimates = getTimeEstimates(i);

                minutesWalking.setText(timeEstimates[0] + " min");
                minutesRunning.setText(timeEstimates[1] + " min");
                minutesCycling.setText(timeEstimates[2] + " min");
                kcal.setText("About " + (int) (i * 0.00112 * SharedPref.getWeight()) + "kcal");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        low.setOnClickListener(view -> seekBar.setProgress(Constants.CONST_LOW_DIST));
        medium.setOnClickListener(view -> seekBar.setProgress(Constants.CONST_MEDIUM_DIST));
        high.setOnClickListener(view -> seekBar.setProgress(Constants.CONST_HIGH_DIST));
        backBtn.setOnClickListener(view -> getActivity().onBackPressed());

        openActivityDetails();

        saveChanges.setOnClickListener(view -> {

            if (seekBar.getProgress() == 0) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
                dialog.setCancelable(false)
                        .setTitle(getResources().getString(R.string.alert_dialog_title_incorrect_value))
                        .setMessage(getResources().getString(R.string.alert_dialog_goal_0))
                        .setPositiveButton(R.string.alert_dialog_positive_button_save_btn,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                        seekBar.setProgress(SharedPref.getGoal());
                                    }
                                }).show();
            } else {

                SharedPref.setGoal(seekBar.getProgress());

                Toast.makeText(getContext(), "Your goal is updated", Toast.LENGTH_SHORT).show();

                if (openedFrom == OPENED_FROM_LOCATION_ACT) {
                    getActivity().onBackPressed();
                } else {
                    //fillProgressBars();
                }
            }
        });
    }

    private void openActivityDetails() {

        mileageWalk.setOnClickListener(view -> {
            if (SharedPref.doesWalkRouteExist()) {
                Bundle bundle = new Bundle();
                bundle.putString(DATA_DETAILS_LABEL, getString(R.string.walking));
                navigationController.navigate(R.id.action_activitiesFragment_to_showDataFragment, bundle);
            } else {
                openAlertDialog();
            }
        });

        mileageRun.setOnClickListener(view -> {
            if (SharedPref.doesRunRouteExist()) {
                Bundle bundle = new Bundle();
                bundle.putString(DATA_DETAILS_LABEL, getString(R.string.running));
                navigationController.navigate(R.id.action_activitiesFragment_to_showDataFragment, bundle);
            } else {
                openAlertDialog();
            }
        });

        mileageRide.setOnClickListener(view -> {
            if (SharedPref.doesRideRouteExist()) {
                Bundle bundle = new Bundle();
                bundle.putString(DATA_DETAILS_LABEL, getString(R.string.cycling));
                navigationController.navigate(R.id.action_activitiesFragment_to_showDataFragment, bundle);
            } else {
                openAlertDialog();
            }
        });
    }

    private void openAlertDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.alert_dialog_empty_title)
                .setPositiveButton(R.string.confirm,
                        (DialogInterface dialog, int whichButton) -> dialog.dismiss())
                .create()
                .show();
    }

    private int[] getTimeEstimates(int dist) {
        int[] returnValues = new int[3];

        returnValues[0] = (int) (dist / (Constants.AVG_SPEED_WALK * 60));
        returnValues[1] = (int) (dist / (Constants.AVG_SPEED_RUN * 60));
        returnValues[2] = (int) (dist / (Constants.AVG_SPEED_CYCLING * 60));

        return returnValues;
    }

    private void initLabels() {
        seekBar.setProgress(SharedPref.getGoal());

        int progress = seekBar.getProgress();
        int[] timeEstimates = getTimeEstimates(progress);

        goal.setText(Integer.toString(progress));

        minutesWalking.setText(timeEstimates[0] + " min");
        minutesRunning.setText(timeEstimates[1] + " min");
        minutesCycling.setText(timeEstimates[2] + " min");

        kcal.setText("About " + (int) (progress * 0.00112 * SharedPref.getWeight()) + "kcal");
    }

    private void fillProgressBars(List<DbRoute> allRoutes) {
        int goal = SharedPref.getGoal();
        Double sumWalk = 0.0, sumRun = 0.0, sumRide = 0.0;
        int walkPercentage = 0, runPercentage = 0, ridePercentage = 0;

        for (DbRoute route : allRoutes) {
            if (route.getActivity_id() == 1)
                sumWalk += route.getLength();
            else if (route.getActivity_id() == 2)
                sumRun += route.getLength();
            else if (route.getActivity_id() == 3)
                sumRide += route.getLength();
        }

        if (sumWalk != 0)
            walkPercentage = (int) (sumWalk * 100 / goal);
        if (sumRun != 0)
            runPercentage = (int) (sumRun * 100 / goal);
        if (sumRide != 0)
            ridePercentage = (int) (sumRide * 100 / goal);

        prbWalk.setProgress(walkPercentage);
        prbRun.setProgress(runPercentage);
        prbRide.setProgress(ridePercentage);

        DecimalFormat df = new DecimalFormat("#.##");

        mileageWalk.setText(df.format(sumWalk / 1000) + "km");
        mileageRun.setText(df.format(sumRun / 1000) + "km");
        mileageRide.setText(df.format(sumRide / 1000) + "km");
    }
}
