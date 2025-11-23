package com.example.b07_group_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private RadioGroup radioGroupRole;
    private RadioButton radioParent;
    private RadioButton radioProvider;
    private RadioButton radioChild;
    private Button buttonRegister;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioParent = findViewById(R.id.radioParent);
        radioProvider = findViewById(R.id.radioProvider);
        radioChild = findViewById(R.id.radioChild);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);

        // Preselect role based on the screen we came from (optional, but nicer UX)
        String preselectedRole = getIntent().getStringExtra("role");
        if (preselectedRole != null) {
            switch (preselectedRole) {
                case "parent":
                    radioParent.setChecked(true);
                    break;
                case "provider":
                    radioProvider.setChecked(true);
                    break;
                case "child":
                    radioChild.setChecked(true);
                    break;
                default:
                    // no-op; user can choose manually
                    break;
            }
        }

        buttonRegister.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String role = getSelectedRole();

        if (!validateForm(email, password, confirmPassword, role)) {
            return;
        }

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        setLoading(false);

                        if (task.isSuccessful()) {
                            // User created in Firebase Authentication
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration successful. You can now log in.",
                                    Toast.LENGTH_LONG
                            ).show();

                            // TODO (team): store role / profile in DB (Realtime DB or Firestore) using auth.getCurrentUser().getUid()

                            // For now, just go back to the previous screen (e.g., role-specific login)
                            finish();

                        } else {
                            String message = "Registration failed.";
                            if (task.getException() != null &&
                                    task.getException().getMessage() != null) {
                                message = task.getException().getMessage();
                            }
                            Toast.makeText(
                                    RegisterActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }

    private boolean validateForm(String email, String password, String confirmPassword, String role) {
        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        if (role == null) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getSelectedRole() {
        int checkedId = radioGroupRole.getCheckedRadioButtonId();
        if (checkedId == R.id.radioParent) {
            return "parent";
        } else if (checkedId == R.id.radioProvider) {
            return "provider";
        } else if (checkedId == R.id.radioChild) {
            return "child";
        } else {
            return null;
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);
        }
    }
}


