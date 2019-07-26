package com.hyperether.getgoing.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.hyperether.getgoing.R;
import com.hyperether.getgoing.data.CBDataFrame;
import com.hyperether.getgoing.util.Constants;

public class SettingsFragment extends Fragment implements OnItemSelectedListener {

    public static final String DATA_KEY = "data_key";

    private CBDataFrame cbDataFrameLocal;
    Spinner spinner;
    int iCountAdapterCalls = 0;
    ImageButton buttonConfirmSettings;
    private NumberPicker agePicker;
    private NumberPicker weightPicker;

    private  SettingsFragmentListener listener;

    public SettingsFragment(){

    }

    public static SettingsFragment newInstance(CBDataFrame dataFrame){
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, dataFrame);
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            cbDataFrameLocal = getArguments().getParcelable(DATA_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        initLayout(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SettingsFragmentListener){
            listener = (SettingsFragmentListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement SettingsFragmentListener interface");
        }
    }

    private void initLayout(View rootView) {
        spinner = rootView.findViewById(R.id.metric_spinner);
        ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(
                getActivity(), R.array.measure_units, R.layout.settings_spinner);
        // Specify the layout to use when the list of choices appears
        spinnerArrayAdapter.setDropDownViewResource(R.layout.settings_spinner);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);

        weightPicker = rootView.findViewById(R.id.num_picker);
        agePicker = rootView.findViewById(R.id.num_picker2);

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

        buttonConfirmSettings = rootView.findViewById(R.id.buttonConfirmSettings);
        buttonConfirmSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbDataFrameLocal.setWeight(weightPicker.getValue());
                cbDataFrameLocal.setAge(agePicker.getValue());
                listener.onDataSent(cbDataFrameLocal);
                
            }
        });
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    public void onResume() {
        super.onResume();
        spinner.setSelection(cbDataFrameLocal.getMeasurementSystemId());
        agePicker.setValue(cbDataFrameLocal.getAge());
        weightPicker.setValue(cbDataFrameLocal.getWeight());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_reset_settings) {
            cbDataFrameLocal.setAge(0);
            cbDataFrameLocal.setWeight(0);
            cbDataFrameLocal.setMeasurementSystemId(0);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(DATA_KEY, cbDataFrameLocal);
            // setResult(Activity.RESULT_OK, resultIntent);
           // finish();
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

    public interface SettingsFragmentListener{
        void onDataSent(CBDataFrame dataFrame);
    }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        cbDataFrameLocal = savedInstanceState.getParcelable("cbDataFrameLocal");
//    }
}
