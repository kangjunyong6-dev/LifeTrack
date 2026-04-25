package com.example.lifetrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

// MPAndroidChart Imports
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter; // NEW

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class RecordHistoryActivity extends AppCompatActivity {

    Button tabCalendar, tabStats;
    ScrollView containerCalendar, containerStats;
    CalendarView calendarView;
    TextView tvSelectedDateStats, tvTotalTime, tvTotalDistance, tvCaloriesBurnt, tvAvgSleep, tvAvgExercise, tvAvgScore, tvTrend;
    LinearLayout layoutHistoryContainer;
    List<DailyHealthRecord> allRecords;

    // Chart Variables
    LineChart healthScoreChart;
    BarChart exerciseBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_history);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Init Views
        tabCalendar = findViewById(R.id.tabCalendar);
        tabStats = findViewById(R.id.tabStats);
        containerCalendar = findViewById(R.id.containerCalendar);
        containerStats = findViewById(R.id.containerStats);
        calendarView = findViewById(R.id.calendarView);
        tvSelectedDateStats = findViewById(R.id.tvSelectedDateStats);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalDistance = findViewById(R.id.tvTotalDistance);
        tvCaloriesBurnt = findViewById(R.id.tvCaloriesBurnt);
        tvTrend = findViewById(R.id.tvTrend);
        layoutHistoryContainer = findViewById(R.id.layoutHistoryContainer);

        // Init Charts
        healthScoreChart = findViewById(R.id.healthScoreChart);
        exerciseBarChart = findViewById(R.id.exerciseBarChart);

        // Tab Switching Logic
        tabCalendar.setOnClickListener(v -> switchTab(true));
        tabStats.setOnClickListener(v -> switchTab(false));

        // Load Data and Process Stats
        AppDatabase db = AppDatabase.getInstance(this);
        Executors.newSingleThreadExecutor().execute(() -> {
            allRecords = db.dailyHealthRecordDao().getAllRecords();
            runOnUiThread(() -> {
                populateRecentHistory();
                calculateOverallStats();
            });
        });

        // Bottom Navigation Logic
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Calendar Date Click Logic
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            filterStatsForDate(selectedDate);
        });
    }

    private void switchTab(boolean isCalendar) {
        if (isCalendar) {
            containerCalendar.setVisibility(View.VISIBLE);
            containerStats.setVisibility(View.GONE);
            tabCalendar.setTextColor(Color.parseColor("#4299E1"));
            tabCalendar.setTypeface(null, android.graphics.Typeface.BOLD);
            tabStats.setTextColor(Color.parseColor("#A0AEC0"));
            tabStats.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            containerCalendar.setVisibility(View.GONE);
            containerStats.setVisibility(View.VISIBLE);
            tabStats.setTextColor(Color.parseColor("#4299E1"));
            tabStats.setTypeface(null, android.graphics.Typeface.BOLD);
            tabCalendar.setTextColor(Color.parseColor("#A0AEC0"));
            tabCalendar.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void filterStatsForDate(String targetDate) {
        if (allRecords == null) return;
        for (DailyHealthRecord r : allRecords) {
            if (r.getDate().equals(targetDate)) {
                tvSelectedDateStats.setText("Exercise: " + r.getExerciseMinutes() + " mins (" + r.getExerciseIntensity() + ")\n" +
                        "Calories: " + r.getCalories() + " kcal\n" +
                        "Sleep: " + r.getSleepHours() + " hours\n" +
                        "Food: " + r.getFoodNote());
                return;
            }
        }
        tvSelectedDateStats.setText("No records found on " + targetDate);
    }

    private void calculateOverallStats() {
        if (allRecords == null || allRecords.isEmpty()) return;
        int totalMins = 0;
        int totalCals = 0;

        for (DailyHealthRecord r : allRecords) {
            totalMins += r.getExerciseMinutes();
            totalCals += r.getCalories();
        }

        double estimatedDistanceKm = totalMins / 10.0;
        int hours = totalMins / 60;
        int mins = totalMins % 60;

        tvTotalTime.setText(hours + "h " + mins + "m");
        tvTotalDistance.setText(String.format(Locale.getDefault(), "%.1f km", estimatedDistanceKm));
        tvCaloriesBurnt.setText(String.valueOf(totalCals));
    }

    private void populateRecentHistory() {
        layoutHistoryContainer.removeAllViews();
        if (allRecords == null || allRecords.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No recent records.");
            layoutHistoryContainer.addView(emptyText);
            return;
        }

        int count = Math.min(allRecords.size(), 5);
        for (int i = 0; i < count; i++) {
            DailyHealthRecord r = allRecords.get(i);
            TextView tv = new TextView(this);
            tv.setText(r.getDate() + " • " + r.getExerciseMinutes() + " mins (" + r.getExerciseIntensity() + ") • " + r.getCalories() + " kcal");
            tv.setPadding(0, 16, 0, 16);
            tv.setTextColor(Color.parseColor("#4A5568"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            layoutHistoryContainer.addView(tv);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(Color.parseColor("#EDF2F7"));
            layoutHistoryContainer.addView(divider);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeeklyStats();
    }

    private void loadWeeklyStats() {
        AppDatabase db = AppDatabase.getInstance(this);

        TextView tvAvgSleep = findViewById(R.id.tvAvgSleep);
        TextView tvAvgExercise = findViewById(R.id.tvAvgExercise);
        TextView tvAvgScore = findViewById(R.id.tvAvgScore);
        TextView tvTrend = findViewById(R.id.tvTrend);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<DailyHealthRecord> records = db.dailyHealthRecordDao().getLast7Records();

            if (records == null || records.isEmpty()) {
                runOnUiThread(() -> {
                    tvAvgSleep.setText("--");
                    tvAvgExercise.setText("--");
                    tvAvgScore.setText("--");
                    tvTrend.setText("No data");
                });
                return;
            }

            List<DailyHealthRecord> chronologicalRecords = new ArrayList<>(records);
            Collections.reverse(chronologicalRecords);

            float totalSleep = 0;
            int totalExercise = 0;
            int totalScore = 0;

            for (DailyHealthRecord r : records) {
                totalSleep += r.getSleepHours();
                totalExercise += r.getExerciseMinutes();
                totalScore += calculateScore(r);
            }

            int count = records.size();
            float avgSleep = totalSleep / count;
            int avgExercise = totalExercise / count;
            int avgScore = totalScore / count;

            runOnUiThread(() -> {
                tvAvgSleep.setText(String.format(Locale.getDefault(), "%.1f hrs", avgSleep));
                tvAvgExercise.setText(avgExercise + " mins");
                tvAvgScore.setText(String.valueOf(avgScore));
                tvTrend.setText(getTrend(records));

                setupCharts(chronologicalRecords);
            });
        });
    }

    private void setupCharts(List<DailyHealthRecord> records) {
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        String[] dates = new String[records.size()]; // Array to hold actual dates

        for (int i = 0; i < records.size(); i++) {
            DailyHealthRecord r = records.get(i);

            // Format the date (e.g., from "2024-04-19" to "04-19")
            String fullDate = r.getDate();
            dates[i] = fullDate.length() > 5 ? fullDate.substring(5) : fullDate;

            lineEntries.add(new Entry(i, calculateScore(r)));
            barEntries.add(new BarEntry(i, r.getExerciseMinutes()));
        }

        // --- Shared X-Axis Formatter ---
        IndexAxisValueFormatter dateFormatter = new IndexAxisValueFormatter(dates);

        // --- Line Chart Setup ---
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Health Score");
        lineDataSet.setColor(Color.parseColor("#4299E1"));
        lineDataSet.setCircleColor(Color.parseColor("#4299E1"));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        healthScoreChart.setData(new LineData(lineDataSet));
        healthScoreChart.getDescription().setEnabled(false);
        healthScoreChart.getAxisRight().setEnabled(false);

        XAxis lxAxis = healthScoreChart.getXAxis();
        lxAxis.setValueFormatter(dateFormatter); // APPLY DATES
        lxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lxAxis.setGranularity(1f);
        lxAxis.setDrawGridLines(false);

        healthScoreChart.animateX(1000);
        healthScoreChart.invalidate();

        // --- Bar Chart Setup ---
        BarDataSet barDataSet = new BarDataSet(barEntries, "Exercise Minutes");
        barDataSet.setColor(Color.parseColor("#48BB78"));

        exerciseBarChart.setData(new BarData(barDataSet));
        exerciseBarChart.getDescription().setEnabled(false);
        exerciseBarChart.getAxisRight().setEnabled(false);

        XAxis bxAxis = exerciseBarChart.getXAxis();
        bxAxis.setValueFormatter(dateFormatter); // APPLY DATES
        bxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bxAxis.setGranularity(1f);
        bxAxis.setDrawGridLines(false);

        exerciseBarChart.animateY(1000);
        exerciseBarChart.invalidate();
    }

    private String getTrend(List<DailyHealthRecord> records) {
        if (records.size() < 2) return "No trend";
        int latest = calculateScore(records.get(0));
        int previous = calculateScore(records.get(1));
        if (latest > previous) return "Health is improving \uD83D\uDCC8 Keep it up!";
        else if (latest < previous) return "Health declining \uD83D\uDCC9 Try improving sleep.";
        else return "Health is stable \u2796 Maintain consistency.";
    }

    private int calculateScore(DailyHealthRecord r) {
        int score = 50;
        if (r.getSleepHours() >= 7) score += 20;
        else if (r.getSleepHours() >= 5) score += 10;
        if (r.getExerciseMinutes() >= 30) score += 20;
        else if (r.getExerciseMinutes() >= 10) score += 10;
        if ("Healthy".equalsIgnoreCase(r.getFoodNote())) score += 10;
        else if ("Unhealthy".equalsIgnoreCase(r.getFoodNote())) score -= 10;
        return Math.max(0, Math.min(score, 100));
    }
}