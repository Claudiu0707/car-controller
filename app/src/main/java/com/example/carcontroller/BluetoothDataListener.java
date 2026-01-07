package com.example.carcontroller;

public interface BluetoothDataListener {

    void onDataReceived (String deviceAddress, byte[] data);
}
