/**
 * @Author Muresan Claudiu
 *
 * */


package com.example.carcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.*;
import android.view.*;
import android.widget.*;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

// TODO: Create new UI interface for app


public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivityTAG";

    DevicesConnected devicesConnected = DevicesConnected.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Buttons initialization
        Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        Button sendDataBUtton = (Button) findViewById(R.id.sendData);
        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: bluetooth");
                launchActivityBluetooth();
            }
        });
        sendDataBUtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                sendData();
            }
        });

    }

    public void sendData () {

        List<BluetoothDevice> deviceList = devicesConnected.getDevices();
        BluetoothService service = new BluetoothService();
        BluetoothDevice device;
        if (!deviceList.isEmpty()) {
            // Later add also a handler
            device = deviceList.get(0);
            BluetoothSocket socket = devicesConnected.getSocket(device);
            service.initializeStream(socket);
            service.write("hello");             // Crashes but also sends message

        }


    }
    public void handleDriverNameText (View v){
        String driverName = ((EditText) findViewById(R.id.driverTextBoxID)).getText().toString();
        ((TextView) findViewById(R.id.inputName)).setText(driverName);
        Toast.makeText(this, "Driver introduced", Toast.LENGTH_LONG).show(); //I can add alert if new driver is added to the DB or if he already exists
        Log.d("Driver name", driverName);
    }
    public void launchActivityStatistics(View v){
        Intent i = new Intent(this, StatisticsActivity.class);
        startActivity(i);
    }

    public void launchActivityBluetooth(){
        Intent intent = new Intent(this, BluetoothManagerActivity.class);
        startActivity(intent);
    }

}
