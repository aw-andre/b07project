package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class childUserInterfaceHome extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.childuserinterfacehome);

        auth = FirebaseAuth.getInstance();

        // Protect this screen: if user is not logged in, send them to LoginActivity
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation buttons
        Button techniqueHelperBtn = findViewById(R.id.button);
        Button medicationLogBtn   = findViewById(R.id.button2);
        Button symptomsBtn        = findViewById(R.id.button3);
        Button troubleBreathingBtn= findViewById(R.id.button4);
        Button badgesBtn          = findViewById(R.id.button5);
        Button logoutBtn          = findViewById(R.id.btnLogout); // must exist in XML

        techniqueHelperBtn.setOnClickListener(v ->
                startActivity(new Intent(this, techniqueHelper.class)));

        medicationLogBtn.setOnClickListener(v ->
                startActivity(new Intent(this, medicationLog.class)));

        symptomsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, checkInAndSymptoms.class)));

        troubleBreathingBtn.setOnClickListener(v ->
                startActivity(new Intent(this, troubleBreathing.class)));

        badgesBtn.setOnClickListener(v ->
                startActivity(new Intent(this, badges.class)));

        // Logout
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                auth.signOut();
                redirectToLogin();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Double-check protection when returning to this Activity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}

