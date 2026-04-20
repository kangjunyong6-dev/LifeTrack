package com.example.lifetrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifetrack.data.entity.AppDatabase;
import com.example.lifetrack.data.entity.DailyHealthRecord;

import java.util.List;
import java.util.concurrent.Executors;

public class RecordHistoryActivity extends AppCompatActivity {

    LinearLayout layoutHistoryContainer;
    Button btnBackHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        layoutHistoryContainer = findViewById(R.id.layoutHistoryContainer);
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

            runOnUiThread(() -> {
                layoutHistoryContainer.removeAllViews();

                if (records.isEmpty()) {
                    TextView emptyText = new TextView(this);
                    emptyText.setText("No records found");
                    emptyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    emptyText.setTextColor(Color.parseColor("#222222"));
                    layoutHistoryContainer.addView(emptyText);
                } else {
                    for (DailyHealthRecord r : records) {
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

                        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        cardParams.setMargins(0, 0, 0, dpToPx(12));
                        card.setLayoutParams(cardParams);

                        TextView tvDate = new TextView(this);
                        tvDate.setText("Date: " + r.getDate());
                        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                        tvDate.setTextColor(Color.parseColor("#222222"));
                        tvDate.setTypeface(null, android.graphics.Typeface.BOLD);

                        TextView tvExercise = new TextView(this);
                        tvExercise.setText("Exercise: " + r.getExerciseMinutes() + " mins");
                        tvExercise.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        tvExercise.setTextColor(Color.parseColor("#666666"));

                        TextView tvSleep = new TextView(this);
                        tvSleep.setText("Sleep: " + r.getSleepHours() + " hours");
                        tvSleep.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        tvSleep.setTextColor(Color.parseColor("#666666"));

                        TextView tvFood = new TextView(this);
                        tvFood.setText("Food: " + r.getFoodNote());
                        tvFood.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        tvFood.setTextColor(Color.parseColor("#666666"));

                        card.addView(tvDate);
                        card.addView(tvExercise);
                        card.addView(tvSleep);
                        card.addView(tvFood);

                        layoutHistoryContainer.addView(card);
                    }
                }
            });
        });
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        ));
    }
}