package com.example.carcontroller.Main;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carcontroller.Bluetooth.BluetoothService;
import com.example.carcontroller.Bluetooth.DevicesConnected;
import com.example.carcontroller.R;

import java.util.List;


public class DriverModeActivity extends AppCompatActivity {
    private static final String TAG = "DriverModeActivityTAG";
    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    BluetoothService bluetoothService = BluetoothService.getInstance();

    String carDeviceAddress = null;
    private Button backButton, forwardButton, reverseButton, steerLeftButton, steerRightButton;
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

        // Buttons initialization
        backButton = (Button) findViewById(R.id.backButtonID);

        forwardButton = (Button) findViewById(R.id.forwardButton);
        reverseButton = (Button) findViewById(R.id.reverseButton);
        steerLeftButton = (Button) findViewById(R.id.steerLeftButton);
        steerRightButton = (Button) findViewById(R.id.steerRightButton);
        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            finish();
        });

        // TODO: Clean this snippet of code
        if (!devicesConnected.getDevices().isEmpty()) {
            carDeviceAddress = devicesConnected.getDevices().get(0).getAddress();
            BluetoothSocket socket = devicesConnected.getSocket(carDeviceAddress);
            bluetoothService.initializeStream(carDeviceAddress, socket);


            forwardButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isForward = true;
                    sendDriveCommand(bluetoothService);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isForward = false;
                    sendDriveCommand(bluetoothService);
                    return true;
                }
                return false;
            });
            reverseButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isReverse = true;
                    sendDriveCommand(bluetoothService);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isReverse = false;
                    sendDriveCommand(bluetoothService);
                    return true;
                }
                return false;
            });
            steerLeftButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isLeft = true;
                    sendDriveCommand(bluetoothService);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isLeft = false;
                    sendDriveCommand(bluetoothService);
                    return true;
                }
                return false;
            });
            steerRightButton.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isRight = true;
                    sendDriveCommand(bluetoothService);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    isRight = false;
                    sendDriveCommand(bluetoothService);
                    return true;
                }
                return false;
            });
        }
    }

    private void sendDriveCommand (BluetoothService service) {
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
        service.write(carDeviceAddress, command.getCommand());
    }
}