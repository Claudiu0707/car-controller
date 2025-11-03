package com.example.carcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.UUID;
public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";

    private final BluetoothSocket targetSocket;
    private final BluetoothDevice targetDevice;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;

    public ConnectThread(BluetoothDevice device, BluetoothManager manager, Context cntx) {
        targetDevice = device;
        bluetoothManager = manager;
        bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothSocket tmp = null;
        context = cntx;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = targetDevice.createRfcommSocketToServiceRecord(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
        } else {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission");
        }
        targetSocket = tmp;
    }

    @Override
    public void run() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission");
            return;
        }

        bluetoothAdapter.cancelDiscovery();

        try {
            targetSocket.connect();
            Log.i(TAG, "Connection successful!");
        } catch (IOException e) {
            Log.e(TAG, "Could not connect; closing socket", e);
            try {
                targetSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "Could not close the client socket", e2);
            }
        }
    }

    public void cancel() {
        try {
            targetSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
