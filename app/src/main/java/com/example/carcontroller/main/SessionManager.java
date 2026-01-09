package com.example.carcontroller.main;

import android.util.Log;

import com.example.carcontroller.devices.DeviceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    public static final String TAG = "SessionManagerTAG";
    private static SessionManager instance;

    private DeviceManager deviceManager = DeviceManager.getInstance();
    private RaceSession currentSession;
    private List<RaceSession> sessionsHistory = new ArrayList<>();
    private Driver currentDriver;

    private SessionManager () {
        sessionsHistory = new ArrayList<>();
    }

    public static synchronized SessionManager getInstance () {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public RaceSession getCurrentSession () {
        return currentSession;
    }

    public void setCurrentDriver (Driver driver) {
        this.currentDriver = driver;
        // Log.i(TAG, "Current driver set to " + driver.getDriverName());
        driver.logDriverDetails();
    }

    public Driver getCurrentDriver () {
        return currentDriver;
    }

    public boolean startNewRaceSession (String circuitName) {
        if (currentDriver == null) {
            Log.e(TAG, "Cannot start session. No driver available!");
            return false;
        }

        currentSession = new RaceSession(currentDriver, circuitName);
        currentSession.setStartTime();
        return true;
    }

    public boolean finishRaceSession () {
        if (currentSession == null) {
            Log.e(TAG, "Session cannot be finished!");
            return false;
        }

        currentSession.setFinishTime();
        sessionsHistory.add(currentSession);
        currentSession.displaySessionData();
        currentSession = null;
        return true;
    }

    public static class RaceSession implements Serializable {
        private String sessionId;
        private Driver driver;
        private String circuitName;
        private long startTime;
        private long finishTime;
        private boolean active;
        private Map<Integer, Long> checkpointsTimeStamps;

        public RaceSession (Driver driver, String circuitName) {
            this.sessionId = UUID.randomUUID().toString(); // Change later to generate unique int ID for database

            this.driver = driver;
            this.circuitName = circuitName;

            this.checkpointsTimeStamps = new HashMap<>();
            this.active = false;
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
            Log.d(TAG, "Session: " + sessionId +
                    " | Driver: " + driver.getDriverName() +
                    " | Finish time: " + finishTime +
                    " | Total time: " + getTotalTime());

            for (Integer index : getAllCheckpointsTimes().keySet()) {
                long timeStamp = checkpointsTimeStamps.get(index);
                Log.d(TAG, "Checkpoint " + index + " | detection time: " + timeStamp);
            }
        }
    }

    public static class Driver implements Serializable {
        private String driverFirstName;
        private String driverLastName;
        private String birthdate;
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

        public void logDriverDetails () {

            Log.d(TAG,
                    "Driver Details -> " +
                            "First Name: " + driverFirstName +
                            ", Last Name: " + driverLastName +
                            ", Age: " + age +
                            ", Gender: " + (gender != null ? gender.name() : "UNKNOWN") +
                            ", Birthdate: " + (birthdate != null ? birthdate : "N/A")
            );
        }
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
