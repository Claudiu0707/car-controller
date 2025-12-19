package com.example.carcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThreadTAG";

    private final BluetoothDevice deviceToConnect;
    public final BluetoothSocket targetSocket;
    private final BluetoothAdapter bluetoothAdapter;
    // Create an instance of DevicesConnected which will store all devices connected
    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    private final Context context;

    public ConnectThread(BluetoothDevice device, BluetoothManager manager, Context cntx) {
        this.bluetoothAdapter = manager.getAdapter();
        this.deviceToConnect = device;
        this.context = cntx;
        BluetoothSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = deviceToConnect.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
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
            // Toast.makeText(BluetoothManagerActivity.class, "Connected to " + deviceToConnect.getName(), Toast.LENGTH_LONG).show();
            devicesConnected.addDevice(deviceToConnect); // Add the device
            devicesConnected.addConnection(deviceToConnect, targetSocket); // Add the socket
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