package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ParentLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);
        setupUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUi();
    }

    private void setupUi() {
        Button loginButton = findViewById(R.id.btnParentLogin);
        if (loginButton == null) return;

        loginButton.setOnClickListener(v -> {
            boolean loginOk = true; // TODO: validation

            if (loginOk) {
                Intent intent = new Intent(ParentLoginActivity.this, ParentHomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(
                        ParentLoginActivity.this,
                        "Invalid parent login",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}



