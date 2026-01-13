package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carcontroller.R;
import com.example.carcontroller.api_package.LocationConfigurationRepository;
import com.example.carcontroller.api_package.models_package.CircuitLocationResponse;
import com.example.carcontroller.bluetooth_package.BluetoothService;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.SessionManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;


public class CircuitCreatorFragment extends Fragment {
    private static final String TAG = "CircuitCreatorFragmentTAG";

    private EditText cityName, locationName;
    SessionManager sessionManager = SessionManager.getInstance();
    MaterialAutoCompleteTextView dropdownModeOptions;
    MaterialAutoCompleteTextView dropdownSegmentOneDifficultyOptions, dropdownSegmentTwoDifficultyOptions, dropdownSegmentThreeDifficultyOptions;
    Button backButton, saveCircuitButton;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_circuit_creator, container, false);

        initializeViewStatus();
        initializeButtons();
        initializeOnClickListeners();
        return view;
    }

    private void initializeViewStatus () {
        int checkpointCount = DeviceManager.getInstance().getCheckpointsCount();

        view.findViewById(R.id.segmentDifficultyLayout1ID).setVisibility(View.GONE);
        view.findViewById(R.id.segmentDifficultyLayout2ID).setVisibility(View.GONE);
        view.findViewById(R.id.segmentDifficultyLayout3ID).setVisibility(View.GONE);

        cityName = view.findViewById(R.id.cityNameViewID);
        locationName = view.findViewById(R.id.locationNameViewID);
        if (checkpointCount == 1)
            view.findViewById(R.id.segmentDifficultyLayout1ID).setVisibility(View.VISIBLE);
        if (checkpointCount == 2)
            view.findViewById(R.id.segmentDifficultyLayout2ID).setVisibility(View.VISIBLE);
        if (checkpointCount == 3)
            view.findViewById(R.id.segmentDifficultyLayout3ID).setVisibility(View.VISIBLE);
    }
    private void initializeButtons () {
        backButton = (Button) view.findViewById(R.id.backButtonID);
        saveCircuitButton = (Button) view.findViewById(R.id.saveCircuitButtonID);

        dropdownModeOptions = view.findViewById(R.id.inputCircuitTypeID);
        dropdownSegmentOneDifficultyOptions = view.findViewById((R.id.inputSegmentDifficulty1ID));
        dropdownSegmentTwoDifficultyOptions = view.findViewById((R.id.inputSegmentDifficulty2ID));
        dropdownSegmentThreeDifficultyOptions = view.findViewById((R.id.inputSegmentDifficulty3ID));
    }

    private void initializeOnClickListeners () {
        backButton.setOnClickListener(v -> {
            close();
        });
        saveCircuitButton.setOnClickListener(v -> {
            saveCircuit();
        });

        dropdownModeOptions.setOnItemClickListener((parent, v, position, id) -> {
            // 0 = drive circuit
            // 1 = line follower circuit
        });
        dropdownSegmentOneDifficultyOptions.setOnItemClickListener((parent, v, position, id) -> {

        });
        dropdownSegmentTwoDifficultyOptions.setOnItemClickListener((parent, v, position, id) -> {

        });
        dropdownSegmentThreeDifficultyOptions.setOnItemClickListener((parent, v, position, id) -> {

        });
    }

    private void saveCircuit() {
        String city = cityName.getText().toString().trim();
        String location = locationName.getText().toString().trim();

        if (city.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "City and location must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationConfigurationRepository.getInstance().saveCircuitLocation(city, location, new LocationConfigurationRepository.CircuitLocationCallback() {
            @Override
            public void onSuccess(CircuitLocationResponse response) {
                Log.d(TAG, "Circuit location saved with ID: " + response.getCircuitLocationId());
                // OPTIONAL: store ID in SessionManager
                // sessionManager.setCircuitLocationId(response.getCircuitLocationId());
                Toast.makeText(requireContext(), "Circuit location saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, error);
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}