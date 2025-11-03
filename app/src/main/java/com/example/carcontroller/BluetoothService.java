package com.example.carcontroller;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;

public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private Handler handler;

    private interface MessageConstants{
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

     static class ConnectThread extends Thread{
        private final BluetoothSocket mySocket;
        private final OutputStream myOutStream;
        private byte[] myBuffer;

        public ConnectThread(BluetoothSocket socket){
            mySocket = socket;
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
            } catch(IOException e){
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            myOutStream = tmpOut;
        }

        public void write(byte[] bytes){
            try {
                myOutStream.write(bytes);
            } catch (IOException e){
                Log.e(TAG, "Error occurred when sending data", e);

            }
        }

        public void cancel(){
            try{
                mySocket.close();
            } catch (IOException e){
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

}
