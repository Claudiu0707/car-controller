package com.example.carcontroller;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.carcontroller.Bluetooth.BluetoothService;

import java.io.IOException;

public class CheckpointDevice extends Device {
    private static final String TAG = "CheckpointDeviceTAG";

    private BluetoothSocket bluetoothSocket;
    private BluetoothService bluetoothService;

    private long detectionTime;
    private int distanceDetectionThreshold;     // Car detection threshold distance
    private int distanceFromPreviousCheckpoint; // Distance interval between this checkpoint and previous one
    private int checkpointIndex;

    private boolean carDetected;

    public CheckpointDevice (String deviceAddress, String deviceName, BluetoothSocket socket, int checkpointIndex) {
        super(deviceAddress, deviceName, DeviceType.CHECKPOINT);
        this.bluetoothService = BluetoothService.getInstance();
        this.bluetoothSocket = socket;
        this.checkpointIndex = checkpointIndex;
        this.distanceDetectionThreshold = 10;
        this.carDetected = false;
    }

    @Override
    public boolean connect () {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothService.initializeStream(getDeviceAddress(), bluetoothSocket);
                setDeviceStatus(DeviceStatus.CONNECTED);
                Log.i(TAG, "Checkpoint device " + getDeviceName() + " with index " + checkpointIndex + " connected successfully");
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Connection failed for checkpoint: " + checkpointIndex, e);
            setDeviceStatus(DeviceStatus.ERROR);
            return false;
        }
    }

    @Override
    public boolean disconnect () {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                setDeviceStatus(DeviceStatus.DISCONNECTED);
                Log.i(TAG, "Checkpoint device " + getDeviceName() + " with index " + checkpointIndex + " disconnected successfully");
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
            return false;
        }
    }

    @Override
    public boolean sendData (String data) {
        if (bluetoothService != null && isConnected()) {
            bluetoothService.write(getDeviceAddress(), data);
            return true;
        }
        return false;
    }

    @Override
    public String receiveData () {
        if (isConnected()) {
            return bluetoothService.read(getDeviceAddress());
        }
        return null;
    }

    @Override
    public boolean isConnected () {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && getDeviceStatus() == DeviceStatus.CONNECTED;
    }
}
