package com.example.b07_group_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class techniqueHelper extends AppCompatActivity {

    private CheckBox checkBox, checkBox3, checkBox4, checkBox5, checkBox8, checkBox9;
    private Button submitButton;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.techniquehelper);

        // Back button
        ImageButton backButton = findViewById(R.id.imageButton4);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(techniqueHelper.this, childUserInterfaceHome.class);
            startActivity(intent);
        });

        // WebView for technique video / content
        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // TODO: replace this URL with the actual inhaler technique resource your team wants
        webView.loadUrl("https://www.youtube.com/results?search_query=how+to+use+an+inhaler+with+spacer");

        // Checklist UI
        checkBox = findViewById(R.id.checkBox);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        checkBox5 = findViewById(R.id.checkBox5);
        checkBox8 = findViewById(R.id.checkBox8);
        checkBox9 = findViewById(R.id.checkBox9);
        submitButton = findViewById(R.id.button6);

        submitButton.setOnClickListener(v -> sendTechniqueDataToFirebase());
    }

    private void sendTechniqueDataToFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(techniqueHelper.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(userId)
                .child("techniqueChecklist");

        DatabaseReference newSubmissionRef = databaseRef.push();

        Map<String, Object> checklistData = new HashMap<>();
        checklistData.put("shakeInhaler", checkBox.isChecked());
        checklistData.put("removeCap", checkBox3.isChecked());
        checklistData.put("exhaleDeeply", checkBox4.isChecked());
        checklistData.put("pressAndInhale", checkBox5.isChecked());
        checklistData.put("holdBreath", checkBox8.isChecked());
        checklistData.put("exhaleSlowly", checkBox9.isChecked());
        checklistData.put("timestamp", ServerValue.TIMESTAMP);

        newSubmissionRef.setValue(checklistData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(techniqueHelper.this, "Checklist submitted successfully!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(techniqueHelper.this, "Failed to submit checklist: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

