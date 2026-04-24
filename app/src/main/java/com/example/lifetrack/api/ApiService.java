package com.example.lifetrack.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {


    @Headers({
            "Prefer: return=representation"
    })
    @POST("rest/v1/health_records")
    Call<Void> sendHealthData(@Body HealthDataRequest request);

    @POST("functions/v1/analyze-health")
    Call<HealthAssessmentResponse> analyzeHealthData(@Body HealthDataRequest request);
}