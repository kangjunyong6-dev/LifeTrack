package com.example.lifetrack.api;

// Removed redundant imports of classes in the same package
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("rest/v1/health_records")
    Call<Void> sendHealthData(@Body HealthDataRequest request);

    @GET("rest/v1/health_assessments?select=*&order=date.desc&limit=1")
    Call<List<HealthAssessmentResponse>> getLatestAssessment();
}