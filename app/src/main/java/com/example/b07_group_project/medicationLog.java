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

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.timepicker.MaterialTimePicker.Builder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.Calendar;
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

    private String childId;

    // test childId
    //private final String childId = "-OerA9MC_EiCnwvC-CVQ";

    private Long rescueTimestamp = null;
    private Long controllerTimestamp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationlog);

        childId = getIntent().getStringExtra("childId");
        if (childId == null) {
            Toast.makeText(this, "Error: No child selected", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ImageButton backButton = findViewById(R.id.imageButton1);
        backButton.setOnClickListener(v -> finish());

        rescueTimeEditText = findViewById(R.id.textInputEditText5);
        rescueDoseEditText = findViewById(R.id.textInputEditText12);
        rescueSubmitButton = findViewById(R.id.button4);

        controlTimeEditText = findViewById(R.id.textInputEditText14);
        controlDoseEditText = findViewById(R.id.textInputEditText15);
        controlSubmitButton = findViewById(R.id.button7);

        feelingSameCheckBox = findViewById(R.id.checkBox2);
        feelingBetterCheckBox = findViewById(R.id.checkBox6);
        feelingWorseCheckBox = findViewById(R.id.checkBox7);
        breathRatingEditText = findViewById(R.id.textInputEditText16);
        checkInSubmitButton = findViewById(R.id.checkInSubmitButton);

        rescueTimeEditText.setOnClickListener(v -> showTimePickerForRescue());
        controlTimeEditText.setOnClickListener(v -> showTimePickerForController());

        rescueSubmitButton.setOnClickListener(v -> sendRescueLogDataToFirebase());
        controlSubmitButton.setOnClickListener(v -> sendControlLogDataToFirebase());
        checkInSubmitButton.setOnClickListener(v -> sendCheckInDataToFirebase());
    }

    private void showTimePickerForRescue() {
        MaterialTimePicker picker = new Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();

        picker.show(getSupportFragmentManager(), "rescue_time");

        picker.addOnPositiveButtonClickListener(dialog -> {
            int hour = (int) picker.getHour();
            int minute = (int) picker.getMinute();

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);

            rescueTimestamp = c.getTimeInMillis();
            rescueTimeEditText.setText(String.format("%02d:%02d", hour, minute));
        });

    }

    private void showTimePickerForController() {
        MaterialTimePicker picker = new Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();

        picker.show(getSupportFragmentManager(), "controller_time");

        picker.addOnPositiveButtonClickListener(dialog -> {
            int hour = (int) picker.getHour();
            int minute = (int) picker.getMinute();

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);

            controllerTimestamp = c.getTimeInMillis();
            controlTimeEditText.setText(String.format("%02d:%02d", hour, minute));
        });

    }

    private void sendRescueLogDataToFirebase() {
        if (childId == null) return;

        String doseStr = rescueDoseEditText.getText().toString().trim();

        if (rescueTimestamp == null || doseStr.isEmpty()) {
            Toast.makeText(medicationLog.this, "Please select a time and enter a dose", Toast.LENGTH_SHORT).show();
            return;
        }

        int dose = Integer.parseInt(doseStr);

        DatabaseReference rescueRef = FirebaseDatabaseManager.getInstance()
                .getDatabaseReference()
                .child("children")
                .child(childId)
                .child("logs")
                .child("medication")
                .child("rescue");

        DatabaseReference newEntry = rescueRef.push();

        Map<String, Object> data = new HashMap<>();
        data.put("rescue", dose);
        data.put("timestamp", rescueTimestamp);

        newEntry.setValue(data)
                .addOnSuccessListener(aVoid -> {

                    deductInventory("rescue", dose);

                    Toast.makeText(medicationLog.this, "Rescue log submitted", Toast.LENGTH_SHORT).show();
                    rescueTimeEditText.setText("");
                    rescueDoseEditText.setText("");
                    rescueTimestamp = null;
                })
                .addOnFailureListener(e ->
                        Toast.makeText(medicationLog.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    private void sendControlLogDataToFirebase() {
        if (childId == null) return;

        String doseStr = controlDoseEditText.getText().toString().trim();

        if (controllerTimestamp == null || doseStr.isEmpty()) {
            Toast.makeText(medicationLog.this, "Please select a time and enter a dose", Toast.LENGTH_SHORT).show();
            return;
        }

        int dose = Integer.parseInt(doseStr);

        DatabaseReference controllerRef = FirebaseDatabaseManager.getInstance()
                .getDatabaseReference()
                .child("children")
                .child(childId)
                .child("logs")
                .child("medication")
                .child("controller");

        DatabaseReference newEntry = controllerRef.push();

        Map<String, Object> data = new HashMap<>();
        data.put("controller", dose);
        data.put("timestamp", controllerTimestamp);

        newEntry.setValue(data)
                .addOnSuccessListener(aVoid -> {

                    deductInventory("controller", dose);

                    Toast.makeText(medicationLog.this, "Controller log submitted", Toast.LENGTH_SHORT).show();
                    controlTimeEditText.setText("");
                    controlDoseEditText.setText("");
                    controllerTimestamp = null;
                })
                .addOnFailureListener(e ->
                        Toast.makeText(medicationLog.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    private void sendCheckInDataToFirebase() {
        if (childId == null) return;

        String feeling;
        if (feelingSameCheckBox.isChecked()) feeling = "same";
        else if (feelingBetterCheckBox.isChecked()) feeling = "better";
        else if (feelingWorseCheckBox.isChecked()) feeling = "worse";
        else {
            feeling = "";
        }

        String breathRatingStr = breathRatingEditText.getText().toString().trim();

        if (feeling.isEmpty() || breathRatingStr.isEmpty()) {
            Toast.makeText(medicationLog.this, "Please select a feeling and enter a breath rating", Toast.LENGTH_SHORT).show();
            return;
        }

        int breathRating;
        try {
            breathRating = Integer.parseInt(breathRatingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(medicationLog.this, "Invalid breath rating", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference pdRef = FirebaseDatabaseManager.getInstance()
                .getDatabaseReference()
                .child("children")
                .child(childId)
                .child("pb");

        pdRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(medicationLog.this, "No PB baseline set", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer pb = snapshot.getValue(Integer.class);
            if (pb == null || pb <= 0) {
                Toast.makeText(medicationLog.this, "Invalid PB value", Toast.LENGTH_SHORT).show();
                return;
            }

            double ratio = (double) breathRating / pb;
            String zone;

            if (ratio >= 0.8) zone = "Green";
            else if (ratio >= 0.5) zone = "Yellow";
            else zone = "Red";

            DatabaseReference postRef = FirebaseDatabaseManager.getInstance()
                    .getDatabaseReference()
                    .child("children")
                    .child(childId)
                    .child("logs")
                    .child("post")
                    .push();

            Map<String, Object> postData = new HashMap<>();
            postData.put("post", feeling);
            postData.put("timestamp", ServerValue.TIMESTAMP);
            postRef.setValue(postData);

            DatabaseReference pefRef = FirebaseDatabaseManager.getInstance()
                    .getDatabaseReference()
                    .child("children")
                    .child(childId)
                    .child("logs")
                    .child("pef")
                    .push();

            Map<String, Object> pefData = new HashMap<>();
            pefData.put("pef", breathRating);
            pefData.put("timestamp", ServerValue.TIMESTAMP);
            pefRef.setValue(pefData);

            DatabaseReference zoneRef = FirebaseDatabaseManager.getInstance()
                    .getDatabaseReference()
                    .child("children")
                    .child(childId)
                    .child("logs")
                    .child("zone")
                    .push();

            Map<String, Object> zoneData = new HashMap<>();
            zoneData.put("zone", zone);
            zoneData.put("timestamp", ServerValue.TIMESTAMP);
            zoneRef.setValue(zoneData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(medicationLog.this, "Check-in submitted", Toast.LENGTH_SHORT).show();
                        feelingSameCheckBox.setChecked(false);
                        feelingBetterCheckBox.setChecked(false);
                        feelingWorseCheckBox.setChecked(false);
                        breathRatingEditText.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(medicationLog.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }
    private void deductInventory(String medType, int usedAmount) {
        DatabaseReference root = FirebaseDatabaseManager.getInstance().getDatabaseReference();

        root.child("children").child(childId).child("parentId")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String parentId = snapshot.getValue(String.class);
                    if (parentId == null) return;

                    DatabaseReference invRef = root.child("inventory")
                            .child(childId)
                            .child(medType)
                            .child("amountLeft");

                    invRef.get().addOnSuccessListener(invSnap -> {
                        Integer current = invSnap.getValue(Integer.class);
                        if (current == null) current = 0;

                        int updated = Math.max(current - usedAmount, 0); // 음수 방지

                        invRef.setValue(updated);
                    });
                });
    }

}
