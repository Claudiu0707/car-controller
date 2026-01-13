package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CircuitLocationRequest {

    @SerializedName("city_name")
    private String cityName;

    @SerializedName("location_name")
    private String locationName;

    public CircuitLocationRequest(String cityName, String locationName) {
        this.cityName = cityName;
        this.locationName = locationName;
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
}
