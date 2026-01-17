package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.carcontroller.R;
import com.example.carcontroller.api_package.CircuitRepository;
import com.example.carcontroller.api_package.TrackSegmentRepository;
import com.example.carcontroller.api_package.models_package.CircuitRequest;
import com.example.carcontroller.api_package.models_package.CircuitResponse;
import com.example.carcontroller.api_package.models_package.TrackSegmentResponse;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.SessionManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for creating and saving a racing circuit.
 */
public class CircuitCreatorFragment extends Fragment {
    private static final String TAG = "CircuitCreatorFragment";

    // ============ UI elements ============
    private EditText cityNameEditText;
    private EditText locationNameEditText;
    private EditText circuitNameEditText;

    private MaterialAutoCompleteTextView circuitTypeDropdown;
    private MaterialAutoCompleteTextView segmentDifficulty1Dropdown;
    private MaterialAutoCompleteTextView segmentDifficulty2Dropdown;
    private MaterialAutoCompleteTextView segmentDifficulty3Dropdown;

    private Button backButton;
    private Button saveCircuitButton;

    private View rootView;

    // ============ State & data ============
    private final SessionManager sessionManager = SessionManager.getInstance();
    private SessionManager.CircuitType circuitType;
    private final List<SessionManager.SegmentDifficulty> segmentDifficulties = new ArrayList<>();
    private int checkpointCount;

    private String circuitName;
    private String cityName;
    private String locationName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_circuit_creator, container, false);

        checkpointCount = DeviceManager.getInstance().getCheckpointsCount();
        initializeSegmentDifficultyList();
        initializeViews();
        initializeButtons();
        initializeListeners();

        Log.d(TAG, "Checkpoint count: " + checkpointCount);
        return rootView;
    }

    /**
     * Initializes the segment difficulty list with null values,
     * based on the number of checkpoints.
     */
    private void initializeSegmentDifficultyList() {
        segmentDifficulties.clear();
        for (int i = 0; i < checkpointCount; i++) {
            segmentDifficulties.add(null);
        }
    }

    /**
     * Initializes view visibility and restores previously entered values
     * from the SessionManager, if available.
     */
    private void initializeViews() {
        rootView.findViewById(R.id.segmentDifficultyLayout1ID).setVisibility(View.GONE);
        rootView.findViewById(R.id.segmentDifficultyLayout2ID).setVisibility(View.GONE);
        rootView.findViewById(R.id.segmentDifficultyLayout3ID).setVisibility(View.GONE);

        cityNameEditText = rootView.findViewById(R.id.cityNameViewID);
        locationNameEditText = rootView.findViewById(R.id.locationNameViewID);
        circuitNameEditText = rootView.findViewById(R.id.circuitNameViewID);

        if (checkpointCount >= 1) {
            rootView.findViewById(R.id.segmentDifficultyLayout1ID).setVisibility(View.VISIBLE);
        }
        if (checkpointCount >= 2) {
            rootView.findViewById(R.id.segmentDifficultyLayout2ID).setVisibility(View.VISIBLE);
        }
        if (checkpointCount >= 3) {
            rootView.findViewById(R.id.segmentDifficultyLayout3ID).setVisibility(View.VISIBLE);
        }

        SessionManager.Circuit existingCircuit = sessionManager.getCurrentCircuit();
        if (existingCircuit != null) {
            if (existingCircuit.getCircuitName() != null) {
                circuitNameEditText.setText(existingCircuit.getCircuitName());
            }
            if (existingCircuit.getLocationName() != null) {
                locationNameEditText.setText(existingCircuit.getLocationName());
            }
            if (existingCircuit.getCityName() != null) {
                cityNameEditText.setText(existingCircuit.getCityName());
            }
        }
    }

    /**
     * Binds UI components to their respective fields.
     */
    private void initializeButtons() {
        backButton = rootView.findViewById(R.id.backButtonID);
        saveCircuitButton = rootView.findViewById(R.id.saveCircuitButtonID);

        circuitTypeDropdown = rootView.findViewById(R.id.inputCircuitTypeID);
        segmentDifficulty1Dropdown = rootView.findViewById(R.id.inputSegmentDifficulty1ID);
        segmentDifficulty2Dropdown = rootView.findViewById(R.id.inputSegmentDifficulty2ID);
        segmentDifficulty3Dropdown = rootView.findViewById(R.id.inputSegmentDifficulty3ID);
    }

    /**
     * Registers all click and selection listeners.
     */
    private void initializeListeners() {
        backButton.setOnClickListener(v -> close());
        saveCircuitButton.setOnClickListener(v -> saveCircuit());

        circuitTypeDropdown.setOnItemClickListener((parent, v, position, id) ->
            circuitType = SessionManager.CircuitType.values()[position]
        );

        segmentDifficulty1Dropdown.setOnItemClickListener((parent, v, position, id) ->
            setSegmentDifficulty(0, mapDifficulty(parent.getItemAtPosition(position).toString()))
        );

        segmentDifficulty2Dropdown.setOnItemClickListener((parent, v, position, id) ->
            setSegmentDifficulty(1, mapDifficulty(parent.getItemAtPosition(position).toString()))
        );

        segmentDifficulty3Dropdown.setOnItemClickListener((parent, v, position, id) ->
            setSegmentDifficulty(2, mapDifficulty(parent.getItemAtPosition(position).toString()))
        );
    }

    /**
     * Assigns a difficulty to a specific segment.
     *
     * @param index      segment index (0-based)
     * @param difficulty selected difficulty
     */
    private void setSegmentDifficulty(int index, SessionManager.SegmentDifficulty difficulty) {
        segmentDifficulties.set(index, difficulty);
    }

    /**
     * Validates user input, creates the circuit locally,
     * and persists it to the backend.
     */
    private void saveCircuit() {
        circuitName = circuitNameEditText.getText().toString().trim();
        cityName = cityNameEditText.getText().toString().trim();
        locationName = locationNameEditText.getText().toString().trim();

        if (!validateInput()) {
            return;
        }

        sessionManager.createCircuit(circuitName, cityName, locationName, circuitType, checkpointCount);

        SessionManager.Circuit currentCircuit = sessionManager.getCurrentCircuit();
        for (int i = 0; i < segmentDifficulties.size(); i++) {
            currentCircuit.setOneSegmentDifficulty(i, segmentDifficulties.get(i));
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
                currentCircuit.setCircuitId(response.getCircuitId());

                for (int i = 0; i < segmentDifficulties.size(); i++) {
                    SessionManager.SegmentDifficulty difficulty = segmentDifficulties.get(i);
                    if (difficulty == null) {
                        Log.w(TAG, "Segment " + i + " has no difficulty selected");
                        continue;
                    }

                    TrackSegmentRepository.getInstance().createTrackSegment(
                            currentCircuit.getCircuitId(),
                            difficulty.ordinal() + 1,
                            new TrackSegmentRepository.TrackSegmentCallback() {
                                @Override
                                public void onSuccess(TrackSegmentResponse segment) {
                                    Log.d(TAG, "Segment created: " + segment.getTrackSegmentId());
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, error);
                                }
                            }
                    );
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, error);
            }
        });
    }

    /**
     * Validates required input fields.
     *
     * @return true if all fields are valid
     */
    private boolean validateInput() {
        if (TextUtils.isEmpty(circuitName)) {
            showToast("Please enter circuit name");
            return false;
        }
        if (TextUtils.isEmpty(cityName)) {
            showToast("Please enter city name");
            return false;
        }
        if (TextUtils.isEmpty(locationName)) {
            showToast("Please enter location name");
            return false;
        }
        if (circuitType == null) {
            showToast("Please select a circuit type");
            return false;
        }
        return true;
    }

    /**
     * Maps a UI string value to a SessionManager.SegmentDifficulty.
     */
    public SessionManager.SegmentDifficulty mapDifficulty(String value) {
        switch (value) {
            case "Very Easy":
                return SessionManager.SegmentDifficulty.VERYEASY;
            case "Easy":
                return SessionManager.SegmentDifficulty.EASY;
            case "Average":
                return SessionManager.SegmentDifficulty.AVERAGE;
            case "Hard":
                return SessionManager.SegmentDifficulty.HARD;
            case "Very Hard":
                return SessionManager.SegmentDifficulty.VERYHARD;
            default:
                throw new IllegalArgumentException("Unknown difficulty: " + value);
        }
    }

    /**
     * Converts a circuit type enum to its display string.
     */
    private String convertCircuitTypeToString(SessionManager.CircuitType type) {
        switch (type) {
            case DRIVER_CIRCUIT:
                return "Driver Circuit";
            case LINE_FOLLOWER_CIRCUIT:
                return "Line Follower Circuit";
            default:
                return null;
        }
    }

    /**
     * Displays a short toast message.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Closes the fragment and returns to the previous screen.
     */
    private void close() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
