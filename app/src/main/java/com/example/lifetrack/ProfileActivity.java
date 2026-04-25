package com.example.lifetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;

import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserGender, tvUserAge, tvUserHeight, tvUserWeight;
    private EditText etName, etAge, etGender, etHeight, etWeight;
    private Button btnEnableEdit, btnSaveProfile, btnLogout;
    private CardView cardViewMode, cardEditMode;
    private LinearLayout navHome, navCalendar;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = AppDatabase.getInstance(this);

        initializeViews();
        setupNavigation();
        loadUserProfile();

        btnEnableEdit.setOnClickListener(v -> {
            cardViewMode.setVisibility(View.GONE);
            cardEditMode.setVisibility(View.VISIBLE);
        });

        btnSaveProfile.setOnClickListener(v -> updateProfile());

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isLoggedIn", false).apply();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserGender = findViewById(R.id.tvUserGender);
        tvUserAge = findViewById(R.id.tvUserAge);
        tvUserHeight = findViewById(R.id.tvUserHeight);
        tvUserWeight = findViewById(R.id.tvUserWeight);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        btnEnableEdit = findViewById(R.id.btnEnableEdit);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        cardViewMode = findViewById(R.id.cardViewMode);
        cardEditMode = findViewById(R.id.cardEditMode);

        navHome = findViewById(R.id.navHome);
        navCalendar = findViewById(R.id.navCalendar);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RecordHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
            String userEmail = prefs.getString("loggedInEmail", null);

            if (userEmail != null) {
                UserProfile profile = db.userProfileDao().getProfileByEmail(userEmail);

                runOnUiThread(() -> {
                    if (profile != null) {
                        tvUserName.setText(profile.getName());
                        tvUserGender.setText(profile.getGender());
                        tvUserAge.setText(String.valueOf(profile.getAge()));
                        tvUserHeight.setText(String.valueOf(profile.getHeight()));
                        tvUserWeight.setText(String.valueOf(profile.getWeight()));

                        etName.setText(profile.getName());
                        etAge.setText(String.valueOf(profile.getAge()));
                        etGender.setText(profile.getGender());
                        etHeight.setText(String.valueOf(profile.getHeight()));
                        etWeight.setText(String.valueOf(profile.getWeight()));
                    }
                });
            }
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            Executors.newSingleThreadExecutor().execute(() -> {
                String currentEmail = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE).getString("loggedInEmail", null);

                if (currentEmail != null) {
                    UserProfile profile = db.userProfileDao().getProfileByEmail(currentEmail);

                    if (profile != null) {
                        profile.setName(name);
                        profile.setAge(age);
                        profile.setGender(gender);
                        profile.setHeight(height);
                        profile.setWeight(weight);

                        db.userProfileDao().update(profile);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                            cardEditMode.setVisibility(View.GONE);
                            cardViewMode.setVisibility(View.VISIBLE);
                            loadUserProfile();
                        });
                    }
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}