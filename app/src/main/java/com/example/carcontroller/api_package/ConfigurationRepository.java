package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.CarConfigurationRequest;
import com.example.carcontroller.api_package.models_package.CarConfigurationResponse;
import com.example.carcontroller.devices_package.CarDevice;

import retrofit2.*;

public class ConfigurationRepository {
    private static final String TAG = "ConfigurationRepositoryTAG";
    private static ConfigurationRepository instance;
    private ApiService apiService;

    private ConfigurationRepository() {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized ConfigurationRepository getInstance() {
        if (instance == null) instance = new ConfigurationRepository();
        return instance;
    }

    public void saveConfiguration(CarDevice carDevice, final ConfigurationCallback callback) {
        CarConfigurationRequest request = new CarConfigurationRequest(
            carDevice.getConfiguration().getKp(),
            carDevice.getConfiguration().getKi(),
            carDevice.getConfiguration().getKd(),
            carDevice.getConfiguration().getBaseLeftSpeed(),
            carDevice.getConfiguration().getBaseRightSpeed(),
            carDevice.getConfiguration().getCreationDate());

        // This version of the app supports only line follower configuration data
        // CarDevice.OperationMode mode = carDevice.getCurrentMode();
        request.setOperationModeId(convertModeToId(CarDevice.OperationMode.LINE_FOLLOWER));

        Call<CarConfigurationResponse> call = apiService.createConfiguration(request);
        call.enqueue(new Callback<CarConfigurationResponse>() {
            @Override
            public void onResponse(Call<CarConfigurationResponse> call, Response<CarConfigurationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CarConfigurationResponse configurationResponse = response.body();
                    Log.d(TAG, "Configuration saved successfully with ID: " + configurationResponse.getCarConfigurationId());
                    callback.onSuccess(configurationResponse);
                } else {
                    String errorMsg = "Failed to save configuration. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }
            @Override
            public void onFailure(Call<CarConfigurationResponse> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    private Integer convertModeToId (CarDevice.OperationMode mode) {
        if (mode == null) return null;

        switch (mode) {
            case DRIVE:
                return 1;
            case LINE_FOLLOWER:
                return 2;
            default:
                return null;
        }
    }
    public interface ConfigurationCallback {
        void onSuccess(CarConfigurationResponse configuration);
        void onError(String error);
    }
}
