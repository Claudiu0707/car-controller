package com.example.carcontroller.main_package;

import android.util.Log;

import com.example.carcontroller.devices_package.DeviceManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static final String TAG = "SessionManagerTAG";
    private static SessionManager instance;

    private DeviceManager deviceManager = DeviceManager.getInstance();

    private RaceSession currentSession;
    private Driver currentDriver;
    private boolean driverLogged;


    public static synchronized SessionManager getInstance () {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public RaceSession getCurrentSession () {
        return currentSession;
    }

    public void setCurrentDriver (Driver driver) {
        this.currentDriver = driver;
        driverLogged = true;
        // driver.logDriverDetails();
    }

    public Driver getCurrentDriver () {
        return currentDriver;
    }

    public boolean isDriverLogged () {
        return driverLogged;
    }

    public void startNewRaceSession (String circuitName, String cityName, String locationName) {
        if (currentDriver == null) {
            Log.e(TAG, "Cannot start session. No driver available!");
            return;
        }

        if (cityName == null && locationName == null)
            currentSession = new RaceSession(currentDriver, circuitName);
        else
            currentSession = new RaceSession(currentDriver, circuitName, cityName, locationName);

        currentSession.setStartTime();
    }

    public void finishRaceSession () {
        if (currentSession == null) {
            Log.e(TAG, "Session cannot be finished!");
            return;
        }

        currentSession.setFinishTime();
        currentSession.displaySessionData();
        currentSession = null;
    }

    public static class RaceSession {
        private Driver driver;
        private String circuitName, cityName, locationName;
        private String raceDate;
        // When creating a circuit log data in database and get back the circuit Id
        private int circuitId;
        private long startTime;
        private long finishTime;
        private boolean active;
        private final Map<Integer, Long> checkpointsTimeStamps;

        public RaceSession (Driver driver, String circuitName) {
            this.driver = driver;
            this.circuitName = circuitName;

            this.checkpointsTimeStamps = new HashMap<>();
            this.active = false;

            raceDate = LocalDate.now().toString();
        }

        public RaceSession (Driver driver, String circuitName, String cityName, String locationName) {
            this.driver = driver;

            this.circuitName = circuitName;
            this.cityName = cityName;
            this.locationName = locationName;

            this.checkpointsTimeStamps = new HashMap<>();
            this.active = false;

            raceDate = LocalDate.now().toString();
        }

        public void setStartTime () {
            this.startTime = System.currentTimeMillis();
            this.active = true;
        }

        public void setFinishTime () {
            this.finishTime = System.currentTimeMillis() - startTime;
            this.active = false;
        }

        public long getStartTime () {
            return startTime;
        }

        public long getFinishTime () {
            return finishTime;
        }

        public long getTotalTime () {
            return finishTime - startTime;
        }

        public int getCircuitId () {
            return circuitId;
        }

        public String getRaceDate () {
            return raceDate;
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
            Log.d(TAG, "Session: " +
                    " | Driver: " + driver.getDriverName() +
                    " | Finish time: " + finishTime +
                    " | Total time: " + getTotalTime());

            for (Integer index : getAllCheckpointsTimes().keySet()) {
                long timeStamp = checkpointsTimeStamps.get(index);
                Log.d(TAG, "Checkpoint " + index + " | detection time: " + timeStamp);
            }
        }
    }

    public static class Driver {
        private String driverFirstName;
        private String driverLastName;
        private String birthdate;
        private Integer driverId;
        private int age;
        private Gender gender;

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

    public enum Gender {MALE, FEMALE, OTHER}
}
