package com.example.lifetrack;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        findViewById(R.id.btnBackSupport).setOnClickListener(v -> finish());
    }
}