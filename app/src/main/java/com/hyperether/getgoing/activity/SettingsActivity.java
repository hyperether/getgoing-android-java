package com.hyperether.getgoing.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.util.Constants;

public class SettingsActivity extends Activity implements OnItemSelectedListener {

    private CBDataFrame cbDataFrameLocal;
    Spinner spinner;
    int iCountAdapterCalls = 0;
    ImageButton buttonConfirmSettings;
    private NumberPicker agePicker;
    private NumberPicker weightPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbDataFrameLocal = new CBDataFrame();
        Bundle b = getIntent().getExtras();
        cbDataFrameLocal = b.getParcelable("searchKey");

        spinner = (Spinner) findViewById(R.id.metric_spinner);
        ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.measure_units, R.layout.settings_spinner);
        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(R.layout.settings_spinner);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);

        weightPicker = (NumberPicker) findViewById(R.id.num_picker);
        agePicker = (NumberPicker) findViewById(R.id.num_picker2);

        String[] numbers = new String[Constants.NUMBER_PICKER_VALUE_SIZE];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.toString(i);
        }

        weightPicker.setDisplayedValues(numbers);
        weightPicker.setMaxValue(Constants.NUMBER_PICKER_MAX_VALUE);
        weightPicker.setMinValue(Constants.NUMBER_PICKER_MIN_VALUE);
        weightPicker.setWrapSelectorWheel(true);
        weightPicker.setValue(Constants.NUMBER_PICKER_DEFAULT_WEIGHT);

        agePicker.setDisplayedValues(numbers);
        agePicker.setMaxValue(Constants.NUMBER_PICKER_MAX_VALUE);
        agePicker.setMinValue(Constants.NUMBER_PICKER_MIN_VALUE);
        agePicker.setWrapSelectorWheel(true);
        agePicker.setValue(Constants.NUMBER_PICKER_DEFAULT_AGE);

        buttonConfirmSettings = (ImageButton) findViewById(R.id.buttonConfirmSettings);
        buttonConfirmSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cbDataFrameLocal.setWeight(weightPicker.getValue());
                cbDataFrameLocal.setAge(agePicker.getValue());

                Intent resultIntent = new Intent();
                resultIntent.putExtra("dataKey", cbDataFrameLocal);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        spinner.setSelection(cbDataFrameLocal.getMeasurementSystemId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_reset_settings) {
            cbDataFrameLocal.setAge(0);
            cbDataFrameLocal.setWeight(0);
            cbDataFrameLocal.setMeasurementSystemId(0);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("dataKey", cbDataFrameLocal);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if (iCountAdapterCalls < 1) {
            iCountAdapterCalls++;
            // This section executes in onCreate, during the initialization
        } else {
            cbDataFrameLocal.setMeasurementSystemId(arg2);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("cbDataFrameLocal", cbDataFrameLocal);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        cbDataFrameLocal = savedInstanceState.getParcelable("cbDataFrameLocal");
    }
}
