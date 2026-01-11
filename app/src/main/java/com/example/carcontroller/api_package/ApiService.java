package com.example.carcontroller.api_package;

import com.example.carcontroller.api_package.models_package.DriverRequest;
import com.example.carcontroller.api_package.models_package.DriverResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("drivers")
    Call<DriverResponse> createDriver (@Body DriverRequest driver);
}
