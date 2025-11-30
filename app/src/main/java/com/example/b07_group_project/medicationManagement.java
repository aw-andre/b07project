package com.example.b07_group_project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class medicationManagement extends AppCompatActivity {

    public static final String EXTRA_SELECTED_CHILD_ID = "selectedChildId";
    //private static final String PARENT_ID = "parent123";
    private static final String PARENT_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private Spinner childSelectorSpinner;
    private TextView childSelectionHint;

    private TextView rescueMedName;
    private TextView rescueMedAmount;
    private TextView rescueMedExpiry;
    private TextView rescueMedPurchase;
    private Button editRescueButton;

    private TextView controllerMedName;
    private TextView controllerMedAmount;
    private TextView controllerMedExpiry;
    private TextView controllerMedPurchase;
    private Button editControllerButton;

    private final List<Child> children = new ArrayList<>();
    private final List<String> childLabels = new ArrayList<>();
    private ArrayAdapter<String> childAdapter;

    private DatabaseReference childrenRef;
    private DatabaseReference inventoryRef;
    private DatabaseReference activeInventoryRef;
    private ValueEventListener inventoryListener;
    private ValueEventListener childrenListener;

    private Medication rescueMed = new Medication("Rescue Inhaler", null, null, null, null);
    private Medication controllerMed = new Medication("Controller Medication", null, null, null, null);

    private String selectedChildId;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationmanagement);

        ImageButton backButton = findViewById(R.id.imageButton1);
        backButton.setOnClickListener(v -> finish());

        bindViews();
        setupSpinner();
        wireButtons();

        DatabaseReference root = FirebaseDatabaseManager
                .getInstance()
                .getDatabaseReference();

        childrenRef = root.child("children");
        inventoryRef = root.child("inventory");

        loadChildren();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachInventoryListener();
        if (childrenListener != null) {
            childrenRef.removeEventListener(childrenListener);
            childrenListener = null;
        }
    }

    private void bindViews() {
        childSelectorSpinner = findViewById(R.id.childSelectorSpinner);
        childSelectionHint = findViewById(R.id.childSelectionHint);

        rescueMedName = findViewById(R.id.rescueMedName);
        rescueMedAmount = findViewById(R.id.rescueMedAmount);
        rescueMedExpiry = findViewById(R.id.rescueMedExpiry);
        rescueMedPurchase = findViewById(R.id.rescueMedPurchase);
        editRescueButton = findViewById(R.id.editRescueButton);

        controllerMedName = findViewById(R.id.controllerMedName);
        controllerMedAmount = findViewById(R.id.controllerMedAmount);
        controllerMedExpiry = findViewById(R.id.controllerMedExpiry);
        controllerMedPurchase = findViewById(R.id.controllerMedPurchase);
        editControllerButton = findViewById(R.id.editControllerButton);
    }

    private void setupSpinner() {
        childLabels.add("Select Child");
        childAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                childLabels
        );
        childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSelectorSpinner.setAdapter(childAdapter);

        childSelectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedChildId = null;
                    childSelectionHint.setText("Choose a child to view medication details.");
                    detachInventoryListener();
                    clearMedicationDisplay();
                    return;
                }

                int index = position - 1;
                if (index >= 0 && index < children.size()) {
                    Child child = children.get(index);
                    selectedChildId = child.childId;
                    String name = child.name != null ? child.name : "Child";
                    childSelectionHint.setText("Managing medications for " + name);
                    attachInventoryListener();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void wireButtons() {
        editRescueButton.setOnClickListener(v -> showMedicationDialog(true));
        editControllerButton.setOnClickListener(v -> showMedicationDialog(false));
    }

    private void loadChildren() {
        final String defaultChildId = getIntent().getStringExtra(EXTRA_SELECTED_CHILD_ID);

        childrenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                children.clear();
                childLabels.clear();
                childLabels.add("Select Child");

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    Child c = childSnap.getValue(Child.class);
                    if (c == null) continue;
                    c.childId = childSnap.getKey();
                    children.add(c);
                    childLabels.add(c.name != null ? c.name : "Child");
                }

                childAdapter.notifyDataSetChanged();

                if (defaultChildId != null && selectedChildId == null) {
                    selectChildById(defaultChildId);
                } else {
                    restoreSelection();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        medicationManagement.this,
                        "Failed to load children",
                        Toast.LENGTH_SHORT
                ).show();
            }
        };

        childrenRef.orderByChild("parentId")
                .equalTo(PARENT_ID)
                .addListenerForSingleValueEvent(childrenListener);
    }

    private void selectChildById(String childId) {
        for (int i = 0; i < children.size(); i++) {
            if (childId.equals(children.get(i).childId)) {
                childSelectorSpinner.setSelection(i + 1);
                return;
            }
        }
        childSelectorSpinner.setSelection(0);
    }

    private void restoreSelection() {
        if (selectedChildId == null) {
            childSelectorSpinner.setSelection(0);
            return;
        }
        selectChildById(selectedChildId);
    }

    private void attachInventoryListener() {
        detachInventoryListener();
        if (selectedChildId == null) return;

        activeInventoryRef = inventoryRef.child(selectedChildId);

        inventoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Medication rescue = snapshot.child("rescue").getValue(Medication.class);
                Medication controller = snapshot.child("controller").getValue(Medication.class);

                if (rescue == null) {
                    rescue = new Medication("Rescue Inhaler", null, null, null, null);
                }
                if (controller == null) {
                    controller = new Medication("Controller Medication", null, null, null, null);
                }

                rescueMed = rescue;
                controllerMed = controller;
                updateMedicationDisplay();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        medicationManagement.this,
                        "Failed to load medications",
                        Toast.LENGTH_SHORT
                ).show();
            }
        };

        activeInventoryRef.addListenerForSingleValueEvent(inventoryListener);
    }

    private void detachInventoryListener() {
        if (activeInventoryRef != null && inventoryListener != null) {
            activeInventoryRef.removeEventListener(inventoryListener);
        }
        activeInventoryRef = null;
        inventoryListener = null;
    }

    private void showMedicationDialog(boolean isRescue) {
        if (selectedChildId == null) {
            Toast.makeText(this, "Select a child first", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_medication, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.medNameInput);
        EditText purchaseDateInput = dialogView.findViewById(R.id.purchaseDateInput);
        EditText amountLeftInput = dialogView.findViewById(R.id.amountLeftInput);
        EditText expiryDateInput = dialogView.findViewById(R.id.expiryDateInput);

        Medication med = isRescue ? rescueMed : controllerMed;

        if (med.name != null) nameInput.setText(med.name);
        if (med.purchaseDate != null && med.purchaseDate > 0)
            purchaseDateInput.setText(dateFormat.format(new Date(med.purchaseDate)));
        if (med.amountLeft != null)
            amountLeftInput.setText(String.valueOf(med.amountLeft));
        if (med.expiryDate != null && med.expiryDate > 0)
            expiryDateInput.setText(dateFormat.format(new Date(med.expiryDate)));

        purchaseDateInput.setOnClickListener(v ->
                purchaseDateInput.setText(dateFormat.format(new Date())));

        expiryDateInput.setOnClickListener(v ->
                expiryDateInput.setText(dateFormat.format(new Date())));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String purchaseStr = purchaseDateInput.getText().toString().trim();
            String amountStr = amountLeftInput.getText().toString().trim();
            String expiryStr = expiryDateInput.getText().toString().trim();
            saveMedication(isRescue, name, purchaseStr, amountStr, expiryStr);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveMedication(boolean isRescue,
                                String name,
                                String purchaseDateStr,
                                String amountStr,
                                String expiryStr) {

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medication name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount left in doses", Toast.LENGTH_SHORT).show();
            return;
        }
        if (activeInventoryRef == null) return;

        Long amountLeft;
        try {
            amountLeft = Long.parseLong(amountStr);
            if (amountLeft < 0) {
                Toast.makeText(this, "Amount must be non-negative", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        Long purchaseDate = parseDateOrNull(purchaseDateStr);
        Long expiryDate = parseDateOrNull(expiryStr);
        Long initialAmount = amountLeft;

        Medication med = new Medication(name, purchaseDate, amountLeft, initialAmount, expiryDate);
        String key = isRescue ? "rescue" : "controller";

        activeInventoryRef.child(key)
                .setValue(med)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Medication saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private Long parseDateOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            Date d = dateFormat.parse(s);
            return d != null ? d.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }

    private void clearMedicationDisplay() {
        rescueMedName.setText("Name: --");
        rescueMedAmount.setText("Amount Left: -- doses");
        rescueMedAmount.setTextColor(getResources().getColor(android.R.color.black));
        rescueMedExpiry.setText("Expiry: --");
        rescueMedPurchase.setText("Purchased: --");

        controllerMedName.setText("Name: --");
        controllerMedAmount.setText("Amount Left: -- doses");
        controllerMedAmount.setTextColor(getResources().getColor(android.R.color.black));
        controllerMedExpiry.setText("Expiry: --");
        controllerMedPurchase.setText("Purchased: --");
    }

    private void updateMedicationDisplay() {
        setMedicationBlock(
                rescueMedName,
                rescueMedAmount,
                rescueMedExpiry,
                rescueMedPurchase,
                rescueMed
        );
        setMedicationBlock(
                controllerMedName,
                controllerMedAmount,
                controllerMedExpiry,
                controllerMedPurchase,
                controllerMed
        );
    }

    private void setMedicationBlock(TextView nameView,
                                    TextView amountView,
                                    TextView expiryView,
                                    TextView purchaseView,
                                    Medication med) {

        if (med.name == null || med.name.isEmpty()) {
            nameView.setText("Name: --");
        } else {
            nameView.setText("Name: " + med.name);
        }

        if (med.amountLeft == null) {
            amountView.setText("Amount Left: -- doses");
            amountView.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            long left = med.amountLeft;
            amountView.setText("Amount Left: " + left + " doses");

            if (med.initialAmount != null && med.initialAmount > 0) {
                double percent = (left * 100.0) / med.initialAmount;
                if (percent <= 20.0) {
                    amountView.setTextColor(
                            getResources().getColor(android.R.color.holo_red_dark));
                } else if (percent <= 40.0) {
                    amountView.setTextColor(
                            getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    amountView.setTextColor(
                            getResources().getColor(android.R.color.black));
                }
            } else {
                amountView.setTextColor(
                        getResources().getColor(android.R.color.black));
            }
        }

        if (med.expiryDate == null || med.expiryDate <= 0) {
            expiryView.setText("Expiry: --");
        } else {
            expiryView.setText("Expiry: " +
                    dateFormat.format(new Date(med.expiryDate)));
        }

        if (med.purchaseDate == null || med.purchaseDate <= 0) {
            purchaseView.setText("Purchased: --");
        } else {
            purchaseView.setText("Purchased: " +
                    dateFormat.format(new Date(med.purchaseDate)));
        }
    }

    public static class Medication {
        public String name;
        public Long purchaseDate;
        public Long amountLeft;
        public Long initialAmount;
        public Long expiryDate;

        public Medication() {
        }

        public Medication(String name,
                          Long purchaseDate,
                          Long amountLeft,
                          Long initialAmount,
                          Long expiryDate) {
            this.name = name;
            this.purchaseDate = purchaseDate;
            this.amountLeft = amountLeft;
            this.initialAmount = initialAmount;
            this.expiryDate = expiryDate;
        }
    }

    private static class Child {
        public String childId;
        public String parentId;
        public String name;

        public Child() {
        }
    }
}
