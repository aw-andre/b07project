package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class checkInAndSymptoms extends AppCompatActivity {

    private Switch switch3, switch4, switch5;
    private Switch switch6, switch7, switch9, switch10, switch11, switch12;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkinandsymptoms);

        ImageButton backButton = findViewById(R.id.imageButton);

        switch3 = findViewById(R.id.switch3);
        switch4 = findViewById(R.id.switch4);
        switch5 = findViewById(R.id.switch5);

        switch6 = findViewById(R.id.switch6);
        switch7 = findViewById(R.id.switch7);
        switch9 = findViewById(R.id.switch9);
        switch10 = findViewById(R.id.switch10);
        switch11 = findViewById(R.id.switch11);
        switch12 = findViewById(R.id.switch12);

        submitButton = findViewById(R.id.button9);

        backButton.setOnClickListener(v -> finish());

        submitButton.setOnClickListener(v -> sendSymptomDataToFirebase());
    }

    private void sendSymptomDataToFirebase() {
        //String childId = "-OerA9MC_EiCnwvC-CVQ"; // Replace with the actual child ID

        String childId = getIntent().getStringExtra("childId");

        if (childId == null) {
            Toast.makeText(checkInAndSymptoms.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference symptomsRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("children")
                .child(childId)
                .child("logs")
                .child("symptoms");

        DatabaseReference newEntry = symptomsRef.push();

        List<String> triggersList = new ArrayList<>();

        if (switch6.isChecked()) triggersList.add("Exercise");
        if (switch7.isChecked()) triggersList.add("Dust/Pets");
        if (switch9.isChecked()) triggersList.add("Cold Air");
        if (switch10.isChecked()) triggersList.add("Smoke");
        if (switch12.isChecked()) triggersList.add("Illness");
        if (switch11.isChecked()) triggersList.add("Perfume/Cleaners");

        Map<String, Object> data = new HashMap<>();

        data.put("activityLimits", switch5.isChecked());
        data.put("author", "Child");
        data.put("coughWheeze", switch3.isChecked());
        data.put("nightWaking", switch4.isChecked());
        data.put("notes", "");
        data.put("timestamp", ServerValue.TIMESTAMP);
        data.put("triggers", triggersList);

        newEntry.setValue(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(checkInAndSymptoms.this, "Submitted!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(checkInAndSymptoms.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
