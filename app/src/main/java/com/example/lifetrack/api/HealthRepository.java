package com.example.lifetrack.api;

import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthRepository {

    private ApiService apiService;

    public HealthRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getHealthAssessment(DailyHealthRecord record, final ApiCallback callback) {

        // 1. Create the request object from your Room record
        HealthDataRequest request = new HealthDataRequest(
                record.getDate(),
                record.getExerciseMinutes(),
                record.getSleepHours(),
                record.getFoodNote()
        );

        // 2. FIRST: Save the record to 'health_records' table
        apiService.sendHealthData(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // 3. SECOND: After saving, fetch the assessment result
                fetchLatestResult(callback);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Failed to upload data: " + t.getMessage());
            }
        });
    }

    private void fetchLatestResult(final ApiCallback callback) {
        // This calls the GET method we fixed in ApiService
        apiService.getLatestAssessment().enqueue(new Callback<List<HealthAssessmentResponse>>() {
            @Override
            public void onResponse(Call<List<HealthAssessmentResponse>> call, Response<List<HealthAssessmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Supabase returns a list, so we take the top (latest) one
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Assessment not ready yet. Please wait.");
                }
            }

            @Override
            public void onFailure(Call<List<HealthAssessmentResponse>> call, Throwable t) {
                callback.onError("Cloud error: " + t.getMessage());
            }
        });
    }

    public interface ApiCallback {
        void onSuccess(HealthAssessmentResponse assessment);
        void onError(String errorMessage);
    }
}