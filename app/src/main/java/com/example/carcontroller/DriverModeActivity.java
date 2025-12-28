package com.example.carcontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;


public class DriverModeActivity extends AppCompatActivity {
    private static final String TAG = "DriverModeActivityTAG";
    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    BluetoothDevice carDevice = null;

    Button backButton;

    Button forwardButton, reverseButton, steerLeftButton, steerRightButton;

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

        if (!devicesConnected.getDevices().isEmpty()) {
            carDevice = devicesConnected.getDevices().get(0);
        }

        List<BluetoothDevice> deviceList = devicesConnected.getDevices();
        BluetoothService service = new BluetoothService();
        BluetoothSocket socket = devicesConnected.getSocket(carDevice);
        service.initializeStream(socket);

        // Buttons initialization
        backButton = (Button) findViewById(R.id.backButton2);

        forwardButton = (Button) findViewById(R.id.forwardButton);
        reverseButton = (Button) findViewById(R.id.reverseButton);
        steerLeftButton = (Button) findViewById(R.id.steerLeftButton);
        steerRightButton = (Button) findViewById(R.id.steerRightButton);
        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            launchActivityMain();
        });
        forwardButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                service.write("D1");
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                service.write("D0");
                return true;
            }
            return false;
        });
        reverseButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                service.write("D2");
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                service.write("D0");
                return true;
            }
            return false;
        });
        steerLeftButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                service.write("D3");
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                service.write("D0");
                return true;
            }
            return false;
        });
        steerRightButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                service.write("D4");
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                service.write("D0");
                return true;
            }
            return false;
        });
    }

    private void launchActivityMain () {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}