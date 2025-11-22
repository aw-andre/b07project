package com.example.b07_group_project.b07_group_project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.b07_group_project.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class medicationManagement extends AppCompatActivity {

    private TextView rescueMedName, rescueMedAmount, rescueMedExpiry;
    private TextView controllerMedName, controllerMedAmount, controllerMedExpiry;
    private Button editRescueButton, editControllerButton;

    private Medication rescueMed = new Medication("Rescue Inhaler", "", "", "");
    private Medication controllerMed = new Medication("Controller Medication", "", "", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationmanagement);

        ImageButton backButton = findViewById(R.id.imageButton1);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rescueMedName = findViewById(R.id.rescueMedName);
        rescueMedAmount = findViewById(R.id.rescueMedAmount);
        rescueMedExpiry = findViewById(R.id.rescueMedExpiry);
        editRescueButton = findViewById(R.id.editRescueButton);

        controllerMedName = findViewById(R.id.controllerMedName);
        controllerMedAmount = findViewById(R.id.controllerMedAmount);
        controllerMedExpiry = findViewById(R.id.controllerMedExpiry);
        editControllerButton = findViewById(R.id.editControllerButton);

        updateMedicationDisplay();

        editRescueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog(true);
            }
        });

        editControllerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMedicationDialog(false);
            }
        });
    }

    private void showMedicationDialog(boolean isRescue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_medication, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.medNameInput);
        EditText purchaseDateInput = dialogView.findViewById(R.id.purchaseDateInput);
        EditText amountLeftInput = dialogView.findViewById(R.id.amountLeftInput);
        EditText expiryDateInput = dialogView.findViewById(R.id.expiryDateInput);

        Medication med = isRescue ? rescueMed : controllerMed;
        nameInput.setText(med.name);
        purchaseDateInput.setText(med.purchaseDate);
        amountLeftInput.setText(med.amountLeft);
        expiryDateInput.setText(med.expiryDate);

        purchaseDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseDateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            }
        });

        expiryDateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expiryDateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameInput.getText().toString().trim();
                String purchaseDate = purchaseDateInput.getText().toString().trim();
                String amountLeft = amountLeftInput.getText().toString().trim();
                String expiryDate = expiryDateInput.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(medicationManagement.this, "Please enter medication name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isRescue) {
                    rescueMed = new Medication(name, purchaseDate, amountLeft, expiryDate);
                } else {
                    controllerMed = new Medication(name, purchaseDate, amountLeft, expiryDate);
                }

                updateMedicationDisplay();
                checkAlerts();
                Toast.makeText(medicationManagement.this, "Medication details saved", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateMedicationDisplay() {
        rescueMedName.setText(rescueMed.name);
        if (!rescueMed.amountLeft.isEmpty()) {
            try {
                rescueMedAmount.setText("Amount Left: " + rescueMed.amountLeft + "%");
                int amount = Integer.parseInt(rescueMed.amountLeft);
                if (amount <= 20) {
                    rescueMedAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    rescueMedAmount.setTextColor(getResources().getColor(android.R.color.black));
                }
            } catch (NumberFormatException e) {
                rescueMedAmount.setText("Amount Left: " + rescueMed.amountLeft);
            }
        } else {
            rescueMedAmount.setText("Amount Left: Not set");
            rescueMedAmount.setTextColor(getResources().getColor(android.R.color.black));
        }
        rescueMedExpiry.setText("Expiry: " + (rescueMed.expiryDate.isEmpty() ? "Not set" : rescueMed.expiryDate));

        controllerMedName.setText(controllerMed.name);
        if (!controllerMed.amountLeft.isEmpty()) {
            try {
                controllerMedAmount.setText("Amount Left: " + controllerMed.amountLeft + "%");
                int amount = Integer.parseInt(controllerMed.amountLeft);
                if (amount <= 20) {
                    controllerMedAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    controllerMedAmount.setTextColor(getResources().getColor(android.R.color.black));
                }
            } catch (NumberFormatException e) {
                controllerMedAmount.setText("Amount Left: " + controllerMed.amountLeft);
            }
        } else {
            controllerMedAmount.setText("Amount Left: Not set");
            controllerMedAmount.setTextColor(getResources().getColor(android.R.color.black));
        }
        controllerMedExpiry.setText("Expiry: " + (controllerMed.expiryDate.isEmpty() ? "Not set" : controllerMed.expiryDate));
    }

    private void checkAlerts() {
        // Check for low canister and expired medication alerts
        // In production, this would trigger notifications
    }

    private class Medication {
        String name;
        String purchaseDate;
        String amountLeft;
        String expiryDate;

        Medication(String name, String purchaseDate, String amountLeft, String expiryDate) {
            this.name = name;
            this.purchaseDate = purchaseDate;
            this.amountLeft = amountLeft;
            this.expiryDate = expiryDate;
        }
    }
}
