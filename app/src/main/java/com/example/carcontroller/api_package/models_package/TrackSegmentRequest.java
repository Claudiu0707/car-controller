package com.example.carcontroller.api_package.models_package;
import com.google.gson.annotations.SerializedName;

public class TrackSegmentRequest {

    @SerializedName("circuit_id")
    private Integer circuitId;

    @SerializedName("difficulty_id")
    private Integer difficultyId;

    public TrackSegmentRequest(Integer circuitId, Integer difficultyId) {
        this.circuitId = circuitId;
        this.difficultyId = difficultyId;
    }

    public Integer getCircuitId() {
        return circuitId;
    }

    public Integer getDifficultyId() {
        return difficultyId;
    }
}


