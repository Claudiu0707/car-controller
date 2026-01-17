package com.example.carcontroller.main_package.fragments_package;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carcontroller.R;
import com.example.carcontroller.api_package.RaceRepository;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.Device;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.Commands;
import com.example.carcontroller.main_package.SessionManager;

/**
 * Fragment that provides manual driving controls for the car device.
 */
public class DriveModeFragment extends Fragment {
    private static final String TAG = "DriveModeFragment";

    // ============ Managers & repositories ============
    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final RaceRepository raceRepository = RaceRepository.getInstance();

    // ============ UI elements ============
    private Button backButton;
    private Button forwardButton;
    private Button reverseButton;
    private Button steerLeftButton;
    private Button steerRightButton;
    private Button startSessionButton;
    private Button endSessionButton;

    // ============ Device ============
    private CarDevice carDevice;

    // ============ Drive state flags ============
    private boolean isForward = false;
    private boolean isReverse = false;
    private boolean isLeft = false;
    private boolean isRight = false;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_drive_mode, container, false);

        carDevice = deviceManager.getCarDevice();

        initializeViews();
        initializeListeners();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Initializes all UI components.
     */
    private void initializeViews() {
        backButton = rootView.findViewById(R.id.backButtonID);

        forwardButton = rootView.findViewById(R.id.forwardButtonID);
        reverseButton = rootView.findViewById(R.id.reverseButtonID);
        steerLeftButton = rootView.findViewById(R.id.steerLeftButtonID);
        steerRightButton = rootView.findViewById(R.id.steerRightButtonID);

        startSessionButton = rootView.findViewById(R.id.startSessionButtonID);
        endSessionButton = rootView.findViewById(R.id.endSessionButtonID);

        if (sessionManager.getCurrentCircuit() == null) {
            startSessionButton.setVisibility(View.GONE);
            endSessionButton.setVisibility(View.GONE);
        }
        else {
            startSessionButton.setVisibility(View.VISIBLE);
            endSessionButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Registers click and touch listeners for all controls.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initializeListeners() {
        backButton.setOnClickListener(v -> close());

        if (carDevice.getDeviceStatus() != Device.DeviceStatus.CONNECTED) {
            Toast.makeText(requireContext(), "Car not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        startSessionButton.setOnClickListener(v -> {
            sessionManager.createNewRaceSession();
            sessionManager.startRaceSession();
        });

        endSessionButton.setOnClickListener(v -> {
            sessionManager.finishRaceSession();
            saveRace();
        });

        forwardButton.setOnTouchListener(createDirectionalTouchListener(
                () -> isForward = true,
                () -> isForward = false
        ));

        reverseButton.setOnTouchListener(createDirectionalTouchListener(
                () -> isReverse = true,
                () -> isReverse = false
        ));

        steerLeftButton.setOnTouchListener(createDirectionalTouchListener(
                () -> isLeft = true,
                () -> isLeft = false
        ));

        steerRightButton.setOnTouchListener(createDirectionalTouchListener(
                () -> isRight = true,
                () -> isRight = false
        ));
    }

    /**
     * Creates a reusable touch listener for directional buttons.
     *
     * @param onPress   action when button is pressed
     * @param onRelease action when button is released
     * @return touch listener
     */
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener createDirectionalTouchListener(Runnable onPress, Runnable onRelease) {
        return (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                onPress.run();
                sendDriveCommand();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                onRelease.run();
                sendDriveCommand();
                return true;
            }
            return false;
        };
    }

    /**
     * Persists the current race session to the backend.
     */
    private void saveRace() {
        raceRepository.saveRace(sessionManager.getCurrentSession(), new RaceRepository.RaceCallback() {
            @Override
            public void onSuccess(RaceResponse race) {
                sessionManager.getCurrentSession().setRaceId(race.getRaceId());
                Log.d(TAG, "Race ID: " + race.getRaceId());
                Toast.makeText(requireContext(), "Race saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Determines and sends the appropriate driving command
     * based on the current directional state.
     */
    private void sendDriveCommand() {
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

    /**
     * Closes the fragment and returns to the previous screen.
     */
    private void close() {
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}
