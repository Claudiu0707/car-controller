package com.example.carcontroller.Settings;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carcontroller.Bluetooth.BluetoothService;
import com.example.carcontroller.CarDevice;
import com.example.carcontroller.DeviceManager;
import com.example.carcontroller.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class CarSettingsFragment extends Fragment {
    private static final String TAG = "CarSettingsFragment";

    BluetoothService bluetoothService = BluetoothService.getInstance();
    DeviceManager deviceManager = DeviceManager.getInstance();
    CarDevice carDevice = deviceManager.getCarDevice();

    private Context context;
    int optionIndex = 0;
    String carDeviceAddress = null;
    BluetoothSocket socket = null;

    private EditText Kp, Ki, Kd, baseSpeedLeft, baseSpeedRight;
    MaterialAutoCompleteTextView dropdownModeOptions;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_car_settings, container, false);
        context = requireContext();

        dropdownModeOptions = view1.findViewById(R.id.inputTVID);
        Kp = view1.findViewById(R.id.kpETID);
        Ki = view1.findViewById(R.id.kiETID);
        Kd = view1.findViewById(R.id.kdETID);
        baseSpeedLeft = view1.findViewById(R.id.baseSpeedLeftETID);
        baseSpeedRight = view1.findViewById(R.id.baseSpeedRightETID);

        // Buttons initialization
        Button backButton = view1.findViewById(R.id.backButtonID);
        Button loadDataButton = view1.findViewById(R.id.loadDataButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(view -> {
            close();
        });
        loadDataButton.setOnClickListener(view -> {
            calibrateLineFollowerData();
        });

        dropdownModeOptions.setOnItemClickListener((parent, view, position, id) -> {
            CarDevice.OperationMode[] operationModes = CarDevice.OperationMode.values();
            CarDevice.OperationMode mode = operationModes[position];
            carDevice.setOperationMode(mode);
        });

        return view1;
    }

    private void calibrateLineFollowerData () {
        float kp = 0, ki = 0, kd = 0, dBaseSpeedLeft = 0, dBaseSpeedRight = 0;
        try {
            kp = Float.parseFloat(Kp.getText().toString());
            ki = Float.parseFloat(Ki.getText().toString());
            kd = Float.parseFloat(Kd.getText().toString());
            dBaseSpeedLeft = Float.parseFloat(baseSpeedLeft.getText().toString());
            dBaseSpeedRight = Float.parseFloat(baseSpeedRight.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid data format!", Toast.LENGTH_LONG).show();
        }

        String logMessage;
        if (!carDevice.configurePID(kp, ki, kd, dBaseSpeedLeft, dBaseSpeedRight)) {
            logMessage = "Invalid data ranges!";
        } else {
            if (carDevice.uploadPID()) logMessage = "Data uploaded!";
            else logMessage = "Data upload failed!";
        }
        // Log.i(TAG, logMessage);
        Toast.makeText(context, logMessage, Toast.LENGTH_LONG).show();
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.settings_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}