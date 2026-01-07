package com.example.carcontroller;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
        Log.i(TAG, "Current driver set to " + driver.getDriverName());
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

            this.startTime = this.finishTime = 0;

            this.checkpointsTimeStamps = new HashMap<>();
            this.active = false;
        }

        public void setStartTime () {
            this.startTime = System.currentTimeMillis();
            this.active = true;
        }

        public void setFinishTime () {
            this.finishTime = System.currentTimeMillis();
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
            // TODO: solve the problem with how times are formated for checkpoints
            // Start and finish time are wrong for some reason (there are crazy large values - e.g.
            // for an approximate 9 seconds session, there are numbers in the values of billions 1767821788657)
            Log.d(TAG, "Session: " + sessionId +
                    " | Driver: " + driver.getDriverName() +
                    " | Start time: " + startTime +
                    " | Finish time: " + finishTime +
                    " | Total time: " + getTotalTime() / 100);

            // Checkpoint time is displayed in correct ms (for example I waited approximate 9 seconds for detection and
            // in the log it appeared as 9131 (close to 9seconds)
            for (Integer index : getAllCheckpointsTimes().keySet()) {
                long timeStamp = checkpointsTimeStamps.get(index);
                Log.d(TAG, "Checkpoint " + index + " | detection time: " + timeStamp / 100);
            }
        }
    }

    public static class Driver implements Serializable {
        private String driverId;
        private String driverName;
        private int age;
        private Gender gender;
        private Date birthdate;

        public Driver (String driverName, int age, Gender gender, Date birthdate) {
            this.driverId = UUID.randomUUID().toString(); // Change later to generate unique int ID for database

            this.driverName = driverName;
            this.age = age;
            this.gender = gender;
            this.birthdate = birthdate;

        }

        public String getDriverName () {
            return driverName;
        }

        public String getDriverId () {
            return driverId;
        }

        public int getDriverAge () {
            return age;
        }

        public Gender getDriverGender () {
            return gender;
        }

        public Date getBirthdate () {
            return birthdate;
        }

        public void setDriverName (String driverName) {
            this.driverName = driverName;
        }

        public void setAge (int age) {
            this.age = age;
        }

        public void setGender (Gender gender) {
            this.gender = gender;
        }

        public void setBirthdate (Date birthdate) {
            this.birthdate = birthdate;
        }

        public void setDriverId (String driverId) {
            this.driverId = driverId;
        }
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
