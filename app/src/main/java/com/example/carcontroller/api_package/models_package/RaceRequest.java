package com.example.carcontroller.api_package.models_package;

import com.example.carcontroller.main_package.SessionManager;
import com.google.gson.annotations.SerializedName;

import java.time.Duration;

public class RaceRequest {

    @SerializedName("driver_id")
    private Integer driverId;

    @SerializedName("circuit_id")
    private Integer circuitId;

    @SerializedName("car_configuration_id")
    private Integer carConfigurationId;

    @SerializedName("race_date")
    private String raceDate;

    @SerializedName("total_time")
    private String totalTime;

    public RaceRequest(Integer driverId, Integer circuitId, Integer carConfigurationId, String raceDate, String totalTime) {
        this.driverId = driverId;
        this.circuitId = circuitId;
        this.carConfigurationId = carConfigurationId;
        this.raceDate = raceDate;
        this.totalTime = totalTime;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public Integer getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(Integer circuitId) {
        this.circuitId = circuitId;
    }

    public Integer getCarConfigurationId() {
        return carConfigurationId;
    }

    public void setCarConfigurationId(Integer carConfigurationId) {
        this.carConfigurationId = carConfigurationId;
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
