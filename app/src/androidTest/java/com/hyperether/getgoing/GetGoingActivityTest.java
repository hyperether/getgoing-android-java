package com.hyperether.getgoing;

import com.hyperether.getgoing.ui.activity.GetGoingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
        onView(withId(R.id.walk_button)).check(matches(isDisplayed()));
        onView(withId(R.id.run_button)).check(matches(isDisplayed()));
        onView(withId(R.id.ride_button)).check(matches(isDisplayed()));
    }

    @Test
    public void clickButtons() {
        onView(withId(R.id.walk_button)).perform(click());
        pressBack();
        onView(withId(R.id.run_button)).perform(click());
        pressBack();
        onView(withId(R.id.ride_button)).perform(click());
    }

    @Test
    public void clickOptions() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Statistics")).perform(click());
        pressBack();
        onView(withId(R.id.action_settings)).perform(click());
        pressBack();
    }
}
