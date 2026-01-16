package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carcontroller.R;
import com.example.carcontroller.api_package.CircuitRepository;
import com.example.carcontroller.api_package.models_package.CircuitRequest;
import com.example.carcontroller.api_package.models_package.CircuitResponse;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.SessionManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;


public class CircuitCreatorFragment extends Fragment {
    private static final String TAG = "CircuitCreatorFragmentTAG";

    private EditText cityName, locationName, circuitName;
    SessionManager sessionManager = SessionManager.getInstance();
    MaterialAutoCompleteTextView dropdownModeOptions;
    MaterialAutoCompleteTextView dropdownSegmentOneDifficultyOptions, dropdownSegmentTwoDifficultyOptions, dropdownSegmentThreeDifficultyOptions;
    Button backButton, saveCircuitButton;
    View view;

    SessionManager.CircuitType circuitType;
    List<SessionManager.SegmentDifficulty> segmentDifficulties = new ArrayList<>();
    int checkpointCount;
    String circuit;
    String city;
    String location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_circuit_creator, container, false);

        initializeViewStatus();
        initializeButtons();
        initializeOnClickListeners();

        checkpointCount = DeviceManager.getInstance().getCheckpointsCount();



        return view;
    }

    private void initializeViewStatus () {
        view.findViewById(R.id.segmentDifficultyLayout1ID).setVisibility(View.GONE);
        view.findViewById(R.id.segmentDifficultyLayout2ID).setVisibility(View.GONE);
        view.findViewById(R.id.segmentDifficultyLayout3ID).setVisibility(View.GONE);

        cityName = view.findViewById(R.id.cityNameViewID);
        locationName = view.findViewById(R.id.locationNameViewID);
        circuitName = view.findViewById(R.id.circuitNameViewID);

        if (checkpointCount == 1)
            view.findViewById(R.id.segmentDifficultyLayout1ID).setVisibility(View.VISIBLE);
        if (checkpointCount == 2)
            view.findViewById(R.id.segmentDifficultyLayout2ID).setVisibility(View.VISIBLE);
        if (checkpointCount == 3)
            view.findViewById(R.id.segmentDifficultyLayout3ID).setVisibility(View.VISIBLE);

        // Initialize the view on resuming the fragment if there are values already inserted
        if (sessionManager.getCurrentCircuit() != null) {
            SessionManager.Circuit circ = sessionManager.getCurrentCircuit();
            if (circ.getCircuitName() != null) circuitName.setText(circ.getCircuitName());
            if (circ.getLocationName() != null) locationName.setText(circ.getLocationName());
            if (circ.getCityName() != null) cityName.setText(circ.getCityName());
            dropdownModeOptions.setText(dropdownModeOptions.getAdapter().getItem(circ.getCircuitType().ordinal()).toString(), false);
        }


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
            circuitType = SessionManager.CircuitType.values()[position];
        });

        dropdownSegmentOneDifficultyOptions.setOnItemClickListener((parent, v, position, id) -> {
            setSegmentDifficulty(0, SessionManager.SegmentDifficulty.values()[position]);
        });
        dropdownSegmentTwoDifficultyOptions.setOnItemClickListener((parent, v, position, id) -> {
            setSegmentDifficulty(1, SessionManager.SegmentDifficulty.values()[position]);
        });
        dropdownSegmentThreeDifficultyOptions.setOnItemClickListener((parent, v, position, id) -> {
            setSegmentDifficulty(2, SessionManager.SegmentDifficulty.values()[position]);
        });
    }

    private void setSegmentDifficulty(int segmentIndex, SessionManager.SegmentDifficulty difficulty) {
        while (segmentDifficulties.size() <= segmentIndex) {
            segmentDifficulties.add(null);
        }
        segmentDifficulties.set(segmentIndex, difficulty);
    }


    private void saveCircuit() {
        circuit = circuitName.getText().toString().trim();
        city = cityName.getText().toString().trim();
        location = locationName.getText().toString().trim();

        if (!validateInput()) {
            return;
        }
        sessionManager.createCircuit(circuit, city, location, circuitType, checkpointCount);

        SessionManager.Circuit currentCircuit = sessionManager.getCurrentCircuit();
        for (int segmentIndex = 0; segmentIndex < segmentDifficulties.size(); segmentIndex++) {
            currentCircuit.setOneSegmentDifficulty(segmentIndex, segmentDifficulties.get(segmentIndex));
        }

        CircuitRequest request = new CircuitRequest(
            currentCircuit.getCircuitName(),
            convertCircuitTypeToString(currentCircuit.getCircuitType()),
            currentCircuit.getCityName(),
            currentCircuit.getLocationName(),
            currentCircuit.getCreationDate()
        );

        CircuitRepository.getInstance().saveCircuit(request, new CircuitRepository.CircuitCallback() {
            @Override
            public void onSuccess(CircuitResponse response) {
                showToast("Circuit saved!");
                int circuitId = response.getCircuitId();
                int locationId = response.getCircuitLocationId();
            }

            @Override
            public void onError(String error) {
                Log.e("UI", error);
            }
        });
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(circuit)) {
            showToast("Please enter circuit name!");
            return false;
        }
        if (TextUtils.isEmpty(city)) {
            showToast("Please enter city name!");
            return false;
        }
        if (TextUtils.isEmpty(location)) {
            showToast("Please enter location name!");
            return false;
        }
        if (circuitType == null) {
            showToast("Please select a circuit type");
            return false;
        }

        return true;
    }

    private String convertCircuitTypeToString(SessionManager.CircuitType circuitType) {
        switch (circuitType) {
            case DRIVER_CIRCUIT: return "Driver Circuit";
            case LINE_FOLLOWER_CIRCUIT: return "Line Follower Circuit";
            default: return null;
        }
    }

    private void showToast (String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}