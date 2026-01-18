package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.carcontroller.R;
import com.example.carcontroller.api_package.RaceCheckpointRepository;
import com.example.carcontroller.api_package.RaceRepository;
import com.example.carcontroller.api_package.models_package.RaceCheckpointRequest;
import com.example.carcontroller.api_package.models_package.RaceCheckpointResponse;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.devices_package.CheckpointDevice;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.SessionManager;

import java.time.Duration;

public class LineFollowModeFragment extends Fragment {
    private static final String TAG = "LineFollowModeFragmentTAG";

    // ============ Managers & repositories ============
    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final RaceRepository raceRepository = RaceRepository.getInstance();

    // ============ UI elements ============
    private Button backButton, startFollowLineButton, stopFollowLineButton;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_line_follow_mode, container, false);
        initializeViews();
        setOnClickListeners();
        return view;
    }

    /**
     * Initializes all UI components.
     */
    private void initializeViews() {
        backButton = (Button) view.findViewById(R.id.backButtonID);
        startFollowLineButton = (Button) view.findViewById(R.id.startFollowLineButtonID);
        stopFollowLineButton = (Button) view.findViewById(R.id.stopFollowLineButtonID);
    }

    /**
     * Registers click and touch listeners for all controls.
     */
    private void setOnClickListeners () {
        backButton.setOnClickListener(v -> {
            close();
        });
        startFollowLineButton.setOnClickListener(v -> {
            deviceManager.getCarDevice().setLineFollowing(true);
            sessionManager.createNewRaceSession();
            sessionManager.startRaceSession();
        });
        stopFollowLineButton.setOnClickListener(v -> {
            deviceManager.getCarDevice().setLineFollowing(false);
            sessionManager.finishRaceSession();
            saveRace();
        });
    }

    private void saveRace() {
        raceRepository.saveRace(sessionManager.getCurrentSession(), new RaceRepository.RaceCallback() {
                    @Override
                    public void onSuccess(RaceResponse race) {
                        sessionManager.getCurrentSession().setRaceId(race.getRaceId());
                        Log.d(TAG, "Race ID: " + race.getRaceId());

                        Toast.makeText(requireContext(), "Race saved", Toast.LENGTH_LONG).show();
                        int checkpointCount = deviceManager.getCheckpointsCount();

                        for (int i = 1; i <= checkpointCount; i++) {
                            CheckpointDevice checkpointDevice = deviceManager.getCheckpointDevice(i);

                            if (checkpointDevice == null) {
                                Log.w(TAG, "Checkpoint device " + i + " not found, skipping");
                                continue;
                            }

                            if (checkpointDevice.getCheckpointId() == null) {
                                Log.w(TAG, "Checkpoint " + i + " has no DB id, skipping");
                                continue;
                            }

                            double detectionTime = checkpointDevice.getDetectionTime() / 1000.0;;

                            if (detectionTime <= 0) {
                                Log.w(TAG, "Checkpoint " + i + " was not triggered, skipping");
                                continue;
                            }

                            long timeStampSeconds = SessionManager.getInstance().getCurrentSession().getCheckpointTime(i);
                            String passedTime= Duration.ofMillis(timeStampSeconds).toString();

                            RaceCheckpointRequest request = new RaceCheckpointRequest(
                                    race.getRaceId(),
                                    checkpointDevice.getCheckpointId(),
                                    passedTime
                            );
                            RaceCheckpointRepository.getInstance().saveRaceCheckpoint(request, new RaceCheckpointRepository.RaceCheckpointCallback() {
                                @Override
                                public void onSuccess(RaceCheckpointResponse response) {
                                    Log.d(TAG, "RaceCheckpoint saved: " + response.getRaceCheckpointId() + " (checkpoint " + response.getCheckpointId() + ")");
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Failed to save RaceCheckpoint: " + error);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(requireContext(), "Error saving race: " + error, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}