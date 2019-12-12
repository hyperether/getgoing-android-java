package com.hyperether.getgoing.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.repository.room.DbHelper;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.util.Constants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.hyperether.getgoing.ui.activity.GetGoingActivity.ratio;

public class ActivitiesFragment extends DialogFragment
{
    public static final String DATA_KEY = "data_key";

    private View whiteView;
    private TextView goal, walkingLabel;
    private SeekBar seekBar;
    private TextView low, medium, high;
    private TextView minutesRunning, minutesWalking, minutesCycling, kcal;
    private TextView mileage1, mileage2, mileage3;
    private ImageButton backBtn;
    private ImageView walkDetails, runDetails, rideDetails;
    private ProgressBar prbWalk, prbRun, prbRide;
    private Button saveChanges;

    private SharedPreferences settings;

    public static ActivitiesFragment newInstance(CBDataFrame dataFrame) {
        ActivitiesFragment activitiesFragment = new ActivitiesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, dataFrame);
        activitiesFragment.setArguments(bundle);
        return activitiesFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        settings = getActivity().getSharedPreferences(Constants.PREF_FILE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_activities, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        seekBar = getView().findViewById(R.id.seekBar);
        goal = getView().findViewById(R.id.tv_fa_goal);
        minutesWalking = getView().findViewById(R.id.tv_fa_minutes);
        minutesRunning = getView().findViewById(R.id.tv_fa_min2);
        minutesCycling = getView().findViewById(R.id.tv_fa_min3);
        kcal = getView().findViewById(R.id.tv_fa_kcal);

        seekBar.incrementProgressBy(10);

        Dialog dialog = getDialog();

        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;

            dialog.getWindow().setLayout(width, height);
        }

        initScreenDimen();
        initLabels();
        initProgressStringColor();
        initListeners();
        fillProgressBars();
    }

    private void initScreenDimen()
    {
        if (ratio >= 1.8)
        {
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

    private void initProgressStringColor()
    {
        int progress = seekBar.getProgress();

        low = getView().findViewById(R.id.tv_fa_low);
        medium = getView().findViewById(R.id.tv_fa_medium);
        high = getView().findViewById(R.id.tv_fa_high);

        if (progress >= 0 && progress <= 3333) {
            low.setTextColor(getResources().getColor(R.color.light_theme_accent));
            medium.setTextColor(getResources().getColor(R.color.mat_gray));
            high.setTextColor(getResources().getColor(R.color.mat_gray));
        }
        else if (progress > 3333 && progress <= 6666) {
            low.setTextColor(getResources().getColor(R.color.mat_gray));
            medium.setTextColor(getResources().getColor(R.color.light_theme_accent));
            high.setTextColor(getResources().getColor(R.color.mat_gray));
        }
        else if (progress > 6666 && progress <= 10000) {
            low.setTextColor(getResources().getColor(R.color.mat_gray));
            medium.setTextColor(getResources().getColor(R.color.mat_gray));
            high.setTextColor(getResources().getColor(R.color.light_theme_accent));
        }
    }

    @SuppressLint("SetTextI18n")
    private void initListeners()
    {
        backBtn = getView().findViewById(R.id.ib_fa_back);
        walkDetails = getView().findViewById(R.id.iv_fa_rightarrow1);
        runDetails = getView().findViewById(R.id.iv_fa_rightarrow2);
        rideDetails = getView().findViewById(R.id.iv_fa_rightarrow3);
        saveChanges = getView().findViewById(R.id.b_fa_save);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                i = i / 10;         /* arithmetic for setting value right */
                i = i * 10;

                goal.setText(Integer.toString(i));

                if (i >= 0 && i <= 3333) {
                    low.setTextColor(getResources().getColor(R.color.light_theme_accent));
                    medium.setTextColor(getResources().getColor(R.color.mat_gray));
                    high.setTextColor(getResources().getColor(R.color.mat_gray));
                }
                else if (i > 3333 && i <= 6666) {
                    low.setTextColor(getResources().getColor(R.color.mat_gray));
                    medium.setTextColor(getResources().getColor(R.color.light_theme_accent));
                    high.setTextColor(getResources().getColor(R.color.mat_gray));
                }
                else if (i > 6666 && i <= 10000) {
                    low.setTextColor(getResources().getColor(R.color.mat_gray));
                    medium.setTextColor(getResources().getColor(R.color.mat_gray));
                    high.setTextColor(getResources().getColor(R.color.light_theme_accent));
                }

                int[] timeEstimates = getTimeEstimates(i);

                minutesWalking.setText(timeEstimates[0] + " min");
                minutesRunning.setText(timeEstimates[1] + " min");
                minutesCycling.setText(timeEstimates[2] + " min");
                kcal.setText("About " + (int) (i * 0.00112 * settings.getInt("weight", 0)) + "kcal");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        low.setOnClickListener(view -> seekBar.setProgress(Constants.CONST_LOW_DIST));
        medium.setOnClickListener(view -> seekBar.setProgress(Constants.CONST_MEDIUM_DIST));
        high.setOnClickListener(view -> seekBar.setProgress(Constants.CONST_HIGH_DIST));
        backBtn.setOnClickListener(view -> this.getDialog().dismiss());

        /* ACTIVITY NOT IMPLEMENTED YET */
//        walkDetails.setOnClickListener(view -> {
//            Intent intent = new Intent(getContext(), ShowDataActivity.class);
//            startActivity(intent);
//        });
//
//        runDetails.setOnClickListener(view -> {
//            Intent intent = new Intent(getContext(), ShowDataActivity.class);
//            startActivity(intent);
//        });
//
//        rideDetails.setOnClickListener(view -> {
//            Intent intent = new Intent(getContext(), ShowDataActivity.class);
//            startActivity(intent);
//        });

        saveChanges.setOnClickListener(view -> {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("goal", seekBar.getProgress());
            editor.apply();

            Toast.makeText(getContext(), "Your goal is updated", Toast.LENGTH_SHORT).show();
        });

    }

    private int[] getTimeEstimates(int dist)
    {
        int[] returnValues = new int[3];

        returnValues[0] = (int) (dist / (Constants.AVG_SPEED_WALK * 60));
        returnValues[1] = (int) (dist / (Constants.AVG_SPEED_RUN * 60));
        returnValues[2] = (int) (dist / (Constants.AVG_SPEED_CYCLING * 60));

        return returnValues;
    }

    private void initLabels()
    {
        seekBar.setProgress(settings.getInt("goal", 5000));

        int progress = seekBar.getProgress();
        int[] timeEstimates = getTimeEstimates(progress);

        goal.setText(Integer.toString(progress));

        minutesWalking.setText(timeEstimates[0] + " min");
        minutesRunning.setText(timeEstimates[1] + " min");
        minutesCycling.setText(timeEstimates[2] + " min");

        kcal.setText("About " + (int) (progress * 0.00112 * settings.getInt("weight", 0)) + "kcal");
    }

    private void fillProgressBars()
    {
        new PullProgressData().execute(null, null, null);
    }

    private class PullProgressData extends AsyncTask<Void, Void, Void>
    {
        List<DbRoute> allRoutes;
        int goal = settings.getInt("goal", 0);
        Double sumWalk = 0.0, sumRun = 0.0, sumRide = 0.0;
        int walkPercentage = 0, runPercentage = 0, ridePercentage = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prbWalk = getView().findViewById(R.id.progressBar);
            prbRun = getView().findViewById(R.id.progressBar2);
            prbRide = getView().findViewById(R.id.progressBar3);

            mileage1 = getView().findViewById(R.id.tv_fa_pb_mileage1);
            mileage2 = getView().findViewById(R.id.tv_fa_pb_mileage2);
            mileage3 = getView().findViewById(R.id.tv_fa_pb_mileage3);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            allRoutes = new ArrayList<>();
            DbHelper.getInstance(getContext()).getRoutes(allRoutes::addAll);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            for (DbRoute route : allRoutes)
            {
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

            mileage1.setText(df.format(sumWalk / 1000)+ "km");
            mileage2.setText(df.format(sumRun / 1000) + "km");
            mileage3.setText(df.format(sumRide / 1000) + "km");
        }
    }

}
