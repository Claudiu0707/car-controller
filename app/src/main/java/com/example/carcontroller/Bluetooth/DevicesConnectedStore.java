package com.example.carcontroller.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.List;

public interface DevicesConnectedStore {

    /**
     * Add a device and its associated BluetoothSocket to the store.
     * If the device already exists, the socket will be updated.
     */
    void addConnection (BluetoothDevice device, BluetoothSocket socket);

    /**
     * Get a list of all currently stored devices.
     */
    List<BluetoothDevice> getDevices ();

    /**
     * Retrieve the socket associated with a specific device.
     */
    BluetoothSocket getSocket (String deviceAddress);

    /**
     * Disconnect and remove all stored devices and sockets.
     */
    void disconnectAllDevices ();

    /**
     * Disconnect and remove a single device by its address.
     */
    void disconnectDevice (String deviceAddress);
}
