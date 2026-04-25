package com.example.lifetrack.api;

import android.os.Handler;
import android.os.Looper;

import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthRepository {

    private ApiService apiService;

    public HealthRepository() {
        // Initializes the Retrofit client
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Used by DailyRecordActivity to push data AND get the result
    public void getHealthAssessment(DailyHealthRecord record, final ApiCallback callback) {
        HealthDataRequest request = new HealthDataRequest(
                record.getDate(),
                record.getExerciseMinutes(),
                record.getSleepHours(),
                record.getFoodNote()
        );

        apiService.sendHealthData(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        fetchCloudAssessment(record, callback);
                    }, 2000);
                } else {
                    generateLocalAssessment(record, "Sync Error (" + response.code() + ")", callback);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                generateLocalAssessment(record, "Offline Mode", callback);
            }
        });
    }

    private void fetchCloudAssessment(DailyHealthRecord record, final ApiCallback callback) {
        apiService.getLatestAssessment().enqueue(new Callback<List<HealthAssessmentResponse>>() {
            @Override
            public void onResponse(Call<List<HealthAssessmentResponse>> call, Response<List<HealthAssessmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0), "CLOUD AI ANALYSIS");
                } else {
                    generateLocalAssessment(record, "Local Assessment (Cloud Pending)", callback);
                }
            }

            @Override
            public void onFailure(Call<List<HealthAssessmentResponse>> call, Throwable t) {
                generateLocalAssessment(record, "Offline Mode", callback);
            }
        });
    }


    public void fetchLatestAssessmentOnly(final ApiCallback callback) {
        apiService.getLatestAssessment().enqueue(new Callback<List<HealthAssessmentResponse>>() {
            @Override
            public void onResponse(Call<List<HealthAssessmentResponse>> call, Response<List<HealthAssessmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0), "CLOUD AI ANALYSIS");
                } else {
                    // Update this to 5 arguments as well
                    HealthAssessmentResponse empty = new HealthAssessmentResponse(
                            "Today",
                            0,
                            "No Data",
                            "Check-in to get your first Cloud AI Analysis.",
                            "No recommendations yet."
                    );
                    callback.onSuccess(empty, "No Data");
                }
            }

            @Override
            public void onFailure(Call<List<HealthAssessmentResponse>> call, Throwable t) {
                callback.onError("Network Error: Could not connect to Supabase");
            }
        });
    }

    // Local Fallback Logic (Only runs if Cloud fails)
    private void generateLocalAssessment(DailyHealthRecord record, String sourceLabel, ApiCallback callback) {
        int score = 50;
        if (record.getSleepHours() >= 7) score += 20;
        else if (record.getSleepHours() >= 5) score += 10;

        if (record.getExerciseMinutes() >= 30) score += 20;
        else if (record.getExerciseMinutes() >= 10) score += 10;

        if ("Healthy".equalsIgnoreCase(record.getFoodNote())) score += 10;
        else if ("Unhealthy".equalsIgnoreCase(record.getFoodNote())) score -= 10;

        score = Math.max(0, Math.min(score, 100));

        String status = (score >= 80) ? "Healthy" : (score >= 50) ? "Moderate" : "Unhealthy";
        String prediction = "Local Analysis: Your activity suggests an improving trend.";
        String localRec = "Sync to cloud for detailed AI recommendations.";

        HealthAssessmentResponse fallback = new HealthAssessmentResponse(
                record.getDate(),
                score,
                status,
                prediction,
                localRec
        );

        callback.onSuccess(fallback, sourceLabel);
    }

    public interface ApiCallback {
        void onSuccess(HealthAssessmentResponse assessment, String source);
        void onError(String errorMessage);
    }
}