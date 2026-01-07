package com.example.carcontroller;

import com.example.carcontroller.Bluetooth.BluetoothService;

import java.util.Date;

public abstract class Device implements BluetoothDataListener {
    private final String deviceAddress;
    private final DeviceType deviceType;
    private String deviceName;
    private DeviceStatus deviceStatus;
    private Date lastConnectedTime;

    public enum DeviceType {CAR, CHECKPOINT}

    public enum DeviceStatus {DISCONNECTED, CONNECTING, CONNECTED, ERROR}

    public Device (String deviceAddress, String deviceName, DeviceType deviceType) {
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.deviceStatus = DeviceStatus.DISCONNECTED;
    }

    public String getDeviceAddress () {
        return deviceAddress;
    }

    public String getDeviceName () {
        return deviceName;
    }

    public DeviceType getDeviceType () {
        return deviceType;
    }

    public DeviceStatus getDeviceStatus () {
        return deviceStatus;
    }

    public Date getLastConnectedTime () {
        return lastConnectedTime;
    }

    protected void setDeviceStatus (DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
        if (deviceStatus == DeviceStatus.CONNECTED)
            this.lastConnectedTime = new Date();
    }

    public void setDeviceName (String deviceName) {
        this.deviceName = deviceName;
    }

    public abstract boolean connect ();

    public abstract boolean disconnect ();

    public abstract boolean sendData (String data);

    public abstract boolean isConnected ();

    public String getDeviceInfo () {
        return String.format("Device[ID=%s, Name=%s, Type=%s, Status=%s]",
                deviceAddress, deviceName, deviceType, deviceStatus);
    }
}
