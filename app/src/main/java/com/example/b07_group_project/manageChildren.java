package com.example.b07_group_project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.b07_group_project.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class manageChildren extends AppCompatActivity {

    private String parentId = "parent123";

    private DatabaseReference childrenRef;
    private DatabaseReference shareSettingsRef;

    private RecyclerView childrenRecyclerView;
    private ChildAdapter childAdapter;
    private List<Child> childrenList;

    private TextView emptyStateText;
    private Button addChildButton;
    private final List<ListenerRecord> activeListeners = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managechildren);

        childrenRef = FirebaseDatabase.getInstance().getReference("children");
        shareSettingsRef = FirebaseDatabase.getInstance().getReference("shareSettings");

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> finish());

        childrenList = new ArrayList<>();
        emptyStateText = findViewById(R.id.emptyStateText);
        addChildButton = findViewById(R.id.addChildButton);

        childrenRecyclerView = findViewById(R.id.childrenRecyclerView);
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childAdapter = new ChildAdapter(childrenList);
        childrenRecyclerView.setAdapter(childAdapter);

        addChildButton.setOnClickListener(v -> showAddChildDialog());

        loadChildrenFromFirebase();
    }

    private void loadChildrenFromFirebase() {

        DatabaseReference ref = childrenRef;

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childrenList.clear();

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    Child child = childSnap.getValue(Child.class);
                    if (child == null) continue;

                    if (child.parentId == null) continue;
                    if (!parentId.equals(child.parentId)) continue;

                    child.childId = childSnap.getKey();
                    childrenList.add(child);
                }

                childAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(manageChildren.this,
                        "Failed to load children.", Toast.LENGTH_SHORT).show();
            }
        };

        trackListener(ref, listener);
        ref.addValueEventListener(listener);
    }

    private void updateEmptyState() {
        if (childrenList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            childrenRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            childrenRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.childNameInput);
        EditText dobInput = dialogView.findViewById(R.id.childDobInput);
        EditText notesInput = dialogView.findViewById(R.id.childNotesInput);
        EditText pbInput = dialogView.findViewById(R.id.childPbInput);

        dobInput.setOnClickListener(v ->
                dobInput.setText(new SimpleDateFormat("yyyy-MM-dd",
                        Locale.getDefault()).format(new Date()))
        );

        builder.setPositiveButton("Save", (dialog, which) -> {

            String name = nameInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim();
            String notes = notesInput.getText().toString().trim();
            Integer pb = parseInteger(pbInput.getText().toString().trim());

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            String childId = childrenRef.push().getKey();
            if (childId == null) return;

            Child child = new Child(childId, parentId, name, dob, notes, pb);

            childrenRef.child(childId).setValue(child)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Child added", Toast.LENGTH_SHORT).show();
                        createDefaultShareAndBadgeSettings(childId);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void createDefaultShareAndBadgeSettings(String childId) {

        ShareSettings defaults = new ShareSettings(
                false, false, false,
                false, false, false, false
        );

        shareSettingsRef.child(childId).setValue(defaults)
                .addOnFailureListener(e ->
                        Toast.makeText(manageChildren.this,
                                "Failed to create share settings: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );

        DatabaseReference badgeRef =
                FirebaseDatabase.getInstance()
                        .getReference("children")
                        .child(childId)
                        .child("badgeSettings");

        badgeRef.child("techniqueSessionsThreshold").setValue(10);
        badgeRef.child("lowRescueMonthThreshold").setValue(4);
    }

    private void showEditChildDialog(Child child) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.childNameInput);
        EditText dobInput = dialogView.findViewById(R.id.childDobInput);
        EditText notesInput = dialogView.findViewById(R.id.childNotesInput);
        EditText pbInput = dialogView.findViewById(R.id.childPbInput);

        nameInput.setText(child.name);
        dobInput.setText(child.dob);
        notesInput.setText(child.notes);
        pbInput.setText(child.pb != null ? String.valueOf(child.pb) : "");

        dobInput.setOnClickListener(v ->
                dobInput.setText(new SimpleDateFormat("yyyy-MM-dd",
                        Locale.getDefault()).format(new Date()))
        );

        builder.setPositiveButton("Save", (dialog, which) -> {

            if (!parentId.equals(child.parentId)) {
                Toast.makeText(manageChildren.this,
                        "Access denied", Toast.LENGTH_SHORT).show();
                return;
            }

            childrenRef.child(child.childId).child("name")
                    .setValue(nameInput.getText().toString().trim());

            childrenRef.child(child.childId).child("dob")
                    .setValue(dobInput.getText().toString().trim());

            childrenRef.child(child.childId).child("notes")
                    .setValue(notesInput.getText().toString().trim());

            Integer updatedPb = parseInteger(pbInput.getText().toString().trim());
            childrenRef.child(child.childId).child("pb")
                    .setValue(updatedPb);

            Toast.makeText(manageChildren.this,
                    "Child updated", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class Child {
        public String childId;
        public String parentId;
        public String name;
        public String dob;
        public String notes;
        public Integer pb;

        public Child() {}

        public Child(String childId, String parentId, String name,
                     String dob, String notes, Integer pb) {
            this.childId = childId;
            this.parentId = parentId;
            this.name = name;
            this.dob = dob;
            this.notes = notes;
            this.pb = pb;
        }
    }

    private static class ShareSettings {
        public boolean charts;
        public boolean controller;
        public boolean pef;
        public boolean rescue;
        public boolean symptoms;
        public boolean triage;
        public boolean triggers;

        public ShareSettings() {}

        public ShareSettings(
                boolean charts, boolean controller, boolean pef,
                boolean rescue, boolean symptoms, boolean triage, boolean triggers) {

            this.charts = charts;
            this.controller = controller;
            this.pef = pef;
            this.rescue = rescue;
            this.symptoms = symptoms;
            this.triage = triage;
            this.triggers = triggers;
        }
    }

    private class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

        private final List<Child> children;

        ChildAdapter(List<Child> children) {
            this.children = children;
        }

        @NonNull
        @Override
        public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_child, parent, false);
            return new ChildViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {

            Child child = children.get(position);
            holder.binding = true;

            holder.nameText.setText(child.name != null ? child.name : "Unnamed");
            holder.ageText.setText(
                    child.dob != null && !child.dob.isEmpty()
                            ? "DOB: " + child.dob
                            : "DOB: Not set"
            );
            holder.notesText.setText(
                    child.notes != null ? "Notes: " + child.notes : "Notes: None"
            );

            if (child.pb != null && child.pb > 0) {
                holder.pbText.setText("PB: " + child.pb + " L/min");
            } else {
                holder.pbText.setText("PB: Not set");
            }

            DatabaseReference ref = shareSettingsRef.child(child.childId);

            ValueEventListener l = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ShareSettings s = snapshot.getValue(ShareSettings.class);
                    if (s == null) {
                        holder.binding = false;
                        return;
                    }

                    holder.binding = true;

                    holder.shareRescueLogs.setChecked(s.rescue);
                    holder.shareControllerAdherence.setChecked(s.controller);
                    holder.shareSymptoms.setChecked(s.symptoms);
                    holder.shareTriggers.setChecked(s.triggers);
                    holder.sharePEF.setChecked(s.pef);
                    holder.shareTriageIncidents.setChecked(s.triage);
                    holder.shareSummaryCharts.setChecked(s.charts);

                    holder.binding = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };

            trackListener(ref, l);
            ref.addListenerForSingleValueEvent(l);

            setToggleListeners(holder, child);
            holder.binding = false;

            holder.editButton.setOnClickListener(v -> showEditChildDialog(child));

            holder.deleteButton.setOnClickListener(v -> {
                if (!parentId.equals(child.parentId)) {
                    Toast.makeText(manageChildren.this,
                            "Access denied", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(manageChildren.this)
                        .setTitle("Delete Child")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {

                            childrenRef.child(child.childId).removeValue();
                            shareSettingsRef.child(child.childId).removeValue();

                            FirebaseDatabase.getInstance().getReference("children")
                                    .child(child.childId)
                                    .child("badgeSettings")
                                    .removeValue();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return children.size();
        }

        private void setToggleListeners(ChildViewHolder h, Child child) {

            CompoundButton.OnCheckedChangeListener listener = (button, isChecked) -> {

                if (h.binding) return;

                String field = null;

                if (button == h.shareRescueLogs) field = "rescue";
                else if (button == h.shareControllerAdherence) field = "controller";
                else if (button == h.shareSymptoms) field = "symptoms";
                else if (button == h.shareTriggers) field = "triggers";
                else if (button == h.sharePEF) field = "pef";
                else if (button == h.shareTriageIncidents) field = "triage";
                else if (button == h.shareSummaryCharts) field = "charts";

                if (field == null) return;

                shareSettingsRef.child(child.childId).child(field).setValue(isChecked);
            };

            h.shareRescueLogs.setOnCheckedChangeListener(listener);
            h.shareControllerAdherence.setOnCheckedChangeListener(listener);
            h.shareSymptoms.setOnCheckedChangeListener(listener);
            h.shareTriggers.setOnCheckedChangeListener(listener);
            h.sharePEF.setOnCheckedChangeListener(listener);
            h.shareTriageIncidents.setOnCheckedChangeListener(listener);
            h.shareSummaryCharts.setOnCheckedChangeListener(listener);
        }

        class ChildViewHolder extends RecyclerView.ViewHolder {

            TextView nameText, ageText, notesText, pbText;
            Button editButton, deleteButton;

            Switch shareRescueLogs;
            Switch shareControllerAdherence;
            Switch shareSymptoms;
            Switch shareTriggers;
            Switch sharePEF;
            Switch shareTriageIncidents;
            Switch shareSummaryCharts;

            boolean binding = false;

            ChildViewHolder(View itemView) {
                super(itemView);

                nameText = itemView.findViewById(R.id.childNameText);
                ageText = itemView.findViewById(R.id.childAgeText);
                notesText = itemView.findViewById(R.id.childNotesText);
                pbText = itemView.findViewById(R.id.childPbText);

                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);

                shareRescueLogs = itemView.findViewById(R.id.shareRescueLogs);
                shareControllerAdherence = itemView.findViewById(R.id.shareControllerAdherence);
                shareSymptoms = itemView.findViewById(R.id.shareSymptoms);
                shareTriggers = itemView.findViewById(R.id.shareTriggers);
                sharePEF = itemView.findViewById(R.id.sharePEF);
                shareTriageIncidents = itemView.findViewById(R.id.shareTriageIncidents);
                shareSummaryCharts = itemView.findViewById(R.id.shareSummaryCharts);
            }
        }
    }

    private static class ListenerRecord {
        final DatabaseReference ref;
        final ValueEventListener l;
        ListenerRecord(DatabaseReference ref, ValueEventListener l) {
            this.ref = ref;
            this.l = l;
        }
    }

    private void trackListener(DatabaseReference ref, ValueEventListener l) {
        activeListeners.add(new ListenerRecord(ref, l));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (ListenerRecord r : activeListeners) {
            r.ref.removeEventListener(r.l);
        }

        activeListeners.clear();
    }
}
