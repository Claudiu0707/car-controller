package com.example.carcontroller.api_package;

import android.util.Log;

import com.example.carcontroller.api_package.models_package.RaceRequest;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.devices_package.DeviceManager;
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

    public void saveRace(SessionManager.RaceSession raceSession, final RaceCallback callback) {
        try {
            if (raceSession == null) {
                throw new NullPointerException("raceSession is null");
            }

            long millis = raceSession.getTotalTime();
            String totalTime = Duration.ofMillis(millis).toString();

            Integer configurationId = null;
            DeviceManager dm = DeviceManager.getInstance();
            if (dm != null && dm.getCarDevice() != null && dm.getCarDevice().getConfiguration() != null) {
                configurationId = dm.getCarDevice().getConfiguration().getConfigurationId();
            }

            Integer driverId = null;
            Integer circuitId = null;
            SessionManager sm = SessionManager.getInstance();
            if (sm != null) {
                if (sm.getCurrentDriver() != null) {
                    driverId = sm.getCurrentDriver().getDriverId();
                }
                if (sm.getCurrentCircuit() != null) {
                    circuitId = sm.getCurrentCircuit().getCircuitId();
                }
            }

            RaceRequest request = new RaceRequest(
                    driverId,
                    circuitId,
                    configurationId,
                    raceSession.getRaceDate(),
                    totalTime
            );

            Call<RaceResponse> call = apiService.createRace(request);
            call.enqueue(new Callback<RaceResponse>() {
                @Override
                public void onResponse(Call<RaceResponse> call, Response<RaceResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError("HTTP " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RaceResponse> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });

        } catch (Throwable t) {
            Log.e(TAG, "CRASH BEFORE NETWORK", t);
            callback.onError("Crash Before Network: " + t.getMessage());
        }
    }

    public interface RaceCallback {
        void onSuccess (RaceResponse race) ;
        void onError (String error);
    }
}
