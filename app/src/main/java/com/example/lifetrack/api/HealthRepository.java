package com.example.lifetrack.api;

import com.example.lifetrack.data.entity.DailyHealthRecord;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthRepository {

    private ApiService apiService;

    public HealthRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    /**
     * Send health data to the external REST API and get an assessment.
     *
     * @param record   The DailyHealthRecord from your local database
     * @param callback The callback to handle success or failure
     */
    public void getHealthAssessment(DailyHealthRecord record, final ApiCallback callback) {

        // Convert the Room entity to an API request model
        HealthDataRequest request = new HealthDataRequest(
                record.getDate(),
                record.getExerciseMinutes(),
                record.getSleepHours(),
                record.getFoodNote()
        );

        Call<HealthAssessmentResponse> call = apiService.analyzeHealthData(request);

        call.enqueue(new Callback<HealthAssessmentResponse>() {
            @Override
            public void onResponse(Call<HealthAssessmentResponse> call, Response<HealthAssessmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<HealthAssessmentResponse> call, Throwable t) {
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }

    // Custom callback interface
    public interface ApiCallback {
        void onSuccess(HealthAssessmentResponse assessment);
        void onError(String errorMessage);
    }
}