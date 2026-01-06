package com.example.carcontroller.Main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcontroller.CarDevice;
import com.example.carcontroller.DeviceManager;
import com.example.carcontroller.R;


public class DriverModeActivity extends AppCompatActivity {
    private static final String TAG = "DriverModeActivityTAG";
    DeviceManager deviceManager = DeviceManager.getInstance();
    CarDevice carDevice;

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

        if (deviceManager.hasCarDevice()) {
            carDevice = deviceManager.getCarDevice();
        }

        // Buttons initialization
        Button backButton = (Button) findViewById(R.id.backButtonID);

        Button forwardButton = (Button) findViewById(R.id.forwardButton);
        Button reverseButton = (Button) findViewById(R.id.reverseButton);
        Button steerLeftButton = (Button) findViewById(R.id.steerLeftButton);
        Button steerRightButton = (Button) findViewById(R.id.steerRightButton);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            finish();
        });

        if (carDevice.isConnected()) {
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
}