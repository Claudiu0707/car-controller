package com.example.carcontroller.api_package;

import com.example.carcontroller.api_package.models_package.CarConfigurationRequest;
import com.example.carcontroller.api_package.models_package.CarConfigurationResponse;
import com.example.carcontroller.api_package.models_package.DriverRequest;
import com.example.carcontroller.api_package.models_package.DriverResponse;
import com.example.carcontroller.api_package.models_package.RaceRequest;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.devices_package.CarDevice;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("drivers")
    Call<DriverResponse> createDriver (@Body DriverRequest driver);

    @POST("races")
    Call<RaceResponse> createRace (@Body RaceRequest race);

    @POST("configurations")
    Call<CarConfigurationResponse> createConfiguration(@Body CarConfigurationRequest configuration);
}
