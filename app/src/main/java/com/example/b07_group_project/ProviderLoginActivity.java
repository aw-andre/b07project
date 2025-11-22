package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProviderLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_login);
        setupUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUi();
    }

    private void setupUi() {
        Button loginButton = findViewById(R.id.btnProviderLogin);
        if (loginButton == null) return;

        loginButton.setOnClickListener(v -> {
            boolean loginOk = true; // TODO: validation

            if (loginOk) {
                Intent intent = new Intent(ProviderLoginActivity.this, ProviderHomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(
                        ProviderLoginActivity.this,
                        "Invalid provider login",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}



