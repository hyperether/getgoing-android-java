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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;

public class SettingsActivity extends Activity implements OnItemSelectedListener {

    private CBDataFrame cbDataFrameLocal;
    Spinner spinner;
    int iCountAdapterCalls = 0;
    //    NumberPicker mNumberPickerW;
//    NumberPicker mNumberPickerA;
    EditText editTextWeight;
    ImageButton increaseWeight;
    ImageButton decreaseWeight;
    EditText editTextAge;
    ImageButton increaseAge;
    ImageButton decreaseAge;
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

        editTextWeight = (EditText) findViewById(R.id.edit_weight);
        editTextWeight.setText("0");
        increaseWeight = (ImageButton) findViewById(R.id.weight_picker_up);
        increaseWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWeight(true);
            }
        });
        decreaseWeight = (ImageButton) findViewById(R.id.weight_picker_down);
        decreaseWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWeight(false);
            }
        });
        editTextAge = (EditText) findViewById(R.id.edit_age);
        editTextAge.setText("0");
        increaseAge = (ImageButton) findViewById(R.id.age_picker_up);
        increaseAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAge(true);
            }
        });
        decreaseAge = (ImageButton) findViewById(R.id.age_picker_down);
        decreaseAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAge(false);
            }
        });

        buttonConfirmSettings = (Button) findViewById(R.id.buttonConfirmSettings);
        buttonConfirmSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int weight = 0, age = 0;
                boolean success = false;
                if ((!editTextWeight.getText().toString().equals("")) &&
                        (!editTextAge.getText().toString().equals(""))) {
                    weight = Integer.parseInt(editTextWeight.getText().toString());
                    age = Integer.parseInt(editTextAge.getText().toString());

                    if ((weight > 0) && (age > 0)) {
                        cbDataFrameLocal.setAge(age);
                        cbDataFrameLocal.setWeight(weight);
                        success = true;
                    }
                }

                if (success) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("dataKey", cbDataFrameLocal);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this,
                            getString(R.string.settings_insert_weight_age), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private void setWeight(boolean increase) {
        int weight;
        if (!editTextWeight.getText().toString().equals("")) {
            weight = Integer.parseInt(editTextWeight.getText().toString());
        } else {
            weight = 0;
        }
        if (increase) {
            weight++;
        } else {
            if (weight > 0) {
                weight--;
            }
        }
        editTextWeight.setText(weight + "");
    }

    private void setAge(boolean increase) {
        int age;
        if (!editTextAge.getText().toString().equals("")) {
            age = Integer.parseInt(editTextAge.getText().toString());
        } else {
            age = 0;
        }
        if (increase) {
            age++;
        } else {
            if (age > 0) {
                age--;
            }
        }
        editTextAge.setText(age + "");
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        spinner.setSelection(cbDataFrameLocal.getMeasurementSystemId());
        editTextAge.setText(cbDataFrameLocal.getAge() + "");
        editTextWeight.setText(cbDataFrameLocal.getWeight() + "");
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
