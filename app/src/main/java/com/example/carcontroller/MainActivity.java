package com.example.carcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.widget.*;

import android.bluetooth.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//    ActivityResultLauncher<Intent> launchData = null;
//    //Create a new context
//    BluetoothManager myBluetoothManager = getSystemService(BluetoothManager.class);
//    BluetoothAdapter myBluetoothAdapter = myBluetoothManager.getAdapter();


    public void turnBluetooth(View v){
//        launchData = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        if (result.getResultCode() == MainActivity.RESULT_OK){
//                            //There are no requests codes
//                            Intent data = result.getData();
//                            data.getStringExtra("details");
//                        }
//                    }
//                });
//        if(myBluetoothAdapter == null){
//            //Device doesn't support Bluetooth
//            Log.d(null, "Bluetooth N/A");
//        }
//        else{
//            //Check if bluetooth device is enabled
//            if(!myBluetoothAdapter.isEnabled()){
//                //Bluetooth disabled => throw a request to enable bluetooth
//                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                launchData.launch(enableBTIntent);
//                //ActivityResultContracts.StartActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
//
//            }
        }


    public void handleDriverNameText(View v){
        String driverName;
        driverName = ((EditText)findViewById(R.id.driverTextBoxID)).getText().toString();
        ((TextView)findViewById(R.id.inputName)).setText(driverName);
        Toast.makeText(this, "Driver introduced", Toast.LENGTH_LONG).show(); //I can add alert if new driver is added to the DB or if he already exists
        Log.d("Driver name", driverName);
    }

    public void launchActivityStatistics(View v){
        Intent i = new Intent(this, StatisticsActivity.class);
        startActivity(i);
    }


}