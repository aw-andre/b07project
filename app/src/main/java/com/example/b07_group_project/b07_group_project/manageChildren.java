package com.example.b07_group_project.b07_group_project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.b07_group_project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class manageChildren extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private ChildAdapter childAdapter;
    private List<Child> childrenList;
    private TextView emptyStateText;
    private Button addChildButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managechildren);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        childrenList = new ArrayList<>();
        emptyStateText = findViewById(R.id.emptyStateText);
        addChildButton = findViewById(R.id.addChildButton);

        childrenRecyclerView = findViewById(R.id.childrenRecyclerView);
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childAdapter = new ChildAdapter(childrenList);
        childrenRecyclerView.setAdapter(childAdapter);

        addChildButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddChildDialog();
            }
        });

        updateEmptyState();
    }

    private void showAddChildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.childNameInput);
        EditText dobInput = dialogView.findViewById(R.id.childDobInput);
        EditText notesInput = dialogView.findViewById(R.id.childNotesInput);

        dobInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simple date picker - in production, use DatePickerDialog
                dobInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString().trim();
                String dob = dobInput.getText().toString().trim();
                String notes = notesInput.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(manageChildren.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                Child child = new Child(name, dob, notes);
                childrenList.add(child);
                childAdapter.notifyItemInserted(childrenList.size() - 1);
                updateEmptyState();
                Toast.makeText(manageChildren.this, "Child added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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

    private class Child {
        String name;
        String dob;
        String notes;

        Child(String name, String dob, String notes) {
            this.name = name;
            this.dob = dob;
            this.notes = notes;
        }
    }

    private class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {
        private List<Child> children;

        ChildAdapter(List<Child> children) {
            this.children = children;
        }

        @Override
        public ChildViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_child, parent, false);
            return new ChildViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChildViewHolder holder, int position) {
            Child child = children.get(position);
            holder.nameText.setText(child.name);
            
            if (!child.dob.isEmpty()) {
                holder.ageText.setText("DOB: " + child.dob);
            } else {
                holder.ageText.setText("DOB: Not set");
            }
            
            if (!child.notes.isEmpty()) {
                holder.notesText.setText("Notes: " + child.notes);
            } else {
                holder.notesText.setText("Notes: None");
            }

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditChildDialog(holder.getAdapterPosition());
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(manageChildren.this)
                            .setTitle("Delete Child")
                            .setMessage("Are you sure you want to delete " + child.name + "?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int pos = holder.getAdapterPosition();
                                    children.remove(pos);
                                    notifyItemRemoved(pos);
                                    updateEmptyState();
                                    Toast.makeText(manageChildren.this, "Child deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return children.size();
        }

        private void showEditChildDialog(int position) {
            Child child = children.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(manageChildren.this);
            View dialogView = LayoutInflater.from(manageChildren.this).inflate(R.layout.dialog_add_child, null);
            builder.setView(dialogView);

            EditText nameInput = dialogView.findViewById(R.id.childNameInput);
            EditText dobInput = dialogView.findViewById(R.id.childDobInput);
            EditText notesInput = dialogView.findViewById(R.id.childNotesInput);

            nameInput.setText(child.name);
            dobInput.setText(child.dob);
            notesInput.setText(child.notes);

            dobInput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dobInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                }
            });

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = nameInput.getText().toString().trim();
                    String dob = dobInput.getText().toString().trim();
                    String notes = notesInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(manageChildren.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    child.name = name;
                    child.dob = dob;
                    child.notes = notes;
                    notifyItemChanged(position);
                    Toast.makeText(manageChildren.this, "Child updated", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        class ChildViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView ageText;
            TextView notesText;
            Button editButton;
            Button deleteButton;

            ChildViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.childNameText);
                ageText = itemView.findViewById(R.id.childAgeText);
                notesText = itemView.findViewById(R.id.childNotesText);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
