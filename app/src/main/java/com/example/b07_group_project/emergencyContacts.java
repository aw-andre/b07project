package com.example.b07_group_project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class emergencyContacts extends AppCompatActivity {

    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private List<EmergencyContact> contactList;
    private TextView emptyStateText;
    private Button addContactButton;

    private DatabaseReference childrenRef;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergencycontacts);

        //parentId = "parent123"; // Replace after integrating authentication
        parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (parentId == null) return;

        childrenRef = FirebaseDatabase.getInstance().getReference("children");

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> finish());

        contactList = new ArrayList<>();
        emptyStateText = findViewById(R.id.emptyStateText);
        addContactButton = findViewById(R.id.addContactButton);

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList);
        contactsRecyclerView.setAdapter(contactAdapter);

        addContactButton.setOnClickListener(v -> showAddContactDialog());

        loadEmergencyContactsFromFirstChild(); // load global list
    }

    /**
     * Load the emergencyContact list from ANY ONE child belonging to the parent.
     * Because the data is meant to be global, but duplicated per child.
     */
    private void loadEmergencyContactsFromFirstChild() {
        childrenRef.orderByChild("parentId").equalTo(parentId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        contactList.clear();

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            if (childSnap.hasChild("emergencyContact")) {
                                for (DataSnapshot ec : childSnap.child("emergencyContact").getChildren()) {
                                    EmergencyContact c = ec.getValue(EmergencyContact.class);
                                    contactList.add(c);
                                }
                            }
                            break; // load from first child only
                        }

                        contactAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_emergency_contact, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.contactNameInput);
        EditText phoneInput = dialogView.findViewById(R.id.contactPhoneInput);
        EditText relationInput = dialogView.findViewById(R.id.contactRelationInput);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String relation = relationInput.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            EmergencyContact contact = new EmergencyContact(name, phone, relation);

            contactList.add(contact);
            contactAdapter.notifyItemInserted(contactList.size() - 1);
            updateEmptyState();
            saveContactsGlobally();

            Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /** Writes the SAME emergency contact list to every child under the parent */
    private void saveContactsGlobally() {
        childrenRef.orderByChild("parentId").equalTo(parentId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        HashMap<String, Object> ecMap = new HashMap<>();
                        for (int i = 0; i < contactList.size(); i++) {
                            ecMap.put(String.valueOf(i), contactList.get(i));
                        }

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            childSnap.getRef().child("emergencyContact").setValue(ecMap);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void updateEmptyState() {
        if (contactList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            contactsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // EmergencyContact model class MUST have empty constructor for Firebase
    public static class EmergencyContact {
        public String name;
        public String phone;
        public String relation;

        public EmergencyContact() { }

        public EmergencyContact(String name, String phone, String relation) {
            this.name = name;
            this.phone = phone;
            this.relation = relation;
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
        private List<EmergencyContact> contacts;

        ContactAdapter(List<EmergencyContact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emergency_contact, parent, false);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {
            EmergencyContact contact = contacts.get(position);
            holder.nameText.setText(contact.name);
            holder.phoneText.setText("Phone: " + contact.phone);
            holder.relationText.setText("Relation: " + (contact.relation.isEmpty() ? "Not specified" : contact.relation));

            holder.callButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact.phone));
                startActivity(intent);
            });

            holder.editButton.setOnClickListener(v -> showEditContactDialog(holder.getAdapterPosition()));

            holder.deleteButton.setOnClickListener(v ->
                    new AlertDialog.Builder(emergencyContacts.this)
                            .setTitle("Delete Contact")
                            .setMessage("Delete " + contact.name + "?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                int pos = holder.getAdapterPosition();
                                contacts.remove(pos);
                                notifyItemRemoved(pos);
                                updateEmptyState();
                                saveContactsGlobally();
                                Toast.makeText(emergencyContacts.this, "Contact deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show()
            );
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        private void showEditContactDialog(int position) {
            EmergencyContact contact = contacts.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(emergencyContacts.this);
            View dialogView = LayoutInflater.from(emergencyContacts.this)
                    .inflate(R.layout.dialog_emergency_contact, null);
            builder.setView(dialogView);

            EditText nameInput = dialogView.findViewById(R.id.contactNameInput);
            EditText phoneInput = dialogView.findViewById(R.id.contactPhoneInput);
            EditText relationInput = dialogView.findViewById(R.id.contactRelationInput);

            nameInput.setText(contact.name);
            phoneInput.setText(contact.phone);
            relationInput.setText(contact.relation);

            builder.setPositiveButton("Save", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                String relation = relationInput.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(emergencyContacts.this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                contact.name = name;
                contact.phone = phone;
                contact.relation = relation;

                notifyItemChanged(position);
                saveContactsGlobally();

                Toast.makeText(emergencyContacts.this, "Contact updated", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView phoneText;
            TextView relationText;
            Button callButton;
            Button editButton;
            Button deleteButton;

            ContactViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.contactNameText);
                phoneText = itemView.findViewById(R.id.contactPhoneText);
                relationText = itemView.findViewById(R.id.contactRelationText);
                callButton = itemView.findViewById(R.id.callButton);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
