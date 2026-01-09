package com.example.carcontroller.api.models;

import com.google.gson.annotations.SerializedName;

public class DriverResponse {
    @SerializedName("driver_id")
    private int driverId;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("birthdate")
    private String birthdate;

    @SerializedName("gender_id")
    private Integer genderId;

    // Getters
    public int getDriverId () {
        return driverId;
    }

    public String getFirstName () {
        return firstName;
    }

    public String getLastName () {
        return lastName;
    }

    public String getBirthdate () {
        return birthdate;
    }

    public Integer getGenderId () {
        return genderId;
    }
}