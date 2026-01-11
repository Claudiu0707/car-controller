package com.example.carcontroller.api_package.models_package;

import com.example.carcontroller.main_package.SessionManager;
import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class RaceRequest {

    @SerializedName("driver_id")
    private int driverId;

    @SerializedName("circuit_id")
    private int circuitId;

    @SerializedName("car_configuration_id")
    private int carConfigurationId;

    @SerializedName("race_location_id")
    private int raceLocationId;

    @SerializedName("race_date")
    private String raceDate;

    @SerializedName("total_time")
    private String totalTime;

    // TODO: Finish this
    public RaceRequest(String raceDate, String totalTime ) {
        if (SessionManager.getInstance().isDriverLogged())
            this.driverId = SessionManager.getInstance().getCurrentDriver().getDriverId();
        this.raceDate = raceDate;
        this.totalTime = totalTime;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(int circuitId) {
        this.circuitId = circuitId;
    }

    public int getCarConfigurationId() {
        return carConfigurationId;
    }

    public void setCarConfigurationId(int carConfigurationId) {
        this.carConfigurationId = carConfigurationId;
    }

    public int getRaceLocationId() {
        return raceLocationId;
    }

    public void setRaceLocationId(int raceLocationId) {
        this.raceLocationId = raceLocationId;
    }

    public String getRaceDate() {
        return raceDate;
    }

    public void setRaceDate(String raceDate) {
        this.raceDate = raceDate;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }
}
