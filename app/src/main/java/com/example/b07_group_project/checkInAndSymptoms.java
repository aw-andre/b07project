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

import java.util.HashMap;
import java.util.Map;

public class checkInAndSymptoms extends AppCompatActivity {

    private Switch switch3, switch4, switch5, switch6, switch7, switch9, switch10, switch11;
    private Button submitButton;

    // Hardcoded child ID for demonstration
    private static final String HARDCODED_CHILD_ID = "test_child_123";

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
        submitButton = findViewById(R.id.button9);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(checkInAndSymptoms.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSymptomDataToFirebase();
            }
        });
    }

    private void sendSymptomDataToFirebase() {
        DatabaseReference symptomsLogRef = FirebaseDatabaseManager.getInstance()
                .getDatabaseReference()
                .child("children")
                .child(HARDCODED_CHILD_ID)
                .child("symptoms");

        DatabaseReference newLogEntryRef = symptomsLogRef.push();

        Map<String, Object> symptomData = new HashMap<>(); // Use Object to allow for different data types
        symptomData.put("dayTimeCoughingWheezing", switch3.isChecked());
        symptomData.put("nightTimeCoughingWheezing", switch4.isChecked());
        symptomData.put("activityLimits", switch5.isChecked());
        symptomData.put("dustExposure", switch6.isChecked());
        symptomData.put("petsExposure", switch7.isChecked());
        symptomData.put("coldAirExposure", switch9.isChecked());
        symptomData.put("smokeExposure", switch10.isChecked());
        symptomData.put("perfumeExposure", switch11.isChecked());
        symptomData.put("timestamp", ServerValue.TIMESTAMP); // Add a server-side timestamp

        newLogEntryRef.setValue(symptomData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(checkInAndSymptoms.this, "Symptom data submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(checkInAndSymptoms.this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
