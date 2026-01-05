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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carcontroller.Bluetooth.BluetoothService;
import com.example.carcontroller.Bluetooth.DevicesConnected;
import com.example.carcontroller.Main.Commands;
import com.example.carcontroller.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.w3c.dom.Text;

import java.util.List;

public class CarSettingsFragment extends Fragment {
    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    BluetoothService bluetoothService = BluetoothService.getInstance();

    private Context context;
    int operationMode = 0;
    String carDeviceAddress = null;
    BluetoothSocket socket = null;

    private EditText Kp, Ki, Kd, baseSpeed;
    MaterialAutoCompleteTextView dropdownModeOptions;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_car_settings, container, false);
        context = requireContext();

        dropdownModeOptions = view1.findViewById(R.id.inputTVID);
        Kp = view1.findViewById(R.id.kpTVID);
        Ki = view1.findViewById(R.id.kiTVID);
        Kd = view1.findViewById(R.id.kdTVID);
        baseSpeed = view1.findViewById(R.id.baseSpeedTVID);

        if (!devicesConnected.getDevices().isEmpty()) {
            carDeviceAddress = devicesConnected.getDevices().get(0).getAddress();
            socket = devicesConnected.getSocket(carDeviceAddress);
            bluetoothService.initializeStream(carDeviceAddress, socket);
        }
        // Buttons initialization
        Button backButton = view1.findViewById(R.id.backButtonID);
        Button loadDataButton = view1.findViewById(R.id.loadDataButtonID);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(view -> {
            close();
        });
        loadDataButton.setOnClickListener(view -> {
            try {
                calibrateLineFollowerData();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        dropdownModeOptions.setOnItemClickListener((parent, view, position, id) -> {
            operationMode = position;
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
            bluetoothService.write(carDeviceAddress, command.getCommand());

            SharedPreferences prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putInt("operationMode", operationMode).apply();

        });
        return view1;
    }

    private void calibrateLineFollowerData () throws InterruptedException {
        String kpString = Kp.getText().toString();
        String kiString = Ki.getText().toString();
        String kdString = Kd.getText().toString();
        String baseSpeedString = baseSpeed.getText().toString();

        boolean bKp = checkData(0, kpString);
        boolean bKi = checkData(0, kiString);
        boolean bKd = checkData(0, kdString);
        boolean bBaseSpeed = checkData(1, baseSpeedString);

        if (!bKp || !bKi || !bKd || !bBaseSpeed) {
            Toast.makeText(context, "Invalid data format!", Toast.LENGTH_LONG).show();
        } else {
            // TODO: Remove Thread.sleep if nothing breaks along with try-catch block in loadDataButton.onClickListener
            convertLineFollowerDataToInstruction(kpString);
            Thread.sleep(20); // wait 20 ms

            convertLineFollowerDataToInstruction(kiString);
            Thread.sleep(20);

            convertLineFollowerDataToInstruction(kdString);
            Thread.sleep(20);

            convertLineFollowerDataToInstruction(baseSpeedString);
        }
    }

    private void convertLineFollowerDataToInstruction (String rawData) {
        switch (rawData.length()) {
            case 1:
                sendLineFollowerInstruction(Commands.WAITFOR1, rawData);
                break;
            case 2:
                sendLineFollowerInstruction(Commands.WAITFOR2, rawData);
                break;
            case 3:
                sendLineFollowerInstruction(Commands.WAITFOR3, rawData);
                break;
            case 4:
                sendLineFollowerInstruction(Commands.WAITFOR4, rawData);
                break;
            default:
                sendLineFollowerInstruction(Commands.WAITFOR5, rawData);
                break;
        }
    }

    private void sendLineFollowerInstruction (Commands command, String data) {
        if (bluetoothService != null) {
            bluetoothService.write(carDeviceAddress, command.getCommand());
            bluetoothService.write(carDeviceAddress, data);
        }
    }

    private boolean checkData (int type, String data) {
        if (data == null) {
            return false;
        }
        try {
            float value = Float.parseFloat(data);

            if (type == 0) {
                return value >= 0.0f && value <= 100.0f;
            } else if (type == 1) {
                return value >= 80.0f && value <= 255.0f;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.settings_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}