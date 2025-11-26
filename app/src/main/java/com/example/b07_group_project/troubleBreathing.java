package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class troubleBreathing extends AppCompatActivity {

    private EditText rescueAttemptEditText;
    private EditText pefEditText;
    private Switch inabilityToSpeakSwitch;
    private Switch chestRetractionsSwitch;
    private Switch blueLipsNailsSwitch;
    private Button submitButton;

    // Hardcoded child ID for demonstration
    private static final String HARDCODED_CHILD_ID = "test_child_123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.troublebreathing);

        ImageButton backButton = findViewById(R.id.imageButton2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(troubleBreathing.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });

        rescueAttemptEditText = findViewById(R.id.rescueAttemptEditText);
        pefEditText = findViewById(R.id.pefEditText);
        inabilityToSpeakSwitch = findViewById(R.id.switch3);
        chestRetractionsSwitch = findViewById(R.id.switch4);
        blueLipsNailsSwitch = findViewById(R.id.switch5);
        submitButton = findViewById(R.id.button5);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTroubleBreathingDataToFirebase();
            }
        });
    }

    private void sendTroubleBreathingDataToFirebase() {
        String rescueAttempt = rescueAttemptEditText.getText().toString().trim();
        String pef = pefEditText.getText().toString().trim();

        if (rescueAttempt.isEmpty()) {
            Toast.makeText(troubleBreathing.this, "Please enter the last rescue attempt.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference logRef = FirebaseDatabaseManager.getInstance()
                .getDatabaseReference()
                .child("children")
                .child(HARDCODED_CHILD_ID)
                .child("trouble_breathing_logs");

        DatabaseReference newLogEntryRef = logRef.push();

        Map<String, Object> logData = new HashMap<>();
        logData.put("lastRescueAttempt", rescueAttempt);
        logData.put("pef", pef);
        logData.put("inabilityToSpeak", inabilityToSpeakSwitch.isChecked());
        logData.put("chestRetractions", chestRetractionsSwitch.isChecked());
        logData.put("blueLipsOrNails", blueLipsNailsSwitch.isChecked());
        logData.put("timestamp", ServerValue.TIMESTAMP);

        newLogEntryRef.setValue(logData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(troubleBreathing.this, "Log submitted successfully!", Toast.LENGTH_SHORT).show();
                    rescueAttemptEditText.setText("");
                    pefEditText.setText("");
                    inabilityToSpeakSwitch.setChecked(false);
                    chestRetractionsSwitch.setChecked(false);
                    blueLipsNailsSwitch.setChecked(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(troubleBreathing.this, "Failed to submit log: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}