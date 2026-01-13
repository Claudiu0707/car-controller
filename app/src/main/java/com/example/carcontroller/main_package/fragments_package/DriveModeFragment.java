package com.example.carcontroller.main_package.fragments_package;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.carcontroller.R;
import com.example.carcontroller.api_package.RaceRepository;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.Device;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.Commands;
import com.example.carcontroller.main_package.SessionManager;


public class DriveModeFragment extends Fragment {
    private static final String TAG = "DriveModeFragment";

    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final RaceRepository raceRepository = RaceRepository.getInstance();
    Button backButton, forwardButton, reverseButton, steerLeftButton, steerRightButton;
    Button startSessionButton, endSessionButton;
    CarDevice carDevice;
    private boolean isForward = false, isReverse = false, isLeft = false, isRight = false;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_drive_mode, container, false);

        carDevice = deviceManager.getCarDevice();

        initializeViews();
        initializeOnClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void initializeViews () {
        // Buttons initialization
        backButton = (Button) view.findViewById(R.id.backButtonID);

        forwardButton = (Button) view.findViewById(R.id.forwardButtonID);
        reverseButton = (Button) view.findViewById(R.id.reverseButtonID);
        steerLeftButton = (Button) view.findViewById(R.id.steerLeftButtonID);
        steerRightButton = (Button) view.findViewById(R.id.steerRightButtonID);

        startSessionButton = (Button) view.findViewById(R.id.startSessionButtonID);
        endSessionButton = (Button) view.findViewById(R.id.endSessionButtonID);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeOnClickListeners() {
        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            close();
        });

        if (carDevice.getDeviceStatus() == Device.DeviceStatus.CONNECTED) {
            startSessionButton.setOnClickListener(v -> {
                sessionManager.startRaceSession();
                // sessionManager.startNewRaceSession("dummyCircuitName", null, null); // TODO: delete this and replace with proper initialization
            });

            endSessionButton.setOnClickListener(v -> {
                sessionManager.finishRaceSession();
                // createRace();    // TODO: finish this
            });

            forwardButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isForward = true;
                    sendDriveCommand();
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isForward = false;
                    sendDriveCommand();
                    return true;
                }
                return false;
            });
            reverseButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isReverse = true;
                    sendDriveCommand();
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isReverse = false;
                    sendDriveCommand();
                    return true;
                }
                return false;
            });
            steerLeftButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isLeft = true;
                    sendDriveCommand();
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isLeft = false;
                    sendDriveCommand();
                    return true;
                }
                return false;
            });
            steerRightButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isRight = true;
                    sendDriveCommand();
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isRight = false;
                    sendDriveCommand();
                    return true;
                }
                return false;
            });
        }
    }
    private void sendDriveCommand () {
        Commands command;
        if (isForward && isLeft) {
            command = Commands.FORWARDLEFT;
        } else if (isForward && isRight) {
            command = Commands.FORWARDRIGHT;
        } else if (isReverse && isLeft) {
            command = Commands.REVERSLEFT;
        } else if (isReverse && isRight) {
            command = Commands.REVERSERIGHT;
        } else if (isForward) {
            command = Commands.FORWARD;
        } else if (isReverse) {
            command = Commands.REVERSE;
        } else if (isLeft) {
            command = Commands.LEFT;
        } else if (isRight) {
            command = Commands.RIGHT;
        } else {
            command = Commands.STOP;
        }
        carDevice.sendCommand(command);
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}