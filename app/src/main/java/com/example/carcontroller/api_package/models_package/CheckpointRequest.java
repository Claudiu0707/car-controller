package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CheckpointRequest {

    @SerializedName("checkpoint_name")
    private String checkpointName;

    @SerializedName("checkpoint_order")
    private Integer checkpointOrder;

    @SerializedName("circuit_id")
    private Integer circuitId;

    public CheckpointRequest(String checkpointName, Integer checkpointOrder, Integer circuitId) {
        this.checkpointName = checkpointName;
        this.checkpointOrder = checkpointOrder;
        this.circuitId = circuitId;
    }

    public String getCheckpointName() {
        return checkpointName;
    }

    public Integer getCheckpointOrder() {
        return checkpointOrder;
    }

    public Integer getCircuitId() {
        return circuitId;
    }
}

