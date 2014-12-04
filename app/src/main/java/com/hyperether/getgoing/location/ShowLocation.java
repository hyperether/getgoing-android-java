package com.hyperether.getgoing.location;

import android.app.Activity;
import android.os.Bundle;

import com.hyperether.getgoing.R;

public class ShowLocation extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Keep screen on all the time
        setContentView(R.layout.show_location);
    }
}
