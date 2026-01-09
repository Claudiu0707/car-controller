package com.example.carcontroller.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.carcontroller.devices.Device;
import com.example.carcontroller.devices.DeviceManager;

import java.io.IOException;
import java.util.UUID;
public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThreadTAG";

    private final BluetoothDevice deviceToConnect;
    private final BluetoothSocket targetSocket;
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;

    DeviceManager deviceManager = DeviceManager.getInstance();
    Device.DeviceType type;
    private int checkpointIndex;

    public ConnectThread (BluetoothDevice device, BluetoothManager manager, Context context, Device.DeviceType type) {
        this.bluetoothAdapter = manager.getAdapter();
        this.deviceToConnect = device;
        this.context = context;
        this.type = type;
        BluetoothSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = deviceToConnect.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        } else {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission");
        }
        targetSocket = tmp;
    }

    public ConnectThread (BluetoothDevice device, BluetoothManager manager, Context context, Device.DeviceType type, int checkpointIndex) {
        this.bluetoothAdapter = manager.getAdapter();
        this.deviceToConnect = device;
        this.context = context;
        this.type = type;
        this.checkpointIndex = checkpointIndex;
        BluetoothSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = deviceToConnect.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        } else {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission");
        }
        targetSocket = tmp;
    }


    @Override
    public void run() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission");
            return;
        }
        bluetoothAdapter.cancelDiscovery();

        try {
            targetSocket.connect();
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Connected to " + deviceToConnect.getName(), Toast.LENGTH_SHORT).show());

            if (type == Device.DeviceType.CAR) {
                deviceManager.registerCarDevice(deviceToConnect.getAddress(), deviceToConnect.getName(), targetSocket);
                deviceManager.getCarDevice().connect();
            } else if (type == Device.DeviceType.CHECKPOINT) {
                deviceManager.registerCheckpointDevice(deviceToConnect.getAddress(), deviceToConnect.getName(), targetSocket, checkpointIndex);
                deviceManager.getCheckpointDevice(checkpointIndex).connect();
            }
            Log.i(TAG, "Connection successful!");
        } catch (IOException e) {
            Log.e(TAG, "Could not connect; closing socket", e);
            try {
                targetSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "Could not close the client socket", e2);
            }
        }
    }

    public void cancel() {
        try {
            targetSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}