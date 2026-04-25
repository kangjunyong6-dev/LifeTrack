package com.example.lifetrack.api;

import com.google.gson.annotations.SerializedName;

public class HealthAssessmentResponse {

    @SerializedName("healthScore")
    private int healthScore;

    @SerializedName("classification")
    private String classification;

    @SerializedName("trendResult")
    private String trendAnalysis;


    public HealthAssessmentResponse(String date, int healthScore, String classification, String trendAnalysis) {
        this.healthScore = healthScore;
        this.classification = classification;
        this.trendAnalysis = trendAnalysis;
    }

    public int getHealthScore() { return healthScore; }
    public String getClassification() { return classification; }
    public String getTrendAnalysis() { return trendAnalysis; }
}