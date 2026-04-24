package com.example.lifetrack;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.api.HealthAssessmentResponse;
import com.example.lifetrack.api.HealthRepository;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;
import com.example.lifetrack.data.entity.UserProfile;

import java.util.List;
import java.util.concurrent.Executors;

public class HealthScoreSystem extends AppCompatActivity {

    Button btnAnalyze;
    TextView tvActiveProfile, tvScore, tvStatus, tvAdvice, tvReportTitle;

    private HealthRepository apiRepository;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_score_system); // Ensure this matches your XML filename

        btnAnalyze = findViewById(R.id.btnAnalyze);
        tvActiveProfile = findViewById(R.id.tvActiveProfile);
        tvScore = findViewById(R.id.tvScore);
        tvStatus = findViewById(R.id.tvStatus);
        tvAdvice = findViewById(R.id.tvAdvice);
        tvReportTitle = findViewById(R.id.tvReportTitle);

        apiRepository = new HealthRepository();
        db = AppDatabase.getInstance(this);

        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile savedUser = db.userProfileDao().getProfile();
            runOnUiThread(() -> {
                if (savedUser != null) {
                    // Friendly greeting instead of "Active User"
                    tvActiveProfile.setText("Welcome back, " + savedUser.getName() + "!");
                } else {
                    tvActiveProfile.setText("Please complete your profile setup.");
                    btnAnalyze.setEnabled(false);
                }
            });
        });

        btnAnalyze.setOnClickListener(v -> triggerSmartAnalysis());
    }

    private void triggerSmartAnalysis() {
        // Consumer-friendly loading text
        tvStatus.setText("Reviewing your habits...");
        tvStatus.setTextColor(Color.parseColor("#4A5568")); // Default grey
        tvScore.setText("--");
        tvAdvice.setText("Please wait a moment while our AI generates your personalized insights.");
        btnAnalyze.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<DailyHealthRecord> records = db.dailyHealthRecordDao().getAllRecords();

            if (records.isEmpty()) {
                runOnUiThread(() -> {
                    btnAnalyze.setEnabled(true);
                    tvStatus.setText("No Activity Found");
                    tvAdvice.setText("It looks like you haven't logged any activities today. Please visit the Daily Record page first.");
                });
                return;
            }

            DailyHealthRecord latestRecord = records.get(0);

            apiRepository.getHealthAssessment(latestRecord, new HealthRepository.ApiCallback() {
                @Override
                public void onSuccess(HealthAssessmentResponse assessment) {
                    runOnUiThread(() -> {
                        btnAnalyze.setEnabled(true);
                        updateUI(assessment.getHealthScore(), assessment.getClassification(), assessment.getTrendAnalysis(), latestRecord.getDate());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    // Fallback to local logic without showing the user "API Failed"
                    int localScore = latestRecord.calculateLocalScore();
                    String localStatus = latestRecord.getLocalClassification();
                    String defaultAdvice = "Keep tracking your daily habits to unlock deeper, personalized health trends over time.";

                    runOnUiThread(() -> {
                        btnAnalyze.setEnabled(true);
                        updateUI(localScore, localStatus, defaultAdvice, latestRecord.getDate());
                    });
                }
            });
        });
    }

    // Helper method to set the colors and text cleanly
    private void updateUI(int score, String status, String advice, String date) {
        tvReportTitle.setText("REPORT FOR " + date);
        tvScore.setText(String.valueOf(score));
        tvStatus.setText(status);
        tvAdvice.setText(advice);

        // Apply strict color coding based on your rubric
        if (status.equalsIgnoreCase("Healthy")) {
            tvStatus.setTextColor(Color.parseColor("#38A169")); // Soft Green
        } else if (status.equalsIgnoreCase("Moderate")) {
            tvStatus.setTextColor(Color.parseColor("#D69E2E")); // Soft Orange/Yellow
        } else {
            tvStatus.setTextColor(Color.parseColor("#E53E3E")); // Soft Red
        }
    }
}