package com.example.b07_group_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class techniqueHelper extends AppCompatActivity {

    private CheckBox checkBox, checkBox3, checkBox4, checkBox5, checkBox8, checkBox9;
    private Button submitButton;

    private String CHILD_ID;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.techniquehelper);

        CHILD_ID = getIntent().getStringExtra("childId");
        if (CHILD_ID == null || CHILD_ID.isEmpty()) {
            Toast.makeText(this, "Child ID missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton button = findViewById(R.id.imageButton4);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(techniqueHelper.this, childUserInterfaceHome.class);
            intent.putExtra("childId", CHILD_ID);
            startActivity(intent);
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
        submitButton.setOnClickListener(v -> sendTechniqueDataToFirebase());
    }

    private void sendTechniqueDataToFirebase() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(CHILD_ID)
                .child("logs")
                .child("techniqueSession");

        DatabaseReference newRef = ref.push();

        boolean shake = checkBox.isChecked();
        boolean cap = checkBox3.isChecked();
        boolean exhale = checkBox4.isChecked();
        boolean press = checkBox5.isChecked();
        boolean hold = checkBox8.isChecked();
        boolean slow = checkBox9.isChecked();

        boolean allCorrect = shake && cap && exhale && press && hold && slow;

        Map<String, Object> data = new HashMap<>();
        data.put("shakeInhaler", shake);
        data.put("removeCap", cap);
        data.put("exhaleDeeply", exhale);
        data.put("pressAndInhale", press);
        data.put("holdBreath", hold);
        data.put("exhaleSlowly", slow);
        data.put("allStepsCorrect", allCorrect);
        data.put("timestamp", ServerValue.TIMESTAMP);

        newRef.setValue(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(techniqueHelper.this, "Checklist submitted successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(techniqueHelper.this, "Failed to submit checklist: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
