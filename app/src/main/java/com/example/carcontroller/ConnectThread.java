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
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothSocket targetSocket = null;
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final BluetoothDevice device;

    public ConnectThread(BluetoothDevice device, BluetoothManager manager, Context cntx) {
        this.device = device;
        this.bluetoothAdapter = manager.getAdapter();
        this.context = cntx;
    }

    @Override
    public void run() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission");
            return;
        }

        Log.d(TAG, "Starting connection to: " + device.getAddress());
        bluetoothAdapter.cancelDiscovery();

        // Wait for discovery to stop
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Try Method 1: Insecure
        targetSocket = tryInsecureConnection();
        if (targetSocket != null && targetSocket.isConnected()) {
            Log.i(TAG, "Connected via insecure method");
            return;
        }

        // Try Method 2: Reflection
        targetSocket = tryReflectionConnection();
        if (targetSocket != null && targetSocket.isConnected()) {
            Log.i(TAG, "Connected via reflection method");
            return;
        }

        // Try Method 3: Secure
        targetSocket = trySecureConnection();
        if (targetSocket != null && targetSocket.isConnected()) {
            Log.i(TAG, "Connected via secure method");
            return;
        }

        Log.e(TAG, "All connection methods failed");
    }

    private BluetoothSocket tryInsecureConnection() {
        Log.d(TAG, "Trying insecure connection...");
        try {
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            Log.i(TAG, "Insecure connection succeeded");
            return socket;
        } catch (IOException e) {
            Log.w(TAG, "Insecure failed: " + e.getMessage());
            return null;
        }
    }

    private BluetoothSocket tryReflectionConnection() {
        Log.d(TAG, "Trying reflection connection...");
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            BluetoothSocket socket = (BluetoothSocket) m.invoke(device, 1);
            socket.connect();
            Log.i(TAG, "Reflection connection succeeded");
            return socket;
        } catch (Exception e) {
            Log.w(TAG, "Reflection failed: " + e.getMessage());
            return null;
        }
    }

    private BluetoothSocket trySecureConnection() {
        Log.d(TAG, "Trying secure connection...");
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();
            Log.i(TAG, "Secure connection succeeded");
            return socket;
        } catch (IOException e) {
            Log.w(TAG, "Secure failed: " + e.getMessage());
            return null;
        }
    }
/*
    public void cancel() {
        if (targetSocket != null) {
            try {
                targetSocket.close();
                Log.d(TAG, "Socket closed");
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }*/
}