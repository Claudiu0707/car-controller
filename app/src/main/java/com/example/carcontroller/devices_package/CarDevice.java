package com.example.carcontroller.devices_package;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.carcontroller.main_package.Commands;

import java.io.IOException;
import java.time.LocalDate;

public class CarDevice extends Device {
    private static final String TAG = "CarDeviceTAG";

    private final BluetoothSocket bluetoothSocket;
    private CarConfiguration configuration;
    private OperationMode currentMode;
    public enum OperationMode {SETUP, DRIVE, LINE_FOLLOWER}
    enum ConfigValidation { PID, MOTOR_SPEED }
    public CarDevice (String deviceAddress, String deviceName, BluetoothSocket socket) {
        super(deviceAddress, deviceName, DeviceType.CAR);
        this.bluetoothSocket = socket;
        this.currentMode = OperationMode.SETUP;
    }

    @Override
    public void connect () {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                getBluetoothService().initializeStream(getDeviceAddress(), bluetoothSocket);
                setDeviceStatus(DeviceStatus.CONNECTED);
                Log.i(TAG, "Car device " + getDeviceName() + " connected successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Connection failed!", e);
            setDeviceStatus(DeviceStatus.ERROR);
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
        if (getBluetoothService() != null && isConnected()) {
            getBluetoothService().write(getDeviceAddress(), data);
            return true;
        }
        return false;
    }

    /**
     * When the BluetoothService receives data from the device, the device, identified by its address is notified that there is available data.
     * The received data is further processed
     * @param deviceAddress device address
     * @param data data received
     */
    @Override
    public void onDataReceived (String deviceAddress, byte[] data) {
        if (getDeviceAddress().contentEquals(deviceAddress)) {
            Log.d(TAG, "Car received data");
        }
    }

    /**
     * Checks if the device is connected
     * @return true if the device is connected, false otherwise
     */
    @Override
    public boolean isConnected () {
        return bluetoothSocket != null && bluetoothSocket.isConnected() && getDeviceStatus() == DeviceStatus.CONNECTED;
    }

    /**
     * Creates a new configuration for the car device. Input is validated before creating
     * the configuration. If you want to check validation process, check method: checkConfigurationData
     *
     * @param kp proportional parameter
     * @param ki integral parameter
     * @param kd derivative parameter
     * @param baseLeftSpeed base speed for the left motor
     * @param baseRightSpeed base speed for the right motor
     *
     * @return true if the configuration was created successfully, false otherwise
     **/
    public boolean createConfiguration(float kp, float ki, float kd, float baseLeftSpeed, float baseRightSpeed) {
        boolean bKp = checkConfigurationData(ConfigValidation.PID, kp);
        boolean bKi = checkConfigurationData(ConfigValidation.PID, ki);
        boolean bKd = checkConfigurationData(ConfigValidation.PID, kd);
        boolean bBaseSpeedLeft = checkConfigurationData(ConfigValidation.MOTOR_SPEED, baseLeftSpeed);
        boolean bBaseSpeedRight = checkConfigurationData(ConfigValidation.MOTOR_SPEED, baseRightSpeed);

        if (!bKp || !bKi || !bKd || !bBaseSpeedLeft || !bBaseSpeedRight)
            return false;

        this.configuration = new CarConfiguration(kp, ki, kd, baseLeftSpeed, baseRightSpeed);
        return true;
    }

    /**
     * Sends a command to the car device. Translates the command to a string and calls the sendData method
     *
     * @param command command to be sent
     * @return true if the command was sent successfully, false otherwise
     */
    public boolean sendCommand (Commands command) {
        return sendData(command.getCommand());
    }

    /**
     * Commands the car to start following the line. This works only if the car is set in Line Follower Mode
     * @param followLine true if the car should start following the line, false otherwise
     */
    public void setLineFollowing(boolean followLine) {
        if (currentMode!= OperationMode.LINE_FOLLOWER)
            return;

        if(followLine) {
            sendCommand(Commands.STARTFOLLOWLINE);
        } else {
            sendCommand(Commands.STOPFOLLOWLINE);
        }
    }

    /**
     * Sets the operation mode of the car device.
     * - SETUP MODE allows the modification of the car configuration
     * - DRIVE MODE allows the manual control of the car
     * - LINE FOLLOW MODE allows the car to follow a predefined path
     *
     * @param operationMode operation mode to be set
     */
    public void setOperationMode (OperationMode operationMode) {
        Commands command;
        switch (operationMode) {
            case SETUP:
                command = Commands.SETUPMODE;
                break;
            case DRIVE:
                command = Commands.DRIVEMODE;
                break;
            case LINE_FOLLOWER:
                command = Commands.LINEFOLLOWERMODE;
                break;
            default:
                return;
        }
        if (sendCommand(command)) {
            currentMode = operationMode;
            Log.i(TAG, "Operation mode changed to " + operationMode);
        }
    }

    /**
     * Start upload PID values sequence. It is important that the car is connected (obviously) and that it is set in SETUP mode, otherwise it won't work
     * @return true if the PID values were uploaded successfully, false otherwise
    * */
    public boolean uploadPID () {
        if (!isConnected() || currentMode != OperationMode.SETUP) return false;
        if (configuration != null) {
            sendPIDValues(String.valueOf(configuration.getKp()));
            sendPIDValues(String.valueOf(configuration.getKi()));
            sendPIDValues(String.valueOf(configuration.getKd()));
            sendPIDValues(String.valueOf(configuration.getBaseLeftSpeed()));
            sendPIDValues(String.valueOf(configuration.getBaseRightSpeed()));
        }
        return true;
    }

    /**
     * Sends an instruction in the standard format (command_type:value) to the car device (check Commands.java for more info about the standard format)
     * Signaling number of bytes the device should listen for (e.g. waitCommand = WAITFOR3 => car device will wait for 3 incoming bytes before processing anything)
     * @param value effective PID value to be sent
     * */
    public void sendPIDValues (String value) {
        // According to the length of the PID parameter value, send a command signalling length of the incoming value
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
            default:
                waitCommand = Commands.WAITFOR5;
        }
        sendCommand(waitCommand);   // Inform the length of the PID parameter value
        sendData(value);            // Send the actual value
    }

    /**
     * Check if the configuration data is valid.
     *
     * @param option option of the data that should be checked (option = PID - effective kp, ki, kd parameters will be checked accordingly |
     *             option = MOTOR_SPEED - motor speeds (PWMs) will be checked accordingly)
     * @return true if data validation passed, false otherwise
     * */
    private boolean checkConfigurationData(ConfigValidation option, float data) {
        if (option == ConfigValidation.PID) {
            return data >= 0.0f;
        } else if (option == ConfigValidation.MOTOR_SPEED) {
            return data >= 0.0f && data <= 255.0f;
        }
        return false;
    }

    public OperationMode getCurrentMode () {
        return currentMode;
    }

    public CarConfiguration getConfiguration () {
        return configuration;
    }


    // =========================== CAR CONFIGURATION ===========================
    public static class CarConfiguration {
        /**
         * PID calibration values
         * kp - influences proportional parameter of the PID controller
         * ki - influences integral parameter of the PID controller
         * kd - influences derivative parameter of the PID controller
         * */
        private float kp, ki, kd;

        /**
         * Weights for each IR sensor (especially important to be calibrated if a digital IR module is used)
         * */
        private float LMSW, LSW, CSW, RSW, RMSW;

        /**
         * Base speeds for the left and right motors for line follower mode. In this mode, the car usually has lower speeds and need more specific tuning
         * */
        private float baseLeftSpeed, baseRightSpeed;

        private String creationDate;

        // NOTE: Will be implemented in future iterations when a drive mode calibration feature will be added
        private float speedRightFWD, speedLeftFWD, speedRightBWD, speedLeftBWD;


        public CarConfiguration(float kp, float ki, float kd, float baseLeftSpeed, float baseRightSpeed) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
            this.baseLeftSpeed = baseLeftSpeed;
            this.baseRightSpeed = baseRightSpeed;

            this.creationDate = LocalDate.now().toString();
        }

        public String getCreationDate() {
            return creationDate;
        }
        public void setCreationDate () {
            this.creationDate = LocalDate.now().toString();
        }

        public float getKp () {
            return kp;
        }

        public float getKi () {
            return ki;
        }

        public float getKd () {
            return kd;
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


        public float getBaseLeftSpeed () {
            return baseLeftSpeed;
        }

        public float getBaseRightSpeed () {
            return baseRightSpeed;
        }
        public void setBaseLeftSpeed (float baseLeftSpeed) {
            this.baseLeftSpeed = baseLeftSpeed;
        }

        public void setBaseRightSpeed (float baseRightSpeed) {
            this.baseRightSpeed = baseRightSpeed;
        }

        public void setBaseSpeed (float baseLeftSpeed, float baseRightSpeed) {
            this.baseLeftSpeed = baseLeftSpeed;
            this.baseRightSpeed = baseRightSpeed;
        }



        // ================================================================
        // FOLLOWING METHODS WILL BE IMPLEMENTED IN FUTURE ITERATIONS
        // THEY ARE DRIVE_MODE MODE CALIBRATIONS RELATED METHODS
        // ================================================================

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
    }
}
