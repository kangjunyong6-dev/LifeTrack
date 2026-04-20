package com.example.lifetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.util.List;
import java.util.concurrent.Executors;

public class RecordHistoryActivity extends AppCompatActivity {

    TextView tvHistory;
    Button btnBackHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tvHistory = findViewById(R.id.tvHistory);
        btnBackHistory = findViewById(R.id.btnBackHistory);

        AppDatabase db = AppDatabase.getInstance(this);

        btnBackHistory.setOnClickListener(v -> {
            Intent intent = new Intent(RecordHistoryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            List<DailyHealthRecord> records = db.dailyHealthRecordDao().getAllRecords();

            StringBuilder builder = new StringBuilder();

            if (records.isEmpty()) {
                builder.append("No records found");
            } else {
                for (DailyHealthRecord r : records) {
                    builder.append("Date: ").append(r.getDate()).append("\n");
                    builder.append("Exercise: ").append(r.getExerciseMinutes()).append(" mins\n");
                    builder.append("Sleep: ").append(r.getSleepHours()).append(" hours\n");
                    builder.append("Food: ").append(r.getFoodNote()).append("\n");
                    builder.append("----------------------\n");
                }
            }

            runOnUiThread(() -> tvHistory.setText(builder.toString()));
        });
    }
}