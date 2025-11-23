package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class parentUserInterfaceHome extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.parentuserinterfacehome);

        auth = FirebaseAuth.getInstance();

        // Required security: protect this page
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonTwo = findViewById(R.id.button2);
        buttonTwo.setOnClickListener(v -> {
            startActivity(new Intent(this, medicationManagement.class));
        });

        Button buttonThree = findViewById(R.id.button3);
        buttonThree.setOnClickListener(v -> {
            startActivity(new Intent(this, symptomReports.class));
        });

        Button buttonFour = findViewById(R.id.button4);
        buttonFour.setOnClickListener(v -> {
            startActivity(new Intent(this, emergencyContacts.class));
        });

        Button buttonFive = findViewById(R.id.button5);
        buttonFive.setOnClickListener(v -> {
            startActivity(new Intent(this, settings.class));
        });

        Button buttonSix = findViewById(R.id.button6);
        buttonSix.setOnClickListener(v -> {
            startActivity(new Intent(this, manageChildren.class));
        });

        // 🔥 Add this to your XML:
        // <Button android:id="@+id/btnLogout" ... />
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
        // Mandatory: protected screen
        if (auth.getCurrentUser() == null) {
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


