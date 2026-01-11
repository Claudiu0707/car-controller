package com.example.carcontroller.main_package.activities_package;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcontroller.api_package.DriverRepository;
import com.example.carcontroller.api_package.RaceRepository;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.Device;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.main_package.Commands;
import com.example.carcontroller.R;
import com.example.carcontroller.main_package.SessionManager;


public class DriverModeActivity extends AppCompatActivity {
    private static final String TAG = "DriverModeActivityTAG";

    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final RaceRepository raceRepository = RaceRepository.getInstance();
    Button backButton, forwardButton, reverseButton, steerLeftButton, steerRightButton;
    CarDevice carDevice;
    Button startSessionButton;
    Button endSessionButton;
    private boolean isForward = false, isReverse = false, isLeft = false, isRight = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!deviceManager.hasCarDevice()) {
            Toast.makeText(this, "Please connect a device!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        carDevice = deviceManager.getCarDevice();

        initializeViews();

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            finish();
        });

        if (carDevice.getDeviceStatus() == Device.DeviceStatus.CONNECTED) {
            startSessionButton.setOnClickListener(v -> {
                sessionManager.startNewRaceSession("dummyCircuitName"); // TODO: delete this and replace with proper initialization
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

    private void createRace() {
        raceRepository.saveRace(sessionManager.getCurrentSession(), new RaceRepository.RaceCallback() {
            @Override
            public void onSuccess(RaceResponse race) {
                Log.d(TAG, "Race session saved with id: " + race.getRaceId());
                Toast.makeText(DriverModeActivity.this, "Race saved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DriverModeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeViews () {
        // Buttons initialization
        backButton = (Button) findViewById(R.id.backButtonID);
        forwardButton = (Button) findViewById(R.id.forwardButtonID);
        reverseButton = (Button) findViewById(R.id.reverseButtonID);
        steerLeftButton = (Button) findViewById(R.id.steerLeftButtonID);
        steerRightButton = (Button) findViewById(R.id.steerRightButtonID);

        startSessionButton = (Button) findViewById(R.id.startSessionButtonID);
        endSessionButton = (Button) findViewById(R.id.endSessionButtonID);
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
}