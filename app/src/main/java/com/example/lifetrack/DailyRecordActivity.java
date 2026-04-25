package com.example.lifetrack;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.lifetrack.data.entity.UserProfile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DailyRecordActivity extends AppCompatActivity {

    Spinner spinnerIntensity;
    EditText etExerciseMinutes, etSleepHours;
    RadioGroup rgFoodType;
    RadioButton rbHealthy, rbNormal, rbUnhealthy;
    Button btnSaveDailyRecord, btnBackToMain;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_record);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etExerciseMinutes = findViewById(R.id.etExerciseMinutes);
        etSleepHours = findViewById(R.id.etSleepHours);
        rgFoodType = findViewById(R.id.rgFoodType);
        rbHealthy = findViewById(R.id.rbHealthy);
        rbNormal = findViewById(R.id.rbNormal);
        rbUnhealthy = findViewById(R.id.rbUnhealthy);
        spinnerIntensity = findViewById(R.id.spinnerIntensity);
        btnSaveDailyRecord = findViewById(R.id.btnSaveDailyRecord);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        db = AppDatabase.getInstance(this);

        String[] levels = {"Low", "Medium", "High"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                levels
        );
        spinnerIntensity.setAdapter(adapter);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> navigateTo(MainActivity.class));
        navCalendar.setOnClickListener(v -> navigateTo(RecordHistoryActivity.class));
        navProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));

        btnBackToMain.setOnClickListener(v -> navigateTo(MainActivity.class));
        btnSaveDailyRecord.setOnClickListener(v -> saveRecord());
    }

    private void saveRecord() {
        String exerciseStr = etExerciseMinutes.getText().toString().trim();
        String sleepStr = etSleepHours.getText().toString().trim();
        String intensity = spinnerIntensity.getSelectedItem() != null ? spinnerIntensity.getSelectedItem().toString() : "Low";

        if (exerciseStr.isEmpty()) {
            Toast.makeText(this, "Please enter your exercise minutes.", Toast.LENGTH_SHORT).show();
            return;
        }

        final int exerciseMinutes = Integer.parseInt(exerciseStr);
        final float sleepHours = sleepStr.isEmpty() ? 0.0f : Float.parseFloat(sleepStr);

        String foodTypeTemp = "Normal";
        if (rgFoodType.getCheckedRadioButtonId() != -1) {
            if (rbHealthy.isChecked()) foodTypeTemp = "Healthy";
            else if (rbUnhealthy.isChecked()) foodTypeTemp = "Unhealthy";
        }
        final String foodType = foodTypeTemp;
        final String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("loggedInEmail", null);

            // Default values in case the profile isn't found
            float userWeight = 70.0f;
            float userHeight = 170.0f;

            if (userEmail != null) {
                UserProfile profile = db.userProfileDao().getProfileByEmail(userEmail);
                if (profile != null) {
                    // Get the real data from the user's profile
                    if (profile.getWeight() > 0) userWeight = profile.getWeight();
                    // Ensure your UserProfile entity has a getHeight() method!
                    if (profile.getHeight() > 0) userHeight = profile.getHeight();
                }
            }

            // Calorie Calculation Logic
            float metValue = 3.0f;
            if (intensity.equalsIgnoreCase("Medium")) metValue = 5.0f;
            else if (intensity.equalsIgnoreCase("High")) metValue = 8.0f;

            final int finalCalculatedCalories = Math.round(exerciseMinutes * (metValue * 3.5f * userWeight) / 200f);

            // Passing all 8 required arguments to match DailyHealthRecord
            DailyHealthRecord record = new DailyHealthRecord(
                    todayDate,
                    exerciseMinutes,
                    intensity,
                    finalCalculatedCalories,
                    sleepHours,
                    foodType,
                    userWeight, // New Argument 7
                    userHeight  // New Argument 8
            );

            db.dailyHealthRecordDao().insert(record);
            Log.d("DailyRecord", "Saved calories: " + finalCalculatedCalories + " kcal");

            runOnUiThread(() -> {
                Toast.makeText(this, "Check-in complete! Burned ~" + finalCalculatedCalories + " kcal.", Toast.LENGTH_LONG).show();
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