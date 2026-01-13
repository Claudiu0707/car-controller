package com.example.carcontroller.main_package.fragments_package;

import android.annotation.SuppressLint;
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

import com.example.carcontroller.api_package.ConfigurationRepository;
import com.example.carcontroller.api_package.models_package.CarConfigurationResponse;
import com.example.carcontroller.bluetooth_package.BluetoothService;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class CarSettingsFragment extends Fragment {
    private static final String TAG = "CarSettingsFragmentTAG";

    DeviceManager deviceManager = DeviceManager.getInstance();
    CarDevice carDevice = deviceManager.getCarDevice();
    ConfigurationRepository configurationRepository = ConfigurationRepository.getInstance();

    Button backButton, loadDataButton;
    private Context context;
    View view;
    private EditText Kp, Ki, Kd, baseSpeedLeft, baseSpeedRight;
    MaterialAutoCompleteTextView dropdownModeOptions;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_car_settings, container, false);
        context = requireContext();

        initializeViews();
        initializeOnClickListeners();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initializeViews() {
        dropdownModeOptions = view.findViewById(R.id.inputModeID);
        Kp = view.findViewById(R.id.kpETID);
        Ki = view.findViewById(R.id.kiETID);
        Kd = view.findViewById(R.id.kdETID);
        baseSpeedLeft = view.findViewById(R.id.baseSpeedLeftETID);
        baseSpeedRight = view.findViewById(R.id.baseSpeedRightETID);

        // Buttons initialization
        backButton = view.findViewById(R.id.backButtonID);
        loadDataButton = view.findViewById(R.id.loadDataButtonID);
        if (carDevice.getConfiguration() != null) {
            CarDevice.CarConfiguration carConfiguration = carDevice.getConfiguration();

            Kp.setText(Float.toString(carConfiguration.getKp()));
            Ki.setText(Float.toString(carConfiguration.getKi()));
            Kd.setText(Float.toString(carConfiguration.getKd()));
            baseSpeedLeft.setText(Float.toString(carConfiguration.getBaseLeftSpeed()));
            baseSpeedRight.setText(Float.toString(carConfiguration.getBaseRightSpeed()));
        }
    }

    private void initializeOnClickListeners () {
        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            close();
        });
        loadDataButton.setOnClickListener(v -> {
            calibrateLineFollowerData();
            createConfiguration();
        });

        dropdownModeOptions.setOnItemClickListener((parent, v, position, id) -> {
            CarDevice.OperationMode[] operationModes = CarDevice.OperationMode.values();
            CarDevice.OperationMode mode = operationModes[position];
            carDevice.setOperationMode(mode);
        });
    }
    private void createConfiguration() {
        configurationRepository.saveConfiguration(carDevice, new ConfigurationRepository.ConfigurationCallback() {
            @Override
            public void onSuccess(CarConfigurationResponse configuration) {
                Toast.makeText(requireContext(), "Configuration saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
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
        if (!carDevice.createConfiguration(kp, ki, kd, dBaseSpeedLeft, dBaseSpeedRight)) {
            logMessage = "Invalid data ranges!";
        } else {
            if (carDevice.uploadPID()) logMessage = "Data uploaded!";
            else logMessage = "Data upload failed!";
        }
        Toast.makeText(context, logMessage, Toast.LENGTH_LONG).show();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}