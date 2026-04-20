package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnProfilePage, btnDailyRecordPage, btnHistoryPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnProfilePage = findViewById(R.id.btnProfilePage);
        btnDailyRecordPage = findViewById(R.id.btnDailyRecordPage);
        btnHistoryPage = findViewById(R.id.btnHistoryPage);

        btnProfilePage.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );

        btnDailyRecordPage.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DailyRecordActivity.class))
        );

        btnHistoryPage.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecordHistoryActivity.class))
        );
    }
}