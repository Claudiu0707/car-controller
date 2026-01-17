package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class RaceResponse {
    @SerializedName("race_session_id")
    private int raceId;
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


    public int getRaceId() {
        return raceId;
    }

    public int getDriverId() {
        return driverId;
    }

    public int getCircuitId() {
        return circuitId;
    }

    public int getCarConfigurationId() {
        return carConfigurationId;
    }

    public int getRaceLocationId() {
        return raceLocationId;
    }

    public String getRaceDate() {
        return raceDate;
    }

}
