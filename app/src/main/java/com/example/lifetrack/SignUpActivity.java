package com.example.lifetrack;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etSignUpName, etSignUpEmail, etSignUpPassword, etSignUpAge, etSignUpHeight, etSignUpWeight;
    private AutoCompleteTextView etSignUpGender;
    private Button btnRegister;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = AppDatabase.getInstance(this);

        // Initialize views
        etSignUpName = findViewById(R.id.etSignUpName);
        etSignUpEmail = findViewById(R.id.etSignUpEmail);
        etSignUpPassword = findViewById(R.id.etSignUpPassword);
        etSignUpAge = findViewById(R.id.etSignUpAge);
        etSignUpHeight = findViewById(R.id.etSignUpHeight);
        etSignUpWeight = findViewById(R.id.etSignUpWeight);
        btnRegister = findViewById(R.id.btnRegister);

        // Setup Gender Dropdown
        etSignUpGender = findViewById(R.id.etSignUpGender);
        String[] genderOptions = {"M", "F"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genderOptions);
        etSignUpGender.setAdapter(adapter);

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

        // 1. Mandatory Fields Check
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);

            // 2. Age Validation (5-100)
            if (age < 5 || age > 100) {
                etSignUpAge.setError("Age must be between 5 and 100");
                etSignUpAge.requestFocus();
                return;
            }

            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            Executors.newSingleThreadExecutor().execute(() -> {
                // Check if account already exists
                if (db.userProfileDao().getProfileByEmail(email) != null) {
                    runOnUiThread(() -> etSignUpEmail.setError("Email already registered!"));
                    return;
                }

                // 3. Save full profile to database
                UserProfile newUser = new UserProfile(name, email, pass, age, gender, height, weight);
                db.userProfileDao().insert(newUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}