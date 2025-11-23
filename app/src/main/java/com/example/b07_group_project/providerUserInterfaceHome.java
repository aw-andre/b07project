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

public class providerUserInterfaceHome extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.provideruserinterfacehome);

        auth = FirebaseAuth.getInstance();

        // Protected screen
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.button).setOnClickListener(v ->
                startActivity(new Intent(this, patientList.class)));

        findViewById(R.id.button2).setOnClickListener(v ->
                startActivity(new Intent(this, treatmentPlans.class)));

        findViewById(R.id.button3).setOnClickListener(v ->
                startActivity(new Intent(this, progressReports.class)));

        findViewById(R.id.button4).setOnClickListener(v ->
                startActivity(new Intent(this, medicationPrescriptions.class)));

        findViewById(R.id.button5).setOnClickListener(v ->
                startActivity(new Intent(this, analyticsDashboard.class)));

        findViewById(R.id.button6).setOnClickListener(v ->
                startActivity(new Intent(this, providerReportView.class)));

        // Logout
        Button logout = findViewById(R.id.btnLogout);
        if (logout != null) {
            logout.setOnClickListener(v -> {
                auth.signOut();
                redirectToLogin();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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

