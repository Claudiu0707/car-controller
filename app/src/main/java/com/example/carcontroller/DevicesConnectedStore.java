package com.example.carcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.ArrayAdapter;

import java.util.List;

public interface DevicesConnectedStore {
    void addDevice (BluetoothDevice device);

    void addConnection (BluetoothDevice device, BluetoothSocket socket);

    List<BluetoothDevice> getDevices ();

    BluetoothSocket getSocket (BluetoothDevice device);
}
