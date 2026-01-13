package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.carcontroller.R;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Buttons initialization
        Button backButton = view.findViewById(R.id.backButtonID);
        Button bluetoothSettingsButton = view.findViewById(R.id.bluetoothSettingsButtonID);
        Button carSettingsButton = view.findViewById(R.id.carSettingsButtonID);
        Button databaseSettingsButton = view.findViewById(R.id.databaseSettingsButtonID);
        Button createCircuitButton = view.findViewById(R.id.circuitCreateButtonID);


        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            close();
        });
        bluetoothSettingsButton.setOnClickListener(v -> {
            open(new BluetoothManagerFragment());
        });
        carSettingsButton.setOnClickListener(v -> {
            open(new CarSettingsFragment());
        });
        createCircuitButton.setOnClickListener(v -> {
            open(new CircuitCreatorFragment());
        });
        databaseSettingsButton.setOnClickListener(v -> {
            // Sometimes :)))
        });

        return view;
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}