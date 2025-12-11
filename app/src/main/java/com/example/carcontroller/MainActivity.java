package com.example.carcontroller;

import java.util.*;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
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


/*
*   TODO: Implement the bluetooth service
*   TODO: Refactor code to free MainActivity
*   TODO: Create new UI interface for app
*/
public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivityTAG";
    BluetoothManager mBluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;
    DeviceListAdapter mDeviceListAdapter, mPairedListAdapter;


    ArrayList<BluetoothDevice> mBTDevices, pairedDevicesList;
    ListView lvNewDevice, lvPairedDevice;
    private ConnectThread activeConnectThread = null;

    private boolean receiver1Registred = false;
    private boolean receiver2Registred = false;
    private boolean receiver3Registred = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons initialization
        Button bluetoothToggleButton = (Button) findViewById(R.id.bluetoothToggleButton);
        Button discoverButton = (Button) findViewById(R.id.discoverButton);

        lvNewDevice = (ListView) findViewById(R.id.lvNewDevices);
        lvPairedDevice = (ListView) findViewById(R.id.lvPairedDevices);

        mBTDevices = new ArrayList<>();
        pairedDevicesList = new ArrayList<>();

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

        lvNewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });

        lvPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                mBluetoothAdapter.cancelDiscovery();

                Log.d(TAG, "onItemClick: User clicked a device.");

                String deviceName = pairedDevicesList.get(position).getName();
                String deviceAddress = pairedDevicesList.get(position).getAddress();

                Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                BluetoothDevice device = pairedDevicesList.get(position);

                // Check bond state
                Log.d(TAG, "Device bond state: " + device.getBondState());

                // If bond is not solid, try to repair
               /* if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.w(TAG, "Device not properly bonded, attempting to bond...");
                    device.createBond();
                    Toast.makeText(MainActivity.this, "Pairing device...", Toast.LENGTH_SHORT).show();
                    return;
                }*/

                if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                    activeConnectThread = new ConnectThread(device, mBluetoothManager, MainActivity.this);
                    activeConnectThread.start();
                }
            }
        });

    }



    // -------------------------------- BROADCAST RECEIVERS --------------------------------

    // mBroadcastReceiver1 - Bluetooth toggle operations
    // Executed by: enableDisableBT() method
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiver1Registred = true;
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
            receiver2Registred = true;
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

                        if (device.getName() != null)
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
            receiver3Registred = true;
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
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();

        // Stop receivers
        if (receiver1Registred) unregisterReceiver(mBroadcastReceiver1);
        if (receiver2Registred) unregisterReceiver(mBroadcastReceiver2);
        if (receiver3Registred) unregisterReceiver(mBroadcastReceiver3);

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

        // Query paired devices to check if desired device is already known
        // TODO: check if I can move all of this in a broadcast receiver | Or at least how can I improve this
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (!pairedDevices.isEmpty()) {
            for(BluetoothDevice device : pairedDevices) {
                String deviceName = (device.getName() != null) ? device.getName() : "Unknown";
                String deviceAddress = (device.getAddress() != null) ? device.getAddress() : "Unknown";
                Log.d(TAG, "discoverBT: PAIRED - " + deviceName + " : " + deviceAddress);

                pairedDevicesList.add(device);
            }
        }

        mPairedListAdapter = new DeviceListAdapter((Context) this, R.layout.device_adapter_view, pairedDevicesList);
        lvPairedDevice.setAdapter(mPairedListAdapter);
        mPairedListAdapter.notifyDataSetChanged();


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


    private static final int PERMISSION_REQUEST_CODE = 100;

 /*   private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            String[] permissions = {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            };
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-11
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "All permissions granted!");
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Some permissions denied!");
                Toast.makeText(this, "Bluetooth permissions required!", Toast.LENGTH_LONG).show();
            }
        }
    }*/
}
