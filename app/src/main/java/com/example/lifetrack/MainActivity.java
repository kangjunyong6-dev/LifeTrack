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
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private CardView cardDailyLog, cardAIAnalysis;
    private TextView tvWelcomeUser;
    private LinearLayout navCalendar, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply Dark Icons to the Status Bar (since background is light)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.parseColor("#F4F7FC"));
        }

        setContentView(R.layout.activity_main);

        // Safety: Hide ActionBar if theme didn't catch it
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Main Action Cards
        cardDailyLog = findViewById(R.id.cardDailyLog); // Your "Daily Check-in" card
        cardAIAnalysis = findViewById(R.id.cardAIAnalysis); // Your "Get Insights" card

        // Text Elements
        tvWelcomeUser = findViewById(R.id.tvActiveProfile);

        // Footer Navigation
        navCalendar = findViewById(R.id.navCalendar);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupClickListeners() {
        // 1. Navigate to Daily Record (Module: Data Entry)
        cardDailyLog.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DailyRecordActivity.class);
            startActivity(intent);
        });

        // 2. Navigate to Health Score / AI Report (Module: AI Logic)
        cardAIAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HealthScoreSystem.class);
            startActivity(intent);
        });

        // 3. Footer: Navigate to History/Calendar
        navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordHistoryActivity.class);
            startActivity(intent);
        });

        // 4. Footer: Navigate to Profile (Module: Settings/About)
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update welcome message if user changed their name in Profile
        updateWelcomeMessage();
    }

    private void updateWelcomeMessage() {
        android.content.SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);

        String name = prefs.getString("name", "User");

        String greeting = "Hi " + name + ", Welcome Back!\nReady for your daily review?";

        tvWelcomeUser.setText(greeting);
    }
}