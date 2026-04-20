package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
        btnSaveDailyRecord = findViewById(R.id.btnSaveDailyRecord);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        AppDatabase db = AppDatabase.getInstance(this);

        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(DailyRecordActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnSaveDailyRecord.setOnClickListener(v -> {
            String exerciseStr = etExerciseMinutes.getText().toString().trim();
            String sleepStr = etSleepHours.getText().toString().trim();

            if (exerciseStr.isEmpty() || sleepStr.isEmpty() || rgFoodType.getCheckedRadioButtonId() == -1) {
                Toast.makeText(DailyRecordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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

            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Executors.newSingleThreadExecutor().execute(() -> {
                DailyHealthRecord record = new DailyHealthRecord(
                        todayDate,
                        exerciseMinutes,
                        sleepHours,
                        foodType
                );

                db.dailyHealthRecordDao().insert(record);

                runOnUiThread(() ->
                        Toast.makeText(DailyRecordActivity.this, "Daily record saved", Toast.LENGTH_SHORT).show()
                );
            });
        });
    }
}