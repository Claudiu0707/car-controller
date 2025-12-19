package com.example.carcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicesConnected implements DevicesConnectedStore {
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
}
