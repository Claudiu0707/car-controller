package com.example.carcontroller.api_package;

import com.example.carcontroller.api_package.models_package.CircuitRequest;
import com.example.carcontroller.api_package.models_package.CircuitResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CircuitRepository {

    private static CircuitRepository instance;
    private ApiService apiService;

    private CircuitRepository() {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized CircuitRepository getInstance() {
        if (instance == null) instance = new CircuitRepository();
        return instance;
    }

    public void saveCircuit(CircuitRequest request, CircuitCallback callback) {
        apiService.createCircuit(request).enqueue(new Callback<CircuitResponse>() {
            @Override
            public void onResponse(Call<CircuitResponse> call, Response<CircuitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CircuitResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface CircuitCallback {
        void onSuccess(CircuitResponse response);
        void onError(String error);
    }
}
