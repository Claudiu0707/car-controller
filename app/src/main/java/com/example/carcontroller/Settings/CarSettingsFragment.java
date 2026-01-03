package com.example.carcontroller.Settings;

import static android.content.Context.MODE_PRIVATE;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.carcontroller.Bluetooth.BluetoothService;
import com.example.carcontroller.Bluetooth.DevicesConnected;
import com.example.carcontroller.Main.Commands;
import com.example.carcontroller.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

public class CarSettingsFragment extends Fragment {
    private View view;
    private Context context;

    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    BluetoothDevice carDevice = null;
    BluetoothService service;
    BluetoothSocket socket;


    private Button backButton;
    MaterialAutoCompleteTextView dropdownModeOptions;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_car_settings, container, false);
        context = requireContext();

        dropdownModeOptions = view.findViewById(R.id.inputTVID);
        TextView tv = view.findViewById(R.id.tv);

        if (!devicesConnected.getDevices().isEmpty()) {
            carDevice = devicesConnected.getDevices().get(0);
            List<BluetoothDevice> deviceList = devicesConnected.getDevices();
            service = new BluetoothService();
            socket = devicesConnected.getSocket(carDevice);
            service.initializeStream(socket);
        }
        // Buttons initialization
        backButton = view.findViewById(R.id.backButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(view -> {
            close();
        });

        dropdownModeOptions.setOnItemClickListener((parent, view, position, id) -> {
            int operationMode = position;
            String text = "empty";

            Commands command = Commands.EMPTY;
            switch (operationMode) {
                case 0:
                    text = "SETUP";
                    command = Commands.SETUPMODE;
                    break;
                case 1:
                    text = "DRIVE";
                    command = Commands.DRIVEMODE;
                    break;
                case 2:
                    text = "LINE FOLLOWER";
                    command = Commands.LINEFOLLOWERMODE;
                    break;
            }
            service.write(command.getCommand());
            tv.setText(text);

            SharedPreferences prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putInt("operationMode", operationMode).apply();

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