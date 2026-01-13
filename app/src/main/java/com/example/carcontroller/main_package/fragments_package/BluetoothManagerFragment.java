package com.example.carcontroller.main_package.fragments_package;

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

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.carcontroller.bluetooth_package.ConnectThread;
import com.example.carcontroller.bluetooth_package.DeviceListAdapter;
import com.example.carcontroller.devices_package.Device;
import com.example.carcontroller.R;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothManagerFragment extends Fragment {
    private static final String TAG = "BluetoothManagerFragmentTAG";
    private View view;
    private Context context;
    BluetoothManager mBluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;
    DeviceListAdapter mDeviceListAdapter, mPairedListAdapter;

    ArrayList<BluetoothDevice> mBTDevicesList, mPairedDevicesList, mSelectedDevicesList;
    ListView lvNewDevice, lvPairedDevice;

    // The followings are assumed:
    // First thread corresponds to car, the other correspond to the checkpoints (in order: CP1, CP2, CP3)
    ArrayList<ConnectThread> connectThreads = new ArrayList<>();
    private ToggleButton bluetoothToggleButton = null, discoverToggleButton = null;

    private boolean receiver1Registered = false, receiver2Registered = false, receiver3Registered = false;
    private int selectedDeviceCount = 0;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bluetooth_manager, container, false);
        context = requireContext();

        requestAllBluetoothPermissions();

        mBTDevicesList = new ArrayList<>();
        mPairedDevicesList = new ArrayList<>();
        mSelectedDevicesList = new ArrayList<>();

        // Buttons initialization
        Button backButton = view.findViewById(R.id.backButtonID);
        Button refreshDevicesButton = view.findViewById(R.id.refreshDevicesButtonID);
        Button startConnectionSequenceButton = view.findViewById(R.id.startConnectionSequenceButtonID);
        Button resetConnectionSequenceButton = view.findViewById(R.id.resetConnectionSequenceButtonID);

        bluetoothToggleButton = view.findViewById(R.id.bluetoothToggleButtonID);
        discoverToggleButton = view.findViewById(R.id.discoverToggleButtonID);

        lvNewDevice = view.findViewById(R.id.lvNewDevicesID);
        lvPairedDevice = view.findViewById(R.id.lvPairedDevicesID);
        // ---------------------------------------------------------


        mBluetoothManager = (BluetoothManager) requireContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.d(TAG, "Device doesn't support bluetooth.");
            }
        }

        // Initialize bt related UI elements
        setBluetoothViewStatus();
        setDiscoveryViewStatus(false);

        // ---------------- BUTTON ONCLICK LISTENERS ----------------
        backButton.setOnClickListener(v -> {
            close();
        });
        refreshDevicesButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: refreshDevicesButton");
            refreshPairedDevices();
        });
        startConnectionSequenceButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: startConnectionSequenceButton");
            startConnectionSequence();
        });
        resetConnectionSequenceButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: resetConnectionSequenceButton");
            resetConnectionSequence();
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

        // Broadcasts when a bond state changes (i.e. pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mBroadcastReceiver3, filter);

        // -------------------------------- LIST VIEWS LISTENERS --------------------------------
        lvNewDevice.setOnItemClickListener((parent, view, position, id) -> {
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "onItemClick: User clicked a device.");
            String deviceName = mBTDevicesList.get(position).getName();
            String deviceAddress = mBTDevicesList.get(position).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            mBTDevicesList.get(position).createBond();
        });

        lvPairedDevice.setOnItemClickListener((parent, view, position, id) -> {
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "onItemClick: User clicked a device.");

            String deviceName = mPairedDevicesList.get(position).getName();
            String deviceAddress = mPairedDevicesList.get(position).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

            BluetoothDevice device = mPairedDevicesList.get(position);

            Log.d(TAG, "Device bond state: " + device.getBondState());

            // Paired devices are selected for connection sequence
            if (!mSelectedDevicesList.contains(device) && selectedDeviceCount < 4) {
                mSelectedDevicesList.add(device);
                selectedDeviceCount++;
                String message = "Device " + device.getName() + " selected";
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } else {
                String message;
                if (mSelectedDevicesList.contains(device)) {
                    message = "Device " + device.getName() + " already selected!";
                } else {
                    message = "Cannot select device!";
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
        // --------------------------------------------------------------------------------------
        return view;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        disableBluetoothProcessesOnExit();
    }

    // -------------------------------- BROADCAST RECEIVERS --------------------------------

    // mBroadcastReceiver1 - Bluetooth toggle operations
    // Executed by: toggleBluetooth() method
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            receiver1Registered = true;
            final String action = intent.getAction();
            // Log.d(TAG, "onReceive: ACTION FOUND.");

            // Looks for changes in bluetooth operations
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
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
        public void onReceive (Context context, Intent intent) {
            receiver2Registered = true;
            final String action = intent.getAction();
            // Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action != null && action.equals(BluetoothDevice.ACTION_FOUND)) {
                // Discovery has found a device. Get the bluetooth device object info from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                if (device != null && !mBTDevicesList.contains(device)) {
                    if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                    }

                    if (device.getName() != null)
                        mBTDevicesList.add(device);

                    String deviceName = (device.getName() != null) ? device.getName() : "Unknown";
                    String deviceAddress = (device.getAddress() != null) ? device.getAddress() : "Unknown";
                    // Log.d(TAG, "onReceive: " + deviceName + " : " + deviceAddress);

                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevicesList);
                    lvNewDevice.setAdapter(mDeviceListAdapter);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // mBroadcastReceiver3 - Detects bond state changes (Pairing status changes)
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive (Context context, Intent intent) {
            receiver3Registered = true;
            final String action = intent.getAction();
            // Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);

                if (device != null) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "onReceive: BOND = BONDED.");
                    }
                    if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "onReceive: BOND = BONDING.");
                    }
                    if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                        Log.d(TAG, "onReceive: BOND = NONE.");
                    }
                }
            }
        }
    };

    public void toggleBluetooth () {
        Log.i(TAG, "toggleBluetooth: called");
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "toggleBluetooth: Device does not have bluetooth capabilities.");
        }
        // Check if bluetooth device is enabled and request permissions if not already given
        if (!mBluetoothAdapter.isEnabled()) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }

            // Bluetooth disabled => throw a request to enable bluetooth
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            // Looks for state changes in bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mBroadcastReceiver1, BTIntent);
        } else if (mBluetoothAdapter.isEnabled()) {
            // TODO: Look for alternatives to disable()
            // NOTE: This doesn't do anything useful if alternative is not found
            // mBluetoothAdapter.disable();

            // Looks for state changes in bluetooth status
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    private void startConnectionSequence () {
        if (selectedDeviceCount >= 1) {
            for (BluetoothDevice device : mSelectedDevicesList) {
                int index = mSelectedDevicesList.indexOf(device);
                ConnectThread thread;

                if (index == 0)
                    thread = new ConnectThread(device, mBluetoothManager, context, Device.DeviceType.CAR);
                else
                    thread = new ConnectThread(device, mBluetoothManager, context, Device.DeviceType.CHECKPOINT, index);

                connectThreads.add(thread);
                connectThreads.get(index).start();
            }
        } else {
            resetConnectionSequence();
            Toast.makeText(context, "No device selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetConnectionSequence () {
        mSelectedDevicesList.clear();
        selectedDeviceCount = 0;
        Toast.makeText(context, "Selected devices reset!", Toast.LENGTH_SHORT).show();
    }

    private void refreshPairedDevices () {
        Log.i(TAG, "refreshPairedDevices: called");

        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // Query paired devices to check if desired device is already known
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = (device.getName() != null) ? device.getName() : "Unknown";
                String deviceAddress = (device.getAddress() != null) ? device.getAddress() : "Unknown";
                // Log.d(TAG, "discoverToggle: PAIRED - " + deviceName + " : " + deviceAddress);

                mPairedDevicesList.add(device);
            }
        }

        mPairedListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mPairedDevicesList);
        lvPairedDevice.setAdapter(mPairedListAdapter);
        mPairedListAdapter.notifyDataSetChanged();
    }

    public void discoverToggle () {
        Log.d(TAG, "discoverToggle: Called.");
        if (mBluetoothAdapter.isEnabled()) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
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
            context.registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        } else {
            final String warning = "Bluetooth not enabled!";
            Toast.makeText(context, warning, Toast.LENGTH_LONG).show();
        }
    }

    private void setBluetoothViewStatus () {
        Log.i(TAG, "setBluetoothViewStatus: called");
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            bluetoothToggleButton.setChecked(true);
            lvPairedDevice.setVisibility(View.VISIBLE);
            view.findViewById(R.id.pairedDevicesTextID).setVisibility(View.VISIBLE);
            refreshPairedDevices();
        } else {
            // bluetoothToggleButton.setChecked(false);
            lvPairedDevice.setVisibility(View.GONE);
            view.findViewById(R.id.pairedDevicesTextID).setVisibility(View.GONE);
        }
    }


    @SuppressLint("MissingPermission")
    private void setDiscoveryViewStatus (Boolean activate) {
        Log.i(TAG, "setDiscoveryViewStatus: called");
        if (activate) {
            discoverToggleButton.setChecked(true);
            lvNewDevice.setVisibility(View.VISIBLE);
            view.findViewById(R.id.newDevicesTextID).setVisibility(View.VISIBLE);
        } else {
            discoverToggleButton.setChecked(false);
            lvNewDevice.setVisibility(View.GONE);
            view.findViewById(R.id.newDevicesTextID).setVisibility(View.GONE);
        }
    }

    @SuppressLint("MissingPermission")
    private void disableBluetoothProcessesOnExit () {
        Log.i(TAG, "disableBluetoothProcessesOnExit: called");

        if (receiver1Registered) context.unregisterReceiver(mBroadcastReceiver1);
        if (receiver2Registered) context.unregisterReceiver(mBroadcastReceiver2);
        if (receiver3Registered) context.unregisterReceiver(mBroadcastReceiver3);

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }


    private void open (Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container_id, fragment).addToBackStack(null).commit();
    }

    private void close () {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void requestAllBluetoothPermissions () {
        ArrayList<String> permissions = new ArrayList<>();

        // Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

}