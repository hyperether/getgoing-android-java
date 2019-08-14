package com.hyperether.getgoing;

import android.os.Parcel;

import com.hyperether.getgoing.model.CBDataFrame;
import com.hyperether.getgoing.ui.activity.ShowLocationActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShowLocationActivityTest {

    private CBDataFrame dataFrame;

    @Rule
    public ActivityTestRule<ShowLocationActivity> mActivityRule =
            new ActivityTestRule(ShowLocationActivity.class, true, true);

    @Before
    public void createFrame() {
        dataFrame.setMeasurementSystemId(1);
        Parcel parcel = Parcel.obtain();
        dataFrame.writeToParcel(parcel, dataFrame.describeContents());
        ShowLocationActivity activity = (ShowLocationActivity) mActivityRule.getActivity();

    }

    @Test
    public void checkButtons() {

    }

    @Test
    public void clickButtons() {

    }
}