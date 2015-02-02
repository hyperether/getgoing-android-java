package com.hyperether.getgoing.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;

public class SettingsActivity extends Activity implements OnItemSelectedListener {

    private CBDataFrame cbDataFrameLocal;
    Spinner spinner;
    int iCountAdapterCalls = 0;
    NumberPicker mNumberPickerW;
    NumberPicker mNumberPickerA;
    Button buttonConfirmSettings;

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

//		mNumberPickerA = (NumberPicker) findViewById(R.id.numberPicker1);
//		mNumberPickerW = (NumberPicker) findViewById(R.id.numberPicker2);
//
//		mNumberPickerW.setOnChangeListener(new OnChangedListener() {
//			@Override
//			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
//				// TODO Auto-generated method stub
//				cbDataFrameLocal.setWeight(newVal);
//			}
//		});
//
//		mNumberPickerA.setOnChangeListener(new OnChangedListener() {
//			@Override
//			public void onChanged(NumberPicker picker, int oldVal, int newVal) {
//				// TODO Auto-generated method stub
//				cbDataFrameLocal.setAge(newVal);
//			}
//		});

        buttonConfirmSettings = (Button) findViewById(R.id.buttonConfirmSettings);
        buttonConfirmSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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
//		mNumberPickerA.setCurrent(cbDataFrameLocal.getAge());
//		mNumberPickerW.setCurrent(cbDataFrameLocal.getWeight());
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
