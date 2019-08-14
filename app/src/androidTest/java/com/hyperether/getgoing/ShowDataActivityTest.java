package com.hyperether.getgoing;

import com.hyperether.getgoing.ui.activity.ShowDataActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

/**
 * Created by nikola on 19.10.17..
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShowDataActivityTest {

    @Rule
    public ActivityTestRule<ShowDataActivity> mActivityRule =
            new ActivityTestRule(ShowDataActivity.class);

    @Test
    public void checkElements() {
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click());
        pressBack();
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0)
                .onChildView(withId(R.id.delete_layout)).perform(click());
        onView(withText(R.string.delete_message)).check(matches(isDisplayed()));
        onView(withText(R.string.confirm)).perform(click());
    }

}
