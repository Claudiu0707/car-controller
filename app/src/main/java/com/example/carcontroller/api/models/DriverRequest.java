package com.example.carcontroller.api.models;

import com.google.gson.annotations.SerializedName;

public class DriverRequest {
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("birthdate")
    private String birthdate;  // Format: "yyyy-MM-dd"

    @SerializedName("gender_id")
    private Integer genderId;  // 1 = MALE, 2 = FEMALE, 3 = OTHER

    public DriverRequest (String firstName, String lastName, String birthdate, Integer genderId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.genderId = genderId;
    }


    public String getFirstName () {
        return firstName;
    }

    public void setFirstName (String firstName) {
        this.firstName = firstName;
    }

    public String getLastName () {
        return lastName;
    }

    public void setLastName (String lastName) {
        this.lastName = lastName;
    }

    public String getBirthdate () {
        return birthdate;
    }

    public void setBirthdate (String birthdate) {
        this.birthdate = birthdate;
    }

    public Integer getGenderId () {
        return genderId;
    }

    public void setGenderId (Integer genderId) {
        this.genderId = genderId;
    }
}
