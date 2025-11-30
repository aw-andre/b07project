package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class ChildLoginActivity extends AppCompatActivity {

    private EditText childIdField;
    private Button loginButton;

    private DatabaseReference childrenRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_login);

        childIdField = findViewById(R.id.editTextEmail); // Reuse existing ID
        loginButton = findViewById(R.id.btnChildLogin);

        childrenRef = FirebaseDatabase.getInstance().getReference("children");

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String childId = childIdField.getText().toString().trim();

        if (childId.isEmpty()) {
            childIdField.setError("ChildID is required");
            childIdField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);

        childrenRef.child(childId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        loginButton.setEnabled(true);

                        if (!snapshot.exists()) {
                            Toast.makeText(ChildLoginActivity.this,
                                    "Invalid ChildID", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Intent i = new Intent(ChildLoginActivity.this, childUserInterfaceHome.class);
                        i.putExtra("childId", childId);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loginButton.setEnabled(true);
                        Toast.makeText(ChildLoginActivity.this,
                                "Login failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}








