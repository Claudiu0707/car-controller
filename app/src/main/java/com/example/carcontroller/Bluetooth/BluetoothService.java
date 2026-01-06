package com.example.carcontroller.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BluetoothService {
    private static final String TAG = "BluetoothServiceTAG";

    private static BluetoothService instance;

    public static synchronized BluetoothService getInstance () {
        if (instance == null) {
            instance = new BluetoothService();
        }
        return instance;
    }

    private final Map<String, ConnectedThread> connections = new HashMap<>();
    private final Map<String, BlockingQueue<byte[]>> readQueues = new HashMap<>();

    public void initializeStream (String deviceAddress, BluetoothSocket socket) {
        Log.i(TAG, "Stream initialized for device: " + deviceAddress);
        readQueues.put(deviceAddress, new LinkedBlockingQueue<>());

        ConnectedThread thread = new ConnectedThread(socket);
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

    public String read (String deviceAddress) {
        BlockingQueue<byte[]> queue = readQueues.get(deviceAddress);
        if (queue != null) {
            byte[] data = queue.poll(); // returns null if no data available
            if (data != null) {
                return new String(data);
            }
        }
        return null;
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread (BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

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
            int numBytes;

            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
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
