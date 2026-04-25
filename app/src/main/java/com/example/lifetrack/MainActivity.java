package com.example.lifetrack;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;
import com.example.lifetrack.api.HealthRepository;
import com.example.lifetrack.api.HealthAssessmentResponse;
import com.example.lifetrack.data.entity.DailyHealthRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.*;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CardView cardDailyLog, cardAIAnalysis;
    private TextView tvWelcome, tvActiveProfile, tvPrediction;
    private LinearLayout navCalendar, navProfile;
    private AppDatabase db;
    LineChart homeChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.parseColor("#F4F7FC"));
        }

        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = AppDatabase.getInstance(this);
        homeChart = findViewById(R.id.homeChart);
        tvPrediction = findViewById(R.id.tvPrediction);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        cardDailyLog = findViewById(R.id.cardDailyLog);
        cardAIAnalysis = findViewById(R.id.cardAIAnalysis);

        // These MUST match the IDs in activity_main.xml
        tvWelcome = findViewById(R.id.tvWelcome);
        tvActiveProfile = findViewById(R.id.tvActiveProfile);

        navCalendar = findViewById(R.id.navCalendar);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupClickListeners() {
        cardDailyLog.setOnClickListener(v -> startActivity(new Intent(this, DailyRecordActivity.class)));
        cardAIAnalysis.setOnClickListener(v -> startActivity(new Intent(this, HealthScoreSystem.class)));
        navCalendar.setOnClickListener(v -> startActivity(new Intent(this, RecordHistoryActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh greeting every time the user returns to this screen
        loadUserGreeting();
        loadHealthScore();
        loadHomeChart();
        loadPrediction();
    }

    private void loadUserGreeting() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("loggedInEmail", null);

            if (userEmail != null) {
                UserProfile profile = db.userProfileDao().getProfileByEmail(userEmail);

                runOnUiThread(() -> {
                    if (profile != null) {
                        tvWelcome.setText("Hi, " + profile.getName());
                    }
                });
            }
        });
    }

    private void loadHealthScore() {

        Executors.newSingleThreadExecutor().execute(() -> {

            DailyHealthRecord latest =
                    db.dailyHealthRecordDao().getLatestRecord();
            MaterialCardView cardScore = findViewById(R.id.cardHealthScore);
            TextView tvScore = findViewById(R.id.tvScore);
            TextView tvStatus = findViewById(R.id.tvStatus);
            TextView tvAdvice = findViewById(R.id.tvAdvice);

            runOnUiThread(() -> {
                MaterialCardView cardInsights = findViewById(R.id.cardInsights);

                if (cardInsights != null) {
                    cardInsights.setAlpha(0f);
                    cardInsights.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .start();
                }

                if (latest == null) {
                    tvScore.setText("--");
                    tvStatus.setText("No Data");
                    tvAdvice.setText("Please log your daily record first.");
                    return;
                }

                int score = calculateScore(latest);
                String status = getStatus(score);
                String advice = generateAdvice(latest, score);

                tvScore.setText(String.valueOf(score));
                tvScore.setTextSize(40);
                tvScore.setTypeface(null, Typeface.BOLD);
                tvScore.setScaleX(0.8f);
                tvScore.setScaleY(0.8f);

                tvScore.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start(); tvScore.setScaleX(0.8f);
                tvScore.setScaleY(0.8f);
                tvScore.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start();

                tvStatus.setText("● " + status);
                tvStatus.setTextSize(16);
                tvStatus.setTextColor(Color.BLACK);
                tvAdvice.setText("Advice: " + advice);
                tvAdvice.setTextSize(14);
                tvAdvice.setAlpha(0.9f);

                if (score >= 80) {
                    cardScore.setBackgroundResource(R.drawable.bg_scorecard);
                    cardScore.setCardBackgroundColor(getColor(R.color.secondary));
                }
                else if (score >= 50) {
                    cardScore.setBackgroundResource(R.drawable.bg_scorecard);
                    cardScore.setCardBackgroundColor(getColor(R.color.warning));
                }
                else {
                    cardScore.setBackgroundResource(R.drawable.bg_scorecard);
                    cardScore.setCardBackgroundColor(getColor(R.color.danger));
                }
            });
        });
    }

    private int calculateScore(DailyHealthRecord r) {
        int score = 50;

        if (r.getSleepHours() >= 7) score += 20;
        else if (r.getSleepHours() >= 5) score += 10;

        if (r.getExerciseMinutes() >= 30) score += 20;
        else if (r.getExerciseMinutes() >= 10) score += 10;

        if (r.getFoodNote().equalsIgnoreCase("Healthy")) score += 10;
        else if (r.getFoodNote().equalsIgnoreCase("Unhealthy")) score -= 10;

        if (score > 100) score = 100;
        if (score < 0) score = 0;

        return score;
    }

    private String getStatus(int score) {
        if (score >= 80) return "Healthy";
        else if (score >= 50) return "Moderate";
        else return "Unhealthy";
    }

    private String generateAdvice(DailyHealthRecord r, int score) {

        if (score >= 80) {
            return "Excellent lifestyle. Keep maintaining your routine.";
        }

        StringBuilder advice = new StringBuilder();

        if (r.getSleepHours() < 6) {
            advice.append("Improve sleep duration. ");
        }

        if (r.getExerciseMinutes() < 20) {
            advice.append("Increase physical activity. ");
        }

        if (r.getFoodNote().equalsIgnoreCase("Unhealthy")) {
            advice.append("Consider healthier food choices. ");
        }

        if (advice.length() == 0) {
            advice.append("You're doing okay. Keep improving gradually.");
        }

        return advice.toString();
    }

    private void loadHomeChart() {

        Executors.newSingleThreadExecutor().execute(() -> {

            List<DailyHealthRecord> records =
                    db.dailyHealthRecordDao().getLast7Records();

            if (records == null || records.isEmpty()) return;

            ArrayList<Entry> entries = new ArrayList<>();

            for (int i = 0; i < records.size(); i++) {
                int score = calculateScore(records.get(i));
                entries.add(new Entry(i, score));
            }

            runOnUiThread(() -> {

                LineDataSet dataSet = new LineDataSet(entries, "Health Trend");
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setDrawFilled(true);
                dataSet.setFillAlpha(80);

                dataSet.setColor(getColor(R.color.primary));
                dataSet.setCircleColor(getColor(R.color.primary));
                LineData lineData = new LineData(dataSet);

                homeChart.getAxisRight().setEnabled(false);
                homeChart.getDescription().setEnabled(false);
                homeChart.getLegend().setEnabled(false);
                homeChart.getXAxis().setDrawGridLines(false);
                homeChart.getAxisLeft().setDrawGridLines(false);
                homeChart.setData(lineData);
                homeChart.getDescription().setText("Last 7 Records");
                homeChart.invalidate();
            });
        });
    }

    private void loadPrediction() {

        Executors.newSingleThreadExecutor().execute(() -> {

            List<DailyHealthRecord> records =
                    db.dailyHealthRecordDao().getLast7Records();

            if (records == null || records.isEmpty()) return;

            runOnUiThread(() -> {
                String prediction = generatePrediction(records);
                tvPrediction.setText("🤖 AI Insight\n" + prediction);
                tvPrediction.setTextColor(getColor(R.color.textPrimary));
                tvPrediction.setAlpha(0f);

                tvPrediction.animate()
                        .alpha(1f)
                        .setDuration(600)
                        .start();
            });
        });
    }

    private String generatePrediction(List<DailyHealthRecord> records) {

        if (records == null || records.size() < 3) {
            return "Not enough data to generate reliable insights.";
        }

        int totalScore = 0;
        int poorDays = 0;

        for (DailyHealthRecord r : records) {
            int score = calculateScore(r);
            totalScore += score;

            if (score < 50) poorDays++;
        }

        int avgScore = totalScore / records.size();

        DailyHealthRecord latest = records.get(0);
        DailyHealthRecord previous = records.get(1);

        int latestScore = calculateScore(latest);
        int prevScore = calculateScore(previous);

        StringBuilder prediction = new StringBuilder();

        // 🔹 1. Overall condition
        if (avgScore >= 80) {
            prediction.append("Your lifestyle is consistently strong. ");
        } else if (avgScore >= 50) {
            prediction.append("Your health is moderate with room for improvement. ");
        } else {
            prediction.append("Your current habits may negatively impact your health. ");
        }

        // 🔹 2. Trend analysis
        if (latestScore > prevScore) {
            prediction.append("Recent trend shows improvement. ");
        } else if (latestScore < prevScore) {
            prediction.append("Recent trend shows decline. ");
        } else {
            prediction.append("Your health trend is stable. ");
        }

        // 🔹 3. Risk detection
        if (latest.getSleepHours() < 5) {
            prediction.append("⚠ Sleep deprivation detected. ");
        }

        if (latest.getExerciseMinutes() < 10) {
            prediction.append("⚠ Low physical activity detected. ");
        }

        if (poorDays >= 3) {
            prediction.append("Multiple low-score days detected, indicating inconsistency. ");
        }

        // 🔹 4. Actionable advice
        if (avgScore < 80) {
            prediction.append("Improving sleep and exercise consistency will significantly boost your health score.");
        } else {
            prediction.append("Maintain your current habits to sustain your health level.");
        }

        return prediction.toString();
    }

    private String getConfidence(List<DailyHealthRecord> records) {

        int consistency = records.size();

        if (consistency >= 7) return "High confidence";
        else if (consistency >= 4) return "Medium confidence";
        else return "Low confidence";
    }
}