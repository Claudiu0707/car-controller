package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CircuitRequest {

    @SerializedName("circuit_name")
    private String circuitName;

    @SerializedName("circuit_type_name")
    private String circuitTypeName;

    @SerializedName("city_name")
    private String cityName;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("creation_date")
    private String creationDate; // yyyy-MM-dd

    public CircuitRequest(
            String circuitName,
            String circuitTypeName,
            String cityName,
            String locationName,
            String creationDate
    ) {
        this.circuitName = circuitName;
        this.circuitTypeName = circuitTypeName;
        this.cityName = cityName;
        this.locationName = locationName;
        this.creationDate = creationDate;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public String getCircuitTypeName() {
        return circuitTypeName;
    }

    public String getCityName() {
        return cityName;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getCreationDate() {
        return creationDate;
    }
}