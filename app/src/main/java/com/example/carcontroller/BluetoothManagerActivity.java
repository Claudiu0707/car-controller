package com.example.carcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Set;

// TODO: Implement the bluetooth service
/*
* TODO:

- Create a new activity containing all BT operations - DONE
- Bluetooth Button -> Enable Bluetooth - Toggle - IDK
- Discover Devices Button -> Discover Devices Toggle - IDK

List views:
- Paired devices list + Refresh button
- Not paired devices list + Refresh Button


- Create a new activity for car controlling
*/
public class BluetoothManagerActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothManagerActivityTAG";

    BluetoothManager mBluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;
    DeviceListAdapter mDeviceListAdapter, mPairedListAdapter;


    ArrayList<BluetoothDevice> mBTDevices, pairedDevicesList;
    ListView lvNewDevice, lvPairedDevice;
    private ConnectThread activeConnectThread = null;

    private boolean receiver1Registered = false;
    private boolean receiver2Registered = false;
    private boolean receiver3Registered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth_manager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Buttons initialization
        Button bluetoothToggleButton =  findViewById(R.id.bluetoothToggleButton);
        Button discoverButton =  findViewById(R.id.discoverButton);
        Button backButton =  findViewById(R.id.backButton);
        /*SwitchCompat bluetoothToggleSwitch = findViewById(R.id.bluetoothToggleSwitch);
        SwitchCompat discoverToggleSwitch = findViewById(R.id.discoverToggleSwitch);*/

        lvNewDevice = findViewById(R.id.lvNewDevices);
        lvPairedDevice = findViewById(R.id.lvPairedDevices);

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
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: backButton");
            launchActivityMain();
        });
        bluetoothToggleButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: bluetoothToggleButton");
            enableDisableBT();
        });
        discoverButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: discoverButton");
            discoverBT();
        });
        /*bluetoothToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            enableDisableBT();
            setViewStatus(lvNewDevice);
        });
        discoverToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            discoverBT();
            setViewStatus(lvPairedDevice);
        });*/

        // Broadcasts when a bond state changes (i.e. pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver3, filter);

        lvNewDevice.setOnItemClickListener((parent, view, position, id) -> {
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "onItemClick: User clicked a device.");
            String deviceName = mBTDevices.get(position).getName();
            String deviceAddress = mBTDevices.get(position).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            mBTDevices.get(position).createBond();
        });

        lvPairedDevice.setOnItemClickListener((parent, view, position, id) -> {
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

            if (device.getBondState() == BluetoothDevice.BOND_BONDED){
                activeConnectThread = new ConnectThread(device, mBluetoothManager, BluetoothManagerActivity.this);
                activeConnectThread.start();
            }
        });
    }
    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();

        // Stop receivers
        if (receiver1Registered) unregisterReceiver(mBroadcastReceiver1);
        if (receiver2Registered) unregisterReceiver(mBroadcastReceiver2);
        if (receiver3Registered) unregisterReceiver(mBroadcastReceiver3);

        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        mBluetoothAdapter.cancelDiscovery();
    }

    // -------------------------------- BROADCAST RECEIVERS --------------------------------

    // mBroadcastReceiver1 - Bluetooth toggle operations
    // Executed by: enableDisableBT() method
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiver1Registered = true;
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
            receiver2Registered = true;
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
            receiver3Registered = true;
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
            // mBluetoothAdapter.disable();

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

        mPairedListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, pairedDevicesList);
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


    private void setViewStatus(View view){
        view.setEnabled(!view.isEnabled());
    }
    private void launchActivityMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}