package com.hyperether.getgoing.ui.activity;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.hyperether.getgoing.model.CBDataFrame;

public class ProfileFragment extends DialogFragment {

    public static final String DATA_KEY = "data_key";

    public static ProfileFragment newInstance(CBDataFrame dataFrame)
    {
        ProfileFragment settingsFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, dataFrame);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }
}
