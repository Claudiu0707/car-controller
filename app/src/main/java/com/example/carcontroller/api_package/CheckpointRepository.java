package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.CheckpointRequest;
import com.example.carcontroller.api_package.models_package.CheckpointResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckpointRepository {
    private static final String TAG = "CheckpointRepositoryTAG";
    private static CheckpointRepository instance;
    private ApiService apiService;

    private CheckpointRepository() {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized CheckpointRepository getInstance() {
        if (instance == null) {
            instance = new CheckpointRepository();
        }
        return instance;
    }

    public void saveCheckpoint(CheckpointRequest request, final CheckpointCallback callback) {
        Call<CheckpointResponse> call = apiService.createCheckpoint(request);

        call.enqueue(new Callback<CheckpointResponse>() {
            @Override
            public void onResponse(Call<CheckpointResponse> call, Response<CheckpointResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Checkpoint saved successfully with ID: " + response.body().getCheckpointId());
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to save checkpoint. Code: " + response.code();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<CheckpointResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public interface CheckpointCallback {
        void onSuccess(CheckpointResponse checkpoint);
        void onError(String error);
    }
}
