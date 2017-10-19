package com.hyperether.getgoing;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.hyperether.getgoing.activity.SettingsActivity;

/**
 * Created by nikola on 19.10.17..
 */

public class SettingsActivityTest extends ActivityInstrumentationTestCase2<SettingsActivity> {
    public SettingsActivityTest() {
        super(SettingsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @SmallTest
    public void testComponents(){
        Spinner spinner = (Spinner)getActivity().findViewById(R.id.metric_spinner);
        NumberPicker agePicker = (NumberPicker)getActivity().findViewById(R.id.num_picker2);
        NumberPicker weightPicker = (NumberPicker)getActivity().findViewById(R.id.num_picker);
        ImageButton imageButton = (ImageButton)getActivity().findViewById(R.id.buttonConfirmSettings);

        assertNotNull(spinner);
        assertNotNull(agePicker);
        assertNotNull(weightPicker);
        assertNotNull(imageButton);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
