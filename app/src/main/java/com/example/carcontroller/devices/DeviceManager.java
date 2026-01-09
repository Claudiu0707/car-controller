package com.example.carcontroller.devices;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceManager {
    private static final String TAG = "DeviceManagerTAG";
    private static DeviceManager instance;

    private CarDevice carDevice = null;
    private Map<Integer, CheckpointDevice> checkpoints;
    private List<Device> allDevices;

    private DeviceManager () {
        checkpoints = new HashMap<>();
        allDevices = new ArrayList<>();
    }

    public static synchronized DeviceManager getInstance () {
        if (instance == null) instance = new DeviceManager();
        return instance;
    }

    public boolean registerCarDevice (String deviceAddress, String deviceName, BluetoothSocket bluetoothSocket) {
        try {
            carDevice = new CarDevice(deviceAddress, deviceName, bluetoothSocket);
            allDevices.add(carDevice);
            Log.i(TAG, "Car device registered successfully!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Car could not be registered!", e);
            return false;
        }
    }

    public boolean registerCheckpointDevice (String deviceAddress, String deviceName, BluetoothSocket bluetoothSocket, int checkpointIndex) {
        if (checkpointIndex < 1 || checkpointIndex > 3) {
            Log.e(TAG, "Invalid checkpoint index: " + checkpointIndex);
            return false;
        }
        try {
            CheckpointDevice checkpointDevice = new CheckpointDevice(deviceAddress, deviceName, bluetoothSocket, checkpointIndex);
            checkpoints.put(checkpointIndex, checkpointDevice);
            allDevices.add(checkpointDevice);
            Log.i(TAG, "Checkpoint device registered successfully!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Checkpoint " + checkpointIndex + " could not be registered!", e);
            return false;
        }
    }

    public boolean disconnectAllDevices () {
        for (Device device : allDevices) {
            if (!device.disconnect())
                return false;
        }
        return true;
    }

    public CarDevice getCarDevice () {
        return carDevice;
    }

    public CheckpointDevice getCheckpointDevice (int checkpointIndex) {
        return checkpoints.get(checkpointIndex);
    }

    public List<CheckpointDevice> getAllCheckpointDevices () {
        return (List<CheckpointDevice>) checkpoints.values();
    }

    public List<Device> getAllDevices () {
        return allDevices;
    }

    public boolean hasCarDevice () {
        return carDevice != null;
    }

    public boolean hasCheckpointDevice (int checkpointIndex) {
        return checkpoints.get(checkpointIndex) != null;
    }
}
