package com.example.carcontroller.main_package.fragments_package;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.carcontroller.api_package.CarConfigurationClient;
import com.example.carcontroller.api_package.ConfigurationRepository;
import com.example.carcontroller.api_package.dto.CarConfigurationDto;
import com.example.carcontroller.api_package.models_package.CarConfigurationResponse;
import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.DeviceManager;
import com.example.carcontroller.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarSettingsFragment extends Fragment {
    private static final String TAG = "CarSettingsFragmentTAG";

    private final DeviceManager deviceManager = DeviceManager.getInstance();
    private final CarDevice carDevice = deviceManager.getCarDevice();
    private final ConfigurationRepository configurationRepository =
            ConfigurationRepository.getInstance();

    private Context context;
    private View view;

    private Button backButton, loadDataButton;
    private EditText Kp, Ki, Kd, baseSpeedLeft, baseSpeedRight;

    private boolean isLoadedFromDatabase = false;

    // dropdown (operation mode)
    private MaterialAutoCompleteTextView dropdownModeOptions;

    // dropdown (saved configurations)
    private MaterialAutoCompleteTextView savedConfigDropdown;

    private CarConfigurationDto[] savedConfigurations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_car_settings, container, false);
        context = requireContext();

        initializeViews();
        initializeOnClickListeners();

        loadSavedConfigurations();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initializeViews() {
        dropdownModeOptions = view.findViewById(R.id.inputModeID);
        savedConfigDropdown = view.findViewById(R.id.savedConfigDropdownID);

        Kp = view.findViewById(R.id.kpETID);
        Ki = view.findViewById(R.id.kiETID);
        Kd = view.findViewById(R.id.kdETID);
        baseSpeedLeft = view.findViewById(R.id.baseSpeedLeftETID);
        baseSpeedRight = view.findViewById(R.id.baseSpeedRightETID);

        backButton = view.findViewById(R.id.backButtonID);
        loadDataButton = view.findViewById(R.id.loadDataButtonID);
    }

    private void initializeOnClickListeners() {
        backButton.setOnClickListener(v -> close());

        loadDataButton.setOnClickListener(v -> {
            calibrateLineFollowerData();

            if (!isLoadedFromDatabase) {
                createConfiguration();
            }
        });

        // Operation mode selection
        dropdownModeOptions.setOnItemClickListener((parent, v, position, id) -> {
            CarDevice.OperationMode[] modes = CarDevice.OperationMode.values();
            carDevice.setOperationMode(modes[position]);
        });

        // Saved configuration selection
        savedConfigDropdown.setOnItemClickListener((parent, v, position, id) -> {
            CarConfigurationDto selected = savedConfigurations[position];
            applyConfigurationToFields(selected);
            isLoadedFromDatabase = true;
            Log.d(TAG, "Loaded configuration ID: " + selected.getId());
        });

        // Detect manual edits
        View.OnFocusChangeListener userEditListener = (v, hasFocus) -> {
            if (hasFocus) isLoadedFromDatabase = false;
        };

        Kp.setOnFocusChangeListener(userEditListener);
        Ki.setOnFocusChangeListener(userEditListener);
        Kd.setOnFocusChangeListener(userEditListener);
        baseSpeedLeft.setOnFocusChangeListener(userEditListener);
        baseSpeedRight.setOnFocusChangeListener(userEditListener);
    }


    // ---------------- BACKEND LOADING ----------------
    private void loadSavedConfigurations() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                CarConfigurationClient client = new CarConfigurationClient();
                CarConfigurationDto[] configs = client.getAllConfigurations();

                requireActivity().runOnUiThread(() -> {
                    savedConfigurations = configs;
                    populateSavedConfigDropdown(configs);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load saved configurations", e);
            }
        });
    }

    private void populateSavedConfigDropdown(CarConfigurationDto[] configs) {
        String[] labels = new String[configs.length];

        for (int i = 0; i < configs.length; i++) {
            labels[i] = configs[i].getDisplayLabel();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labels);
        savedConfigDropdown.setAdapter(adapter);
    }

    // ---------------- APPLY CONFIG ----------------
    private void applyConfigurationToFields(CarConfigurationDto c) {
        Kp.setText(String.valueOf(c.getKp()));
        Ki.setText(String.valueOf(c.getKi()));
        Kd.setText(String.valueOf(c.getKd()));
        baseSpeedLeft.setText(String.valueOf(c.getLeftSpeed()));
        baseSpeedRight.setText(String.valueOf(c.getRightSpeed()));
    }

    // ---------------- SAVE CONFIG ----------------
    private void createConfiguration() {
        configurationRepository.saveConfiguration(carDevice, new ConfigurationRepository.ConfigurationCallback() {
            @Override
            public void onSuccess(CarConfigurationResponse configuration) {
                int id = configuration.getCarConfigurationId();
                carDevice.getConfiguration().setConfigurationId(id);
                Toast.makeText(context, "Configuration saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void calibrateLineFollowerData() {
        try {
            float kp = Float.parseFloat(Kp.getText().toString());
            float ki = Float.parseFloat(Ki.getText().toString());
            float kd = Float.parseFloat(Kd.getText().toString());
            float left = Float.parseFloat(baseSpeedLeft.getText().toString());
            float right = Float.parseFloat(baseSpeedRight.getText().toString());

            if (!carDevice.createConfiguration(kp, ki, kd, left, right)) {
                Toast.makeText(context, "Invalid data ranges!", Toast.LENGTH_LONG).show();
                return;
            }

            Toast.makeText(context, carDevice.uploadPID() ? "Data uploaded!" : "Data upload failed!", Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid data format!", Toast.LENGTH_LONG).show();
        }
    }

    private void close() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}
