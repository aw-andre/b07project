package com.example.b07_group_project;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.b07_group_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class providerSharing extends AppCompatActivity {

    //private static final String PARENT_ID = "parent123";
    private static final String PARENT_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private TextView inviteCodeText;
    private TextView inviteCodeTimestamp;
    private TextView inviteHistoryText;
    private Button copyInviteButton;
    private Button generateInviteButton;

    private DatabaseReference inviteRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.providersharing);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> finish());

        inviteCodeText = findViewById(R.id.inviteCodeText);
        inviteCodeTimestamp = findViewById(R.id.inviteCodeTimestamp);
        inviteHistoryText = findViewById(R.id.inviteHistoryText);
        copyInviteButton = findViewById(R.id.copyInviteButton);
        generateInviteButton = findViewById(R.id.generateInviteButton);

        inviteRef = FirebaseDatabase.getInstance()
                .getReference("invites")
                .child(PARENT_ID);

        copyInviteButton.setOnClickListener(v -> copyInviteCode());
        generateInviteButton.setOnClickListener(v -> generateNewCode());

        loadInviteData();
    }

    private void loadInviteData() {
        inviteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String code = snapshot.child("currentCode").getValue(String.class);
                Long updatedAt = snapshot.child("updatedAt").getValue(Long.class);
                StringBuilder historyBuilder = new StringBuilder();

                for (DataSnapshot historySnap : snapshot.child("history").getChildren()) {
                    String historyCode = historySnap.child("code").getValue(String.class);
                    Long created = historySnap.child("createdAt").getValue(Long.class);
                    if (historyCode == null || created == null) continue;
                    historyBuilder.insert(0,
                            historyCode + " · " + formatTimestamp(created) + "\n");
                }

                if (code == null || code.isEmpty()) {
                    inviteCodeText.setText("??????");
                } else {
                    inviteCodeText.setText(code);
                }

                inviteCodeTimestamp.setText(updatedAt != null
                        ? "Last updated: " + formatTimestamp(updatedAt)
                        : "Last updated: --");

                if (historyBuilder.length() == 0) {
                    inviteHistoryText.setText("No invites generated yet.");
                } else {
                    inviteHistoryText.setText(historyBuilder.toString().trim());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(providerSharing.this,
                        "Failed to load invites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void copyInviteCode() {
        String code = inviteCodeText.getText().toString();
        if (code == null || code.trim().isEmpty() || code.contains("??")) {
            Toast.makeText(this, "No invite code to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Invite Code", code);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Invite code copied", Toast.LENGTH_SHORT).show();
    }

    private void generateNewCode() {
        String code = buildCode();
        long timestamp = System.currentTimeMillis();

        inviteRef.child("currentCode").setValue(code);
        inviteRef.child("updatedAt").setValue(timestamp);
        inviteRef.child("history").push()
                .setValue(new InviteHistory(code, timestamp));

        Toast.makeText(this, "New invite generated", Toast.LENGTH_SHORT).show();
    }

    private String buildCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    private String formatTimestamp(long timestamp) {
        return new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }

    private static class InviteHistory {
        public String code;
        public long createdAt;

        public InviteHistory() {
        }

        InviteHistory(String code, long createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }
    }
}

