package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.b07_group_project.alerts.AlertManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

public class parentUserInterfaceHome extends AppCompatActivity {

    private static final String PARENT_ID = "parent123";

    private Spinner childSelectorSpinner;
    private TextView todaysZoneText;
    private TextView lastRescueTime;
    private TextView weeklyRescueCount;
    private TextView trendSnippetText;
    private ToggleButton trendToggle;

    private DatabaseReference childrenRef;

    private List<String> childNames = new ArrayList<>();
    private List<String> childIds = new ArrayList<>();
    private ArrayAdapter<String> childAdapter;

    private final DateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.parentuserinterfacehome);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AlertManager.init(getApplicationContext(), PARENT_ID); // background monitor

        bindViews();
        setupSpinner();
        setupButtons();

        DatabaseReference root = FirebaseDatabaseManager.getInstance().getDatabaseReference();
        childrenRef = root.child("children");

        loadChildrenForParent();
    }

    private void bindViews() {
        childSelectorSpinner = findViewById(R.id.childSelectorSpinner);
        todaysZoneText = findViewById(R.id.todaysZoneText);
        lastRescueTime = findViewById(R.id.lastRescueTime);
        weeklyRescueCount = findViewById(R.id.weeklyRescueCount);
        trendSnippetText = findViewById(R.id.trendSnippetText);
        trendToggle = findViewById(R.id.trendToggle);
    }

    private void setupSpinner() {
        childNames.add("Select child");
        childIds.add(null);

        childAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, childNames);
        childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSelectorSpinner.setAdapter(childAdapter);

        childSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos <= 0) {
                    clearDashboard();
                    return;
                }
                loadDashboard(childIds.get(pos));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                clearDashboard();
            }
        });

        trendToggle.setOnClickListener(v -> {
            String id = getSelectedChildId();
            if (id != null) loadTrendSnippet(id);
        });
    }

    private void setupButtons() {
        findViewById(R.id.button2).setOnClickListener(
                v -> startActivity(new Intent(this, medicationManagement.class)));

        findViewById(R.id.button3).setOnClickListener(
                v -> startActivity(new Intent(this, symptomReports.class)));

        findViewById(R.id.button4).setOnClickListener(
                v -> startActivity(new Intent(this, emergencyContacts.class)));

        findViewById(R.id.button5).setOnClickListener(
                v -> startActivity(new Intent(this, settings.class)));

        findViewById(R.id.button6).setOnClickListener(
                v -> startActivity(new Intent(this, manageChildren.class)));

        findViewById(R.id.button7).setOnClickListener(
                v -> startActivity(new Intent(this, providerSharing.class)));
    }

    // ----------------------------------------------------------
    // CHILD LOADING
    // ----------------------------------------------------------
    private void loadChildrenForParent() {
        childrenRef.orderByChild("parentId").equalTo(PARENT_ID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        childNames.clear();
                        childIds.clear();

                        childNames.add("Select child");
                        childIds.add(null);

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String id = snap.getKey();
                            String name = snap.child("name").getValue(String.class);
                            if (id != null && name != null) {
                                childIds.add(id);
                                childNames.add(name);
                            }
                        }
                        childAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private String getSelectedChildId() {
        int pos = childSelectorSpinner.getSelectedItemPosition();
        if (pos <= 0 || pos >= childIds.size()) return null;
        return childIds.get(pos);
    }

    // ----------------------------------------------------------
    // DASHBOARD LOADING
    // ----------------------------------------------------------
    private void loadDashboard(String childId) {
        loadTodayZone(childId);
        loadLastRescue(childId);
        loadWeeklyRescueCount(childId);
        loadTrendSnippet(childId);
    }

    private void loadTodayZone(String childId) {
        DatabaseReference pefRef = childrenRef.child(childId).child("logs").child("pef");

        pefRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                final long[] latestPef = new long[1];
                latestPef[0] = -1;
                long latestTs = -1;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Long ts = snap.child("timestamp").getValue(Long.class);
                    Long pefVal = snap.child("pef").getValue(Long.class);

                    if (ts != null && pefVal != null && ts > latestTs) {
                        latestTs = ts;
                        latestPef[0] = pefVal;
                    }
                }

                if (latestPef[0] == -1) {
                    todaysZoneText.setText("Today's Zone: -");
                    return;
                }

                childrenRef.child(childId).child("pb")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot s) {
                                Long pb = s.getValue(Long.class);
                                if (pb == null || pb == 0) {
                                    todaysZoneText.setText("Today's Zone: -");
                                    return;
                                }

                                double percent = (latestPef[0] * 100.0) / pb;

                                String zone;
                                if (percent >= 80) zone = "Green";
                                else if (percent >= 50) zone = "Yellow";
                                else zone = "Red";

                                todaysZoneText.setText("Today's Zone: " + zone);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                todaysZoneText.setText("Today's Zone: -");
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                todaysZoneText.setText("Today's Zone: -");
            }
        });
    }


    private void loadLastRescue(String childId) {
        childrenRef.child(childId).child("logs").child("medication").child("rescue")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        long latest = -1;

                        for (DataSnapshot row : snap.getChildren()) {
                            Long ts = row.child("timestamp").getValue(Long.class);
                            if (ts != null && ts > latest) latest = ts;
                        }

                        if (latest > 0) {
                            lastRescueTime.setText(timeFormat.format(latest));
                        } else {
                            lastRescueTime.setText("-");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void loadWeeklyRescueCount(String childId) {
        long now = System.currentTimeMillis();
        long week = 7L * 24 * 60 * 60 * 1000;

        childrenRef.child(childId).child("logs").child("medication").child("rescue")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        int count = 0;

                        for (DataSnapshot row : snap.getChildren()) {
                            Long ts = row.child("timestamp").getValue(Long.class);
                            if (ts != null && ts >= now - week) count++;
                        }

                        weeklyRescueCount.setText(String.valueOf(count));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    // ----------------------------------------------------------
    // TREND SNIPPET
    // ----------------------------------------------------------
    private void loadTrendSnippet(String childId) {
        boolean is30 = trendToggle.isChecked();
        long now = System.currentTimeMillis();
        long range = is30 ? 30L * 24 * 60 * 60 * 1000 : 7L * 24 * 60 * 60 * 1000;

        childrenRef.child(childId).child("logs")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot logsSnap) {

                        // rescue trend
                        int rescueCount = 0;
                        long rescueSum = 0;

                        DataSnapshot rescueSnap = logsSnap.child("medication").child("rescue");
                        for (DataSnapshot row : rescueSnap.getChildren()) {
                            Long ts = row.child("timestamp").getValue(Long.class);
                            if (ts != null && ts >= now - range) {
                                rescueCount++;
                                rescueSum += ts;
                            }
                        }

                        // PEF trend
                        long latestTs = -1;
                        Long latestPEF = null;

                        DataSnapshot pefSnap = logsSnap.child("pef");
                        for (DataSnapshot p : pefSnap.getChildren()) {
                            Long ts = p.child("timestamp").getValue(Long.class);
                            Long val = p.child("pef").getValue(Long.class);
                            if (ts != null && val != null && ts > latestTs) {
                                latestTs = ts;
                                latestPEF = val;
                            }
                        }

                        // controller adherence (rough estimate)
                        int controllerCount = 0;
                        DataSnapshot controllerSnap = logsSnap.child("medication").child("controller");
                        for (DataSnapshot c : controllerSnap.getChildren()) {
                            Long ts = c.child("timestamp").getValue(Long.class);
                            if (ts != null && ts >= now - range) controllerCount++;
                        }

                        String snippet = generateTrendSnippet(rescueCount, controllerCount, latestPEF, is30);
                        trendSnippetText.setText(snippet);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private String generateTrendSnippet(int rescueCount,
                                        int controllerCount,
                                        Long latestPEF,
                                        boolean is30Days) {

        String period = is30Days ? "the month" : "the week";

        StringBuilder sb = new StringBuilder();

        // Rescue usage
        if (rescueCount == 0) sb.append("No rescue use in ").append(period).append(". ");
        else if (rescueCount <= 2) sb.append("Low rescue use in ").append(period).append(". ");
        else if (rescueCount <= 6) sb.append("Moderate rescue use recently. ");
        else sb.append("High rescue frequency — review triggers. ");

        // Controller adherence
        if (controllerCount == 0) sb.append("Controller use missing. ");
        else if (controllerCount < (is30Days ? 15 : 3))
            sb.append("Controller adherence could improve. ");
        else sb.append("Good controller adherence. ");

        // PEF
        if (latestPEF == null) sb.append("No recent PEF data.");
        else sb.append("PEF recently measured.");

        return sb.toString();
    }

    // ----------------------------------------------------------
    private void clearDashboard() {
        todaysZoneText.setText("Today's Zone: -");
        lastRescueTime.setText("-");
        weeklyRescueCount.setText("0");
        trendSnippetText.setText("-");
    }
}
