package com.example.lifetrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.api.HealthAssessmentResponse;
import com.example.lifetrack.api.HealthRepository;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.util.List;
import java.util.concurrent.Executors;

public class HealthScoreSystem extends AppCompatActivity {

    Button btnAnalyze;
    TextView tvScore, tvStatus, tvAdvice, tvReportTitle;
    LinearLayout navHome, navCalendar, navProfile;

    private HealthRepository apiRepository;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CRITICAL: Point to the new layout file
        setContentView(R.layout.activity_health_score_system);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Initialize Views
        btnAnalyze = findViewById(R.id.btnAnalyze);
        tvScore = findViewById(R.id.tvScore);
        tvStatus = findViewById(R.id.tvStatus);
        tvAdvice = findViewById(R.id.tvAdvice);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        apiRepository = new HealthRepository();
        db = AppDatabase.getInstance(this);

        // Footer Navigation Logic
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navCalendar = findViewById(R.id.navCalendar);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(HealthScoreSystem.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(HealthScoreSystem.this, RecordHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HealthScoreSystem.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnAnalyze.setOnClickListener(v -> triggerSmartAnalysis());
    }

    private void triggerSmartAnalysis() {
        tvStatus.setText("Analyzing...");
        btnAnalyze.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<DailyHealthRecord> records = db.dailyHealthRecordDao().getAllRecords();

            if (records.isEmpty()) {
                runOnUiThread(() -> {
                    btnAnalyze.setEnabled(true);
                    tvStatus.setText("No Data");
                    tvAdvice.setText("Please log your activity today before running analysis.");
                });
                return;
            }

            DailyHealthRecord latest = records.get(0);

            // API Call
            apiRepository.getHealthAssessment(latest, new HealthRepository.ApiCallback() {
                @Override
                public void onSuccess(HealthAssessmentResponse result) {
                    runOnUiThread(() -> updateResultUI(result.getHealthScore(), result.getClassification(), result.getTrendAnalysis()));
                }

                @Override
                public void onError(String error) {
                    // Fallback to local logic (80-100 Healthy, 50-79 Moderate, etc.)
                    int score = latest.calculateLocalScore();
                    String status = latest.getLocalClassification();
                    runOnUiThread(() -> updateResultUI(score, status, "Using local processing. Connect to internet for AI trends."));
                }
            });
        });
    }

    private void updateResultUI(int score, String status, String advice) {
        btnAnalyze.setEnabled(true);
        tvScore.setText(String.valueOf(score));
        tvStatus.setText(status);
        tvAdvice.setText(advice);

        // Color Coding based on your Module 4 requirements
        if (status.equalsIgnoreCase("Healthy")) {
            tvStatus.setTextColor(Color.parseColor("#38A169")); // Green
        } else if (status.equalsIgnoreCase("Moderate")) {
            tvStatus.setTextColor(Color.parseColor("#D69E2E")); // Yellow/Orange
        } else {
            tvStatus.setTextColor(Color.parseColor("#E53E3E")); // Red
        }
    }
}