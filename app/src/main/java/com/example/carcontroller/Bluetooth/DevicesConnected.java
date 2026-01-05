package com.example.carcontroller.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DevicesConnected implements DevicesConnectedStore {
    private static final String TAG = "DevicesConnectedTAG";
    private static DevicesConnected instance;

    private final Map<String, BluetoothDevice> deviceMap = new HashMap<>();
    private final Map<String, BluetoothSocket> socketMap = new HashMap<>();

    private DevicesConnected () {
    }

    public static synchronized DevicesConnected getInstance () {
        if (instance == null) instance = new DevicesConnected();
        return instance;
    }

    @Override
    public synchronized void addConnection (BluetoothDevice device, BluetoothSocket socket) {
        String address = device.getAddress();
        deviceMap.put(address, device);
        socketMap.put(address, socket);
    }

    @Override
    public synchronized List<BluetoothDevice> getDevices () {
        return new ArrayList<>(deviceMap.values());
    }

    @Override
    public synchronized BluetoothSocket getSocket (String deviceAddress) {
        return socketMap.get(deviceAddress);
    }

    @Override
    public synchronized void disconnectAllDevices () {
        for (BluetoothSocket socket : socketMap.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close socket", e);
            }
        }
        deviceMap.clear();
        socketMap.clear();
    }

    @Override
    public void disconnectDevice (String deviceAddress) {
        BluetoothSocket socket = socketMap.get(deviceAddress);
        if (socket != null) {
            try {
                socket.close();
                socketMap.remove(deviceAddress);
            } catch (IOException e) {
                Log.e(TAG, "Could not close socket", e);
            }
        }
    }
}
