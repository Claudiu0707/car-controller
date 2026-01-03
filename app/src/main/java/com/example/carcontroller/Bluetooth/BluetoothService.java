package com.example.carcontroller.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothService {
    private static final String TAG = "BluetoothServiceTAG";

    ConnectedThread connectedThread;

    public void initializeStream (BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void write (String message) {
        connectedThread.write(message.getBytes());
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

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
            mmBuffer = new byte[1024];
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
