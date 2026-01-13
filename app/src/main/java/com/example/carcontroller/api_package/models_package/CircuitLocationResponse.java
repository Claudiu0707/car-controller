package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CircuitLocationResponse {

    @SerializedName("circuit_location_id")
    private int circuitLocationId;

    @SerializedName("city_id")
    private int cityId;

    @SerializedName("location_id")
    private int locationId;

    public int getCircuitLocationId() {
        return circuitLocationId;
    }

    public int getCityId() {
        return cityId;
    }

    public int getLocationId() {
        return locationId;
    }
}
