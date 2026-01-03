package com.example.carcontroller.Settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.carcontroller.Bluetooth.BluetoothManagerFragment;
import com.example.carcontroller.R;

public class SettingsFragment extends Fragment {
    private Button backButton, bluetoothSettingsButton, carSettingsButton, databaseSettingsButton;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Buttons initialization
        backButton = view.findViewById(R.id.backButtonID);
        bluetoothSettingsButton = view.findViewById(R.id.bluetoothSettingsButtonID);
        carSettingsButton = view.findViewById(R.id.carSettingsButtonID);
        databaseSettingsButton = view.findViewById(R.id.databaseSettingsButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            requireActivity().finish();
        });
        bluetoothSettingsButton.setOnClickListener(v -> {
            open(new BluetoothManagerFragment());
        });
        carSettingsButton.setOnClickListener(v -> {
            open(new CarSettingsFragment());
        });
        databaseSettingsButton.setOnClickListener(v -> {

        });

        return view;
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.settings_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}