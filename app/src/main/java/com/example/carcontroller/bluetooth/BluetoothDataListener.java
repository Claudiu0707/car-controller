package com.example.carcontroller.bluetooth;

public interface BluetoothDataListener {

    void onDataReceived (String deviceAddress, byte[] data);
}
