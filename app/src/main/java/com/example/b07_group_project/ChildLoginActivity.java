package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChildLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);
        setupUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure UI and listener are always attached when coming back
        setupUi();
    }

    private void setupUi() {
        Button loginButton = findViewById(R.id.btnChildLogin);
        if (loginButton == null) return; // defensive: if wrong layout ever used

        loginButton.setOnClickListener(v -> {
            boolean loginOk = true; // TODO: real validation

            if (loginOk) {
                Intent intent = new Intent(ChildLoginActivity.this, childUserInterfaceHome.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(
                        ChildLoginActivity.this,
                        "Invalid child login",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}



