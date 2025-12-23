package com.example.carcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicesConnected implements DevicesConnectedStore {
    private final String TAG = "DevicesConnectedTAG";
    private static DevicesConnected instance;
    private final List<BluetoothDevice> devicesList = new ArrayList<>();
    private final Map<String, BluetoothSocket> socketMap = new HashMap<>();

    private DevicesConnected () {
    }

    public synchronized static DevicesConnected getInstance () {
        if (instance == null) instance = new DevicesConnected();
        return instance;
    }

    @Override
    public void addDevice (BluetoothDevice device) {
        if (!devicesList.contains(device)) devicesList.add(device);
    }

    @Override
    public void addConnection (BluetoothDevice device, BluetoothSocket socket) {
        addDevice(device);
        socketMap.put(device.getAddress(), socket);
    }

    @Override
    public List<BluetoothDevice> getDevices () {
        return devicesList;
    }

    @Override
    public BluetoothSocket getSocket (BluetoothDevice device) {
        return socketMap.get(device.getAddress());
    }

    @Override
    public void disconnectAllDevices () {
        for (BluetoothDevice device : devicesList) {
            devicesList.remove(device);
            try {
                getSocket(device).close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket.");
            }
        }
        devicesList.clear();
        socketMap.clear();
    }
}
