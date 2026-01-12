package com.example.carcontroller.devices_package;

import com.example.carcontroller.bluetooth_package.BluetoothDataListener;
import com.example.carcontroller.bluetooth_package.BluetoothService;

import java.util.Date;

public abstract class Device implements BluetoothDataListener {

    private final BluetoothService bluetoothService;

    private final String deviceAddress;
    private final DeviceType deviceType;
    private String deviceName;
    private DeviceStatus deviceStatus;
    private Date lastConnectedTime;

    public enum DeviceType {CAR, CHECKPOINT}
    public enum DeviceStatus {DISCONNECTED, CONNECTED, ERROR}

    public Device (String deviceAddress, String deviceName, DeviceType deviceType) {
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceStatus = DeviceStatus.DISCONNECTED;

        this.bluetoothService = BluetoothService.getInstance();

        // Sets a new incoming data listener for the device with the address
        bluetoothService.registerListener(deviceAddress, this);
    }

    /**
     * Returns the bluetooth service
     * @return bluetooth service
     */
    public BluetoothService getBluetoothService () {
        return bluetoothService;
    }

    /**
     * Returns the address of the device
     * @return device address
     */
    public String getDeviceAddress () {
        return deviceAddress;
    }

    /**
     * Returns the name of the device
     * @return device name
     */
    public String getDeviceName () {
        return deviceName;
    }

    /**
    * Returns the type of the device
    * @return device type
    */
    public DeviceType getDeviceType () {
        return deviceType;
    }

    /**
     * Returns the status of the device
     * @return device status
     */
    public DeviceStatus getDeviceStatus () {
        return deviceStatus;
    }

    public Date getLastConnectedTime () {
        return lastConnectedTime;
    }

    /**
     * Modifies the status of the device
     * @param deviceStatus device status
     */
    protected void setDeviceStatus (DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
        if (deviceStatus == DeviceStatus.CONNECTED)
            this.lastConnectedTime = new Date();
    }

    /**
     * Modifies the name of the device
     * @param deviceName device name
     */
    public void setDeviceName (String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Initializes data stream for the device if the connection was successful. Creates a new thread to listen for data or send data.
     * @return true if the initialization was successful, false otherwise
     */
    public abstract boolean connect ();

    /**
     * Closes the connection socket to the device and sets the device status to DISCONNECTED
     * @return true if the connection was closed successfully, false otherwise
     */
    public abstract boolean disconnect ();

    /**
     * Sends data to the device
     * @param data data to be sent
     * @return true if the data can be sent, false otherwise (e.g. device not connected)
     */
    public abstract boolean sendData (String data);

    /**
     * Checks if the device is connected
     * @return true if the device is connected, false otherwise
     */
    public abstract boolean isConnected ();

    /**
     * Returns the device information
     * @return device information
     */
    public String getDeviceInfo () {
        return String.format("Device: ID=%s | Name=%s | Type=%s | Status=%s", deviceAddress, deviceName, deviceType, deviceStatus);
    }
}
