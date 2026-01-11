package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CarConfigurationResponse {
    @SerializedName("car_configuration_id")
    private int carConfigurationId;

    @SerializedName("operation_mode_id")
    private int operationModeId;

    @SerializedName("kp")
    private float kp;

    @SerializedName("ki")
    private float ki;

    @SerializedName("kd")
    private float kd;

    @SerializedName("left_speed")
    private float left_speed;

    @SerializedName("right_speed")
    private float right_speed;

    @SerializedName("creation_date")
    private String creation_date;

    public int getCarConfigurationId() {
        return carConfigurationId;
    }

    public int getOperationModeId() {
        return operationModeId;
    }

    public float getKp() {
        return kp;
    }

    public float getKi() {
        return ki;
    }

    public float getKd() {
        return kd;
    }

    public float getLeft_speed() {
        return left_speed;
    }

    public float getRight_speed() {
        return right_speed;
    }

    public String getCreation_date() {
        return creation_date;
    }
}
