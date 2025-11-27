package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class badges extends AppCompatActivity {

    private ImageView bronzeTrophy, silverTrophy, goldTrophy;
    private DatabaseReference childRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.badges);

        // Back button
        ImageButton backButton = findViewById(R.id.imageButton3);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(badges.this, childUserInterfaceHome.class);
            startActivity(intent);
        });

        // Initialize ImageViews
        bronzeTrophy = findViewById(R.id.imageView5);
        silverTrophy = findViewById(R.id.imageView6);
        goldTrophy = findViewById(R.id.imageView7);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(badges.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get reference to the child's data in Firebase
        childRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("children")
                .child(userId);

        // Check conditions for all badges
        checkBronzeBadge();
        checkSilverBadge();
        checkGoldBadge();
    }

    private void checkBronzeBadge() {
        DatabaseReference controlLogsRef = childRef.child("controlLogs");
        controlLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<LocalDate> logDates = new HashSet<>();
                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    Long timestamp = logSnapshot.child("timestamp").getValue(Long.class);
                    if (timestamp != null) {
                        logDates.add(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate());
                    }
                }

                if (hasSevenDayStreak(logDates)) {
                    bronzeTrophy.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(badges.this, "Failed to load control logs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean hasSevenDayStreak(Set<LocalDate> dates) {
        if (dates.size() < 7) {
            return false;
        }
        List<LocalDate> sortedDates = dates.stream().sorted().collect(Collectors.toList());
        for (int i = 0; i <= sortedDates.size() - 7; i++) {
            boolean isStreak = true;
            for (int j = 0; j < 6; j++) {
                if (!sortedDates.get(i + j).plusDays(1).equals(sortedDates.get(i + j + 1))) {
                    isStreak = false;
                    break;
                }
            }
            if (isStreak) {
                return true;
            }
        }
        return false;
    }

    private void checkSilverBadge() {
        DatabaseReference checklistLogsRef = childRef.child("techniqueChecklist");
        checklistLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int perfectSessions = 0;
                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    Boolean shookInhaler = logSnapshot.child("shakeInhaler").getValue(Boolean.class);
                    Boolean removedCap = logSnapshot.child("removeCap").getValue(Boolean.class);
                    Boolean exhaledDeeply = logSnapshot.child("exhaleDeeply").getValue(Boolean.class);
                    Boolean pressedWhileInhaling = logSnapshot.child("pressAndInhale").getValue(Boolean.class);
                    Boolean heldBreath = logSnapshot.child("holdBreath").getValue(Boolean.class);
                    Boolean exhaledSlowly = logSnapshot.child("exhaleSlowly").getValue(Boolean.class);

                    if (Boolean.TRUE.equals(shookInhaler) &&
                        Boolean.TRUE.equals(removedCap) &&
                        Boolean.TRUE.equals(exhaledDeeply) &&
                        Boolean.TRUE.equals(pressedWhileInhaling) &&
                        Boolean.TRUE.equals(heldBreath) &&
                        Boolean.TRUE.equals(exhaledSlowly)) {
                        perfectSessions++;
                    }
                }

                if (perfectSessions >= 10) {
                    silverTrophy.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(badges.this, "Failed to load technique checklist logs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkGoldBadge() {
        DatabaseReference rescueLogsRef = childRef.child("rescueLogs");
        rescueLogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<YearMonth, Integer> monthlyCounts = new HashMap<>();
                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    Long timestamp = logSnapshot.child("timestamp").getValue(Long.class);
                    if (timestamp != null) {
                        YearMonth ym = YearMonth.from(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate());
                        monthlyCounts.put(ym, monthlyCounts.getOrDefault(ym, 0) + 1);
                    }
                }

                // Condition: less than three rescue sessions in ANY one month
                // This means if even one month has < 3 sessions, they get the badge.
                boolean conditionMet = false;
                if (monthlyCounts.isEmpty()) {
                    conditionMet = true; // No logs means 0 logs, which is < 3
                } else {
                    for (Integer count : monthlyCounts.values()) {
                        if (count < 3) {
                            conditionMet = true;
                            break;
                        }
                    }
                }

                if (conditionMet) {
                    goldTrophy.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(badges.this, "Failed to load rescue logs.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
