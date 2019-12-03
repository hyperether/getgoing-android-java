package com.hyperether.getgoing;

import com.hyperether.getgoing.ui.fragment.old.SettingsFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsActivityTest {

//    @Rule
//    public ActivityTestRule<SettingsFragment> mActivityRule =
//            new ActivityTestRule(SettingsFragment.class);
//
//
//    @Test
//    public void setDataFrame() {
//        onView(withId(R.id.metric_spinner)).perform(click());
//        onData(anything()).atPosition(0).perform(click());
//        onView(withId(R.id.metric_spinner))
//                .check(matches(withSpinnerText(containsString("Metric"))));
//    }
//
//
//    @Test
//    public void checkElements() {
//        onView(withId(R.id.buttonConfirmSettings)).check(matches(isDisplayed()));
//    }

}
