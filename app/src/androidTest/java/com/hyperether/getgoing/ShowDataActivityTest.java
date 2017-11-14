package com.hyperether.getgoing;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ListView;

import com.hyperether.getgoing.activity.ShowDataActivity;

/**
 * Created by nikola on 19.10.17..
 */

public class ShowDataActivityTest extends ActivityInstrumentationTestCase2<ShowDataActivity> {
    public ShowDataActivityTest() {
        super(ShowDataActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @SmallTest
    public void testComponents(){
        ListView listView = (ListView)getActivity().findViewById(android.R.id.list);
        assertNotNull(listView);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
