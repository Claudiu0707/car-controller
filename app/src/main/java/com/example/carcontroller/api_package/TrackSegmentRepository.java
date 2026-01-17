package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.TrackSegmentRequest;
import com.example.carcontroller.api_package.models_package.TrackSegmentResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackSegmentRepository {
    private static final String TAG = "TrackSegmentRepositoryTAG";
    private static TrackSegmentRepository instance;

    private ApiService apiService;

    private TrackSegmentRepository() {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized TrackSegmentRepository getInstance() {
        if (instance == null) {
            instance = new TrackSegmentRepository();
        }
        return instance;
    }

    public void createTrackSegment(Integer circuitId, Integer difficultyId, final TrackSegmentCallback callback) {
        try {
            if (apiService == null) {
                throw new NullPointerException("apiService is null");
            }

            TrackSegmentRequest request = new TrackSegmentRequest(circuitId, difficultyId);
            Call<TrackSegmentResponse> call = apiService.createTrackSegment(request);

            call.enqueue(new Callback<TrackSegmentResponse>() {
                @Override
                public void onResponse(Call<TrackSegmentResponse> call, Response<TrackSegmentResponse> response) {
                    callback.onSuccess(response.body());
                }

                @Override
                public void onFailure(Call<TrackSegmentResponse> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });

        } catch (Throwable t) {
            Log.e(TAG, "CRASH BEFORE NETWORK", t);
            callback.onError("Crash Before Network: " + t.getMessage());
        }
    }

    public interface TrackSegmentCallback {
        void onSuccess(TrackSegmentResponse segment);
        void onError(String error);
    }
}