package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DailyRecordActivity extends AppCompatActivity {

    Spinner spinnerIntensity;
    EditText etExerciseMinutes, etSleepHours, etCalories;
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
        spinnerIntensity = findViewById(R.id.spinnerIntensity);
        etCalories = findViewById(R.id.etCalories);
        btnSaveDailyRecord = findViewById(R.id.btnSaveDailyRecord);
        btnBackToMain = findViewById(R.id.btnBackToMain); // "Cancel" button

        db = AppDatabase.getInstance(this);

        String[] levels = {"Low", "Medium", "High"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                levels
        );

        spinnerIntensity.setAdapter(adapter);
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

        // ⭐ NEW: get intensity + calories
        String intensity = spinnerIntensity.getSelectedItem().toString();

        String caloriesStr = etCalories.getText().toString().trim();
        int calories = caloriesStr.isEmpty() ? 0 : Integer.parseInt(caloriesStr);

        // validation
        if (exerciseStr.isEmpty() || sleepStr.isEmpty()
                || rgFoodType.getCheckedRadioButtonId() == -1) {

            Toast.makeText(this,
                    "Please complete your check-in so we can analyze your day.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int exerciseMinutes = Integer.parseInt(exerciseStr);
        float sleepHours = Float.parseFloat(sleepStr);

        String foodType;
        if (rbHealthy.isChecked()) {
            foodType = "Healthy";
        } else if (rbNormal.isChecked()) {
            foodType = "Normal";
        } else {
            foodType = "Unhealthy";
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {

            DailyHealthRecord record = new DailyHealthRecord(
                    todayDate,
                    exerciseMinutes,
                    intensity,
                    calories,
                    sleepHours,
                    foodType
            );

            db.dailyHealthRecordDao().insert(record);
            Log.d("TEST", "Saved: " + exerciseMinutes + " / " + sleepHours + " / " + foodType);
            runOnUiThread(() -> {
                Toast.makeText(this,
                        "Daily check-in complete! Your insights are ready.",
                        Toast.LENGTH_LONG).show();

                navigateTo(MainActivity.class);
            });
        });
    }

    private void navigateTo(Class<?> targetClass) {
        Intent intent = new Intent(this, targetClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}