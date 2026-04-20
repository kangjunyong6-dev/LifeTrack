package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;

import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    EditText etName, etAge, etGender, etHeight, etWeight;
    Button btnSaveProfile, btnBackProfile;
    TextView tvProfileResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBackProfile = findViewById(R.id.btnBackProfile);
        tvProfileResult = findViewById(R.id.tvProfileResult);

        AppDatabase db = AppDatabase.getInstance(this);

        btnBackProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile savedUser = db.userProfileDao().getProfile();

            runOnUiThread(() -> {
                if (savedUser != null) {
                    etName.setText(savedUser.getName());
                    etAge.setText(String.valueOf(savedUser.getAge()));
                    etGender.setText(savedUser.getGender());
                    etHeight.setText(String.valueOf(savedUser.getHeight()));
                    etWeight.setText(String.valueOf(savedUser.getWeight()));

                    tvProfileResult.setText(
                            "Profile Loaded\n" +
                                    "Name: " + savedUser.getName() +
                                    "\nAge: " + savedUser.getAge() +
                                    "\nGender: " + savedUser.getGender() +
                                    "\nHeight: " + savedUser.getHeight() +
                                    "\nWeight: " + savedUser.getWeight()
                    );
                }
            });
        });

        btnSaveProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String gender = etGender.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty()
                    || heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int age = Integer.parseInt(ageStr);
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            Executors.newSingleThreadExecutor().execute(() -> {
                UserProfile existingUser = db.userProfileDao().getProfile();

                if (existingUser == null) {
                    UserProfile newUser = new UserProfile(name, age, gender, height, weight);
                    db.userProfileDao().insert(newUser);
                } else {
                    existingUser.setName(name);
                    existingUser.setAge(age);
                    existingUser.setGender(gender);
                    existingUser.setHeight(height);
                    existingUser.setWeight(weight);
                    db.userProfileDao().update(existingUser);
                }

                UserProfile savedUser = db.userProfileDao().getProfile();

                runOnUiThread(() -> {
                    if (savedUser != null) {
                        tvProfileResult.setText(
                                "Profile Saved Successfully\n" +
                                        "Name: " + savedUser.getName() +
                                        "\nAge: " + savedUser.getAge() +
                                        "\nGender: " + savedUser.getGender() +
                                        "\nHeight: " + savedUser.getHeight() +
                                        "\nWeight: " + savedUser.getWeight()
                        );

                        Toast.makeText(ProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}