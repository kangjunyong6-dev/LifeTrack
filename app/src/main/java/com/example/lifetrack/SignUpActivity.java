package com.example.lifetrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    // FIX: Added etSignUpName here
    private TextInputEditText etSignUpName, etSignUpEmail, etSignUpPassword, etSignUpAge, etSignUpGender, etSignUpHeight, etSignUpWeight;
    private Button btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = AppDatabase.getInstance(this);

        // FIX: Linked etSignUpName here
        etSignUpName = findViewById(R.id.etSignUpName);
        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etSignUpAge = findViewById(R.id.etSignUpAge);
        etSignUpGender = findViewById(R.id.etSignUpGender);
        etSignUpHeight = findViewById(R.id.etSignUpHeight);
        etSignUpWeight = findViewById(R.id.etSignUpWeight);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> performRegistration());
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etSignUpName.getText().toString().trim();
        String email = etSignUpEmail.getText().toString().trim();
        String pass = etSignUpPassword.getText().toString().trim();
        String ageStr = etSignUpAge.getText().toString().trim();
        String gender = etSignUpGender.getText().toString().trim();
        String heightStr = etSignUpHeight.getText().toString().trim();
        String weightStr = etSignUpWeight.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            Executors.newSingleThreadExecutor().execute(() -> {
                if (db.userProfileDao().getProfileByEmail(email) != null) {
                    runOnUiThread(() -> etSignUpEmail.setError("Email already exists!"));
                    return;
                }

                UserProfile newUser = new UserProfile(name, email, pass, age, gender, height, weight);
                db.userProfileDao().insert(newUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}