package com.example.landmarkdirect.ui.settings;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.landmarkdirect.R;
import com.example.landmarkdirect.databinding.FragmentSettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private SettingsViewModel settingsViewModel;
    private FragmentSettingsBinding binding;

    //Firebase variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = database.getReference("Users");
    private String id;

    private String measurementSystem, preferredLandmark;
    private RadioButton rbMetric, rbImperial, rbMeasurement;
    private RadioGroup radioGroupMeasurement;
    private Button saveButton;
    private Spinner spinner;
    private String googleType;
    private String[] googleTypes;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rbMetric = (RadioButton) getView().findViewById(R.id.rbMetric);
        rbImperial = (RadioButton) getView().findViewById(R.id.rbImperial);
        radioGroupMeasurement = (RadioGroup) getView().findViewById(R.id.radioGroupMeasurement);

        //dropdown with landmark types
        spinner = (Spinner) getView().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext()
                , R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        googleTypes = getContext().getResources().getStringArray(R.array.google_types);

        configureInputs();

        saveButton = (Button) getView().findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });
    }

    //have an a parallel array to have google types and types for the user to see
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        googleTypes = getContext().getResources().getStringArray(R.array.google_types);
        googleType = googleTypes[i];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //updating the settings to the user's newly picked settings
    private void updateSettings() {
        int selectedMeasurementId = radioGroupMeasurement.getCheckedRadioButtonId();

        rbMeasurement = (RadioButton) getView().findViewById(selectedMeasurementId);
        mDatabase.child(id).child("measurementSystem").setValue(rbMeasurement.getText().toString());

        mDatabase.child(id).child("preferredLandmark").setValue(googleType);

        Toast.makeText(getContext(), "Settings updated!", Toast.LENGTH_SHORT).show();
    }

    //setting the inputs from the user settings from firebase
    private void configureInputs() {
        mDatabase.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                measurementSystem = snapshot.child("measurementSystem").getValue(String.class);

                if (measurementSystem.equals("Metric")) {
                    rbMetric.setChecked(true);
                    rbImperial.setChecked(false);
                } else {
                    rbMetric.setChecked(false);
                    rbImperial.setChecked(true);
                }

                preferredLandmark = snapshot.child("preferredLandmark").getValue(String.class);


                for (int i = 0; i < googleTypes.length; i++) {
                    if (preferredLandmark.equals(googleTypes[i])) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}