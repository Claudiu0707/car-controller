package com.example.carcontroller.api_package;

import com.example.carcontroller.api_package.models_package.CarConfigurationRequest;
import com.example.carcontroller.api_package.models_package.CarConfigurationResponse;
import com.example.carcontroller.api_package.models_package.CircuitRequest;
import com.example.carcontroller.api_package.models_package.CircuitResponse;
import com.example.carcontroller.api_package.models_package.DriverRequest;
import com.example.carcontroller.api_package.models_package.DriverResponse;
import com.example.carcontroller.api_package.models_package.RaceRequest;
import com.example.carcontroller.api_package.models_package.RaceResponse;
import com.example.carcontroller.api_package.models_package.TrackSegmentRequest;
import com.example.carcontroller.api_package.models_package.TrackSegmentResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("drivers")
    Call<DriverResponse> createDriver (@Body DriverRequest driver);

    @POST("races")
    Call<RaceResponse> createRace (@Body RaceRequest race);

    @POST("configurations")
    Call<CarConfigurationResponse> createConfiguration(@Body CarConfigurationRequest configuration);

    @POST("/circuits")
    Call<CircuitResponse> createCircuit(@Body CircuitRequest request);

    @POST("/track-segments")
    Call<TrackSegmentResponse> createTrackSegment(@Body TrackSegmentRequest request);


}
