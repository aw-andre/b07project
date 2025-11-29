package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ChildLoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton, registerButton;
    private TextView forgotPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);

        auth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.btnChildLogin);
        registerButton = findViewById(R.id.btnChildRegister);
        forgotPassword = findViewById(R.id.textForgotPassword);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> openRegisterScreen());
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> sendPasswordReset());
        }
    }

    private void attemptLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty()) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordField.setError("Min 6 characters");
            passwordField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Intent i = new Intent(ChildLoginActivity.this, childUserInterfaceHome.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    } else {
                        String msg = (task.getException() != null && task.getException().getMessage() != null)
                                ? task.getException().getMessage()
                                : "Login failed";
                        Toast.makeText(ChildLoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendPasswordReset() {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            emailField.setError("Enter your email first");
            emailField.requestFocus();
            return;
        }
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Could not send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openRegisterScreen() {
        Intent i = new Intent(ChildLoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }
}







