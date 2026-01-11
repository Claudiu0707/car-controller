package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.DriverResponse;
import com.example.carcontroller.api_package.models_package.RaceRequest;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.main_package.SessionManager;

import java.time.Duration;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RaceRepository {
    private static final String TAG = "RaceRepositoryTAG";
    private static RaceRepository instance;
    private ApiService apiService;
    private RaceRepository () {
        apiService =ApiClient.getInstance().getApiService();
    }

    public static synchronized RaceRepository getInstance() {
        if (instance == null) instance = new RaceRepository();
        return instance;
    }

    public void saveRace (SessionManager.RaceSession raceSession, final RaceCallback callback) {
        // TODO: solve the crash problem when accessing raceSession
        /*raceSession.getCircuitId(),
            raceSession.getRaceDate(),
            raceSession.getFinishTime()*/
        RaceRequest request = new RaceRequest(
            "1000-10-10", Duration.ofMinutes(1).plusSeconds(23).toString()
        );
        Log.d(TAG, "Crashes here?");
        Call<RaceResponse> call = apiService.createRace(request);

        call.enqueue(new Callback<RaceResponse>() {
            @Override
            public void onResponse(Call<RaceResponse> call, Response<RaceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RaceResponse raceResponse = response.body();
                    Log.d(TAG, "Race saved successfully wit ID: " + raceResponse.getRaceId());
                    callback.onSuccess(raceResponse);
                } else {
                    String errorMessage = "Failed to save race. Code: " + response.code();
                    Log.d(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<RaceResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public interface RaceCallback {
        void onSuccess (RaceResponse race) ;
        void onError (String error);
    }
}
