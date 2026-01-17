package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class RaceCheckpointResponse {

    @SerializedName("race_checkpoint_id")
    private Integer raceCheckpointId;

    @SerializedName("race_session_id")
    private Integer raceSessionId;

    @SerializedName("checkpoint_id")
    private Integer checkpointId;

    @SerializedName("passed_time")
    private String passedTime;

    public Integer getRaceCheckpointId() {
        return raceCheckpointId;
    }

    public Integer getRaceSessionId() {
        return raceSessionId;
    }

    public Integer getCheckpointId() {
        return checkpointId;
    }

    public String getPassedTime() {
        return passedTime;
    }
}
