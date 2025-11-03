package com.example.carcontroller;

import java.util.*;

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
*       - Some links I found:
*               - https://stackoverflow.com/questions/15120502/how-to-save-a-list-of-discoverable-bluetooth-devices-in-android
*               - https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices
*               - https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
*/

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> launchData = null;
    BluetoothManager myBluetoothManager = null;
    BluetoothAdapter myBluetoothAdapter = null;
    ArrayList<BluetoothDevice> myBluetoothDevices = null;
    ArrayList<String> myBluetoothDevicesNames = null;
    ArrayAdapter<String> devicesAdapter = null;
    ListView lvNewDevice;
    BluetoothDevice selectedDevice;

    BluetoothService.ConnectThread communicationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetoothDevices = new ArrayList<>();
        myBluetoothDevicesNames = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myBluetoothDevicesNames);
        lvNewDevice = findViewById(R.id.listView); //Create a ListView with ID lvNewDevices
        lvNewDevice.setAdapter(devicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Create a new context to get access to bluetooth resources
        myBluetoothManager = getSystemService(BluetoothManager.class);
        if(myBluetoothManager != null)
            myBluetoothAdapter = myBluetoothManager.getAdapter();

        launchData = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->{
                    if (result.getResultCode() == RESULT_OK){
                        Log.d("BluetoothOperations", "Bluetooth enabled successfully");
                    } else{
                        Log.d("BluetoothOperations", "Bluetooth request denied");
                    }
                });


        // Wait for user to select a bluetooth device to connect to
        lvNewDevice.setOnItemClickListener((parent, view, position, id) -> {
            String btDeviceName = lvNewDevice.getItemAtPosition(position).toString();
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
            for(int i = 0; i < myBluetoothDevices.size(); i++){
                if (Objects.equals(myBluetoothDevices.get(i).getName(), btDeviceName)){
                    selectedDevice = myBluetoothDevices.get(i);
                    ((TextView) findViewById(R.id.textView2)).setText(selectedDevice.getName());
//                    for (ParcelUuid uuid : selectedDevice.getUuids()) {
//                        Log.d("BT", "Device UUID: " + uuid.toString());
//                    }
                    if (selectedDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        Log.e("Bluetitties", "Device not paired!");
                        selectedDevice.createBond();
                        return;
                    }
                    myBluetoothAdapter.cancelDiscovery();
                    ConnectThread newConnectThread = new ConnectThread(selectedDevice, myBluetoothManager,this);
                    newConnectThread.start();
                }
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


    // Create a receiver to collect bluetooth devices data
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
                    // A device was found during the discovery
                    // Check if we already found this device(we add it to our names list and devices list)
                    // After we add it to the list, notify change (update list view)
                    if(!myBluetoothDevices.contains(device)){
                        String deviceInfo = (device.getName() != null) ? device.getName() : "Unknown";
                        myBluetoothDevices.add(device);
                        myBluetoothDevicesNames.add(deviceInfo);
                        devicesAdapter.notifyDataSetChanged();
                    }
                    Log.d("BluetoothOperations", "Device found");
                }
            }
        }
    };

    // Method is called to enable bluetooth (if not enabled already)
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
        else {
            Log.d("BluetoothOperations", "Bluetooth already enabled");
        }
    }
    public void startDiscoveryDevices(View v){
        // Permissions again because Android thinks that I want to spy on ..... me
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);

        // Before device starts discovering other devices, check if it is not already discovering
        // Immediately after checking (and maybe canceling) discovery-mode, start discovery
        if(myBluetoothAdapter.isDiscovering()){
            myBluetoothAdapter.cancelDiscovery();
            Log.d("BluetoothOperations", "Discovery was canceled");
        }
        myBluetoothAdapter.startDiscovery();
        Log.d("BluetoothOperations", "Discovery was started");


    }

    public void onBluetoothConnected(BluetoothSocket socket){
        runOnUiThread(() -> {
            Toast.makeText(this, "Bluetooth connected!", Toast.LENGTH_SHORT).show();
            communicationThread = new BluetoothService.ConnectThread(socket);
        });
    }
    public void sendDataStream(View v) {
        if (communicationThread == null) {
            Toast.makeText(this, "Not connected yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] data = sendData();
        communicationThread.write(data);
    }
    public byte[] sendData(){
        byte[] data = new byte[6];
        data[0] = 'a';
        data[1] = 'b';
        data[2] = 'c';
        data[3] = 'd';
        data[4] = 'e';
        data[5] = 'f';
        /*String word = "bluetooth";
        for(int i = 0; i < word.length(); i++){
            data[i] = (byte) word.charAt(i);
        }
        Log.d("SentData", data.toString());*/
        return data;
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
}
