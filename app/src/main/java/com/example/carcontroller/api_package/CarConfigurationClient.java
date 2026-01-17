package com.example.carcontroller.api_package;

import com.example.carcontroller.api_package.dto.CarConfigurationDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CarConfigurationClient {
    private static final String BASE_URL = "http://10.0.2.2:8000/car-configurations";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();

    public CarConfigurationDto[] getAllConfigurations() throws Exception {
        Request request = new Request.Builder().url(BASE_URL).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP error: " + response.code());
            }
            String responseBody = response.body().string();
            return mapper.readValue(responseBody, CarConfigurationDto[].class
            );
        }
    }
}
