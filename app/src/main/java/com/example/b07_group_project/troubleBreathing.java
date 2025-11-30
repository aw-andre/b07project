package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class troubleBreathing extends AppCompatActivity {

    private String CHILD_ID;
    private DatabaseReference childRef;
    private EditText rescueAttemptEditText;
    private EditText pefEditText;
    private Switch inabilityToSpeakSwitch;
    private Switch chestRetractionsSwitch;
    private Switch blueLipsNailsSwitch;
    private Button submitButton;
    private EditText assessmentEditText;
    private EditText timerEditText;

    private boolean isFollowUpMode = false;
    private String currentIncidentId;
    private Integer pbValue;

    private CountDownTimer countDownTimer;

    private Long rescueAttemptTimestamp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.troublebreathing);

        CHILD_ID = getIntent().getStringExtra("childId");

        if (CHILD_ID == null) {
            Toast.makeText(this, "No child selected", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        childRef = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(CHILD_ID);

        ImageButton backButton = findViewById(R.id.imageButton2);
        backButton.setOnClickListener(v -> finish());

        rescueAttemptEditText = findViewById(R.id.rescueAttemptEditText);
        pefEditText = findViewById(R.id.pefEditText);
        inabilityToSpeakSwitch = findViewById(R.id.switch3);
        chestRetractionsSwitch = findViewById(R.id.switch4);
        blueLipsNailsSwitch = findViewById(R.id.switch5);
        submitButton = findViewById(R.id.button5);
        assessmentEditText = findViewById(R.id.editTextTextMultiLine);
        timerEditText = findViewById(R.id.editTextTime);

        rescueAttemptEditText.setOnClickListener(v -> showRescueAttemptPicker());

        submitButton.setOnClickListener(v -> {
            if (isFollowUpMode) {
                handleFollowUpSubmit();
            } else {
                handleInitialSubmit();
            }
        });

        loadPbValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void showRescueAttemptPicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .build();

        picker.show(getSupportFragmentManager(), "rescue_attempt_time");

        picker.addOnPositiveButtonClickListener(dialog -> {
            int hour = (int) picker.getHour();
            int minute = (int) picker.getMinute();

            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);

            rescueAttemptTimestamp = c.getTimeInMillis();
            rescueAttemptEditText.setText(String.format("%02d:%02d", hour, minute));
        });
    }

    private void handleInitialSubmit() {
        String rescueAttempt = rescueAttemptEditText.getText().toString().trim();
        String pefText = pefEditText.getText().toString().trim();

        if (rescueAttempt.isEmpty()) {
            Toast.makeText(this, "Please select the time of last rescue attempt.", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer pefValue = null;
        if (!pefText.isEmpty()) {
            try {
                pefValue = Integer.parseInt(pefText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "PEF must be a number.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean inabilityToSpeak = inabilityToSpeakSwitch.isChecked();
        boolean chestRetractions = chestRetractionsSwitch.isChecked();
        boolean blueLipsOrNails = blueLipsNailsSwitch.isChecked();
        int redFlagCount = 0;
        if (inabilityToSpeak) redFlagCount++;
        if (chestRetractions) redFlagCount++;
        if (blueLipsOrNails) redFlagCount++;

        String zone = computeZone(pefValue, pbValue, redFlagCount);

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference incidentsRef = root.child("children")
                .child(CHILD_ID)
                .child("logs")
                .child("incidents");
        DatabaseReference newIncidentRef = incidentsRef.push();
        currentIncidentId = newIncidentRef.getKey();

        Map<String, Object> incidentData = new HashMap<>();
        incidentData.put("timestamp", ServerValue.TIMESTAMP);
        incidentData.put("lastRescueAttempt", rescueAttemptTimestamp);
        if (pefValue != null) {
            incidentData.put("pef", pefValue);
        }
        incidentData.put("inabilityToSpeak", inabilityToSpeak);
        incidentData.put("chestRetractions", chestRetractions);
        incidentData.put("blueLipsOrNails", blueLipsOrNails);
        incidentData.put("zone", zone);

        final Integer finalPefValue = pefValue;
        final String finalZone = zone;

        newIncidentRef.setValue(incidentData)
                .addOnSuccessListener(aVoid -> {
                    if (finalPefValue != null) {
                        logPef(finalPefValue);
                    }
                    logZone(finalZone);
                    assessmentEditText.setText(buildActionMessage(finalZone, false));
                    if ("GREEN".equals(finalZone)) {
                        Toast.makeText(troubleBreathing.this, "You are in the green zone.", Toast.LENGTH_SHORT).show();
                    } else {
                        isFollowUpMode = true;
                        submitButton.setText("Submit follow-up");
                        startTimer();
                        Toast.makeText(troubleBreathing.this, "Triage logged. Please recheck in 10 minutes.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(troubleBreathing.this, "Failed to submit triage: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void handleFollowUpSubmit() {
        if (currentIncidentId == null) {
            Toast.makeText(this, "No active triage session.", Toast.LENGTH_SHORT).show();
            return;
        }

        String pefText = pefEditText.getText().toString().trim();

        Integer pefValue = null;
        if (!pefText.isEmpty()) {
            try {
                pefValue = Integer.parseInt(pefText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "PEF must be a number.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean inabilityToSpeak = inabilityToSpeakSwitch.isChecked();
        boolean chestRetractions = chestRetractionsSwitch.isChecked();
        boolean blueLipsOrNails = blueLipsNailsSwitch.isChecked();
        int redFlagCount = 0;
        if (inabilityToSpeak) redFlagCount++;
        if (chestRetractions) redFlagCount++;
        if (blueLipsOrNails) redFlagCount++;

        String zone = computeZone(pefValue, pbValue, redFlagCount);

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference followUpRef = root.child("children")
                .child(CHILD_ID)
                .child("logs")
                .child("incidents")
                .child(currentIncidentId)
                .child("followup");

        Map<String, Object> followUpData = new HashMap<>();
        followUpData.put("timestamp", ServerValue.TIMESTAMP);
        if (pefValue != null) {
            followUpData.put("pef", pefValue);
        }
        followUpData.put("inabilityToSpeak", inabilityToSpeak);
        followUpData.put("chestRetractions", chestRetractions);
        followUpData.put("blueLipsOrNails", blueLipsOrNails);
        followUpData.put("zone", zone);

        final Integer finalPefValue = pefValue;
        final String finalZone = zone;

        followUpRef.setValue(followUpData)
                .addOnSuccessListener(aVoid -> {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                    if (finalPefValue != null) {
                        logPef(finalPefValue);
                    }
                    logZone(finalZone);

                    boolean improved = "GREEN".equals(finalZone);
                    if (!improved) {
                        sendAlertToParent("Triage follow-up is still in " + finalZone + " zone.");
                        Toast.makeText(troubleBreathing.this, "Condition not improved. An alert was sent to your parent.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(troubleBreathing.this, "You are back in the green zone.", Toast.LENGTH_SHORT).show();
                    }

                    resetUiToInitialState();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(troubleBreathing.this, "Failed to submit follow-up: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(10 * 60 * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000L;
                long seconds = (millisUntilFinished % 60000L) / 1000L;
                timerEditText.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerEditText.setText("00:00");
                assessmentEditText.setText("10 minutes have passed. Please check how you feel now and submit a follow-up.");
            }
        }.start();
    }

    private void resetUiToInitialState() {
        isFollowUpMode = false;
        currentIncidentId = null;
        submitButton.setText("Submit");
        rescueAttemptEditText.setText("");
        pefEditText.setText("");
        inabilityToSpeakSwitch.setChecked(false);
        chestRetractionsSwitch.setChecked(false);
        blueLipsNailsSwitch.setChecked(false);
        assessmentEditText.setText("ASSESSMENT RESULTS");
        timerEditText.setText("10:00");
    }

    private String computeZone(Integer pefValue, Integer pbValue, int redFlagCount) {
        String zoneFromPef = null;
        if (pefValue != null && pbValue != null && pbValue > 0) {
            double ratio = (double) pefValue / (double) pbValue;
            if (ratio >= 0.8) {
                zoneFromPef = "GREEN";
            } else if (ratio >= 0.5) {
                zoneFromPef = "YELLOW";
            } else {
                zoneFromPef = "RED";
            }
        }

        if (redFlagCount > 0) {
            return "RED";
        }

        if (zoneFromPef != null) {
            return zoneFromPef;
        }

        return "GREEN";
    }

    private String buildActionMessage(String zone, boolean isFollowUp) {
        if ("RED".equals(zone)) {
            if (isFollowUp) {
                return "Red zone after follow-up. Call emergency services now.";
            } else {
                return "Red zone. Use your rescue medication now and recheck in 10 minutes.";
            }
        } else if ("YELLOW".equals(zone)) {
            if (isFollowUp) {
                return "Still in the yellow zone. Continue your action steps and contact your provider if not improving.";
            } else {
                return "Yellow zone. Follow your home action steps and recheck in 10 minutes.";
            }
        } else {
            if (isFollowUp) {
                return "You are back in the green zone. Continue regular controller medicine and monitoring.";
            } else {
                return "Green zone. Symptoms look controlled. Keep following your regular plan.";
            }
        }
    }

    private void logPef(int pefValue) {
        DatabaseReference pefRef = FirebaseDatabase.getInstance().getReference()
                .child("children")
                .child(CHILD_ID)
                .child("logs")
                .child("pef")
                .push();

        Map<String, Object> pefData = new HashMap<>();
        pefData.put("pef", pefValue);
        pefData.put("timestamp", ServerValue.TIMESTAMP);
        pefRef.setValue(pefData);
    }

    private void logZone(String zone) {
        DatabaseReference zoneRef = FirebaseDatabase.getInstance().getReference()
                .child("children")
                .child(CHILD_ID)
                .child("logs")
                .child("zone")
                .push();

        Map<String, Object> zoneData = new HashMap<>();
        zoneData.put("zone", zone);
        zoneData.put("timestamp", ServerValue.TIMESTAMP);
        zoneRef.setValue(zoneData);
    }
    private void sendAlertToParent(String message) {

        DatabaseReference parentIdRef = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(CHILD_ID)
                .child("parentId");

        parentIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String parentId = snapshot.getValue(String.class);

                if (parentId == null || parentId.isEmpty()) {
                    Toast.makeText(troubleBreathing.this,
                            "Parent not found. Alert not sent.", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference alertRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("alerts")
                        .child(parentId)
                        .push();

                Map<String, Object> alertData = new HashMap<>();
                alertData.put("message", message);
                alertData.put("timestamp", ServerValue.TIMESTAMP);
                alertData.put("childId", CHILD_ID);
                alertData.put("seen", false);

                alertRef.setValue(alertData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadPbValue() {
        DatabaseReference pbRef = FirebaseDatabase.getInstance().getReference()
                .child("children")
                .child(CHILD_ID)
                .child("pb");

        pbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer value = snapshot.getValue(Integer.class);
                pbValue = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
