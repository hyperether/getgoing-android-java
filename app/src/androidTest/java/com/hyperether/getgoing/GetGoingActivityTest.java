package com.hyperether.getgoing;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageButton;

import com.hyperether.getgoing.activity.GetGoingActivity;

/**
 * Created by nikola on 19.10.17..
 */

public class GetGoingActivityTest extends ActivityInstrumentationTestCase2<GetGoingActivity> {
    public GetGoingActivityTest() {
        super(GetGoingActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @SmallTest
    public void testaddButtonListener(){
        ImageButton imageButtonwalk = (ImageButton)getActivity().findViewById(R.id.walk_button);
        ImageButton imageButtonrun = (ImageButton)getActivity().findViewById(R.id.walk_button);
        ImageButton imageButtonride = (ImageButton)getActivity().findViewById(R.id.walk_button);
        assertNotNull(imageButtonwalk);
        assertNotNull(imageButtonride);
        assertNotNull(imageButtonrun);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
