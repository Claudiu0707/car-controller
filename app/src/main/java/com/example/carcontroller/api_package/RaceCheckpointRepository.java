package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.RaceCheckpointRequest;
import com.example.carcontroller.api_package.models_package.RaceCheckpointResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RaceCheckpointRepository {
    private static final String TAG = "RaceCheckpointRepoTAG";
    private static RaceCheckpointRepository instance;
    private final ApiService apiService;

    private RaceCheckpointRepository() {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized RaceCheckpointRepository getInstance() {
        if (instance == null) {
            instance = new RaceCheckpointRepository();
        }
        return instance;
    }

    public void saveRaceCheckpoint(RaceCheckpointRequest request, final RaceCheckpointCallback callback) {
        Call<RaceCheckpointResponse> call = apiService.createRaceCheckpoint(request);

        call.enqueue(new Callback<RaceCheckpointResponse>() {
            @Override
            public void onResponse(Call<RaceCheckpointResponse> call, Response<RaceCheckpointResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "RaceCheckpoint saved with ID: " + response.body().getRaceCheckpointId());
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to save race checkpoint. Code: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<RaceCheckpointResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error);
                callback.onError(error);
            }
        });
    }

    public interface RaceCheckpointCallback {
        void onSuccess(RaceCheckpointResponse response);
        void onError(String error);
    }
}
