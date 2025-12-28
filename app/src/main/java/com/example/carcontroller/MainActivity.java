/**
 * @Author Muresan Claudiu
 *
 * */


package com.example.carcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.widget.*;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivityTAG";
    Button bluetoothButton, driverButton;
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
        bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        driverButton = (Button) findViewById(R.id.driverButton);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        bluetoothButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: bluetooth");
            launchActivityBluetooth();
        });
        driverButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: driverMode");
            launchActivityDriverMode();
        });
    }

    public void handleDriverNameText (View v) {
        String driverName = ((EditText) findViewById(R.id.driverTextBoxID)).getText().toString();
        ((TextView) findViewById(R.id.inputName)).setText(driverName);
        Toast.makeText(this, "Driver introduced", Toast.LENGTH_LONG).show();
        Log.d("Driver name", driverName);
    }

    public void launchActivityBluetooth () {
        Intent intent = new Intent(this, BluetoothManagerActivity.class);
        startActivity(intent);
    }

    public void launchActivityDriverMode () {
        Intent intent = new Intent(this, DriverModeActivity.class);
        startActivity(intent);
    }

}
