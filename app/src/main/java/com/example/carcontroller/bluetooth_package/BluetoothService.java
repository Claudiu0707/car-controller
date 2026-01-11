package com.example.carcontroller.bluetooth_package;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class BluetoothService {
    private static final String TAG = "BluetoothServiceTAG";

    private static BluetoothService instance;
    private final Map<String, ConnectedThread> connections = new HashMap<>();
    private final Map<String, BluetoothDataListener> listeners = new HashMap<>();

    public static synchronized BluetoothService getInstance () {
        if (instance == null) {
            instance = new BluetoothService();
        }
        return instance;
    }

    public void registerListener (String deviceAddress, BluetoothDataListener listener) {
        listeners.put(deviceAddress, listener);
    }

    public void initializeStream (String deviceAddress, BluetoothSocket socket) {
        Log.i(TAG, "Stream initialized for device: " + deviceAddress);

        ConnectedThread thread = new ConnectedThread(socket, deviceAddress);
        connections.put(deviceAddress, thread);
        thread.start();
    }

    public void write (String deviceAddress, String message) {
        Log.d(TAG, "Message: " + message);
        ConnectedThread thread = connections.get(deviceAddress);
        if (thread != null) {
            thread.write(message.getBytes());
        } else {
            Log.e(TAG, "Write failed: device " + deviceAddress);
        }
    }

    public void notifyDataReceived (String deviceAddress, byte[] data) {
        BluetoothDataListener listener = listeners.get(deviceAddress);
        if (listener != null) {
            listener.onDataReceived(deviceAddress, data);
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private String deviceAddress;

        public ConnectedThread (BluetoothSocket socket, String deviceAddress) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.deviceAddress = deviceAddress;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream.");
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream.");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run () {
            byte[] mmBuffer = new byte[1024];
            byte[] instructionBuffer = new byte[2];
            int byteInstructionIndex = 0;
            while (true) {
                try {
                    int readBytes = mmInStream.read(mmBuffer);
                    if (readBytes == -1) break;
                    for (int i = 0; i < readBytes; i++) {
                        instructionBuffer[byteInstructionIndex++] = mmBuffer[i];
                        if (byteInstructionIndex == 2) {
                            notifyDataReceived(deviceAddress, instructionBuffer);
                            byteInstructionIndex = 0;
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write (byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e){
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e){
                Log.e(TAG, "Could not close the connect socket.");
            }
        }
    }
}
