package com.example.lifetrack.api;

import com.google.gson.annotations.SerializedName;

public class HealthAssessmentResponse {

    @SerializedName("healthScore")
    private int healthScore;

    @SerializedName("classification")
    private String classification;

    @SerializedName("trendAnalysis")
    private String trendAnalysis;

    // Getters and Setters
    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getTrendAnalysis() {
        return trendAnalysis;
    }

    public void setTrendAnalysis(String trendAnalysis) {
        this.trendAnalysis = trendAnalysis;
    }
}