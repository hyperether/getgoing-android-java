package com.hyperether.getgoing.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.hyperether.getgoing.R;
import com.hyperether.getgoing.repository.room.entity.DbRoute;
import com.hyperether.getgoing.util.Constants;
import com.hyperether.getgoing.viewmodel.RouteViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.hyperether.getgoing.ui.fragment.GetGoingFragment.ratio;


public class ProfileFragment extends Fragment {

    private ImageButton genderBtn, ageBtn, heightBtn, weightBtn, backBtn;
    private TextView tvAge, tvGender, tvHeight, tvWeight;
    private TextView totalMileage, totalCalories;
    private TextView dataLabel;
    private ImageView genderImg;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private RouteViewModel routeViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getContext().getSharedPreferences(Constants.PREF_FILE, 0);

        // default gender selection in shared prefs if nothing is set
        editor = settings.edit();
        if (!settings.contains("gender")) {
            editor.putInt("gender", 0).apply();
        }

        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        genderImg = rootView.findViewById(R.id.iv_fp_gender);

        int genderSel = settings.getInt("gender", 0);

        if (genderSel == 0) {
            genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_gendersign_male));
        } else if (genderSel == 1) {
            genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_gender_female_icon));
        } else if (genderSel == 2) {
            genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_gender_icon_trans));
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        tvGender = getView().findViewById(R.id.tv_fp_gender);
        tvHeight = getView().findViewById(R.id.tv_fp_height);
        tvAge = getView().findViewById(R.id.tv_fp_age);
        tvWeight = getView().findViewById(R.id.tv_fp_weight);
        genderBtn = getView().findViewById(R.id.ib_fp_gender);
        totalMileage = getView().findViewById(R.id.tv_fp_mileage);
        totalCalories = getView().findViewById(R.id.tv_fp_calories);

        initScreenDimen();
        initLabels();
        initDialogs();
        routeViewModel.getAllRoutes().observe(this, new Observer<List<DbRoute>>() {
            @Override
            public void onChanged(List<DbRoute> dbRoutes) {
                initTotals(dbRoutes);
            }
        });
    }

    private void initScreenDimen() {
        if (ratio > 1.8) {
            dataLabel = getView().findViewById(R.id.tv_fp_mydata);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) dataLabel.getLayoutParams();
            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) genderBtn.getLayoutParams();

            params.topMargin = 60;
            params1.topMargin = 100;

            dataLabel.setLayoutParams(params);
            genderBtn.setLayoutParams(params1);
        }
    }

    private void initDialogs() {
        ageBtn = getView().findViewById(R.id.ib_fp_age);
        weightBtn = getView().findViewById(R.id.ib_fp_weight);
        heightBtn = getView().findViewById(R.id.ib_fp_height);

        backBtn = getView().findViewById(R.id.ib_fp_backbutton);

        genderBtn.setOnClickListener(view -> {
            String id = "gender";
            AlertDialog.Builder builder = createDialog(id, view);
            if (builder != null) {
                builder.show();
            }
        });

        ageBtn.setOnClickListener(view -> {
            String id = "age";
            AlertDialog.Builder builder = createDialog(id, view);
            if (builder != null) {
                builder.show();
            }
        });

        weightBtn.setOnClickListener(view -> {
            String id = "weight";
            AlertDialog.Builder builder = createDialog(id, view);
            if (builder != null) {
                builder.show();
            }
        });

        heightBtn.setOnClickListener(view -> {
            String id = "height";
            AlertDialog.Builder builder = createDialog(id, view);
            if (builder != null) {
                builder.show();
            }
        });

        backBtn.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });
    }

    private AlertDialog.Builder createDialog(String pID, View pView) {
        AlertDialog.Builder genderBuilder, ageBuilder,
                weightBuilder, heightBuilder;

        switch (pID) {
            case "gender": {
                genderBuilder = new AlertDialog.Builder(pView.getContext(), R.style.AlertDialogTheme);
                final String[] newText = new String[1];
                newText[0] = "Male";

                genderBuilder.setSingleChoiceItems(R.array.genders, settings.getInt("gender", 0), (dialog, which) -> {

                    if (which == 0) {
                        newText[0] = "Male";
                        editor.putInt("gender", 0);
                    } else if (which == 1) {
                        newText[0] = "Female";
                        editor.putInt("gender", 1);
                    } else {
                        newText[0] = "Other";
                        editor.putInt("gender", 2);
                    }
                    editor.apply();
                })
                        .setPositiveButton("Confirm", (dialogInterface, i) -> {
                            tvGender.setText(newText[0]);

                            switch (newText[0]) {
                                case "Male":
                                    genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_gendersign_male));
                                    break;
                                case "Female":
                                    genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_gender_female_icon));
                                    break;
                                case "Other":
                                    genderImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_gender_icon_trans));
                                    break;
                            }
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                        .setTitle("Please select your gender:");

                return genderBuilder;
            }
            case "age": {
                List ageList = new ArrayList<String>();
                for (int i = 1; i <= 120; i++) {
                    ageList.add(Integer.toString(i));
                }

                ageBuilder = new AlertDialog.Builder(pView.getContext(), R.style.AlertDialogTheme);

                View toInflate = getActivity().getLayoutInflater().inflate(R.layout.alertdialog_age, null);
                ageBuilder.setView(toInflate);

                Spinner ageSpinner = toInflate.findViewById(R.id.dialog_spinner_age);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(pView.getContext(), android.R.layout.simple_list_item_1, ageList);
                ageSpinner.setAdapter(adapter);
                ageSpinner.setSelection(settings.getInt("age", 0) - 1);

                ageBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                    tvAge.setText(ageSpinner.getSelectedItem() + getResources().getString(R.string.textview_age_end));

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("age", Integer.valueOf((String) ageSpinner.getSelectedItem()));
                    editor.apply();
                })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                        .setTitle("How old are you?");

                return ageBuilder;
            }
            case "weight": {
                List weightList = new ArrayList<String>();
                for (int i = 40; i <= 150; i++) {
                    weightList.add(Integer.toString(i));
                }

                weightBuilder = new AlertDialog.Builder(pView.getContext(), R.style.AlertDialogTheme);

                View toInflate = getActivity().getLayoutInflater().inflate(R.layout.alertdialog_weight, null);
                weightBuilder.setView(toInflate);

                Spinner weightSpinner = toInflate.findViewById(R.id.dialog_spinner_weight);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(pView.getContext(), android.R.layout.simple_list_item_1, weightList);
                weightSpinner.setAdapter(adapter);
                weightSpinner.setSelection(settings.getInt("weight", 0) - 40);

                weightBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                    tvWeight.setText(weightSpinner.getSelectedItem() + " kg");

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("weight", Integer.valueOf((String) weightSpinner.getSelectedItem()));
                    editor.apply();
                })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                        .setTitle("Enter your weight:");

                return weightBuilder;
            }
            case "height": {
                List heightList = new ArrayList<String>();
                for (int i = 110; i <= 250; i++) {
                    heightList.add(Integer.toString(i));
                }

                heightBuilder = new AlertDialog.Builder(pView.getContext(), R.style.AlertDialogTheme);

                View toInflate = getActivity().getLayoutInflater().inflate(R.layout.alertdialog_height, null);
                heightBuilder.setView(toInflate);

                Spinner heightSpinner = toInflate.findViewById(R.id.dialog_spinner_height);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(pView.getContext(), android.R.layout.simple_list_item_1, heightList);
                heightSpinner.setAdapter(adapter);
                heightSpinner.setSelection(settings.getInt("height", 0) - 110);

                heightBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                    tvHeight.setText(heightSpinner.getSelectedItem() + " cm");

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("height", Integer.valueOf((String) heightSpinner.getSelectedItem()));
                    editor.apply();
                })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                        .setTitle("Enter your height:");

                return heightBuilder;
            }
        }

        return null;
    }

    private void initLabels() {
        tvAge.setText(settings.getInt("age", 0) + " years");
        tvHeight.setText(settings.getInt("height", 0) + "cm");
        tvWeight.setText(settings.getInt("weight", 0) + "kg");

        int gender = settings.getInt("gender", 0);
        if (gender == 0) {
            tvGender.setText(R.string.gender_male);
        } else if (gender == 1) {
            tvGender.setText(R.string.gender_female);
        } else if (gender == 2) {
            tvGender.setText(R.string.gender_other);
        }
    }

    @SuppressLint("DefaultLocale")
    private void initTotals(List<DbRoute> dbRoutes) {
        final float[] totalRoute = new float[1];
        final int[] totalKcal = new int[1];

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                totalRoute[0] = 0;
                totalKcal[0] = 0;

                for (DbRoute route : dbRoutes) {
                    totalRoute[0] += (route.getLength() / 1000);
                    totalKcal[0] += route.getEnergy();
                }

                getActivity().runOnUiThread(() -> {
                    totalMileage.setText(String.format("%.02f km", totalRoute[0]));
                    totalCalories.setText(totalKcal[0] + "kcal");
                });
            }
        });
    }
}
