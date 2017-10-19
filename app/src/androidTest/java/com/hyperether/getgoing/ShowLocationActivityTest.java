package com.hyperether.getgoing;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.Chronometer;

import com.hyperether.getgoing.activity.ShowLocationActivity;

/**
 * Created by nikola on 19.10.17..
 */

public class ShowLocationActivityTest extends ActivityInstrumentationTestCase2<ShowLocationActivity> {
    public ShowLocationActivityTest() {
        super(ShowLocationActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @SmallTest
    public void testComponents(){
        Button button_start;
        button_start = (Button)getActivity().findViewById(R.id.start_button);
        assertNotNull(button_start);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
