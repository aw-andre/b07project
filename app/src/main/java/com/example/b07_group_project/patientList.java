package com.example.b07_group_project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class patientList extends AppCompatActivity {

    private static final String PROVIDER_ID = "provider789"; // To be replaced with actual provider ID

    private LinearLayout childContainer;
    private TextView emptyText;
    private Button addChildBtn;

    private DatabaseReference usersRef;
    private DatabaseReference childrenRef;
    private DatabaseReference invitesRef;

    private static final long CODE_VALIDITY_MS = 7L * 24L * 60L * 60L * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patientlist);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        childrenRef = FirebaseDatabase.getInstance().getReference("children");
        invitesRef = FirebaseDatabase.getInstance().getReference("invites");

        childContainer = findViewById(R.id.childContainer);
        emptyText = findViewById(R.id.emptyText);
        addChildBtn = findViewById(R.id.addChildBtn);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> finish());

        loadLinkedChildren();
        addChildBtn.setOnClickListener(v -> showInviteCodeDialog());
    }

    private void loadLinkedChildren() {
        usersRef.child(PROVIDER_ID).child("linkedChildren")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        childContainer.removeAllViews();
                        List<String> childIds = new ArrayList<>();

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            if (childSnap.getValue(Boolean.class)) {
                                childIds.add(childSnap.getKey());
                            }
                        }

                        if (childIds.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                        } else {
                            emptyText.setVisibility(View.GONE);
                        }

                        fetchChildProfiles(childIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void fetchChildProfiles(List<String> childIds) {
        for (String childId : childIds) {
            childrenRef.child(childId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) return;

                            String name = snapshot.child("name").getValue(String.class);
                            String dob = snapshot.child("dob").getValue(String.class);
                            String notes = snapshot.child("notes").getValue(String.class);
                            Long pbValue = snapshot.child("pb").getValue(Long.class);

                            addChildCard(
                                    snapshot.getKey(),
                                    name,
                                    dob,
                                    notes == null ? "—" : notes,
                                    pbValue == null ? "Not set" : pbValue.toString()
                            );
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
        }
    }

    private void addChildCard(String childId, String name, String dob, String notes, String pb) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_child, childContainer, false);

        TextView nameText = card.findViewById(R.id.childNameText);
        TextView ageText = card.findViewById(R.id.childAgeText);
        TextView notesText = card.findViewById(R.id.childNotesText);
        TextView pbText = card.findViewById(R.id.childPbText);

        nameText.setText(name);
        ageText.setText("DOB: " + dob);
        notesText.setText("Notes: " + notes);
        pbText.setText("PB: " + pb);

        card.findViewById(R.id.editButton).setVisibility(View.GONE);
        card.findViewById(R.id.deleteButton).setVisibility(View.GONE);
        card.findViewById(R.id.shareRescueLogs).setVisibility(View.GONE);
        card.findViewById(R.id.shareControllerAdherence).setVisibility(View.GONE);
        card.findViewById(R.id.shareSymptoms).setVisibility(View.GONE);
        card.findViewById(R.id.shareTriggers).setVisibility(View.GONE);
        card.findViewById(R.id.sharePEF).setVisibility(View.GONE);
        card.findViewById(R.id.shareTriageIncidents).setVisibility(View.GONE);
        card.findViewById(R.id.shareSummaryCharts).setVisibility(View.GONE);

        childContainer.addView(card);
    }

    private void showInviteCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_enter_invite_code, null);

        EditText codeInput = view.findViewById(R.id.inviteCodeInput);
        Button confirm = view.findViewById(R.id.confirmAddChild);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        confirm.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "Enter invite code", Toast.LENGTH_SHORT).show();
                return;
            }
            traceParentForInviteCode(code, dialog);
        });

        dialog.show();
    }

    private void traceParentForInviteCode(String code, AlertDialog dialog) {
        invitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot parentSnap : snapshot.getChildren()) {
                    String parentId = parentSnap.getKey();
                    String currentCode = parentSnap.child("currentCode").getValue(String.class);
                    Long updatedAt = parentSnap.child("updatedAt").getValue(Long.class);

                    if (currentCode == null || updatedAt == null) continue;
                    if (!currentCode.equals(code)) continue;

                    long now = System.currentTimeMillis();
                    if (now - updatedAt > CODE_VALIDITY_MS) {
                        Toast.makeText(patientList.this, "Invite code expired", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    attachChildrenFromParent(parentId, dialog);
                    return;
                }

                Toast.makeText(patientList.this, "Invalid invite code", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void attachChildrenFromParent(String parentId, AlertDialog dialog) {
        childrenRef.orderByChild("parentId").equalTo(parentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(patientList.this, "No children found for this parent", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            String childId = childSnap.getKey();
                            usersRef.child(PROVIDER_ID)
                                    .child("linkedChildren")
                                    .child(childId)
                                    .setValue(true);
                        }

                        Toast.makeText(patientList.this, "Patients added", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadLinkedChildren();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
