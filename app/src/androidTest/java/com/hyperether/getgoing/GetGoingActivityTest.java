package com.hyperether.getgoing;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hyperether.getgoing.activity.GetGoingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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
    public void checkButtons(){
        onView(withId(R.id.walk_button)).check(matches(isDisplayed()));
        onView(withId(R.id.run_button)).check(matches(isDisplayed()));
        onView(withId(R.id.ride_button)).check(matches(isDisplayed()));
    }

    @Test
    public void clickButtons(){
        onView(withId(R.id.walk_button)).perform(click());
        pressBack();
        onView(withId(R.id.run_button)).perform(click());
        pressBack();
        onView(withId(R.id.ride_button)).perform(click());
    }

    @Test
    public void clickOptions(){
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Statistics")).perform(click());
        pressBack();
        onView(withId(R.id.action_settings)).perform(click());
        pressBack();
    }
}
