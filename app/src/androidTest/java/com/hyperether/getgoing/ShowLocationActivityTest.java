package com.hyperether.getgoing;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hyperether.getgoing.activity.ShowLocationActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShowLocationActivityTest {

    @Rule
    public ActivityTestRule<ShowLocationActivity> mActivityRule =
            new ActivityTestRule(ShowLocationActivity.class);

    @Test
    public void checkButtons() {

    }

    @Test
    public void clickButtons() {

    }
}