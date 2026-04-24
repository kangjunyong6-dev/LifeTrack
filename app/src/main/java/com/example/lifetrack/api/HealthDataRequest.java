package com.example.lifetrack.api;

import com.google.gson.annotations.SerializedName;

public class HealthDataRequest {

    @SerializedName("date")
    private String date;

    @SerializedName("exerciseMinutes")
    private int exerciseMinutes;

    @SerializedName("sleepHours")
    private float sleepHours;

    @SerializedName("foodNote")
    private String foodNote;

    // Constructor
    public HealthDataRequest(String date, int exerciseMinutes, float sleepHours, String foodNote) {
        this.date = date;
        this.exerciseMinutes = exerciseMinutes;
        this.sleepHours = sleepHours;
        this.foodNote = foodNote;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getExerciseMinutes() {
        return exerciseMinutes;
    }

    public void setExerciseMinutes(int exerciseMinutes) {
        this.exerciseMinutes = exerciseMinutes;
    }

    public float getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(float sleepHours) {
        this.sleepHours = sleepHours;
    }

    public String getFoodNote() {
        return foodNote;
    }

    public void setFoodNote(String foodNote) {
        this.foodNote = foodNote;
    }
}