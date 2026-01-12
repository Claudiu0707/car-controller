package com.example.carcontroller.bluetooth_package;

public interface BluetoothDataListener {
    void onDataReceived (String deviceAddress, byte[] data);
}
