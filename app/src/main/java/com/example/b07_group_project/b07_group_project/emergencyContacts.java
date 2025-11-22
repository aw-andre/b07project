package com.example.b07_group_project.b07_group_project;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.b07_group_project.R;

import java.util.ArrayList;
import java.util.List;

public class emergencyContacts extends AppCompatActivity {

    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private List<EmergencyContact> contactList;
    private TextView emptyStateText;
    private Button addContactButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergencycontacts);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contactList = new ArrayList<>();
        emptyStateText = findViewById(R.id.emptyStateText);
        addContactButton = findViewById(R.id.addContactButton);

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList);
        contactsRecyclerView.setAdapter(contactAdapter);

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContactDialog();
            }
        });

        // Add sample emergency contact
        contactList.add(new EmergencyContact("Emergency Services", "911", "Emergency"));
        contactAdapter.notifyItemInserted(0);
        updateEmptyState();
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_emergency_contact, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.contactNameInput);
        EditText phoneInput = dialogView.findViewById(R.id.contactPhoneInput);
        EditText relationInput = dialogView.findViewById(R.id.contactRelationInput);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                String relation = relationInput.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(emergencyContacts.this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                EmergencyContact contact = new EmergencyContact(name, phone, relation);
                contactList.add(contact);
                contactAdapter.notifyItemInserted(contactList.size() - 1);
                updateEmptyState();
                Toast.makeText(emergencyContacts.this, "Contact added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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

    private class EmergencyContact {
        String name;
        String phone;
        String relation;

        EmergencyContact(String name, String phone, String relation) {
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

            holder.callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contact.phone));
                    startActivity(intent);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditContactDialog(holder.getAdapterPosition());
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(emergencyContacts.this)
                            .setTitle("Delete Contact")
                            .setMessage("Are you sure you want to delete " + contact.name + "?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int pos = holder.getAdapterPosition();
                                    contacts.remove(pos);
                                    notifyItemRemoved(pos);
                                    updateEmptyState();
                                    Toast.makeText(emergencyContacts.this, "Contact deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }

        private void showEditContactDialog(int position) {
            EmergencyContact contact = contacts.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(emergencyContacts.this);
            View dialogView = LayoutInflater.from(emergencyContacts.this).inflate(R.layout.dialog_emergency_contact, null);
            builder.setView(dialogView);

            EditText nameInput = dialogView.findViewById(R.id.contactNameInput);
            EditText phoneInput = dialogView.findViewById(R.id.contactPhoneInput);
            EditText relationInput = dialogView.findViewById(R.id.contactRelationInput);

            nameInput.setText(contact.name);
            phoneInput.setText(contact.phone);
            relationInput.setText(contact.relation);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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
                    Toast.makeText(emergencyContacts.this, "Contact updated", Toast.LENGTH_SHORT).show();
                }
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
