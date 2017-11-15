package com.hyperether.getgoing;

import android.content.ComponentName;
import android.content.Intent;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;



import com.hyperether.getgoing.activity.GetGoingActivity;
import com.hyperether.getgoing.activity.ShowDataActivity;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
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

    }


}
