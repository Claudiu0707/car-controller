package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.CircuitLocationRequest;
import com.example.carcontroller.api_package.models_package.CircuitLocationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationConfigurationRepository {
    private static final String TAG = "LocationConfigRepositoryTAG";
    private static LocationConfigurationRepository instance;
    private ApiService apiService;

    private LocationConfigurationRepository() {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized LocationConfigurationRepository getInstance() {
        if (instance == null) {
            instance = new LocationConfigurationRepository();
        }
        return instance;
    }

    // Save / get circuit location
    public void saveCircuitLocation(String cityName, String locationName, final CircuitLocationCallback callback) {

        CircuitLocationRequest request = new CircuitLocationRequest(cityName, locationName);

        Call<CircuitLocationResponse> call = apiService.createCircuitLocation(request);

        call.enqueue(new Callback<CircuitLocationResponse>() {
            @Override
            public void onResponse(Call<CircuitLocationResponse> call, Response<CircuitLocationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CircuitLocationResponse locationResponse = response.body();
                    Log.d(TAG, "Circuit location saved. ID: "
                            + locationResponse.getCircuitLocationId());
                    callback.onSuccess(locationResponse);
                } else {
                    String errorMsg = "Failed to save circuit location. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CircuitLocationResponse> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    // Callback interface
    public interface CircuitLocationCallback {
        void onSuccess(CircuitLocationResponse response);
        void onError(String error);
    }
}
