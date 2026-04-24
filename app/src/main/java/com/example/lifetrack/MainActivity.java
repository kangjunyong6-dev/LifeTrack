package com.example.lifetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private CardView cardDailyLog, cardAIAnalysis;
    private TextView tvWelcome, tvActiveProfile;
    private LinearLayout navCalendar, navProfile;
    private AppDatabase db;

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
}