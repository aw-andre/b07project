package com.example.b07_group_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class settings extends AppCompatActivity {

    private static final String PARENT_ID = "parent123";

    private EditText techniqueSessionsThreshold;
    private EditText lowRescueMonthThreshold;
    private Button saveSettingsButton;

    private DatabaseReference usersRef;
    private DatabaseReference childrenRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> finish());

        bindViews();

        usersRef =
                FirebaseDatabaseManager.getInstance()
                        .getDatabaseReference()
                        .child("users")
                        .child(PARENT_ID)
                        .child("badgeSettings");

        childrenRef =
                FirebaseDatabaseManager.getInstance()
                        .getDatabaseReference()
                        .child("children");

        loadParentBadgeSettings();

        saveSettingsButton.setOnClickListener(v -> saveBadgeSettings());
    }

    private void bindViews() {
        techniqueSessionsThreshold = findViewById(R.id.techniqueSessionsThreshold);
        lowRescueMonthThreshold = findViewById(R.id.lowRescueMonthThreshold);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);
    }

    private void loadParentBadgeSettings() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer technique = snapshot.child("techniqueSessionsThreshold").getValue(Integer.class);
                Integer lowRescue = snapshot.child("lowRescueMonthThreshold").getValue(Integer.class);

                techniqueSessionsThreshold.setText(String.valueOf(technique != null ? technique : 10));
                lowRescueMonthThreshold.setText(String.valueOf(lowRescue != null ? lowRescue : 4));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(settings.this, "Failed to load settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveBadgeSettings() {
        try {
            int technique = Integer.parseInt(techniqueSessionsThreshold.getText().toString().trim());
            int lowRescue = Integer.parseInt(lowRescueMonthThreshold.getText().toString().trim());

            if (technique < 1 || technique > 100) {
                Toast.makeText(this, "Technique sessions must be 1–100", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lowRescue < 0 || lowRescue > 30) {
                Toast.makeText(this, "Low rescue (per month) must be 0–30", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Save global settings
            usersRef.child("techniqueSessionsThreshold").setValue(technique);
            usersRef.child("lowRescueMonthThreshold").setValue(lowRescue);

            // 2. Update all children of this parent
            propagateToChildren(technique, lowRescue);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void propagateToChildren(int technique, int lowRescue) {
        childrenRef
                .orderByChild("parentId")
                .equalTo(PARENT_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            String childId = childSnap.getKey();

                            DatabaseReference perChildBadgeRef = childrenRef
                                    .child(childId)
                                    .child("badgeSettings");

                            perChildBadgeRef.child("techniqueSessionsThreshold").setValue(technique);
                            perChildBadgeRef.child("lowRescueMonthThreshold").setValue(lowRescue);
                        }

                        Toast.makeText(settings.this, "Settings saved for all children", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(settings.this, "Failed to update children", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
