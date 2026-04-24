package com.example.lifetrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.api.HealthAssessmentResponse;
import com.example.lifetrack.api.HealthRepository;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.util.List;
import java.util.concurrent.Executors;

public class HealthScoreSystem extends AppCompatActivity {

    private Button btnAnalyze, btnBackToDashboard;
    private TextView tvScore, tvStatus, tvAdvice;
    private LinearLayout navHome, navCalendar, navProfile;

    private HealthRepository apiRepository;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Stabilize UI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.parseColor("#F4F7FC"));
        }

        setContentView(R.layout.activity_health_score_system);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initializeViews();
        setupNavigation();

        btnAnalyze.setOnClickListener(v -> triggerSmartAnalysis());

        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);
        tvScore = findViewById(R.id.tvScore);
        tvStatus = findViewById(R.id.tvStatus);
        tvAdvice = findViewById(R.id.tvAdvice);

        navHome = findViewById(R.id.navHome);
        navCalendar = findViewById(R.id.navCalendar);
        navProfile = findViewById(R.id.navProfile);

        apiRepository = new HealthRepository();
        db = AppDatabase.getInstance(this);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        navCalendar.setOnClickListener(v -> {
            startActivity(new Intent(this, RecordHistoryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
    }

    private void triggerSmartAnalysis() {
        tvStatus.setText("Analyzing...");
        tvAdvice.setText("Processing health metrics...");
        btnAnalyze.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<DailyHealthRecord> records = db.dailyHealthRecordDao().getAllRecords();

                if (records == null || records.isEmpty()) {
                    runOnUiThread(() -> {
                        btnAnalyze.setEnabled(true);
                        tvStatus.setText("No Data");
                        tvAdvice.setText("Please add a daily record first.");
                    });
                    return;
                }

                // Get newest record
                DailyHealthRecord latest = records.get(0);

                apiRepository.getHealthAssessment(latest, new HealthRepository.ApiCallback() {
                    @Override
                    public void onSuccess(HealthAssessmentResponse result) {
                        runOnUiThread(() -> {
                            try {
                                if (result != null && !isFinishing()) {
                                    updateResultUI(result.getHealthScore(), result.getClassification(), result.getTrendAnalysis());
                                } else {
                                    handleFallback(latest);
                                }
                            } catch (Exception e) {
                                // Catch missing API data crashes
                                handleFallback(latest);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> handleFallback(latest));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnAnalyze.setEnabled(true);
                    tvStatus.setText("System Error");
                    Toast.makeText(this, "Internal process failed.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleFallback(DailyHealthRecord record) {
        if (isFinishing()) return;
        int localScore = calculateLocalFallback(record);
        updateResultUI(localScore, "Moderate", "Connection timeout. Showing offline analysis based on your activity.");
    }

    private void updateResultUI(int score, String status, String advice) {
        if (isFinishing()) return;

        btnAnalyze.setEnabled(true);
        tvScore.setText(String.valueOf(score));

        // Prevent crashes if the server sends empty text
        tvStatus.setText(status != null ? status : "Analyzed");
        tvAdvice.setText(advice != null ? advice : "Keep up the good work.");

        if (score >= 80) tvStatus.setTextColor(Color.parseColor("#38A169"));
        else if (score >= 50) tvStatus.setTextColor(Color.parseColor("#D69E2E"));
        else tvStatus.setTextColor(Color.parseColor("#E53E3E"));
    }

    private int calculateLocalFallback(DailyHealthRecord record) {
        float score = (record.getSleepHours() * 7) + (record.getExerciseMinutes() / 2.5f);

        // THE FIX: We use FoodNote instead of CalorieIntake because CalorieIntake is empty!
        if (record.getFoodNote() != null && record.getFoodNote().equalsIgnoreCase("Healthy")) {
            score += 15;
        } else if (record.getFoodNote() != null && record.getFoodNote().equalsIgnoreCase("Normal")) {
            score += 5;
        }

        return (int) Math.min(score, 100);
    }
}