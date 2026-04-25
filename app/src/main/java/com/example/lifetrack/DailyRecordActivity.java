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
        String intensity = spinnerIntensity.getSelectedItem().toString();

        if (exerciseStr.isEmpty() || sleepStr.isEmpty() || rgFoodType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please complete your check-in so we can analyze your day.", Toast.LENGTH_SHORT).show();
            return;
        }

        int exerciseMinutes = Integer.parseInt(exerciseStr);
        float sleepHours = Float.parseFloat(sleepStr);

        String foodType;
        if (rbHealthy.isChecked()) foodType = "Healthy";
        else if (rbNormal.isChecked()) foodType = "Normal";
        else foodType = "Unhealthy";

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("loggedInEmail", null);
            float userWeight = 70.0f; // Default

            if (userEmail != null) {
                UserProfile profile = db.userProfileDao().getProfileByEmail(userEmail);
                if (profile != null && profile.getWeight() > 0) {
                    userWeight = profile.getWeight();
                }
            }

            float metValue = 3.0f; // Low
            if (intensity.equalsIgnoreCase("Medium")) metValue = 5.0f;
            else if (intensity.equalsIgnoreCase("High")) metValue = 8.0f;

            int calculatedCalories = Math.round(exerciseMinutes * (metValue * 3.5f * userWeight) / 200f);

            DailyHealthRecord record = new DailyHealthRecord(
                    todayDate, exerciseMinutes, intensity, calculatedCalories, sleepHours, foodType
            );

            db.dailyHealthRecordDao().insert(record);
            Log.d("DailyRecord", "Saved calories: " + calculatedCalories + " kcal");

            runOnUiThread(() -> {
                Toast.makeText(this, "Daily check-in complete! Burned ~" + calculatedCalories + " kcal.", Toast.LENGTH_LONG).show();
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