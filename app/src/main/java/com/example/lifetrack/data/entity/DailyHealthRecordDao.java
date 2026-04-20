package com.example.lifetrack.data.entity;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DailyHealthRecordDao {

    @Insert
    void insert(DailyHealthRecord record);

    @Query("SELECT * FROM daily_health_record ORDER BY recordId DESC")
    List<DailyHealthRecord> getAllRecords();
}