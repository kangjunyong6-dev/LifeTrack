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

    public DailyHealthRecord(String date, int exerciseMinutes, float sleepHours, String foodNote) {
        this.date = date;
        this.exerciseMinutes = exerciseMinutes;
        this.sleepHours = sleepHours;
        this.foodNote = foodNote;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

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