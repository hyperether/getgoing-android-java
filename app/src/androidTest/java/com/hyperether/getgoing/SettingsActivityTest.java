package com.hyperether.getgoing;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hyperether.getgoing.activity.SettingsActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsActivityTest {

    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule =
            new ActivityTestRule(SettingsActivity.class);

    @Before
    public void setDataFrame() {
        onView(withId(R.id.metric_spinner)).perform(click());
        onData(anything()).atPosition(0).perform(click());
        onView(withId(R.id.metric_spinner)).check(matches(withSpinnerText(containsString("Metric"))));
    }


    @Test
    public void checkElements() {
        onView(withId(R.id.buttonConfirmSettings)).check(matches(isDisplayed()));
    }

}
