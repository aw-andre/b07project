package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class badges extends AppCompatActivity {

    private ImageView bronzeTrophy, silverTrophy, goldTrophy;
    private DatabaseReference childRef;

    private int lowRescueMonthThreshold = 4;
    private int techniqueSessionsThreshold = 10;
    private int adherenceValue = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.badges);

        ImageButton backButton = findViewById(R.id.imageButton3);
        backButton.setOnClickListener(v -> finish());

        bronzeTrophy = findViewById(R.id.imageView5);
        silverTrophy = findViewById(R.id.imageView6);
        goldTrophy = findViewById(R.id.imageView7);

        // test id
        //String childId = "-OerA9MC_EiCnwvC-CVQ";

        String childId = getIntent().getStringExtra("childId");
        if (childId == null) return;

        childRef = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(childId);

        childRef.child("badgeSettings").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        Integer lowRescue = s.child("lowRescueMonthThreshold").getValue(Integer.class);
                        Integer technique = s.child("techniqueSessionsThreshold").getValue(Integer.class);

                        if (lowRescue != null) lowRescueMonthThreshold = lowRescue;
                        if (technique != null) techniqueSessionsThreshold = technique;

                        loadAdherenceAndProceed();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadAdherenceAndProceed();
                    }
                });
    }

    private void loadAdherenceAndProceed() {
        childRef.child("adherence").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        Integer a = s.getValue(Integer.class);
                        if (a != null) adherenceValue = a;

                        checkBronzeBadge();
                        checkSilverBadge();
                        checkGoldBadge();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        checkBronzeBadge();
                        checkSilverBadge();
                        checkGoldBadge();
                    }
                });
    }

    private void checkBronzeBadge() {

        DatabaseReference controllerRef =
                childRef.child("logs").child("medication").child("controller");

        controllerRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Map<String, Integer> dailyCount = new HashMap<>();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts == null) continue;

                            String dateKey = dateFormat.format(new Date(ts));
                            int c = dailyCount.containsKey(dateKey) ? dailyCount.get(dateKey) : 0;
                            dailyCount.put(dateKey, c + 1);
                        }

                        // adherenceValue must be matched exactly
                        List<String> goodDays = new ArrayList<>();

                        for (String d : dailyCount.keySet()) {
                            if (dailyCount.get(d) == adherenceValue) {
                                goodDays.add(d);
                            }
                        }

                        java.util.Collections.sort(goodDays);

                        if (hasSevenDayStreak(goodDays)) {
                            bronzeTrophy.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private boolean hasSevenDayStreak(List<String> list) {
        if (list.size() < 7) return false;

        for (int i = 0; i <= list.size() - 7; i++) {
            boolean ok = true;
            for (int j = 0; j < 6; j++) {
                if (!isNextDay(list.get(i + j), list.get(i + j + 1))) {
                    ok = false;
                    break;
                }
            }
            if (ok) return true;
        }
        return false;
    }

    private boolean isNextDay(String a, String b) {
        try {
            Date da = dateFormat.parse(a);
            Date db = dateFormat.parse(b);

            Calendar c = Calendar.getInstance();
            c.setTime(da);
            c.add(Calendar.DAY_OF_YEAR, 1);

            return dateFormat.format(c.getTime()).equals(b);
        } catch (ParseException e) {
            return false;
        }
    }

    private void checkSilverBadge() {

        DatabaseReference techRef =
                childRef.child("logs").child("techniqueSession");

        techRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int count = 0;

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Boolean ok = s.child("allStepsCorrect").getValue(Boolean.class);
                            if (Boolean.TRUE.equals(ok)) {
                                count++;
                            }
                        }

                        if (count >= techniqueSessionsThreshold) {
                            silverTrophy.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void checkGoldBadge() {

        DatabaseReference rescueRef =
                childRef.child("logs").child("medication").child("rescue");

        rescueRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long now = System.currentTimeMillis();
                        long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);

                        int recentCount = 0;

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts != null && ts >= thirtyDaysAgo) {
                                recentCount++;
                            }
                        }

                        if (recentCount <= lowRescueMonthThreshold) {
                            goldTrophy.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}

