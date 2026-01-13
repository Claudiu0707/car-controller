package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CircuitResponse {

    @SerializedName("circuit_id")
    private int circuitId;

    @SerializedName("circuit_location_id")
    private int circuitLocationId;

    @SerializedName("circuit_type_id")
    private int circuitTypeId;

    public int getCircuitId() {
        return circuitId;
    }

    public int getCircuitLocationId() {
        return circuitLocationId;
    }

    public int getCircuitTypeId() {
        return circuitTypeId;
    }
}
