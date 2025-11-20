package com.example.b07_group_project;

import static android.widget.VideoView.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class techniqueHelper extends AppCompatActivity {

    // Hardcoded child ID for demonstration
    private static final String HARDCODED_CHILD_ID = "test_child_123";

    private CheckBox checkShake, checkCap, checkExhale, checkPress, checkHold, checkExhaleSlow;
    private Button submitChecklistButton;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.techniquehelper);

        ImageButton backButton = findViewById(R.id.imageButton4);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(techniqueHelper.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });

        WebView webView = findViewById(R.id.webView);
        String video = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/Lx_e5nXfi5w?si=qXs3U5z1DgpxBTDY&amp;start=7\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>";
        webView.loadData(video, "text/html", "utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        checkShake = findViewById(R.id.checkBox);
        checkCap = findViewById(R.id.checkBox3);
        checkExhale = findViewById(R.id.checkBox4);
        checkPress = findViewById(R.id.checkBox5);
        checkHold = findViewById(R.id.checkBox8);
        checkExhaleSlow = findViewById(R.id.checkBox9);
        submitChecklistButton = findViewById(R.id.button6);

        submitChecklistButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChecklistDataToFirebase();
            }
        });
    }

    private void sendChecklistDataToFirebase() {
        DatabaseReference logRef = FirebaseDatabaseManager.getInstance()
                .getDatabaseReference()
                .child("children")
                .child(HARDCODED_CHILD_ID)
                .child("technique_checklist_logs");

        DatabaseReference newLogEntryRef = logRef.push();

        Map<String, Object> checklistData = new HashMap<>();
        checklistData.put("shookInhaler", checkShake.isChecked());
        checklistData.put("removedCap", checkCap.isChecked());
        checklistData.put("exhaledDeeply", checkExhale.isChecked());
        checklistData.put("pressedWhileInhaling", checkPress.isChecked());
        checklistData.put("heldBreath", checkHold.isChecked());
        checklistData.put("exhaledSlowly", checkExhaleSlow.isChecked());
        checklistData.put("timestamp", ServerValue.TIMESTAMP);

        newLogEntryRef.setValue(checklistData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(techniqueHelper.this, "Checklist submitted successfully!", Toast.LENGTH_SHORT).show();
                    checkShake.setChecked(false);
                    checkCap.setChecked(false);
                    checkExhale.setChecked(false);
                    checkPress.setChecked(false);
                    checkHold.setChecked(false);
                    checkExhaleSlow.setChecked(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(techniqueHelper.this, "Failed to submit checklist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
