package com.example.lifetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize DB
        db = AppDatabase.getInstance(this);

        // 2. CHECK SESSION BEFORE LOADING UI
        checkExistingSession();

        // 3. Check Session to perform 1 time login
        SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 4. Normal UI Setup
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }

    private void checkExistingSession() {
        // Read the "isLoggedIn" flag
        SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is actively logged in! Skip login screen.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile profile = db.userProfileDao().getProfileByEmail(email);

            runOnUiThread(() -> {
                if (profile != null) {

                    SharedPreferences prefs = getSharedPreferences("LifeTrackPrefs", MODE_PRIVATE);

                    prefs.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("loggedInEmail", email)
                            .apply();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}