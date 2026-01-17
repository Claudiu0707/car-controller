package com.example.carcontroller.api_package.models_package;
import com.google.gson.annotations.SerializedName;

public class TrackSegmentResponse {

    @SerializedName("track_segment_id")
    private Integer trackSegmentId;

    @SerializedName("circuit_id")
    private Integer circuitId;

    @SerializedName("difficulty_id")
    private Integer difficultyId;

    public Integer getTrackSegmentId() {
        return trackSegmentId;
    }

    public Integer getCircuitId() {
        return circuitId;
    }

    public Integer getDifficultyId() {
        return difficultyId;
    }
}
