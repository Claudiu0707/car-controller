package com.example.carcontroller;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/*
*   Notes:
*       - Some links I found:
*               - https://stackoverflow.com/questions/15120502/how-to-save-a-list-of-discoverable-bluetooth-devices-in-android
*               - https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices
*               - https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
*/

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivityTag";
    BluetoothManager mBluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;
    DeviceListAdapter mDeviceListAdapter;
    ArrayList<BluetoothDevice> mBTDevices;
    ListView lvNewDevice;
    BluetoothService.ConnectThread communicationThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons initialization
        Button bluetoothToggleButton = (Button) findViewById(R.id.bluetoothToggleButton);
        Button discoverButton = (Button) findViewById(R.id.discoverButton);

        lvNewDevice = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        // Create a new context to get access to bluetooth resources
        mBluetoothManager = getSystemService(BluetoothManager.class);
        if(mBluetoothManager != null){
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null){
                // Device doesn't support bluetooth
                Log.d(TAG, "Device doesn't support bluetooth.");
            }
        }

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        bluetoothToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: toggle bluetooth.");
                enableDisableBT();
            }
        });
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: discover devices.");
                discoverBT();
            }
        });



        // Broadcasts when a bond state changes (i.e. pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver3, filter);

        lvNewDevice.setOnItemClickListener(MainActivity.this);
    }



    // -------------------------------- BROADCAST RECEIVERS --------------------------------

    // mBroadcastReceiver1 - Bluetooth toggle operations
    // Executed by: enableDisableBT() method
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            // Looks for changes in bluetooth operations
            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: TURNING ON");
                        break;
                }
            }
        }
    };

    // mBroadcastReceiver2 - List devices that are not yet paired
    // Executed by: discoverBT() method
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                // Discovery has found a device. Get the bluetooth device object info from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                if (device != null){
                    if (!mBTDevices.contains(device)){
                        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                        }

                        mBTDevices.add(device);

                        String deviceName = (device.getName() != null) ? device.getName() : "Unknown";
                        String deviceAddress = (device.getAddress() != null) ? device.getAddress() : "Unknown";
                        Log.d(TAG, "onReceive: " + deviceName + " : " + deviceAddress);

                        mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                        lvNewDevice.setAdapter(mDeviceListAdapter);
                        mDeviceListAdapter.notifyDataSetChanged();
                    }

                }
            }
        }
    };

    // mBroadcastReceiver3 - Detects bond state changes (Pairing status changes)
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);

                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }

                assert device != null;
                // Device is already bonded
                if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "onReceive: BOND = BONDED.");
                }
                // Device is creating a bond
                if (device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "onReceive: BOND = BONDING.");

                }
                // Device is breaking a bond
                if (device.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "onReceive: BOND = NONE.");

                }
            }

        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: User clicked a device.");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        mBTDevices.get(position).createBond();
    }

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();

        // Stop receivers
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);

        //F***ing permissions
        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        mBluetoothAdapter.cancelDiscovery();
    }

    public void enableDisableBT(){
        Log.d(TAG, "enableDisableBT: called");
        if (mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Device does not have bluetooth capabilities.");
        }
        // Check if bluetooth device is enabled and request permissions if not already given
        if (!mBluetoothAdapter.isEnabled()){
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }

            // Bluetooth disabled => throw a request to enable bluetooth
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            // Looks for state changes in bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()){
            // TODO: Look for alternatives to disable()
            mBluetoothAdapter.disable();

            // Looks for state changes in bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    public void discoverBT(){
        Log.d(TAG, "discoverBT: Called.");

        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);

        // Before device starts discovering other devices, check if it is not already discovering
        // Immediately after checking (and maybe canceling) discovery-mode, start discovery
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "discoverBT: Discovery was canceled.");
        }

        mBluetoothAdapter.startDiscovery();
        Log.d(TAG, "discoverBT: Discovery was started.");

        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
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
