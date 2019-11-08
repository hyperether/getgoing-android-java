package com.hyperether.getgoing.ui.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.model.CBDataFrame;

public class ProfileFragment extends DialogFragment {

    public static final String DATA_KEY = "data_key";

    public static ProfileFragment newInstance(CBDataFrame dataFrame) {
        ProfileFragment settingsFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, dataFrame);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dialog dialog = this.getDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }
}
