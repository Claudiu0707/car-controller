package com.example.carcontroller;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Set;

// TODO: Implement the bluetooth service
// TODO: Create a list to hold all active sockets => for each socket create a bluetooth service
// TODO: activeSockets => Must be accessible from BluetoothManager and from

// TODO: Create a class that implements a storage for bluetooth devices + getInstance of it
// TODO: Here, create an instance of it to add devices when connected
// TODO: In other classes, get instance of this objects
public class BluetoothManagerActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothManagerActivityTAG";
    BluetoothManager mBluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;
    DeviceListAdapter mDeviceListAdapter, mPairedListAdapter;


    ArrayList<BluetoothDevice> mBTDevices, pairedDevicesList;
    ListView lvNewDevice, lvPairedDevice;
    private ConnectThread activeConnectThread = null;

    Button backButton = null;
    Button refreshDevicesButton = null;

    ToggleButton bluetoothToggleButton = null;
    ToggleButton discoverToggleButton = null;

    private boolean receiver1Registered = false;
    private boolean receiver2Registered = false;
    private boolean receiver3Registered = false;


    @SuppressLint("MissingPermission")
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
        backButton = findViewById(R.id.backButton);
        refreshDevicesButton = findViewById(R.id.refreshDevicesButton);

        bluetoothToggleButton = findViewById(R.id.bluetoothToggleButton);
        discoverToggleButton = findViewById(R.id.discoverToggleButton);

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

        // Initialize views when activity is started
        setBluetoothViewStatus();
        setDiscoveryViewStatus(mBluetoothAdapter.isDiscovering());


        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: backButton");
            launchActivityMain();
        });
        refreshDevicesButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: refreshDevicesButton");
            refreshPairedDevices();
        });

        bluetoothToggleButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: bluetoothToggleButton");
            toggleBluetooth();
            setBluetoothViewStatus();
        });

        discoverToggleButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: discoverToggleButton");
            discoverToggle();
            if (!mBluetoothAdapter.isEnabled())
                discoverToggleButton.setChecked(false);
        });
        // discoverToggleButton.setEnabled(mBluetoothAdapter.isEnabled());

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
        Log.i(TAG, "onDestroy: called");
        disableBluetoothProcessesOnExit();
        super.onDestroy();

    }

    // -------------------------------- BROADCAST RECEIVERS --------------------------------

    // mBroadcastReceiver1 - Bluetooth toggle operations
    // Executed by: toggleBluetooth() method
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiver1Registered = true;
            final String action = intent.getAction();
            // Log.d(TAG, "onReceive: ACTION FOUND.");

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
                        setBluetoothViewStatus();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: TURNING ON");
                        break;
                }
            }
        }
    };

    // mBroadcastReceiver2 - List devices that are not yet paired
    // Executed by: discoverToggle() method
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receiver2Registered = true;
            final String action = intent.getAction();
            // Log.d(TAG, "onReceive: ACTION FOUND.");
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                // Discovery has found a device. Get the bluetooth device object info from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                if (device != null && !mBTDevices.contains(device)){
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    }

                    if (device.getName() != null)
                        mBTDevices.add(device);

                    String deviceName = (device.getName() != null) ? device.getName() : "Unknown";
                    String deviceAddress = (device.getAddress() != null) ? device.getAddress() : "Unknown";
                    // Log.d(TAG, "onReceive: " + deviceName + " : " + deviceAddress);

                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevice.setAdapter(mDeviceListAdapter);
                    mDeviceListAdapter.notifyDataSetChanged();
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
            // Log.d(TAG, "onReceive: ACTION FOUND.");

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

    public void toggleBluetooth(){
        Log.i(TAG, "toggleBluetooth: called");
        if (mBluetoothAdapter == null){
            Log.d(TAG, "toggleBluetooth: Device does not have bluetooth capabilities.");
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
        else if (mBluetoothAdapter.isEnabled()){
            // TODO: Look for alternatives to disable()
            // NOTE: This doesn't do anything useful if alternative is not found
            // mBluetoothAdapter.disable();

            // Looks for state changes in bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
    private void refreshPairedDevices(){
        Log.i(TAG, "refreshPairedDevices: called");

        if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // Query paired devices to check if desired device is already known
        if (!pairedDevices.isEmpty()) {
            for(BluetoothDevice device : pairedDevices) {
                String deviceName = (device.getName() != null) ? device.getName() : "Unknown";
                String deviceAddress = (device.getAddress() != null) ? device.getAddress() : "Unknown";
                // Log.d(TAG, "discoverToggle: PAIRED - " + deviceName + " : " + deviceAddress);

                pairedDevicesList.add(device);
            }
        }

        mPairedListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, pairedDevicesList);
        lvPairedDevice.setAdapter(mPairedListAdapter);
        mPairedListAdapter.notifyDataSetChanged();
    }


    public void discoverToggle(){
        Log.d(TAG, "discoverToggle: Called.");
        if (mBluetoothAdapter.isEnabled()) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                setDiscoveryViewStatus(false);
                Log.i(TAG, "discoverToggle: Discovery was canceled.");
            } else {
                mBluetoothAdapter.startDiscovery();
                setDiscoveryViewStatus(true);
                Log.i(TAG, "discoverToggle: Discovery was started.");
            }

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        } else {
            final String warning = "Bluetooth not enabled!";
            Toast.makeText(this, warning, Toast.LENGTH_LONG).show();
        }
    }
    
    
    private void setBluetoothViewStatus() {
        Log.i(TAG, "setBluetoothViewStatus: called");
        // Checks for current BT states and update the views accordingly (for lvPairedDevice related views)
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
            bluetoothToggleButton.setChecked(true);
            lvPairedDevice.setVisibility(View.VISIBLE);
            findViewById(R.id.textView3).setVisibility(View.VISIBLE);
            refreshPairedDevices();
        } else {
            // bluetoothToggleButton.setChecked(false);
            lvPairedDevice.setVisibility(View.GONE);
            findViewById(R.id.textView3).setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    private void setDiscoveryViewStatus(Boolean activate){
        Log.i(TAG, "setDiscoveryViewStatus: called");
        if (activate){
            discoverToggleButton.setChecked(true);
            lvNewDevice.setVisibility(View.VISIBLE);
            findViewById(R.id.textView4).setVisibility(View.VISIBLE);
        } else {
            discoverToggleButton.setChecked(false);
            lvNewDevice.setVisibility(View.GONE);
            findViewById(R.id.textView4).setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    private void disableBluetoothProcessesOnExit(){
        Log.i(TAG, "disableBluetoothProcessesOnExit: called");

        if (receiver1Registered) unregisterReceiver(mBroadcastReceiver1);
        if (receiver2Registered) unregisterReceiver(mBroadcastReceiver2);
        if (receiver3Registered) unregisterReceiver(mBroadcastReceiver3);

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

    }

    private void launchActivityMain(){
        disableBluetoothProcessesOnExit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}