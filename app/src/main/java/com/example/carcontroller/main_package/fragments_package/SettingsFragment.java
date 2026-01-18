package com.example.carcontroller.main_package.fragments_package;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.carcontroller.R;
import com.example.carcontroller.devices_package.DeviceManager;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Buttons initialization
        Button backButton = view.findViewById(R.id.backButtonID);
        Button bluetoothSettingsButton = view.findViewById(R.id.bluetoothSettingsButtonID);
        Button carSettingsButton = view.findViewById(R.id.carSettingsButtonID);
        Button createCircuitButton = view.findViewById(R.id.circuitCreateButtonID);


        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            close();
        });
        bluetoothSettingsButton.setOnClickListener(v -> {
            open(new BluetoothManagerFragment());
        });
        carSettingsButton.setOnClickListener(v -> {
            if (DeviceManager.getInstance().hasCarDevice())
                open(new CarSettingsFragment());
            else showToast("Please connect a car");
        });
        createCircuitButton.setOnClickListener(v -> {
            open(new CircuitCreatorFragment());
        });

        return view;
    }

    private void showToast (String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}