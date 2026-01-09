package com.example.carcontroller.api;

import com.example.carcontroller.api.models.DriverRequest;
import com.example.carcontroller.api.models.DriverResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("drivers")
    Call<DriverResponse> createDriver (@Body DriverRequest driver);
}
