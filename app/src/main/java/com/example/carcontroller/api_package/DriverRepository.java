package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.main_package.SessionManager;
import com.example.carcontroller.api_package.models_package.DriverRequest;
import com.example.carcontroller.api_package.models_package.DriverResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRepository {
    private static final String TAG = "DriverRepositoryTAG";
    private static DriverRepository instance;
    private ApiService apiService;

    private DriverRepository () {
        apiService = ApiClient.getInstance().getApiService();
    }

    public static synchronized DriverRepository getInstance () {
        if (instance == null) instance = new DriverRepository();
        return instance;
    }

    // Save driver to database
    public void saveDriver (SessionManager.Driver driver, final DriverCallback callback) {
        // Convert SessionManager.Gender enum to gender_id
        Integer genderId = convertGenderToId(driver.getDriverGender());

        // Create request object
        DriverRequest request = new DriverRequest(
                driver.getDriverFirstName(),
                driver.getDriverLastName(),
                driver.getBirthdate(),  // Should be in "yyyy-MM-dd" format
                genderId
        );

        // Make API call
        Call<DriverResponse> call = apiService.createDriver(request);
        call.enqueue(new Callback<DriverResponse>() {
            @Override
            public void onResponse (Call<DriverResponse> call, Response<DriverResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DriverResponse driverResponse = response.body();
                    Log.d(TAG, "Driver saved successfully with ID: " + driverResponse.getDriverId());
                    callback.onSuccess(driverResponse);
                } else {
                    String errorMsg = "Failed to save driver. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure (Call<DriverResponse> call, Throwable t) {
                String errorMsg = "Network error: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }


    // This information must correspond to the database Gender
    private Integer convertGenderToId (SessionManager.Gender gender) {
        if (gender == null) return null;

        switch (gender) {
            case MALE:
                return 1;
            case FEMALE:
                return 2;
            case OTHER:
                return 3;
            default:
                return null;
        }
    }

    // Callback interfaces
    public interface DriverCallback {
        void onSuccess (DriverResponse driver);

        void onError (String error);
    }
}