package com.example.lifetrack.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_health_record")
public class DailyHealthRecord {

    @PrimaryKey(autoGenerate = true)
    private int recordId;

    private String date;
    private int exerciseMinutes;
    private float sleepHours;
    private String foodNote;

    // Kept for your local offline calculation
    private int calorieIntake;

    // This constructor exactly matches what your teammate wrote in DailyRecordActivity.java
    public DailyHealthRecord(String date, int exerciseMinutes, float sleepHours, String foodNote) {
        this.date = date;
        this.exerciseMinutes = exerciseMinutes;
        this.sleepHours = sleepHours;
        this.foodNote = foodNote;

        // Default calorie intake so your local math fallback doesn't break
        this.calorieIntake = 2000;
    }

    // --- Getters and Setters Required by Room Database ---
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getDate() {
        return date;
    }
    @SuppressWarnings("unused")
    public void setDate(String date) {
        this.date = date;
    }

    public int getExerciseMinutes() {
        return exerciseMinutes;
    }
    @SuppressWarnings("unused")
    public void setExerciseMinutes(int exerciseMinutes) {
        this.exerciseMinutes = exerciseMinutes;
    }

    public float getSleepHours() {
        return sleepHours;
    }
    @SuppressWarnings("unused")
    public void setSleepHours(float sleepHours) {
        this.sleepHours = sleepHours;
    }

    @SuppressWarnings("unused")
    public String getFoodNote() {
        return foodNote;
    }
    @SuppressWarnings("unused")
    public void setFoodNote(String foodNote) {
        this.foodNote = foodNote;
    }

    public int getCalorieIntake() {
        return calorieIntake;
    }

    public void setCalorieIntake(int calorieIntake) {
        this.calorieIntake = calorieIntake;
    }

    public int calculateLocalScore() {
        // Algorithm: 40% Sleep, 30% Exercise, 30% Diet
        double score = (sleepHours / 8.0 * 40) +
                (exerciseMinutes / 60.0 * 30) +
                (1.0 - (Math.abs(2000 - calorieIntake) / 2000.0)) * 30;

        return (int) Math.min(100, Math.max(0, score));
    }

    public String getLocalClassification() {
        int score = calculateLocalScore();

        if (score >= 80 && score <= 100) {
            return "Healthy";
        } else if (score >= 50 && score <= 79) {
            return "Moderate";
        } else {
            return "Unhealthy"; // Covers 0 to 49
        }
    }
}