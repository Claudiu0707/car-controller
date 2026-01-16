package com.example.carcontroller.devices_package;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  DeviceManager creates and manages all main devices along with their operations. Every class that works with the bluetooth devices
 *  (either a car device or checkpoints devices) should get an instance of this class.
 *
 *  Class is a singleton to guarantee at any time only one instance of the manager is created.
 * */
public class DeviceManager {
    private static final String TAG = "DeviceManagerTAG";
    private static DeviceManager instance;

    private final Map<Integer, CheckpointDevice> checkpoints;
    private final List<Device> allDevices;
    private CarDevice carDevice = null;

    private DeviceManager () {
        checkpoints = new HashMap<>();
        allDevices = new ArrayList<>();
    }

    public static synchronized DeviceManager getInstance () {
        if (instance == null) instance = new DeviceManager();
        return instance;
    }

    /**
     * Registers to the device manager a new car device.
     *
     * @param deviceAddress - the MAC address of the device
     * @param deviceName - the name of the device
     * @param bluetoothSocket - the bluetooth socket of the device
     * */
    public void registerCarDevice (String deviceAddress, String deviceName, BluetoothSocket bluetoothSocket) {
        carDevice = new CarDevice(deviceAddress, deviceName, bluetoothSocket);
        allDevices.add(carDevice);
        Log.i(TAG, "Car device registered successfully!");
    }

    /**
     * Registers to the device manager a new checkpoint device.
     *
     * @param deviceAddress - the MAC address of the device
     * @param deviceName - the name of the device
     * @param bluetoothSocket - the bluetooth socket of the device
     * @param checkpointIndex - 1, 2, 3, etc. - it should correspond to the order of connection and placement on the circuit
     * */
    public void registerCheckpointDevice (String deviceAddress, String deviceName, BluetoothSocket bluetoothSocket, int checkpointIndex) {
        if (checkpointIndex < 1) {
            Log.e(TAG, "Invalid checkpoint index: " + checkpointIndex);
            return;
        }
        CheckpointDevice checkpointDevice = new CheckpointDevice(deviceAddress, deviceName, bluetoothSocket, checkpointIndex);
        checkpoints.put(checkpointIndex, checkpointDevice);
        allDevices.add(checkpointDevice);
        Log.i(TAG, "Checkpoint device registered successfully!");
    }

    /**
     *  Disconnects all bluetooth devices and closes all sockets
     *
     *  @return true - if all devices were connected successfully, false - otherwise
     * */
    public boolean disconnectAllDevices () {
        for (Device device : allDevices) {
            if (!device.disconnect())
                return false;
        }
        return true;
    }

    /**
     * @return CarDevice - the car device that is connected
     * */
    public CarDevice getCarDevice () {
        return carDevice;
    }

    /**
     * @param checkpointIndex - 1, 2, 3, etc. - it should correspond to the order of connection and placement on the circuit
     *
     * @return CheckpointDevice - the checkpoint device with the corresponding index that is connected
     * */
    public CheckpointDevice getCheckpointDevice (int checkpointIndex) {
        return checkpoints.get(checkpointIndex);
    }

    /**
     *  @return A list of all checkpoints that have been registered
     * */
    public List<CheckpointDevice> getAllCheckpointDevices () {
        return new ArrayList<>(checkpoints.values());
    }

    /**
     * @return A count of all checkpoints that have been registered
     * */
    public int getCheckpointsCount () {
        return checkpoints.size();
    }

    /**
     * Makes a copy of the device list and returns it
     * @return List<Device> - a copy of the device list
     * */
    public List<Device> getAllDevices () {
        return new ArrayList<>(allDevices);
    }

    /**
     * Checks if there is a car device connected
     * @return true - if there is a car device connected, false - otherwise
     * */
    public boolean hasCarDevice () {
        return carDevice != null && carDevice.isConnected();
    }

    /**
     * Checks the existence of a checkpoint device using its relative connection index. The indexes are automatically
     * assigned during the connection sequence, corresponding to the order of selection by the user
     *
     * @param checkpointIndex - 1, 2, 3, etc. - it should correspond to the order of connection and placement on the circuit
     * @return true - if there exists a checkpoint device with the given index, false - otherwise
     *
     * */
    public boolean hasCheckpointDevice (int checkpointIndex) {
        return checkpoints.get(checkpointIndex) != null;
    }
}
