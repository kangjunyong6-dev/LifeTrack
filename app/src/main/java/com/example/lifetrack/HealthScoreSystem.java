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
        navHome.setOnClickListener(v -> navigateTo(MainActivity.class));
        navCalendar.setOnClickListener(v -> navigateTo(RecordHistoryActivity.class));
        navProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
    }

    private void triggerSmartAnalysis() {
        tvStatus.setText("Syncing...");
        tvAdvice.setText("Connecting to Supabase Cloud...");
        btnAnalyze.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Get the latest local record
                List<DailyHealthRecord> records = db.dailyHealthRecordDao().getAllRecords();
                if (records == null || records.isEmpty()) {
                    runOnUiThread(() -> {
                        btnAnalyze.setEnabled(true);
                        tvStatus.setText("No Data");
                        tvAdvice.setText("Please record your health data first.");
                    });
                    return;
                }
                DailyHealthRecord latest = db.dailyHealthRecordDao().getLatestRecord();

                // Analyze using Supabase Function
                apiRepository.getHealthAssessment(latest, new HealthRepository.ApiCallback() {
                    @Override
                    public void onSuccess(HealthAssessmentResponse result) {
                        runOnUiThread(() -> {
                            if (result != null && !isFinishing()) {
                                updateResultUI(result.getHealthScore(), result.getClassification(), result.getTrendAnalysis());
                            } else {
                                handleFallback(latest, "Success but empty response.");
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> handleFallback(latest, "Cloud unreachable: " + error));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnAnalyze.setEnabled(true);
                    tvStatus.setText("Local Error");
                });
            }
        });
    }

    private void handleFallback(DailyHealthRecord record, String reason) {
        if (isFinishing()) return;
        int score = calculateLocalFallback(record);
        updateResultUI(score, "Moderate (Offline)", "Showing local assessment. Reason: " + reason);
        Toast.makeText(this, "Operating in Offline Mode", Toast.LENGTH_SHORT).show();
    }

    private void updateResultUI(int score, String status, String advice) {
        btnAnalyze.setEnabled(true);
        tvScore.setText(String.valueOf(score));
        tvStatus.setText(status);
        tvAdvice.setText(advice);

        if (score >= 80) tvStatus.setTextColor(Color.parseColor("#38A169"));
        else if (score >= 50) tvStatus.setTextColor(Color.parseColor("#D69E2E"));
        else tvStatus.setTextColor(Color.parseColor("#E53E3E"));
    }

    private int calculateLocalFallback(DailyHealthRecord r) {
        int score = 50;

        if (r.getSleepHours() >= 7) score += 20;
        else if (r.getSleepHours() >= 5) score += 10;

        if (r.getExerciseMinutes() >= 30) score += 20;
        else if (r.getExerciseMinutes() >= 10) score += 10;

        if (r.getFoodNote().equalsIgnoreCase("Healthy")) score += 10;
        else if (r.getFoodNote().equalsIgnoreCase("Unhealthy")) score -= 10;

        return Math.max(0, Math.min(score, 100));
    }

    private void navigateTo(Class<?> target) {
        startActivity(new Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}