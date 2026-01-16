package com.example.carcontroller.devices_package;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.carcontroller.main_package.Commands;
import com.example.carcontroller.main_package.SessionManager;

import java.io.IOException;

public class CheckpointDevice extends Device {
    private static final String TAG = "CheckpointDeviceTAG";
    SessionManager sessionManager = SessionManager.getInstance();
    private final BluetoothSocket bluetoothSocket;

    private final int checkpointIndex;
    private long detectionTime;
    private boolean carDetected;

    public CheckpointDevice (String deviceAddress, String deviceName, BluetoothSocket socket, int checkpointIndex) {
        super(deviceAddress, deviceName, DeviceType.CHECKPOINT);
        this.bluetoothSocket = socket;
        this.checkpointIndex = checkpointIndex;
        this.carDetected = false;
    }

    @Override
    public void connect () {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                getBluetoothService().initializeStream(getDeviceAddress(), bluetoothSocket);
                setDeviceStatus(DeviceStatus.CONNECTED);
                Log.i(TAG, "Checkpoint device " + getDeviceName() + " with index " + checkpointIndex + " connected successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Connection failed for checkpoint: " + checkpointIndex, e);
            setDeviceStatus(DeviceStatus.ERROR);
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
        if (getBluetoothService()!= null && isConnected()) {
            getBluetoothService().write(getDeviceAddress(), data);
            return true;
        }
        return false;
    }

    /**
     * When the BluetoothService receives data from the device, the device, identified by its address is notified that there is available data.
     * The received data is further processed
     * @param deviceAddress device address
     * @param data data received
     */
    @Override
    public void onDataReceived (String deviceAddress, byte[] data) {
        if (getDeviceAddress().contentEquals(deviceAddress)) {
            String dataString = new String(data);
            processCheckpointData(dataString);
            Log.d(TAG, dataString);
        }
    }

    /**
     * Checks if the device is connected
     * @return true if the device is connected, false otherwise
     */
    @Override
    public boolean isConnected () {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && getDeviceStatus() == DeviceStatus.CONNECTED;
    }

    /**
     * Processes the data received from the device.
     * @param data data received
     */
    public void processCheckpointData (String data) {
        String commandStringDetected = Commands.DETECTED.getCommand();

        // If checkpoint detected a car and no car was previously detected - prevents multiple checkpoints crossings in the same race session
        if (data.contentEquals(commandStringDetected) && !carDetected) {
            carDetected = true;

            // Record the time of detection in the current session
            detectionTime = System.currentTimeMillis();
            SessionManager.RaceSession currentSession = sessionManager.getCurrentSession();
            if (currentSession != null) {
                currentSession.recordCheckpointTime(checkpointIndex, detectionTime);
            }

            Log.i(TAG, "Checkpoint " + checkpointIndex + " detected object!");
        } else {
            Log.e(TAG, "Data format incorrect");
        }
    }

    /**
     * When a new race session is started, this method can be  called to ensure that the checkpoint info is reset
     * */
    public void resetCheckpoint () {
        carDetected = false;
        detectionTime = 0;
    }

    public long getDetectionTime () {
        return detectionTime;
    }

    public void setDetectionTime (long detectionTime) {
        this.detectionTime = detectionTime;
    }

    public boolean isCarDetected () {
        return carDetected;
    }
}
