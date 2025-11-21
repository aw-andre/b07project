package com.example.b07_group_project.b07_group_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.b07_group_project.R;

public class settings extends AppCompatActivity {

    private EditText rapidRescueThreshold;
    private EditText lowCanisterThreshold;
    private EditText techniqueSessionsThreshold;
    private EditText lowRescueMonthThreshold;
    private Button saveSettingsButton;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "SmartAirSettings";
    private static final String KEY_RAPID_RESCUE = "rapidRescueThreshold";
    private static final String KEY_LOW_CANISTER = "lowCanisterThreshold";
    private static final String KEY_TECHNIQUE_SESSIONS = "techniqueSessionsThreshold";
    private static final String KEY_LOW_RESCUE_MONTH = "lowRescueMonthThreshold";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        rapidRescueThreshold = findViewById(R.id.rapidRescueThreshold);
        lowCanisterThreshold = findViewById(R.id.lowCanisterThreshold);
        techniqueSessionsThreshold = findViewById(R.id.techniqueSessionsThreshold);
        lowRescueMonthThreshold = findViewById(R.id.lowRescueMonthThreshold);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        loadSettings();

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void loadSettings() {
        // Load default values or saved preferences
        rapidRescueThreshold.setText(String.valueOf(sharedPreferences.getInt(KEY_RAPID_RESCUE, 3)));
        lowCanisterThreshold.setText(String.valueOf(sharedPreferences.getInt(KEY_LOW_CANISTER, 20)));
        techniqueSessionsThreshold.setText(String.valueOf(sharedPreferences.getInt(KEY_TECHNIQUE_SESSIONS, 10)));
        lowRescueMonthThreshold.setText(String.valueOf(sharedPreferences.getInt(KEY_LOW_RESCUE_MONTH, 4)));
    }

    private void saveSettings() {
        try {
            int rapidRescue = Integer.parseInt(rapidRescueThreshold.getText().toString().trim());
            int lowCanister = Integer.parseInt(lowCanisterThreshold.getText().toString().trim());
            int techniqueSessions = Integer.parseInt(techniqueSessionsThreshold.getText().toString().trim());
            int lowRescueMonth = Integer.parseInt(lowRescueMonthThreshold.getText().toString().trim());

            // Validate ranges
            if (rapidRescue < 1 || rapidRescue > 10) {
                Toast.makeText(this, "Rapid rescue threshold must be between 1 and 10", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lowCanister < 0 || lowCanister > 100) {
                Toast.makeText(this, "Low canister threshold must be between 0 and 100", Toast.LENGTH_SHORT).show();
                return;
            }

            if (techniqueSessions < 1 || techniqueSessions > 100) {
                Toast.makeText(this, "Technique sessions threshold must be between 1 and 100", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lowRescueMonth < 0 || lowRescueMonth > 30) {
                Toast.makeText(this, "Low rescue month threshold must be between 0 and 30", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_RAPID_RESCUE, rapidRescue);
            editor.putInt(KEY_LOW_CANISTER, lowCanister);
            editor.putInt(KEY_TECHNIQUE_SESSIONS, techniqueSessions);
            editor.putInt(KEY_LOW_RESCUE_MONTH, lowRescueMonth);
            editor.apply();

            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
