package com.example.carcontroller.main_package;

import android.util.Log;

import com.example.carcontroller.devices_package.CarDevice;
import com.example.carcontroller.devices_package.CheckpointDevice;
import com.example.carcontroller.devices_package.DeviceManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *  Central manager for creating and managing race sessions, drivers and circuits instances.
 *  Class is a singleton to guarantee at any time only one instance of the manager is created.
 * */
public class SessionManager {
    private static final String TAG = "SessionManagerTAG";
    private static SessionManager instance;

    private RaceSession currentSession;
    private Driver currentDriver;
    private Circuit currentCircuit;

    private boolean driverLogged;

    public static synchronized SessionManager getInstance () {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    /**
     * Initiates a new race session.
     * */
    public void createNewRaceSession () {
        // If I have no driver logged in, but the car is set in line follower mode, I want to be able to create a race session
        // the existence of a current driver is necessary only for DRIVE MODE

        if (currentDriver ==  null && DeviceManager.getInstance().getCarDevice().getCurrentMode() == CarDevice.OperationMode.DRIVE) {
            Log.e(TAG, "Cannot start session. No driver available!");
            return;
        }

        currentSession = new RaceSession(currentDriver);
    }

    /**
     * Starts the race session and initializes the start time.
     * */
    public void startRaceSession () {
        resetAllCheckpoints();
        if (currentSession == null) {
            Log.e(TAG, "No available race session");
            return;
        }
        currentSession.setStartTime();
    }

    /**
     * Finishes the race session and sets the finish time.
     * */
    public void finishRaceSession () {
        if (currentSession == null) {
            Log.e(TAG, "Session cannot be finished!");
            return;
        }

        currentSession.setFinishTime();
        currentSession.displaySessionData();
    }

    /**
     * Clears detection times and resets checkpoints. Must be called when starting a new race session.
     * */
    public void resetAllCheckpoints () {
        for (CheckpointDevice checkpointDevice: DeviceManager.getInstance().getAllCheckpointDevices()) {
            checkpointDevice.resetCheckpoint();
        }
    }

    public RaceSession getCurrentSession () {
        return currentSession;
    }

    /**
     * Creates a new circuit instance.
     **/
    public void createCircuit(String circuitName, String cityName, String locationName, CircuitType circuitType, Integer segmentsCount) {
        this.currentCircuit = new Circuit(circuitName, cityName, locationName, circuitType, segmentsCount);
    }

    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }

    public Driver getCurrentDriver () {
        return currentDriver;
    }
    public void setCurrentDriver (Driver driver) {
        this.currentDriver = driver;
        driverLogged = true;
    }

    public boolean isDriverLogged () {
        return driverLogged;
    }

    // ======================== RACE SESSION CLASS ========================
    public static class RaceSession {
        private Integer raceId;

        private final Driver driver;
        private final String raceDate;
        private long startTime;
        private long finishTime;
        private long stopTime;
        private boolean active;
        private final Map<Integer, Long> checkpointsTimeStamps;

        public RaceSession (Driver driver) {
            this.driver = driver;

            this.checkpointsTimeStamps = new HashMap<>();
            this.active = false;

            this.raceDate = LocalDate.now().toString();
        }

        public void setStartTime () {
            this.startTime = System.currentTimeMillis();
            this.active = true;
        }

        public void setFinishTime () {
            this.stopTime = System.currentTimeMillis();
            this.finishTime = stopTime - startTime;
            this.active = false;
        }

        public long getStartTime () {
            return startTime;
        }

        public long getFinishTime () {
            return finishTime;
        }

        public long getTotalTime () {
            return finishTime;
        }

        public String getRaceDate () {
            return raceDate;
        }

        public Integer getRaceId() {
            return raceId;
        }

        public void setRaceId(Integer raceId) {
            this.raceId = raceId;
        }

        public Long getCheckpointTime (int checkpointIndex) {
            if (checkpointsTimeStamps.containsKey(checkpointIndex))
                return checkpointsTimeStamps.get(checkpointIndex);
            return (long) -1;
        }

        public Map<Integer, Long> getAllCheckpointsTimes () {
            return new HashMap<>(checkpointsTimeStamps);
        }

        public void recordCheckpointTime (int checkpointIndex, long timeStamp) {
            if (active) {
                checkpointsTimeStamps.put(checkpointIndex, timeStamp - startTime);
            }
        }

        public void displaySessionData () {
            Log.d(TAG, "Session: " + " | Driver: " + driver.getDriverName() + " | Finish time: " + finishTime + " | Total time: " + stopTime);

            for (Integer index : getAllCheckpointsTimes().keySet()) {
                Long timeStamp = checkpointsTimeStamps.get(index);
                if (timeStamp != null)
                    Log.d(TAG, "Checkpoint " + index + " | detection time: " + timeStamp / 100F);
            }
        }
    }

    // ======================== DRIVER CLASS ========================
    public static class Driver {
        private Integer driverId;

        private String driverFirstName;
        private String driverLastName;
        private String birthdate;
        private Gender gender;
        private int age;

        public Driver (String driverFirstName, String driverLastName, int age, Gender gender, String birthdate) {
            this.driverFirstName = driverFirstName;
            this.driverLastName = driverLastName;
            this.age = age;
            this.gender = gender;
            this.birthdate = birthdate;
        }

        public String getDriverName () {
            return driverFirstName + driverLastName;
        }

        public String getDriverFirstName () {
            return driverFirstName;
        }

        public String getDriverLastName () {
            return driverLastName;
        }

        public int getDriverAge () {
            return age;
        }

        public Gender getDriverGender () {
            return gender;
        }

        public String getBirthdate () {
            return birthdate;
        }

        public void setDriverFirstName (String driverFirstName) {
            this.driverFirstName = driverFirstName;
        }

        public void setDriverLastName (String driverLastName) {
            this.driverLastName = driverLastName;
        }

        public void setAge (int age) {
            this.age = age;
        }

        public void setGender (Gender gender) {
            this.gender = gender;
        }

        public void setBirthdate (String birthdate) {
            this.birthdate = birthdate;
        }

        public void setDriverId (int driverId) {
            this.driverId = driverId;
        }
        public Integer getDriverId () {
            return driverId;
        }
        public void logDriverDetails () {
            Log.d(TAG, "Driver Details: " +
                    "Driver ID: " + driverId +
                    ", First Name: " + driverFirstName +
                    ", Last Name: " + driverLastName +
                    ", Age: " + age +
                    ", Gender: " + (gender != null ? gender.name() : "UNKNOWN") +
                    ", Birthdate: " + (birthdate != null ? birthdate : "N/A")
            );
        }
    }

    // ======================== CIRCUIT CLASS ========================
    public static class Circuit {
        private Integer circuitId;

        private String circuitName;
        private String cityName, locationName;
        private CircuitType circuitType;
        private String creationDate;
        private Integer segmentsCount;
        private ArrayList<SegmentDifficulty> segmentDifficulties;

        public Circuit(String circuitName, String cityName, String locationName, CircuitType circuitType, Integer segmentsCount) {
            this.circuitName = circuitName;
            this.cityName = cityName;
            this.locationName = locationName;
            this.circuitType = circuitType;
            this.creationDate = LocalDate.now().toString();
            this.segmentsCount = segmentsCount;

            this.segmentDifficulties = new ArrayList<>();
            for (int i = 0; i < segmentsCount; i++) {
                this.segmentDifficulties.add(null);
            }
        }

        public String getCircuitName() {
            return circuitName;
        }

        public void setCircuitName(String circuitName) {
            this.circuitName = circuitName;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(String creationDate) {
            this.creationDate = creationDate;
        }

        public Integer getSegmentsCount() {
            return segmentsCount;
        }

        public void setSegmentsCount(Integer segmentsCount) {
            this.segmentsCount = segmentsCount;
        }

        public ArrayList<SegmentDifficulty> getSegmentDifficulties() {
            return segmentDifficulties;
        }

        public SegmentDifficulty getOneSegmentDifficulty(int segmentIndex) {
            return segmentDifficulties.get(segmentIndex);
        }

        public void setSegmentDifficulties(ArrayList<SegmentDifficulty> segmentDifficulties) {
            this.segmentDifficulties = segmentDifficulties;
        }

        public void setOneSegmentDifficulty(int segmentPosition, SegmentDifficulty difficulty) {
            segmentDifficulties.set(segmentPosition, difficulty);
        }

        public CircuitType getCircuitType() {
            return circuitType;
        }

        public void setCircuitType(CircuitType circuitType) {
            this.circuitType = circuitType;
        }

        public Integer getCircuitId() {
            return circuitId;
        }

        public void setCircuitId(Integer circuitId) {
            this.circuitId = circuitId;
        }
    }


    // ======================== ENUMERATORS ========================
    public enum CircuitType {LINE_FOLLOWER_CIRCUIT, DRIVER_CIRCUIT}
    public enum Gender {MALE, FEMALE, OTHER}
    public enum SegmentDifficulty {VERYEASY, EASY, AVERAGE, HARD, VERYHARD}
}
