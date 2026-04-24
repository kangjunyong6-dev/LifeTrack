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

public class ProfileActivity extends AppCompatActivity {

    // UI Elements - Modes
    private CardView cardViewMode, cardEditMode;
    private Button btnEnableEdit, btnSaveProfile;

    // UI Elements - Details
    private TextView tvViewDetails, tvAboutDetails;
    private LinearLayout layoutQA;
    private TextView btnHelpSupport, btnAboutUs;

    // UI Elements - Inputs
    private EditText etName, etAge, etGender, etHeight, etWeight;

    // Navigation
    private LinearLayout navHome, navCalendar;

    // Local Storage
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initializeViews();
        setupNavigation();
        setupToggles();
        loadProfileData();

        // Save Button Logic
        btnSaveProfile.setOnClickListener(v -> saveProfileData());


// Open Support Page
        findViewById(R.id.btnHelpSupport).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SupportActivity.class));
        });

// Open About Page
        findViewById(R.id.btnAboutUs).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AboutActivity.class));
        });
    }

    private void initializeViews() {
        // Mode Cards
        cardViewMode = findViewById(R.id.cardViewMode);
        cardEditMode = findViewById(R.id.cardEditMode);

        // Buttons/Links
        btnEnableEdit = findViewById(R.id.btnEnableEdit);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnHelpSupport = findViewById(R.id.btnHelpSupport);
        btnAboutUs = findViewById(R.id.btnAboutUs);

        // Display/Expansion Areas
        tvViewDetails = findViewById(R.id.tvViewDetails);
        tvAboutDetails = findViewById(R.id.tvAboutDetails);
        layoutQA = findViewById(R.id.layoutQA);

        // Input Fields
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        // Navbar
        navHome = findViewById(R.id.navHome);
        navCalendar = findViewById(R.id.navCalendar);

        sharedPreferences = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
    }

    private void setupNavigation() {
        // Link to Dashboard
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Link to Calendar/Stats
        navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RecordHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupToggles() {
        // Toggle from View Mode to Edit Mode
        btnEnableEdit.setOnClickListener(v -> {
            cardViewMode.setVisibility(View.GONE);
            cardEditMode.setVisibility(View.VISIBLE);
        });

        // Expand/Collapse Help & Support
        btnHelpSupport.setOnClickListener(v -> {
            int visibility = (layoutQA.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
            layoutQA.setVisibility(visibility);
        });

        // Expand/Collapse About Us
        btnAboutUs.setOnClickListener(v -> {
            int visibility = (tvAboutDetails.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
            tvAboutDetails.setVisibility(visibility);
        });
    }

    private void loadProfileData() {
        String name = sharedPreferences.getString("name", "User");
        int age = sharedPreferences.getInt("age", 0);
        String gender = sharedPreferences.getString("gender", "Not Set");
        float height = sharedPreferences.getFloat("height", 0f);
        float weight = sharedPreferences.getFloat("weight", 0f);

        // Set View Mode Text
        String summary = "Name: " + name + "\n" +
                "Age: " + (age > 0 ? age : "--") + "\n" +
                "Gender: " + gender + "\n" +
                "Height: " + (height > 0 ? height + " cm" : "--") + "\n" +
                "Weight: " + (weight > 0 ? weight + " kg" : "--");
        tvViewDetails.setText(summary);

        // Pre-fill Edit Mode Fields
        etName.setText(name);
        if (age > 0) etAge.setText(String.valueOf(age));
        etGender.setText(gender);
        if (height > 0) etHeight.setText(String.valueOf(height));
        if (weight > 0) etWeight.setText(String.valueOf(weight));
    }

    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Please enter at least Name and Age", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putInt("age", Integer.parseInt(ageStr));
        editor.putString("gender", gender);
        editor.putFloat("height", Float.parseFloat(heightStr.isEmpty() ? "0" : heightStr));
        editor.putFloat("weight", Float.parseFloat(weightStr.isEmpty() ? "0" : weightStr));
        editor.apply();

        // Refresh and switch back to View Mode
        loadProfileData();
        cardEditMode.setVisibility(View.GONE);
        cardViewMode.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
    }
}