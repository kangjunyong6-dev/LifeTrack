package com.example.lifetrack.api;

import com.google.gson.annotations.SerializedName;

public class HealthAssessmentResponse {

    @SerializedName("date")
    private String date;

    @SerializedName("healthScore")
    private int healthScore;

    @SerializedName("classification")
    private String classification;

    @SerializedName("trendResult")
    private String trendAnalysis;

    @SerializedName("recommendation")
    private String recommendation;

    // Constructor with 5 arguments
    public HealthAssessmentResponse(String date, int healthScore, String classification, String trendAnalysis, String recommendation) {
        this.date = date;
        this.healthScore = healthScore;
        this.classification = classification;
        this.trendAnalysis = trendAnalysis;
        this.recommendation = recommendation;
    }

    // Getters
    public String getDate() { return date; }
    public int getHealthScore() { return healthScore; }
    public String getClassification() { return classification; }
    public String getTrendAnalysis() { return trendAnalysis; }
    public String getRecommendation() { return recommendation; }
}