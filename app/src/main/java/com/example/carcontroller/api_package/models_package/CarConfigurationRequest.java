package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CarConfigurationRequest {
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

    public CarConfigurationRequest(float kp, float ki, float kd, float left_speed, float right_speed, String creation_date) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
        this.left_speed = left_speed;
        this.right_speed = right_speed;
        this.creation_date = creation_date;
    }

    public int getOperationModeId() {
        return operationModeId;
    }

    public void setOperationModeId(int operationModeId) {
        this.operationModeId = operationModeId;
    }

    public float getKp() {
        return kp;
    }

    public void setKp(float kp) {
        this.kp = kp;
    }

    public float getKi() {
        return ki;
    }

    public void setKi(float ki) {
        this.ki = ki;
    }

    public float getKd() {
        return kd;
    }

    public void setKd(float kd) {
        this.kd = kd;
    }

    public float getLeft_speed() {
        return left_speed;
    }

    public void setLeft_speed(float left_speed) {
        this.left_speed = left_speed;
    }

    public float getRight_speed() {
        return right_speed;
    }

    public void setRight_speed(float right_speed) {
        this.right_speed = right_speed;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }
}
