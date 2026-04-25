package com.example.lifetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.lifetrack.api.HealthAssessmentResponse;
import com.example.lifetrack.api.HealthRepository;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;
import com.example.lifetrack.data.entity.UserProfile;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CardView cardDailyLog, cardAIAnalysis;
    private TextView tvWelcome, tvActiveProfile, tvPrediction;
    private LinearLayout navCalendar, navProfile;
    private AppDatabase db;
    private LineChart homeChart;

    // NEW: Add the HealthRepository to fetch Cloud data
    private HealthRepository healthRepository;

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

        // Initialize the repository
        healthRepository = new HealthRepository();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        cardDailyLog = findViewById(R.id.cardDailyLog);
        cardAIAnalysis = findViewById(R.id.cardAIAnalysis);
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
        loadUserGreeting();
        loadHealthScore(); // This now fetches from the Cloud!
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
            // 1. Fetch the latest local record to display raw stats (sleep hours, calories)
            DailyHealthRecord latest = db.dailyHealthRecordDao().getLatestRecord();

            runOnUiThread(() -> {
                MaterialCardView cardScore = findViewById(R.id.cardHealthScore);
                TextView tvScore = findViewById(R.id.tvScore);
                TextView tvStatus = findViewById(R.id.tvStatus);
                TextView tvAdvice = findViewById(R.id.tvAdvice);
                TextView tvStatSleep = findViewById(R.id.tvStatSleep);
                TextView tvStatCalories = findViewById(R.id.tvStatCalories);
                MaterialCardView cardInsights = findViewById(R.id.cardInsights);

                if (cardInsights != null) {
                    cardInsights.setAlpha(0f);
                    cardInsights.animate().alpha(1f).setDuration(500).start();
                }

                // If no local data exists, show empty state
                if (latest == null) {
                    tvScore.setText("--");
                    tvStatus.setText("No Data");
                    tvAdvice.setText("Please log your daily record first.");
                    tvStatSleep.setText("-- hrs");
                    tvStatCalories.setText("-- kcal");
                    return;
                }

                // Update the raw local stats (since the Cloud Assessment only returns the score/status)
                tvStatSleep.setText(latest.getSleepHours() + " h");
                tvStatCalories.setText(latest.getCalories() + " kcal");

                // 2. Fetch the OFFICIAL Cloud Score from Supabase
                healthRepository.fetchLatestAssessmentOnly(new HealthRepository.ApiCallback() {
                    @Override
                    public void onSuccess(HealthAssessmentResponse assessment, String source) {
                        runOnUiThread(() -> {
                            int cloudScore = assessment.getHealthScore();
                            tvScore.setText(String.valueOf(cloudScore));
                            tvStatus.setText("● " + assessment.getClassification());
                            tvAdvice.setText("Cloud AI: " + assessment.getTrendAnalysis());

                            // Apply styling based on cloud score
                            updateScoreCardStyling(cardScore, tvScore, tvStatus, tvAdvice, cloudScore);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Cloud Sync Failed. Showing Local Score.", Toast.LENGTH_SHORT).show();

                            // 3. Fallback to Local Brain if Cloud is unreachable (Offline mode)
                            int localScore = calculateScore(latest);
                            tvScore.setText(String.valueOf(localScore));
                            tvStatus.setText("● " + getStatus(localScore));
                            tvAdvice.setText("Advice (OFFLINE): " + generateAdvice(latest, localScore));

                            // Apply styling based on local score
                            updateScoreCardStyling(cardScore, tvScore, tvStatus, tvAdvice, localScore);
                        });
                    }
                });
            });
        });
    }

    // Helper method to keep your UI updates clean
    private void updateScoreCardStyling(MaterialCardView cardScore, TextView tvScore, TextView tvStatus, TextView tvAdvice, int score) {
        tvStatus.setTextColor(Color.WHITE);
        tvScore.setTextColor(Color.WHITE);
        tvAdvice.setTextColor(Color.parseColor("#E2E8F0"));

        if (score >= 80) {
            cardScore.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.secondary));
        } else if (score >= 50) {
            cardScore.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.warning));
        } else {
            cardScore.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.danger));
        }

        tvScore.setScaleX(0.8f);
        tvScore.setScaleY(0.8f);
        tvScore.animate().scaleX(1f).scaleY(1f).setDuration(300).start();
    }

    // Kept for offline fallback and for generating the Home Chart
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
        if (score >= 80) return "Excellent lifestyle. Keep maintaining your routine.";
        StringBuilder advice = new StringBuilder();
        if (r.getSleepHours() < 6) advice.append("Improve sleep duration. ");
        if (r.getExerciseMinutes() < 20) advice.append("Increase physical activity. ");
        if (r.getFoodNote().equalsIgnoreCase("Unhealthy")) advice.append("Consider healthier food choices. ");
        if (advice.length() == 0) advice.append("You're doing okay. Keep improving gradually.");
        return advice.toString();
    }

    // Chart still uses local DB to avoid making 7 API calls at once
    private void loadHomeChart() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<DailyHealthRecord> records = db.dailyHealthRecordDao().getLast7Records();
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

                int colorPrimary = ContextCompat.getColor(MainActivity.this, R.color.primary);
                dataSet.setColor(colorPrimary);
                dataSet.setCircleColor(colorPrimary);
                LineData lineData = new LineData(dataSet);

                homeChart.getAxisRight().setEnabled(false);
                homeChart.getDescription().setEnabled(true);
                homeChart.getDescription().setText("Last 7 Records");
                homeChart.getLegend().setEnabled(false);
                homeChart.getXAxis().setDrawGridLines(false);
                homeChart.getAxisLeft().setDrawGridLines(false);
                homeChart.setData(lineData);
                homeChart.invalidate();
            });
        });
    }

    private void loadPrediction() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<DailyHealthRecord> records = db.dailyHealthRecordDao().getLast7Records();
            if (records == null || records.isEmpty()) return;

            runOnUiThread(() -> {
                String prediction = generatePrediction(records);
                tvPrediction.setText("AI Insight: " + prediction);
                tvPrediction.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.textPrimary));
                tvPrediction.setAlpha(0f);
                tvPrediction.animate().alpha(1f).setDuration(600).start();
            });
        });
    }

    private String generatePrediction(List<DailyHealthRecord> records) {
        if (records == null || records.size() < 3) return "Not enough data to generate reliable insights.";
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
        if (avgScore >= 80) prediction.append("Your lifestyle is consistently strong. ");
        else if (avgScore >= 50) prediction.append("Your health is moderate. ");
        else prediction.append("Your habits may negatively impact your health. ");

        if (latestScore > prevScore) prediction.append("Trend is improving. ");
        else if (latestScore < prevScore) prediction.append("Trend is declining. ");

        if (latest.getSleepHours() < 5) prediction.append("⚠ Sleep deprivation detected. ");
        if (latest.getExerciseMinutes() < 10) prediction.append("⚠ Low activity detected. ");

        return prediction.toString();
    }
}