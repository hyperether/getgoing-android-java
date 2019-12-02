package com.hyperether.getgoing.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.model.CBDataFrame;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends DialogFragment {

    public static final String DATA_KEY = "data_key";

    private ImageButton genderBtn, ageBtn, heightBtn, weightBtn;
    private TextView tvAge, tvGender, tvHeight, tvWeight;
    private RadioGroup radioGroup;

    private CBDataFrame mDataFrame;
    private ViewGroup rootViewGroup;


    public ProfileFragment(CBDataFrame pDataFrame) {mDataFrame = pDataFrame;}

    public static ProfileFragment newInstance(CBDataFrame dataFrame) {
        ProfileFragment profileFragment = new ProfileFragment(dataFrame);
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, dataFrame);
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootViewGroup = container;

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;

            dialog.getWindow().setLayout(width, height);
        }

        initDialogs();
    }

    private void initDialogs()
    {
        genderBtn = getView().findViewById(R.id.ib_fp_gender);
        ageBtn = getView().findViewById(R.id.ib_fp_age);
        weightBtn = getView().findViewById(R.id.ib_fp_weight);
        heightBtn = getView().findViewById(R.id.ib_fp_height);

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
    }

    private AlertDialog.Builder createDialog(String pID, View pView)
    {
        AlertDialog.Builder genderBuilder, ageBuilder,
                weightBuilder, heightBuilder;

        LayoutInflater inflater;

        switch (pID)
        {
            case "gender":
            {
                tvGender = getView().findViewById(R.id.tv_fp_gender);

                genderBuilder = new AlertDialog.Builder(pView.getContext());
                inflater = LayoutInflater.from(pView.getContext());

                View toInflate = inflater.inflate(R.layout.alertdialog_gender, rootViewGroup);
                radioGroup = toInflate.findViewById(R.id.dialog_radiogroup);

                genderBuilder.setView(toInflate)
                        .setPositiveButton("Confirm", (dialogInterface, i) -> {
                            //TODO: add model field for gender
                            int checkedID = radioGroup.getCheckedRadioButtonId();
                            View radioButton = radioGroup.findViewById(checkedID);
                            int index = radioGroup.indexOfChild(radioButton);

                            String newText;
                            if (index == 0)
                                newText = "Male";
                            else if (index == 1)
                                newText = "Female";
                            else
                                newText = "Other";

                            tvGender.setText(newText);
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

                return genderBuilder;
            }
            case "age":
            {
                tvAge = getView().findViewById(R.id.tv_fp_age);

                List ageList = new ArrayList<String>();
                for (int i = 1; i <= 120; i++) {
                    ageList.add(Integer.toString(i));
                }

                ageBuilder = new AlertDialog.Builder(pView.getContext());
                inflater = LayoutInflater.from(pView.getContext());

                View toInflate = inflater.inflate(R.layout.alertdialog_age, rootViewGroup);
                ageBuilder.setView(toInflate);

                Spinner ageSpinner = toInflate.findViewById(R.id.dialog_spinner_age);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(pView.getContext(), android.R.layout.simple_list_item_1, ageList);
                ageSpinner.setAdapter(adapter);

                ageBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
//                    mDataFrame.setAge(Integer.valueOf((String) ageSpinner.getSelectedItem())); //TODO: CHECK
                    tvAge.setText(ageSpinner.getSelectedItem() + getResources().getString(R.string.textview_age_end));
                })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

                return ageBuilder;
            }
            case "weight":
            {
                tvWeight = getView().findViewById(R.id.tv_fp_weight);

                List weightList = new ArrayList<String>();
                for (int i = 40; i <= 150; i++) {
                    weightList.add(Integer.toString(i));
                }

                weightBuilder = new AlertDialog.Builder(pView.getContext());
                inflater = LayoutInflater.from(pView.getContext());

                View toInflate = inflater.inflate(R.layout.alertdialog_weight, rootViewGroup);
                weightBuilder.setView(toInflate);

                Spinner weightSpinner = toInflate.findViewById(R.id.dialog_spinner_weight);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(pView.getContext(), android.R.layout.simple_list_item_1, weightList);
                weightSpinner.setAdapter(adapter);

                weightBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                    //mDataFrame.setWeight(Integer.valueOf((String) weightSpinner.getSelectedItem())); //TODO: CHECK
                    tvWeight.setText(weightSpinner.getSelectedItem() + " kg");
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

                return weightBuilder;
            }
            case "height":
            {
                tvHeight = getView().findViewById(R.id.tv_fp_height);

                List heightList = new ArrayList<String>();
                for (int i = 110; i <= 250; i++) {
                    heightList.add(Integer.toString(i));
                }

                heightBuilder = new AlertDialog.Builder(pView.getContext());
                inflater = LayoutInflater.from(pView.getContext());

                View toInflate = inflater.inflate(R.layout.alertdialog_height, rootViewGroup);
                heightBuilder.setView(toInflate);

                Spinner heightSpinner = toInflate.findViewById(R.id.dialog_spinner_height);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(pView.getContext(), android.R.layout.simple_list_item_1, heightList);
                heightSpinner.setAdapter(adapter);

                heightBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                            //TODO: modify model
                    tvHeight.setText(heightSpinner.getSelectedItem() + " cm");
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

                return heightBuilder;
            }
        }

        return null;
    }
}
