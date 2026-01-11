package com.example.carcontroller.devices_package;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.carcontroller.bluetooth_package.BluetoothService;
import com.example.carcontroller.main_package.Commands;
import com.example.carcontroller.main_package.SessionManager;

import java.io.IOException;

public class CheckpointDevice extends Device {
    private static final String TAG = "CheckpointDeviceTAG";

    SessionManager sessionManager = SessionManager.getInstance();

    private final BluetoothSocket bluetoothSocket;
    private final BluetoothService bluetoothService;

    private long detectionTime;
    private int distanceDetectionThreshold;     // Car detection threshold distance
    private int distanceFromPreviousCheckpoint; // Distance interval between this checkpoint and previous one
    private final int checkpointIndex;

    private boolean carDetected;

    public CheckpointDevice (String deviceAddress, String deviceName, BluetoothSocket socket, int checkpointIndex) {
        super(deviceAddress, deviceName, DeviceType.CHECKPOINT);
        this.bluetoothService = BluetoothService.getInstance();
        this.bluetoothSocket = socket;
        this.checkpointIndex = checkpointIndex;
        this.distanceDetectionThreshold = 10;
        this.carDetected = false;

        bluetoothService.registerListener(deviceAddress, this);
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
    public void onDataReceived (String deviceAddress, byte[] data) {
        if (getDeviceAddress().contentEquals(deviceAddress)) {
            String dataString = new String(data);
            processCheckpointData(dataString);
            Log.d(TAG, dataString);
        }
    }

    @Override
    public boolean isConnected () {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && getDeviceStatus() == DeviceStatus.CONNECTED;
    }

    public void processCheckpointData (String data) {
        String commandStringDetected = Commands.DETECTED.getCommand();
        String commandStringNotDetected = Commands.NOTDETECTED.getCommand();

        if (data.contentEquals(commandStringDetected)) {
            carDetected = true;
            detectionTime = System.currentTimeMillis();
            SessionManager.RaceSession currentSession = sessionManager.getCurrentSession();
            if (currentSession != null) {
                currentSession.recordCheckpointTime(checkpointIndex, detectionTime);
            }
            Log.i(TAG, "Checkpoint " + checkpointIndex + " detected object!");
        } else if (data.contentEquals(commandStringNotDetected)) {
            carDetected = false;
            detectionTime = 0;
        } else {
            Log.e(TAG, "Data format incorrect");
        }
    }

    public long getDetectionTime () {
        return detectionTime;
    }

    public void setDetectionTime (long detectionTime) {
        this.detectionTime = detectionTime;
    }

    public void setDistanceDetectionThreshold (int distanceDetectionThreshold) {
        this.distanceDetectionThreshold = distanceDetectionThreshold;
    }

    public void setDistanceFromPreviousCheckpoint (int distanceFromPreviousCheckpoint) {
        this.distanceFromPreviousCheckpoint = distanceFromPreviousCheckpoint;
    }

    public boolean isCarDetected () {
        return carDetected;
    }

    public int getDistanceFromPreviousCheckpoint () {
        return distanceFromPreviousCheckpoint;
    }


}
