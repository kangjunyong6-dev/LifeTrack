package com.example.lifetrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText etName, etAge, etGender, etHeight, etWeight;
    Button btnSave;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnSave = findViewById(R.id.btnSave);
        tvResult = findViewById(R.id.tvResult);

        AppDatabase db = AppDatabase.getInstance(this);

        // Load existing profile when app opens
        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile savedUser = db.userProfileDao().getProfile();

            runOnUiThread(() -> {
                if (savedUser != null) {
                    etName.setText(savedUser.getName());
                    etAge.setText(String.valueOf(savedUser.getAge()));
                    etGender.setText(savedUser.getGender());
                    etHeight.setText(String.valueOf(savedUser.getHeight()));
                    etWeight.setText(String.valueOf(savedUser.getWeight()));

                    tvResult.setText(
                            "Existing Profile Loaded\n" +
                                    "Name: " + savedUser.getName() +
                                    "\nAge: " + savedUser.getAge() +
                                    "\nGender: " + savedUser.getGender() +
                                    "\nHeight: " + savedUser.getHeight() +
                                    "\nWeight: " + savedUser.getWeight()
                    );
                }
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String gender = etGender.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty()
                    || heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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
                        tvResult.setText(
                                "Profile Saved Successfully\n" +
                                        "Name: " + savedUser.getName() +
                                        "\nAge: " + savedUser.getAge() +
                                        "\nGender: " + savedUser.getGender() +
                                        "\nHeight: " + savedUser.getHeight() +
                                        "\nWeight: " + savedUser.getWeight()
                        );

                        Toast.makeText(MainActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }
}