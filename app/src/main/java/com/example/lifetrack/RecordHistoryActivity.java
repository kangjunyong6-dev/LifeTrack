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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class RecordHistoryActivity extends AppCompatActivity {

    Button tabCalendar, tabStats, btnBackHistoryCal, btnBackHistoryStats;
    ScrollView containerCalendar, containerStats;
    CalendarView calendarView;
    TextView tvSelectedDateStats, tvTotalTime, tvTotalDistance, tvCaloriesBurnt;
    LinearLayout layoutHistoryContainer;
    List<DailyHealthRecord> allRecords;

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
        layoutHistoryContainer = findViewById(R.id.layoutHistoryContainer);

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

        // Inside onCreate of RecordHistoryActivity.java
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfile = findViewById(R.id.navProfile);

// Link Home Icon
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

// Link Profile Icon
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Calendar Date Click Logic
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Room DB dates are saved as "yyyy-MM-dd". Month is 0-indexed in CalendarView.
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
                tvSelectedDateStats.setText("Exercise: " + r.getExerciseMinutes() + " mins\nSleep: " + r.getSleepHours() + " hours\nFood: " + r.getFoodNote());
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
            totalCals += r.getCalorieIntake();
        }

        // Mocking distance based on exercise time (Assuming 10 mins = 1 km)
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

        // Only show up to 5 recent items to save space
        int count = Math.min(allRecords.size(), 5);
        for (int i = 0; i < count; i++) {
            DailyHealthRecord r = allRecords.get(i);
            TextView tv = new TextView(this);
            tv.setText(r.getDate() + " - " + r.getExerciseMinutes() + " mins exercise");
            tv.setPadding(0, 16, 0, 16);
            tv.setTextColor(Color.parseColor("#4A5568"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            layoutHistoryContainer.addView(tv);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
            divider.setBackgroundColor(Color.parseColor("#EDF2F7"));
            layoutHistoryContainer.addView(divider);
        }

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });


        // Initialize the Tab Buttons and Containers
        Button tabCalendar = findViewById(R.id.tabCalendar);
        Button tabStats = findViewById(R.id.tabStats);
        ScrollView containerCalendar = findViewById(R.id.containerCalendar);
        ScrollView containerStats = findViewById(R.id.containerStats);

        //  Set the Stats Tab Click Listener
        tabStats.setOnClickListener(v -> {
            // Show the Stats screen and hide the Calendar screen
            containerCalendar.setVisibility(View.GONE);
            containerStats.setVisibility(View.VISIBLE);

            // Update the button colors to show "Stats" is now active
            tabStats.setTextColor(Color.parseColor("#4299E1"));
            tabStats.setTypeface(null, android.graphics.Typeface.BOLD);

            // Dim the Calendar button
            tabCalendar.setTextColor(Color.parseColor("#A0AEC0"));
            tabCalendar.setTypeface(null, android.graphics.Typeface.NORMAL);
        });

//  Set the Calendar Tab Click Listener (to switch back)
        tabCalendar.setOnClickListener(v -> {
            containerCalendar.setVisibility(View.VISIBLE);
            containerStats.setVisibility(View.GONE);

            tabCalendar.setTextColor(Color.parseColor("#4299E1"));
            tabCalendar.setTypeface(null, android.graphics.Typeface.BOLD);

            tabStats.setTextColor(Color.parseColor("#A0AEC0"));
            tabStats.setTypeface(null, android.graphics.Typeface.NORMAL);
        });

    }
}