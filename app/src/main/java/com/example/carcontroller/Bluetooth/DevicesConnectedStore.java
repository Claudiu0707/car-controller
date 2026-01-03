package com.example.carcontroller.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.List;

public interface DevicesConnectedStore {
    void addDevice (BluetoothDevice device);

    void addConnection (BluetoothDevice device, BluetoothSocket socket);

    void disconnectAllDevices ();
    List<BluetoothDevice> getDevices ();

    BluetoothSocket getSocket (BluetoothDevice device);
}
