package com.example.carcontroller.api_package.models_package;

import com.google.gson.annotations.SerializedName;

public class CheckpointResponse {

        @SerializedName("checkpoint_id")
        private Integer checkpointId;

        @SerializedName("checkpoint_name")
        private String checkpointName;

        @SerializedName("checkpoint_order")
        private Integer checkpointOrder;

        @SerializedName("circuit_id")
        private Integer circuitId;

        public Integer getCheckpointId() {
            return checkpointId;
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

