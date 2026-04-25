package com.example.lifetrack;

import android.content.Intent;
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

import java.util.concurrent.Executors;

public class HealthScoreSystem extends AppCompatActivity {

    private Button btnAnalyze, btnBackToDashboard;
    private TextView tvScore, tvStatus, tvAdvice, tvReportTitle;
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
        tvReportTitle = findViewById(R.id.tvReportTitle); // NEW: Title element

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
        tvAdvice.setText("Please wait while we process your daily metrics.");
        btnAnalyze.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DailyHealthRecord latest = db.dailyHealthRecordDao().getLatestRecord();

                if (latest == null) {
                    runOnUiThread(() -> {
                        btnAnalyze.setEnabled(true);
                        tvStatus.setText("No Data Found");
                        tvAdvice.setText("Please record your health data first.");
                    });
                    return;
                }

                apiRepository.getHealthAssessment(latest, new HealthRepository.ApiCallback() {
                    @Override
                    public void onSuccess(HealthAssessmentResponse result, String source) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                // Update the title to show Offline/Cloud
                                tvReportTitle.setText(source.toUpperCase());
                                updateResultUI(result.getHealthScore(), result.getClassification(), result.getTrendAnalysis());
                            }
                        });
                    }



                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            btnAnalyze.setEnabled(true);
                            tvStatus.setText("System Error");
                            tvAdvice.setText(error);
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
        tvAdvice.setText(advice);

        // Styling based on score brackets
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

    private void navigateTo(Class<?> target) {
        startActivity(new Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}