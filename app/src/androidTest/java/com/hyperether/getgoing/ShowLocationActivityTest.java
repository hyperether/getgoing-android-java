package com.hyperether.getgoing;

import android.os.Parcel;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hyperether.getgoing.activity.ShowLocationActivity;
import com.hyperether.getgoing.data.CBDataFrame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShowLocationActivityTest {

    private CBDataFrame dataFrame;

    @Rule
    public ActivityTestRule<ShowLocationActivity> mActivityRule =
            new ActivityTestRule(ShowLocationActivity.class,true,true);

    @Before
    public void createFrame(){
        dataFrame.setMeasurementSystemId(1);
        Parcel parcel = Parcel.obtain();
        dataFrame.writeToParcel(parcel,dataFrame.describeContents());
        ShowLocationActivity activity = (ShowLocationActivity)mActivityRule.getActivity();

    }

    @Test
    public void checkButtons() {

    }

    @Test
    public void clickButtons() {

    }
}