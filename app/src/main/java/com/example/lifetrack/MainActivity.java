package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    Button btnRecord, btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        btnRecord = findViewById(R.id.btnRecord);
        btnHistory = findViewById(R.id.btnHistory);

        btnRecord.setOnClickListener(v ->
                startActivity(new Intent(this, DailyRecordActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, RecordHistoryActivity.class)));

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            if (id == R.id.nav_record) {
                startActivity(new Intent(this, DailyRecordActivity.class));
                return true;
            }

            if (id == R.id.nav_history) {
                startActivity(new Intent(this, RecordHistoryActivity.class));
                return true;
            }

            return false;
        });
    }
}