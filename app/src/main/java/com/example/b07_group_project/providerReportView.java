package com.example.b07_group_project;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class providerReportView extends AppCompatActivity {

    private Spinner childSelector;
    private TextView rescueFrequencyText;
    private TextView adherenceText;
    private TextView symptomBurdenText;
    private TextView zoneDistributionText;
    private TextView triageIncidentsText;

    private final List<String> linkedChildIds = new ArrayList<>();
    private final List<String> linkedChildNames = new ArrayList<>();

    private DatabaseReference usersRef, childrenRef, shareSettingsRef;
    private String providerId = "provider789"; // TODO: Replace with FirebaseAuth.getUid()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.providerreportview);

        bindViews();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        childrenRef = FirebaseDatabase.getInstance().getReference("children");
        shareSettingsRef = FirebaseDatabase.getInstance().getReference("shareSettings");

        loadLinkedChildren();

        ImageButton button = findViewById(R.id.imageButton);
        button.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        childSelector = findViewById(R.id.providerReportChildSelector);
        rescueFrequencyText = findViewById(R.id.rescueFrequencyText);
        adherenceText = findViewById(R.id.adherenceText);
        symptomBurdenText = findViewById(R.id.symptomBurdenText);
        zoneDistributionText = findViewById(R.id.zoneDistributionText);
        triageIncidentsText = findViewById(R.id.triageIncidentsText);
    }

    private void loadLinkedChildren() {
        usersRef.child(providerId).child("linkedChildren")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        linkedChildIds.clear();
                        linkedChildNames.clear();

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            if (Boolean.TRUE.equals(childSnap.getValue(Boolean.class))) {
                                String childId = childSnap.getKey();
                                loadChildName(childId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadChildName(String childId) {
        childrenRef.child(childId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot nameSnap) {

                        String name = nameSnap.getValue(String.class);
                        if (name != null) {
                            linkedChildIds.add(childId);
                            linkedChildNames.add(name);
                        }

                        updateChildSelector();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateChildSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                linkedChildNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSelector.setAdapter(adapter);

        childSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReportForChild(linkedChildIds.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (!linkedChildIds.isEmpty()) {
            childSelector.setSelection(0);
            loadReportForChild(linkedChildIds.get(0));
        }
    }

    private void loadReportForChild(String childId) {
        shareSettingsRef.child(childId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shareSnap) {
                        boolean shareRescue = shareSnap.child("rescue").getValue(Boolean.class) == Boolean.TRUE;
                        boolean shareSymptoms = shareSnap.child("symptoms").getValue(Boolean.class) == Boolean.TRUE;
                        boolean shareTriage = shareSnap.child("triage").getValue(Boolean.class) == Boolean.TRUE;
                        boolean sharePef = shareSnap.child("pef").getValue(Boolean.class) == Boolean.TRUE;

                        loadRescue(childId, shareRescue);
                        loadSymptoms(childId, shareSymptoms);
                        loadTriage(childId, shareTriage);

                        // zone & adherence removed
                        // <Zone distribution> to be implemented
                        // <Adherence calculation> to be implemented

                        if (!sharePef) {
                            zoneDistributionText.setText("Zone: Not shared");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadRescue(String childId, boolean allowed) {
        if (!allowed) {
            rescueFrequencyText.setText("Rescue: Not shared");
            return;
        }

        childrenRef.child(childId).child("logs").child("medication").child("rescue")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot rescueSnap) {
                        int count = 0;
                        for (DataSnapshot entry : rescueSnap.getChildren()) {
                            count++;
                        }
                        rescueFrequencyText.setText("Rescue uses: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void loadSymptoms(String childId, boolean allowed) {
        if (!allowed) {
            symptomBurdenText.setText("Symptoms: Not shared");
            return;
        }

        childrenRef.child(childId).child("logs").child("symptoms")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot symptomsSnap) {
                        int count = 0;
                        for (DataSnapshot entry : symptomsSnap.getChildren()) {
                            count++;
                        }
                        symptomBurdenText.setText("Symptom reports: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadTriage(String childId, boolean allowed) {
        if (!allowed) {
            triageIncidentsText.setText("Triage incidents: Not shared");
            return;
        }

        childrenRef.child(childId).child("logs").child("triage")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot triageSnap) {
                        int count = 0;
                        for (DataSnapshot entry : triageSnap.getChildren()) {
                            count++;
                        }
                        triageIncidentsText.setText("Triage incidents: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
