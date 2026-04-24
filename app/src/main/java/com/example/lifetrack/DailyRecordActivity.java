package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DailyRecordActivity extends AppCompatActivity {

    EditText etExerciseMinutes, etSleepHours;
    RadioGroup rgFoodType;
    RadioButton rbHealthy, rbNormal, rbUnhealthy;
    Button btnSaveDailyRecord, btnBackToMain;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_record);

        // Hide Action Bar for clean UI
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize UI Elements
        etExerciseMinutes = findViewById(R.id.etExerciseMinutes);
        etSleepHours = findViewById(R.id.etSleepHours);
        rgFoodType = findViewById(R.id.rgFoodType);
        rbHealthy = findViewById(R.id.rbHealthy);
        rbNormal = findViewById(R.id.rbNormal);
        rbUnhealthy = findViewById(R.id.rbUnhealthy);
        btnSaveDailyRecord = findViewById(R.id.btnSaveDailyRecord);
        btnBackToMain = findViewById(R.id.btnBackToMain); // "Cancel" button

        db = AppDatabase.getInstance(this);

        // --- GLOBAL NAVIGATION FOOTER ---
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> navigateTo(MainActivity.class));
        navCalendar.setOnClickListener(v -> navigateTo(RecordHistoryActivity.class));
        navProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));

        // --- SECONDARY ACTION: CANCEL ---
        btnBackToMain.setOnClickListener(v -> navigateTo(MainActivity.class));

        // --- PRIMARY ACTION: SAVE RECORD ---
        btnSaveDailyRecord.setOnClickListener(v -> saveRecord());
    }

    private void saveRecord() {
        String exerciseStr = etExerciseMinutes.getText().toString().trim();
        String sleepStr = etSleepHours.getText().toString().trim();

        if (exerciseStr.isEmpty() || sleepStr.isEmpty() || rgFoodType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please complete your check-in so we can analyze your day.", Toast.LENGTH_SHORT).show();
            return;
        }

        int exerciseMinutes = Integer.parseInt(exerciseStr);
        float sleepHours = Float.parseFloat(sleepStr);

        String foodType;
        if (rbHealthy.isChecked()) {
            foodType = "Healthy"; // Displays as Nourishing
        } else if (rbNormal.isChecked()) {
            foodType = "Normal";  // Displays as Balanced
        } else {
            foodType = "Unhealthy"; // Displays as Treat/Heavy
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {
            DailyHealthRecord record = new DailyHealthRecord(
                    todayDate,
                    exerciseMinutes,
                    sleepHours,
                    foodType
            );

            db.dailyHealthRecordDao().insert(record);

            runOnUiThread(() -> {
                Toast.makeText(this, "Daily check-in complete! Your insights are ready.", Toast.LENGTH_LONG).show();
                navigateTo(MainActivity.class); // Auto-return to dashboard after saving
            });
        });
    }

    // Helper for clean navigation
    private void navigateTo(Class<?> targetClass) {
        Intent intent = new Intent(this, targetClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}