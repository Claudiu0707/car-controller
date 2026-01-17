package com.example.carcontroller.api_package.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CarConfigurationDto {

    private int id;
    private double kp;
    private double ki;
    private double kd;

    @JsonProperty("left_speed")
    private double leftSpeed;

    @JsonProperty("right_speed")
    private double rightSpeed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getKp() {
        return kp;
    }

    public void setKp(double kp) {
        this.kp = kp;
    }

    public double getKi() {
        return ki;
    }

    public void setKi(double ki) {
        this.ki = ki;
    }

    public double getKd() {
        return kd;
    }

    public void setKd(double kd) {
        this.kd = kd;
    }

    public double getLeftSpeed() {
        return leftSpeed;
    }

    public void setLeftSpeed(double leftSpeed) {
        this.leftSpeed = leftSpeed;
    }

    public double getRightSpeed() {
        return rightSpeed;
    }

    public void setRightSpeed(double rightSpeed) {
        this.rightSpeed = rightSpeed;
    }

    public String getDisplayLabel() {
        return "ID " + id +
                " | kp=" + kp +
                " ki=" + ki +
                " kd=" + kd +
                " | L=" + leftSpeed +
                " R=" + rightSpeed;
    }
}