package com.example.carcontroller.devices_package;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.carcontroller.bluetooth_package.BluetoothService;
import com.example.carcontroller.main_package.Commands;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class CarDevice extends Device {
    private static final String TAG = "CarDeviceTAG";

    private final BluetoothSocket bluetoothSocket;
    private final BluetoothService bluetoothService;
    private final CarConfiguration configuration;
    private OperationMode currentMode;

    // Line-follower mode related fields
    private float kp, ki, kd;
    private float baseSpeedLeft, baseSpeedRight;
    private float LMSW, LSW, CSW, RSW, RMSW;

    // Driver mode related fields
    private float speedRightFWD, speedLeftFWD, speedRightBWD, speedLeftBWD;

    public enum OperationMode {SETUP, DRIVER, LINE_FOLLOWER}

    public CarDevice (String deviceAddress, String deviceName, BluetoothSocket socket) {
        super(deviceAddress, deviceName, DeviceType.CAR);
        this.bluetoothSocket = socket;
        this.bluetoothService = BluetoothService.getInstance();
        this.currentMode = OperationMode.SETUP;
        this.configuration = new CarConfiguration();


        bluetoothService.registerListener(deviceAddress, this);
    }

    @Override
    public boolean connect () {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothService.initializeStream(getDeviceAddress(), bluetoothSocket);
                setDeviceStatus(DeviceStatus.CONNECTED);
                Log.i(TAG, "Car device " + getDeviceName() + " connected successfully");
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Connection failed!", e);
            setDeviceStatus(DeviceStatus.ERROR);
            return false;
        }
    }

    @Override
    public boolean disconnect () {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                setDeviceStatus(DeviceStatus.DISCONNECTED);
                Log.i(TAG, "Car device " + getDeviceName() + " disconnected successfully");
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
            return false;
        }
    }

    @Override
    public boolean sendData (String data) {
        if (bluetoothService != null && isConnected()) {
            bluetoothService.write(getDeviceAddress(), data);
            return true;
        }
        return false;
    }

    @Override
    public void onDataReceived (String deviceAddress, byte[] data) {
        if (getDeviceAddress().contentEquals(deviceAddress)) {
            Log.d(TAG, "Car received data");
        }
    }

    @Override
    public boolean isConnected () {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && getDeviceStatus() == DeviceStatus.CONNECTED;
    }

    public boolean sendCommand (Commands command) {
        return sendData(command.getCommand());
    }

    public boolean setOperationMode (OperationMode operationMode) {
        Commands command;
        switch (operationMode) {
            case SETUP:
                command = Commands.SETUPMODE;
                break;
            case DRIVER:
                command = Commands.DRIVEMODE;
                break;
            case LINE_FOLLOWER:
                command = Commands.LINEFOLLOWERMODE;
                break;
            default:
                return false;
        }
        if (sendCommand(command)) {
            this.currentMode = operationMode;
            Log.i(TAG, "Operation mode changed to " + operationMode);
            return true;
        }
        return false;
    }

    public boolean configurePID (float kp, float ki, float kd, float baseLeftSpeed, float baseRightSpeed) {
        boolean bKp = checkData(0, kp);
        boolean bKi = checkData(0, ki);
        boolean bKd = checkData(0, kd);
        boolean bBaseSpeedLeft = checkData(1, baseLeftSpeed);
        boolean bBaseSpeedRight = checkData(1, baseRightSpeed);

        if (!bKp || !bKi || !bKd || !bBaseSpeedLeft || !bBaseSpeedRight)
            return false;

        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.baseSpeedLeft = baseLeftSpeed;
        this.baseSpeedRight = baseRightSpeed;
        this.configuration.setCreationDate();

        configuration.setKp(kp);
        configuration.setKi(ki);
        configuration.setKd(kd);
        configuration.setBaseSpeed(baseLeftSpeed, baseRightSpeed);
        /*String logMessage = "kp = " + kp + " | " +
                            "ki = " + ki + " | " +
                            "kd = " + kd + " | " +
                            "leftSpeed = "  + baseLeftSpeed  + " | " +
                            "rightSpeed = " + baseRightSpeed;

        Log.i(TAG, logMessage);*/
        return true;
    }

    public boolean uploadPID () {
        if (!isConnected()) return false;

        sendPIDValues(String.valueOf(kp));
        sendPIDValues(String.valueOf(ki));
        sendPIDValues(String.valueOf(kd));
        sendPIDValues(String.valueOf(baseSpeedLeft));
        sendPIDValues(String.valueOf(baseSpeedRight));

        return true;
    }

    public void sendPIDValues (String value) {
        Commands waitCommand;
        switch (value.length()) {
            case 1:
                waitCommand = Commands.WAITFOR1;
                break;
            case 2:
                waitCommand = Commands.WAITFOR2;
                break;
            case 3:
                waitCommand = Commands.WAITFOR3;
                break;
            case 4:
                waitCommand = Commands.WAITFOR4;
                break;
            case 5:
                waitCommand = Commands.WAITFOR5;
                break;
            default:
                return;
        }
        sendCommand(waitCommand);
        sendData(value);
    }

    private boolean checkData (int type, float data) {
        if (type == 0) {
            return data >= 0.0f && data <= 100.0f;
        } else if (type == 1) {
            return data >= 80.0f && data <= 255.0f;
        }
        return false;
    }

    public OperationMode getCurrentMode () {
        return currentMode;
    }

    public CarConfiguration getConfiguration () {
        return configuration;
    }

    public static class CarConfiguration {
        private float kp, ki, kd;
        private float LMSW, LSW, CSW, RSW, RMSW;
        private float baseLeftSpeed;
        private float baseRightSpeed;

        private float speedRightFWD, speedLeftFWD, speedRightBWD, speedLeftBWD;

        private String creationDate;

        public float getKp () {
            return kp;
        }

        public float getKi () {
            return ki;
        }

        public float getKd () {
            return kd;
        }

        public float getLMSW () {
            return LMSW;
        }

        public float getLSW () {
            return LSW;
        }

        public float getCSW () {
            return CSW;
        }

        public float getRSW () {
            return RSW;
        }

        public float getRMSW () {
            return RMSW;
        }

        public float getBaseLeftSpeed () {
            return baseLeftSpeed;
        }

        public float getBaseRightSpeed () {
            return baseRightSpeed;
        }

        public float getSpeedRightFWD () {
            return speedRightFWD;
        }

        public float getSpeedLeftFWD () {
            return speedLeftFWD;
        }

        public float getSpeedRightBWD () {
            return speedRightBWD;
        }

        public float getSpeedLeftBWD () {
            return speedLeftBWD;
        }

        public void setKp (float kp) {
            this.kp = kp;
        }

        public void setKi (float ki) {
            this.ki = ki;
        }

        public void setKd (float kd) {
            this.kd = kd;
        }

        public void setLMSW (float LMSW) {
            this.LMSW = LMSW;
        }

        public void setLSW (float LSW) {
            this.LSW = LSW;
        }

        public void setCSW (float CSW) {
            this.CSW = CSW;
        }

        public void setRSW (float RSW) {
            this.RSW = RSW;
        }

        public void setRMSW (float RMSW) {
            this.RMSW = RMSW;
        }

        public void setBaseSpeed (float baseLeftSpeed, float baseRightSpeed) {
            this.baseLeftSpeed = baseLeftSpeed;
            this.baseRightSpeed = baseRightSpeed;
        }

        public void setBaseLeftSpeed (float baseLeftSpeed) {
            this.baseLeftSpeed = baseLeftSpeed;
        }

        public void setBaseRightSpeed (float baseRightSpeed) {
            this.baseRightSpeed = baseRightSpeed;
        }

        public void setSpeed (float speedRightFWD, float speedLeftFWD, float speedRightBWD, float speedLeftBWD) {
            this.speedLeftFWD = speedLeftFWD;
            this.speedRightFWD = speedRightFWD;
            this.speedLeftBWD = speedLeftBWD;
            this.speedRightBWD = speedRightBWD;
        }

        public void setSpeedFWD (float speedRightFWD, float speedLeftFWD) {
            this.speedLeftFWD = speedLeftFWD;
            this.speedRightFWD = speedRightFWD;
        }

        public void setSpeedRightFWD (float speedRightFWD) {
            this.speedRightFWD = speedRightFWD;
        }

        public void setSpeedLeftFWD (float speedLeftFWD) {
            this.speedLeftFWD = speedLeftFWD;
        }

        public void setSpeedBWD (float speedRightBWD, float speedLeftBWD) {
            this.speedLeftBWD = speedLeftBWD;
            this.speedRightBWD = speedRightBWD;
        }

        public void setSpeedRightBWD (float speedRightBWD) {
            this.speedRightBWD = speedRightBWD;
        }

        public void setSpeedLeftBWD (float speedLeftBWD) {
            this.speedLeftBWD = speedLeftBWD;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public void setCreationDate () {
            this.creationDate = LocalDate.now().toString();
        }
    }
}
