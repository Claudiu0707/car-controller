package com.example.carcontroller;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.*;
import android.view.*;
import android.widget.*;

import android.bluetooth.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/*
*   Notes:
*       - I implemented a bluetooth broadcast receiver
*       - I think that method turn bluetooth on is almost done (no changes for now)
*       - In onDestroy i ensure that I cancel any discovery in progress and unregister any receivers
*
*       - I should continue with saving the data of the discovered bluetooth devices (maybe using the  ArrayAdapter<BluetoothDevice> devicesDiscovered;)
*       - Some links I found:
*               - https://stackoverflow.com/questions/15120502/how-to-save-a-list-of-discoverable-bluetooth-devices-in-android
*               - https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices
*               - https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
*
*       - Also search how can you emulate other bluetooth devices
*
*       - Sometimes in future, I should better organize the code
*/

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> launchData = null;
    BluetoothManager myBluetoothManager = null;
    BluetoothAdapter myBluetoothAdapter = null;
    ArrayAdapter<BluetoothDevice> devicesDiscovered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a new context to get access to bluetooth resources
        myBluetoothManager = getSystemService(BluetoothManager.class);
        if(myBluetoothManager != null)
            myBluetoothAdapter = myBluetoothManager.getAdapter();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        launchData = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->{
                    if (result.getResultCode() == RESULT_OK){
                        Log.d("BluetoothOperations", "Bluetooth enabled successfully");
                    } else{
                        Log.d("BluetoothOperations", "Bluetooth request denied");
                    }
                });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        //F***ing permissions
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        myBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                if(device != null){
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    devicesDiscovered.add(device);
                    Log.d("BluetoothOperations", "Device found");
                }
            }
        }
    };

    // Method is called to enable bluetooth (if not enabled already) and find available devices
    public void turnBluetooth(View v) {
        if (myBluetoothAdapter == null) {
            //Device doesn't support Bluetooth
            Log.d("BluetoothOperations", "Bluetooth N/A");
            return;
        }
        // Check if bluetooth device is enabled and request permissions if not already given
        if (!myBluetoothAdapter.isEnabled()) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }
            // Bluetooth disabled => throw a request to enable bluetooth
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            launchData.launch(enableBTIntent);
        }
        else{
            Log.d("BluetoothOperations", "Bluetooth already enabled");
        }
    }

    public void startDiscoveryDevices(View v){
        //Permissions again because Android thinks that I want to spy on ..... me
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);

        //Before device starts discovering other devices, check if it is not already discovering
        //Immediately after checking (and maybe canceling) discovery-mode, start discovery
        if(myBluetoothAdapter.isDiscovering()){
            myBluetoothAdapter.cancelDiscovery();
            Log.d("BluetoothOperations", "Discovery was canceled");
        }
        boolean bool = myBluetoothAdapter.startDiscovery();
        Log.d("BluetoothOperations", "Discovery was started");
    }
    public void handleDriverNameText (View v){
        String driverName;
        driverName = ((EditText) findViewById(R.id.driverTextBoxID)).getText().toString();
        ((TextView) findViewById(R.id.inputName)).setText(driverName);
        Toast.makeText(this, "Driver introduced", Toast.LENGTH_LONG).show(); //I can add alert if new driver is added to the DB or if he already exists
        Log.d("Driver name", driverName);
    }

    public void launchActivityStatistics (View v){
        Intent i = new Intent(this, StatisticsActivity.class);
        startActivity(i);
    }
}
