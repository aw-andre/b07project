package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class medicationLog extends AppCompatActivity {

    private EditText rescueTimeEditText;
    private EditText rescueDoseEditText;
    private Button rescueSubmitButton;

    private EditText controlTimeEditText;
    private EditText controlDoseEditText;
    private Button controlSubmitButton;

    private CheckBox feelingSameCheckBox;
    private CheckBox feelingBetterCheckBox;
    private CheckBox feelingWorseCheckBox;
    private EditText breathRatingEditText;
    private Button checkInSubmitButton;

    // Hardcoded child ID for demonstration
    private static final String HARDCODED_CHILD_ID = "test_child_123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationlog);

        ImageButton backButton = findViewById(R.id.imageButton1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(medicationLog.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });

        rescueTimeEditText = findViewById(R.id.textInputEditText5);
        rescueDoseEditText = findViewById(R.id.textInputEditText12);
        rescueSubmitButton = findViewById(R.id.button4);

        rescueSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRescueLogDataToFirebase();
            }
        });

        controlTimeEditText = findViewById(R.id.textInputEditText14);
        controlDoseEditText = findViewById(R.id.textInputEditText15);
        controlSubmitButton = findViewById(R.id.button7);

        controlSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendControlLogDataToFirebase();
            }
        });

        feelingSameCheckBox = findViewById(R.id.checkBox2);
        feelingBetterCheckBox = findViewById(R.id.checkBox6);
        feelingWorseCheckBox = findViewById(R.id.checkBox7);
        breathRatingEditText = findViewById(R.id.textInputEditText16);
        checkInSubmitButton = findViewById(R.id.checkInSubmitButton);

        checkInSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCheckInDataToFirebase();
            }
        });
    }

    private void sendRescueLogDataToFirebase() {
        String time = rescueTimeEditText.getText().toString().trim();
        String dose = rescueDoseEditText.getText().toString().trim();

        if (time.isEmpty() || dose.isEmpty()) {
            Toast.makeText(medicationLog.this, "Please fill out all fields for the Rescue Log", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference rescueLogRef = FirebaseDatabaseManager.getInstance().getDatabaseReference().child("children").child(HARDCODED_CHILD_ID).child("rescue_logs");
        DatabaseReference newLogEntryRef = rescueLogRef.push();
        Map<String, Object> logData = new HashMap<>();
        logData.put("time", time);
        logData.put("dose", dose);
        logData.put("timestamp", ServerValue.TIMESTAMP);

        newLogEntryRef.setValue(logData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(medicationLog.this, "Rescue Log submitted successfully!", Toast.LENGTH_SHORT).show();
                    rescueTimeEditText.setText("");
                    rescueDoseEditText.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(medicationLog.this, "Failed to submit Rescue Log: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void sendControlLogDataToFirebase() {
        String time = controlTimeEditText.getText().toString().trim();
        String dose = controlDoseEditText.getText().toString().trim();

        if (time.isEmpty() || dose.isEmpty()) {
            Toast.makeText(medicationLog.this, "Please fill out all fields for the Control Log", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference controlLogRef = FirebaseDatabaseManager.getInstance().getDatabaseReference().child("children").child(HARDCODED_CHILD_ID).child("control_logs");
        DatabaseReference newLogEntryRef = controlLogRef.push();
        Map<String, Object> logData = new HashMap<>();
        logData.put("time", time);
        logData.put("dose", dose);
        logData.put("timestamp", ServerValue.TIMESTAMP);

        newLogEntryRef.setValue(logData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(medicationLog.this, "Control Log submitted successfully!", Toast.LENGTH_SHORT).show();
                    controlTimeEditText.setText("");
                    controlDoseEditText.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(medicationLog.this, "Failed to submit Control Log: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void sendCheckInDataToFirebase() {
        String breathRating = breathRatingEditText.getText().toString().trim();
        String feeling = "";
        if (feelingSameCheckBox.isChecked()) {
            feeling = "Same";
        } else if (feelingBetterCheckBox.isChecked()) {
            feeling = "Better";
        } else if (feelingWorseCheckBox.isChecked()) {
            feeling = "Worse";
        }

        if (feeling.isEmpty() || breathRating.isEmpty()) {
            Toast.makeText(medicationLog.this, "Please select a feeling and enter a breath rating", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference checkInRef = FirebaseDatabaseManager.getInstance().getDatabaseReference().child("children").child(HARDCODED_CHILD_ID).child("post_medication_checkins");
        DatabaseReference newLogEntryRef = checkInRef.push();
        Map<String, Object> logData = new HashMap<>();
        logData.put("feeling", feeling);
        logData.put("breathRating", breathRating);
        logData.put("timestamp", ServerValue.TIMESTAMP);

        newLogEntryRef.setValue(logData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(medicationLog.this, "Check-In submitted successfully!", Toast.LENGTH_SHORT).show();
                    feelingSameCheckBox.setChecked(false);
                    feelingBetterCheckBox.setChecked(false);
                    feelingWorseCheckBox.setChecked(false);
                    breathRatingEditText.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(medicationLog.this, "Failed to submit Check-In: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
