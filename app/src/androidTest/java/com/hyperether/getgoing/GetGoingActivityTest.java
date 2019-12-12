package com.hyperether.getgoing;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.hyperether.getgoing.ui.activity.GetGoingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GetGoingActivityTest {

    @Rule
    public ActivityTestRule<GetGoingActivity> mActivityRule =
            new ActivityTestRule(GetGoingActivity.class);

    @Test
    public void checkButtons() {
//        onView(withId(R.id.walk_button)).check(matches(isDisplayed()));
//        onView(withId(R.id.run_button)).check(matches(isDisplayed()));
//        onView(withId(R.id.ride_button)).check(matches(isDisplayed()));
    }

    @Test
    public void clickButtons() {
//        onView(withId(R.id.walk_button)).perform(click());
//        pressBack();
//        onView(withId(R.id.run_button)).perform(click());
//        pressBack();
//        onView(withId(R.id.ride_button)).perform(click());
    }

    @Test
    public void clickOptions() {
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//        onView(withText("Statistics")).perform(click());
//        pressBack();
//        onView(withId(R.id.action_settings)).perform(click());
//        pressBack();
    }
}
