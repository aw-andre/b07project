package com.example.b07_group_project;

import static android.widget.VideoView.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;
import android.widget.Button;
import android.widget.CheckBox;

public class techniqueHelper extends AppCompatActivity {

    private CheckBox checkBox, checkBox3, checkBox4, checkBox5, checkBox8, checkBox9;
    private Button submitButton;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.techniquehelper);

        ImageButton button = findViewById(R.id.imageButton4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(techniqueHelper.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });

        VideoView videoView = findViewById(R.id.videoView3);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(findViewById(R.id.scrollView2));
        mediaController.setMediaPlayer(videoView);

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.asthma_video));
        videoView.start();

        checkBox = findViewById(R.id.checkBox);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);
        checkBox5 = findViewById(R.id.checkBox5);
        checkBox8 = findViewById(R.id.checkBox8);
        checkBox9 = findViewById(R.id.checkBox9);
        submitButton = findViewById(R.id.button6);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTechniqueDataToFirebase();
            }
        });
    }

    private void sendTechniqueDataToFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(techniqueHelper.this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("children").child(userId).child("techniqueChecklist");
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
                .addOnSuccessListener(aVoid -> Toast.makeText(techniqueHelper.this, "Checklist submitted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(techniqueHelper.this, "Failed to submit checklist: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
