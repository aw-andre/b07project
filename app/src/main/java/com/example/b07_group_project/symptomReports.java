package com.example.b07_group_project;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class symptomReports extends AppCompatActivity {

    public static final String EXTRA_SELECTED_CHILD_ID = "selectedChildId";
    //private static final String PARENT_ID = "parent123";
    private static final String PARENT_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private RecyclerView symptomRecyclerView;
    private SymptomReportAdapter reportAdapter;
    private TextView emptyStateText;
    private Spinner symptomFilterSpinner;
    private Spinner triggerFilterSpinner;
    private Spinner childFilterSpinner;
    private Button exportButton;
    private Button addReportButton;

    private final List<SymptomReport> allReports = new ArrayList<>();
    private final List<SymptomReport> visibleReports = new ArrayList<>();

    private final List<Child> children = new ArrayList<>();
    private final List<String> childLabels = new ArrayList<>();
    private ArrayAdapter<String> childFilterAdapter;

    private DatabaseReference childrenRef;
    private ValueEventListener childrenListener;

    private String selectedChildId = null;
    private String symptomFilter = "All Symptoms";
    private String triggerFilter = "All Triggers";

    private Long filterStartMillis = null;
    private Long filterEndMillis = null;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptomreports);

        ImageButton back = findViewById(R.id.imageButton);
        back.setOnClickListener(v -> finish());

        bindViews();
        setupSpinners();
        wireButtons();

        DatabaseReference root =
                FirebaseDatabaseManager.getInstance().getDatabaseReference();
        childrenRef = root.child("children");

        selectedChildId = getIntent().getStringExtra(EXTRA_SELECTED_CHILD_ID);

        attachChildrenListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childrenListener != null && childrenRef != null) {
            childrenRef.removeEventListener(childrenListener);
        }
    }

    private void bindViews() {
        emptyStateText = findViewById(R.id.emptyStateText);
        symptomFilterSpinner = findViewById(R.id.symptomFilterSpinner);
        triggerFilterSpinner = findViewById(R.id.triggerFilterSpinner);
        childFilterSpinner = findViewById(R.id.childFilterSpinner);
        exportButton = findViewById(R.id.exportButton);
        addReportButton = findViewById(R.id.addReportButton);

        symptomRecyclerView = findViewById(R.id.symptomRecyclerView);
        symptomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new SymptomReportAdapter(visibleReports);
        symptomRecyclerView.setAdapter(reportAdapter);
    }

    private void setupSpinners() {
        ArrayAdapter<String> symptomAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"All Symptoms", "Night Waking", "Activity Limits", "Cough/Wheeze"}
        );
        symptomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomFilterSpinner.setAdapter(symptomAdapter);
        symptomFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                symptomFilter = parent.getItemAtPosition(pos).toString();
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"All Triggers", "Exercise", "Cold Air", "Dust/Pets", "Smoke", "Illness", "Perfume/Cleaners"}
        );
        triggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triggerFilterSpinner.setAdapter(triggerAdapter);
        triggerFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                triggerFilter = parent.getItemAtPosition(pos).toString();
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        childLabels.clear();
        childLabels.add("All Children");
        childFilterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                childLabels
        );
        childFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childFilterSpinner.setAdapter(childFilterAdapter);
        childFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {
                    selectedChildId = null;
                } else {
                    int idx = pos - 1;
                    if (idx >= 0 && idx < children.size()) {
                        selectedChildId = children.get(idx).childId;
                    }
                }
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void wireButtons() {
        addReportButton.setOnClickListener(v -> {
            if (children.isEmpty()) {
                Toast.makeText(this, "Add a child first", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddReportDialog();
        });

        exportButton.setOnClickListener(v -> showExportRangeDialog());
    }

    private void attachChildrenListener() {
        childrenListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                rebuildFromChildrenSnapshot(snapshot);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(symptomReports.this,
                        "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        };
        childrenRef.addValueEventListener(childrenListener);
    }

    private void rebuildFromChildrenSnapshot(DataSnapshot snapshot) {
        children.clear();
        childLabels.clear();
        childLabels.add("All Children");
        allReports.clear();

        for (DataSnapshot childSnap : snapshot.getChildren()) {
            Child c = childSnap.getValue(Child.class);
            if (c == null) continue;
            if (!PARENT_ID.equals(c.parentId)) continue;

            c.childId = childSnap.getKey();
            children.add(c);
            childLabels.add(c.name != null ? c.name : "Child");

            DataSnapshot symptomSnap = childSnap
                    .child("logs")
                    .child("symptoms");

            for (DataSnapshot s : symptomSnap.getChildren()) {
                SymptomReport r = s.getValue(SymptomReport.class);
                if (r == null) continue;

                r.id = s.getKey();
                r.childId = c.childId;
                r.childName = c.name != null ? c.name : "Child";
                allReports.add(r);
            }
        }

        Collections.sort(allReports, (a, b) -> Long.compare(b.timestamp, a.timestamp));

        childFilterAdapter.notifyDataSetChanged();
        restoreChildSelection();
        applyFilters();
    }

    private void restoreChildSelection() {
        if (selectedChildId == null) {
            childFilterSpinner.setSelection(0);
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            if (selectedChildId.equals(children.get(i).childId)) {
                childFilterSpinner.setSelection(i + 1);
                return;
            }
        }
        childFilterSpinner.setSelection(0);
    }

    private void applyFilters() {
        visibleReports.clear();

        for (SymptomReport r : allReports) {
            if (selectedChildId != null && !selectedChildId.equals(r.childId)) continue;
            if (!"All Symptoms".equals(symptomFilter) && !r.matchesSymptom(symptomFilter)) continue;
            if (!"All Triggers".equals(triggerFilter) && !r.matchesTrigger(triggerFilter)) continue;

            if (filterStartMillis != null && r.timestamp < filterStartMillis) continue;
            if (filterEndMillis != null && r.timestamp > filterEndMillis) continue;

            visibleReports.add(r);
        }

        reportAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (visibleReports.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            symptomRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            symptomRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_symptom_report, null);
        builder.setView(view);

        Spinner childSpinner = view.findViewById(R.id.reportChildSpinner);
        Spinner authorSpinner = view.findViewById(R.id.authorSpinner);
        EditText dateInput = view.findViewById(R.id.reportDateInput);
        ToggleButton nightWakingToggle = view.findViewById(R.id.nightWakingToggle);
        ToggleButton activityToggle = view.findViewById(R.id.activityToggle);
        ToggleButton coughWheezeToggle = view.findViewById(R.id.coughWheezeToggle);

        CheckBox trigExercise = view.findViewById(R.id.triggerExercise);
        CheckBox trigColdAir = view.findViewById(R.id.triggerColdAir);
        CheckBox trigDustPets = view.findViewById(R.id.triggerDustPets);
        CheckBox trigSmoke = view.findViewById(R.id.triggerSmoke);
        CheckBox trigIllness = view.findViewById(R.id.triggerIllness);
        CheckBox trigPerfume = view.findViewById(R.id.triggerPerfume);

        EditText notesInput = view.findViewById(R.id.notesInput);

        List<String> childNames = new ArrayList<>();
        for (Child c : children) childNames.add(c.name != null ? c.name : "Child");
        ArrayAdapter<String> childAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                childNames
        );
        childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSpinner.setAdapter(childAdapter);

        if (selectedChildId != null) {
            for (int i = 0; i < children.size(); i++) {
                if (selectedChildId.equals(children.get(i).childId)) {
                    childSpinner.setSelection(i);
                    break;
                }
            }
        }

        ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Parent", "Child"}
        );
        authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authorSpinner.setAdapter(authorAdapter);

        Date now = new Date();
        dateInput.setText(dateFormat.format(now));
        dateInput.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(
                    symptomReports.this,
                    (view1, year, month, dayOfMonth) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, dayOfMonth, 0, 0, 0);
                        dateInput.setText(dateFormat.format(c.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            int idx = childSpinner.getSelectedItemPosition();
            if (idx < 0 || idx >= children.size()) {
                Toast.makeText(this, "Select a child", Toast.LENGTH_SHORT).show();
                return;
            }
            Child c = children.get(idx);

            long ts;
            try {
                Date d = dateFormat.parse(dateInput.getText().toString().trim());
                ts = (d != null) ? d.getTime() : System.currentTimeMillis();
            } catch (ParseException e) {
                ts = System.currentTimeMillis();
            }

            SymptomReport r = new SymptomReport();
            r.timestamp = ts;
            r.author = authorSpinner.getSelectedItem().toString();
            r.nightWaking = nightWakingToggle.isChecked();
            r.activityLimits = activityToggle.isChecked();
            r.coughWheeze = coughWheezeToggle.isChecked();
            r.notes = notesInput.getText().toString().trim();

            r.triggers = new ArrayList<>();
            if (trigExercise.isChecked()) r.triggers.add("Exercise");
            if (trigColdAir.isChecked()) r.triggers.add("Cold Air");
            if (trigDustPets.isChecked()) r.triggers.add("Dust/Pets");
            if (trigSmoke.isChecked()) r.triggers.add("Smoke");
            if (trigIllness.isChecked()) r.triggers.add("Illness");
            if (trigPerfume.isChecked()) r.triggers.add("Perfume/Cleaners");

            DatabaseReference ref = childrenRef.child(c.childId)
                    .child("logs")
                    .child("symptoms")
                    .push();
            ref.setValue(r).addOnSuccessListener(x ->
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showExportRangeDialog() {
        String[] options = {"Last 7 days", "Last 30 days", "Last 90 days", "Custom range"};
        new AlertDialog.Builder(this)
                .setTitle("Select date range")
                .setItems(options, (dialog, which) -> {
                    long now = System.currentTimeMillis();
                    long dayMs = 24L * 60L * 60L * 1000L;
                    if (which == 0) {
                        filterStartMillis = now - 7L * dayMs;
                        filterEndMillis = now;
                        applyFilters();
                        exportCurrentView();
                    } else if (which == 1) {
                        filterStartMillis = now - 30L * dayMs;
                        filterEndMillis = now;
                        applyFilters();
                        exportCurrentView();
                    } else if (which == 2) {
                        filterStartMillis = now - 90L * dayMs;
                        filterEndMillis = now;
                        applyFilters();
                        exportCurrentView();
                    } else {
                        showCustomRangePicker();
                    }
                })
                .show();
    }

    private void showCustomRangePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog startPicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(year, month, dayOfMonth, 0, 0, 0);
                    long startMs = startCal.getTimeInMillis();

                    DatePickerDialog endPicker = new DatePickerDialog(
                            this,
                            (view2, year2, month2, day2) -> {
                                Calendar endCal = Calendar.getInstance();
                                endCal.set(year2, month2, day2, 23, 59, 59);
                                filterStartMillis = startMs;
                                filterEndMillis = endCal.getTimeInMillis();
                                applyFilters();
                                exportCurrentView();
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                    );
                    endPicker.setTitle("End date");
                    endPicker.show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        startPicker.setTitle("Start date");
        startPicker.show();
    }
    private Bitmap generatePdfPreviewBitmap(List<SymptomReport> reports) {

        int width = 1080;
        int height = 1600;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setTextSize(36f);

        int y = 80;

        paint.setFakeBoldText(true);
        canvas.drawText("Symptom Report Summary (Preview)", 40, y, paint);
        paint.setFakeBoldText(false);
        y += 60;

        for (SymptomReport r : reports) {
            if (y > height - 150) break;

            String dateStr = dateFormat.format(new Date(r.timestamp));
            String triggersStr = (r.triggers != null && !r.triggers.isEmpty())
                    ? TextUtils.join(", ", r.triggers) : "-";

            canvas.drawText("Date: " + dateStr, 40, y, paint); y += 40;
            canvas.drawText("Child: " + r.childName + "  Author: " + r.author, 40, y, paint); y += 40;
            canvas.drawText("Night waking: " + (r.nightWaking ? "Yes" : "No"), 40, y, paint); y += 40;
            canvas.drawText("Activity limits: " + (r.activityLimits ? "Yes" : "No"), 40, y, paint); y += 40;
            canvas.drawText("Cough/Wheeze: " + (r.coughWheeze ? "Yes" : "No"), 40, y, paint); y += 40;
            canvas.drawText("Triggers: " + triggersStr, 40, y, paint); y += 40;

            if (r.notes != null && !r.notes.isEmpty()) {
                canvas.drawText("Notes: " + r.notes, 40, y, paint);
                y += 40;
            }
            y += 50;
        }

        return bmp;
    }
    private void showPdfPreviewDialog(Bitmap preview, File pdfFile, File csvFile) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_pdf_preview, null);

        ImageView previewImage = view.findViewById(R.id.previewImage);
        Button btnDownload = view.findViewById(R.id.btnDownload);
        Button btnShare = view.findViewById(R.id.btnShare);

        previewImage.setImageBitmap(preview);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnDownload.setOnClickListener(v -> {
            dialog.dismiss();

            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        });

        btnShare.setOnClickListener(v -> {
            dialog.dismiss();

            Uri pdfUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    pdfFile);

            Uri csvUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider",
                    csvFile);

            ArrayList<Uri> list = new ArrayList<>();
            list.add(pdfUri);
            list.add(csvUri);

            Intent sendIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            sendIntent.setType("*/*");
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(sendIntent, "Share Reports"));
        });
    }


    private void exportCurrentView() {
        if (visibleReports.isEmpty()) {
            Toast.makeText(this, "No reports in this range", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File dir = new File(getCacheDir(), "exports");
            if (!dir.exists()) dir.mkdirs();

            long stamp = System.currentTimeMillis();
            File csvFile = new File(dir, "symptom_reports_" + stamp + ".csv");
            File pdfFile = new File(dir, "symptom_reports_" + stamp + ".pdf");

            writeCsv(csvFile, visibleReports);
            writePdf(pdfFile, visibleReports);

            PdfDocument doc = new PdfDocument();

            Bitmap previewBitmap = generatePdfPreviewBitmap(visibleReports);

            showPdfPreviewDialog(previewBitmap, pdfFile, csvFile);

        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void writeCsv(File file, List<SymptomReport> reports) throws Exception {
        FileWriter fw = new FileWriter(file);
        fw.append("Date,Child,Author,NightWaking,ActivityLimits,CoughWheeze,Triggers,Notes\n");
        for (SymptomReport r : reports) {
            String dateStr = dateFormat.format(new Date(r.timestamp));
            String triggersStr = (r.triggers != null && !r.triggers.isEmpty())
                    ? TextUtils.join(";", r.triggers) : "";
            fw.append(escape(dateStr)).append(',')
                    .append(escape(r.childName)).append(',')
                    .append(escape(r.author)).append(',')
                    .append(r.nightWaking ? "1" : "0").append(',')
                    .append(r.activityLimits ? "1" : "0").append(',')
                    .append(r.coughWheeze ? "1" : "0").append(',')
                    .append(escape(triggersStr)).append(',')
                    .append(escape(r.notes == null ? "" : r.notes)).append('\n');
        }
        fw.flush();
        fw.close();
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private void writePdf(File file, List<SymptomReport> reports) throws Exception {
        PdfDocument doc = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12f);

        int pageWidth = 595;
        int pageHeight = 842;

        int y = 40;
        int pageNumber = 1;

        PdfDocument.Page page = doc.startPage(
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        );
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16f);
        canvas.drawText("Symptom Report Summary", 40, y, paint);
        paint.setTextSize(12f);
        y += 30;

        for (SymptomReport r : reports) {
            if (y > pageHeight - 80) {
                doc.finishPage(page);
                pageNumber++;
                page = doc.startPage(
                        new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                );
                canvas = page.getCanvas();
                y = 40;
            }

            String dateStr = dateFormat.format(new Date(r.timestamp));
            String triggersStr = (r.triggers != null && !r.triggers.isEmpty())
                    ? TextUtils.join(", ", r.triggers) : "-";

            canvas.drawText("Date: " + dateStr, 40, y, paint); y += 15;
            canvas.drawText("Child: " + r.childName + "   Author: " + r.author, 40, y, paint); y += 15;
            canvas.drawText("Night waking: " + (r.nightWaking ? "Yes" : "No"), 40, y, paint); y += 15;
            canvas.drawText("Activity limits: " + (r.activityLimits ? "Yes" : "No"), 40, y, paint); y += 15;
            canvas.drawText("Cough/Wheeze: " + (r.coughWheeze ? "Yes" : "No"), 40, y, paint); y += 15;
            canvas.drawText("Triggers: " + triggersStr, 40, y, paint); y += 15;

            if (r.notes != null && !r.notes.isEmpty()) {
                canvas.drawText("Notes: " + r.notes, 40, y, paint);
                y += 15;
            }
            y += 10;
        }

        doc.finishPage(page);

        FileOutputStream fos = new FileOutputStream(file);
        doc.writeTo(fos);
        fos.flush();
        fos.close();
        doc.close();
    }

    public static class Child {
        public String childId;
        public String parentId;
        public String name;
        public Child() {}
    }

    public static class SymptomReport {
        public String id;
        public String childId;
        public String childName;
        public long timestamp;
        public String author;
        public boolean nightWaking;
        public boolean activityLimits;
        public boolean coughWheeze;
        public List<String> triggers;
        public String notes;

        public SymptomReport() {}

        public boolean matchesSymptom(String f) {
            if ("Night Waking".equals(f)) return nightWaking;
            if ("Activity Limits".equals(f)) return activityLimits;
            if ("Cough/Wheeze".equals(f)) return coughWheeze;
            return true;
        }

        public boolean matchesTrigger(String f) {
            if (triggers == null || triggers.isEmpty()) return false;
            for (String t : triggers) {
                if (t.equalsIgnoreCase(f)) return true;
            }
            return false;
        }
    }

    private static class SymptomReportAdapter extends RecyclerView.Adapter<SymptomReportAdapter.Holder> {

        private final List<SymptomReport> reports;
        private final SimpleDateFormat df =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        SymptomReportAdapter(List<SymptomReport> reports) {
            this.reports = reports;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_symptom_report, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int pos) {
            SymptomReport r = reports.get(pos);
            h.dateText.setText(df.format(new Date(r.timestamp)));
            h.childText.setText("Child: " + r.childName);
            h.authorText.setText("Author: " + r.author);
            h.symptomText.setText(
                    "Night waking: " + (r.nightWaking ? "Yes" : "No") + "\n" +
                            "Activity limits: " + (r.activityLimits ? "Yes" : "No") + "\n" +
                            "Cough/Wheeze: " + (r.coughWheeze ? "Yes" : "No")
            );
            String triggersStr = (r.triggers != null && !r.triggers.isEmpty())
                    ? TextUtils.join(", ", r.triggers) : "-";
            h.triggersText.setText("Triggers: " + triggersStr);
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView dateText, childText, authorText, symptomText, triggersText;
            Holder(@NonNull View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.reportDateText);
                childText = itemView.findViewById(R.id.reportChildText);
                authorText = itemView.findViewById(R.id.reportAuthorText);
                symptomText = itemView.findViewById(R.id.symptomsText);
                triggersText = itemView.findViewById(R.id.triggersText);
            }
        }
    }
}
