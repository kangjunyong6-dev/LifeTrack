package com.example.lifetrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
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
import com.example.lifetrack.data.entity.UserProfile;

import java.util.concurrent.Executors;

public class HealthScoreSystem extends AppCompatActivity {

    private Button btnAnalyze, btnBackToDashboard;
    private TextView tvScore, tvStatus, tvAdvice, tvReportTitle, tvAiGuidance;
    private TextView tvBmiValue, tvBmiStatus, tvBmiAdvice;
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
        tvReportTitle = findViewById(R.id.tvReportTitle);
        tvAiGuidance = findViewById(R.id.tvAiGuidance);

        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        tvBmiAdvice = findViewById(R.id.tvBmiAdvice);

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
        tvStatus.setText("Checking Database...");
        tvAdvice.setText("Processing your latest metrics...");
        btnAnalyze.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1. Get the latest health record for the score
                DailyHealthRecord latestRecord = db.dailyHealthRecordDao().getLatestRecord();

                // 2. Get the CURRENT user profile for the BMI (so it updates immediately)
                SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", Context.MODE_PRIVATE);
                String userEmail = prefs.getString("loggedInEmail", null);
                UserProfile currentProfile = (userEmail != null) ? db.userProfileDao().getProfileByEmail(userEmail) : null;

                if (latestRecord == null) {
                    runOnUiThread(() -> {
                        btnAnalyze.setEnabled(true);
                        tvStatus.setText("No Data Found");
                        tvAdvice.setText("Please record your daily health data first.");
                    });
                    return;
                }

                // Call API for analysis
                apiRepository.getHealthAssessment(latestRecord, new HealthRepository.ApiCallback() {
                    @Override
                    public void onSuccess(HealthAssessmentResponse result, String source) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                tvReportTitle.setText(source.toUpperCase());
                                updateResultUI(result.getHealthScore(), result.getClassification(), result.getTrendAnalysis());

                                // Priority: Use current profile data for BMI, fallback to record data
                                if (currentProfile != null && currentProfile.getWeight() > 0) {
                                    calculateAndDisplayBmi(currentProfile.getWeight(), currentProfile.getHeight());
                                } else {
                                    calculateAndDisplayBmi(latestRecord.getWeight(), latestRecord.getHeight());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            btnAnalyze.setEnabled(true);
                            tvStatus.setText("Sync Error");
                            tvAdvice.setText("Using local metrics for now.");
                            // Still show BMI even if API fails
                            if (currentProfile != null) calculateAndDisplayBmi(currentProfile.getWeight(), currentProfile.getHeight());
                        });
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

    private void updateResultUI(int score, String status, String advice) {
        btnAnalyze.setEnabled(true);
        tvScore.setText(String.valueOf(score));
        tvAdvice.setText("Your daily summary is ready.");

        String formattedAiAdvice = "✨ AI Insight:\n" + advice +
                "\n\nConsistency is key, Keep pushing!";
        tvAiGuidance.setText(formattedAiAdvice);

        if (score >= 80) {
            tvStatus.setTextColor(Color.parseColor("#38A169"));
            tvStatus.setText("● Healthy Status");
        } else if (score >= 50) {
            tvStatus.setTextColor(Color.parseColor("#D69E2E"));
            tvStatus.setText("● Moderate Status");
        } else {
            tvStatus.setTextColor(Color.parseColor("#E53E3E"));
            tvStatus.setText("● Unhealthy Status");
        }
    }

    private void calculateAndDisplayBmi(float weight, float height) {
        if (height > 0 && weight > 0) {
            float heightMeters = height / 100f;
            float bmi = weight / (heightMeters * heightMeters);

            tvBmiValue.setText(String.format("%.1f", bmi));

            if (bmi < 18.5) {
                tvBmiStatus.setText("Underweight");
                tvBmiStatus.setTextColor(Color.parseColor("#D69E2E"));
                tvBmiAdvice.setText("Fuel your body with nutrient-dense meals. Every small bite counts towards your strength!");
            } else if (bmi < 25) {
                tvBmiStatus.setText("Balanced");
                tvBmiStatus.setTextColor(Color.parseColor("#38A169"));
                tvBmiAdvice.setText("Your body is in a beautiful rhythm! Keep up the consistent habits that keep you feeling this way.");
            } else if (bmi < 30) {
                tvBmiStatus.setText("Overweight");
                tvBmiStatus.setTextColor(Color.parseColor("#D69E2E"));
                tvBmiAdvice.setText("Be kind to yourself. A few extra minutes of walking can boost your energy and your mood!");
            } else {
                tvBmiStatus.setText("Obese");
                tvBmiStatus.setTextColor(Color.parseColor("#E53E3E"));
                tvBmiAdvice.setText("Focus on small, sustainable changes. Your health journey is a marathon, not a sprint.");
            }
        } else {
            tvBmiStatus.setText("Incomplete Profile");
            tvBmiAdvice.setText("Update your weight and height in your profile to see your BMI analysis.");
        }
    }

    private void navigateTo(Class<?> target) {
        startActivity(new Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}