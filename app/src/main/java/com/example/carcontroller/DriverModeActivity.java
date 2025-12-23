package com.example.carcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class DriverModeActivity extends AppCompatActivity {
    private static final String TAG = "DriverModeActivityTAG";
    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    BluetoothDevice car = null;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!devicesConnected.getDevices().isEmpty()) {
            car = devicesConnected.getDevices().get(0);
        }
    }


   /* public void sendData () {
        List<BluetoothDevice> deviceList = devicesConnected.getDevices();
        BluetoothService service = new BluetoothService();
        if (!deviceList.isEmpty()) {
            for (BluetoothDevice device: deviceList){
                BluetoothSocket socket = devicesConnected.getSocket(device);
                service.initializeStream(socket);
                service.write("D1");
            }
        }
    }*/
}