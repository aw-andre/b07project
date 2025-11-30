package com.example.b07_group_project;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class providerReportView extends AppCompatActivity {

    private Spinner childSelector;
    private Spinner reportRangeSelector;
    private TextView rescueFrequencyText;
    private TextView adherenceText;
    private TextView symptomBurdenText;
    private TextView zoneDistributionText;
    private TextView triageIncidentsText;
    private LineChart rescueChart;
    private PieChart zoneChart;
    private Button exportPdfBtn;

    private final List<String> linkedChildIds = new ArrayList<>();
    private final List<String> linkedChildNames = new ArrayList<>();

    private DatabaseReference usersRef;
    private DatabaseReference childrenRef;
    private DatabaseReference shareSettingsRef;

    //private String providerId = "provider789";
    private String providerId;
    private String currentChildId;
    private int monthRange = 3;

    private final SimpleDateFormat dayFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.providerreportview);

        providerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        bindViews();

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        childrenRef = FirebaseDatabase.getInstance().getReference("children");
        shareSettingsRef = FirebaseDatabase.getInstance().getReference("shareSettings");

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(v -> finish());

        setupRangeSpinner();
        loadLinkedChildren();

        exportPdfBtn.setOnClickListener(v -> exportToPdf());
    }

    private void bindViews() {
        childSelector = findViewById(R.id.providerReportChildSelector);
        reportRangeSelector = findViewById(R.id.reportRangeSelector);
        rescueFrequencyText = findViewById(R.id.rescueFrequencyText);
        adherenceText = findViewById(R.id.adherenceText);
        symptomBurdenText = findViewById(R.id.symptomBurdenText);
        zoneDistributionText = findViewById(R.id.zoneDistributionText);
        triageIncidentsText = findViewById(R.id.triageIncidentsText);
        rescueChart = findViewById(R.id.rescueChart);
        zoneChart = findViewById(R.id.zoneChart);
        exportPdfBtn = findViewById(R.id.exportPdfBtn);
    }

    private void setupRangeSpinner() {
        String[] items = new String[]{"Last 3 months", "Last 4 months", "Last 5 months", "Last 6 months"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportRangeSelector.setAdapter(adapter);
        reportRangeSelector.setSelection(0);

        reportRangeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monthRange = 3 + position;
                if (currentChildId != null) {
                    loadReportForChild(currentChildId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadLinkedChildren() {
        usersRef.child(providerId).child("linkedChildren")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        linkedChildIds.clear();
                        linkedChildNames.clear();

                        for (DataSnapshot childSnap : snapshot.getChildren()) {
                            Boolean linked = childSnap.getValue(Boolean.class);
                            if (Boolean.TRUE.equals(linked)) {
                                String childId = childSnap.getKey();
                                loadChildName(childId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadChildName(String childId) {
        childrenRef.child(childId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            linkedChildIds.add(childId);
                            linkedChildNames.add(name);
                        }
                        updateChildSelector();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void updateChildSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                linkedChildNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childSelector.setAdapter(adapter);

        childSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < linkedChildIds.size()) {
                    String childId = linkedChildIds.get(position);
                    loadReportForChild(childId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        if (!linkedChildIds.isEmpty()) {
            childSelector.setSelection(0);
        }

        if (!linkedChildIds.isEmpty()) {
            childSelector.setSelection(0);
        }

    }

    private long getCutoffMillis() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -monthRange);
        return cal.getTimeInMillis();
    }

    private void loadReportForChild(String childId) {
        currentChildId = childId;
        long cutoffMillis = getCutoffMillis();

        shareSettingsRef.child(childId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot shareSnap) {
                        boolean shareRescue = Boolean.TRUE.equals(shareSnap.child("rescue").getValue(Boolean.class));
                        boolean shareSymptoms = Boolean.TRUE.equals(shareSnap.child("symptoms").getValue(Boolean.class));
                        boolean shareTriage = Boolean.TRUE.equals(shareSnap.child("triage").getValue(Boolean.class));
                        boolean shareCharts = Boolean.TRUE.equals(shareSnap.child("charts").getValue(Boolean.class));
                        boolean shareController = Boolean.TRUE.equals(shareSnap.child("controller").getValue(Boolean.class));

                        loadRescue(childId, shareRescue, cutoffMillis);
                        loadSymptoms(childId, shareSymptoms, cutoffMillis);
                        loadTriage(childId, shareTriage, cutoffMillis);
                        loadZone(childId, shareCharts, cutoffMillis);
                        loadAdherence(childId, shareController, cutoffMillis);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadRescue(String childId, boolean allowed, long cutoffMillis) {
        if (!allowed) {
            rescueFrequencyText.setText("Rescue: Not shared");
            rescueChart.clear();
            return;
        }

        childrenRef.child(childId).child("logs").child("medication").child("rescue")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalCount = 0;
                        Map<String, Integer> perDay = new LinkedHashMap<>();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts == null || ts < cutoffMillis) continue;

                            totalCount++;
                            String day = dayFormat.format(new Date(ts));
                            int c = perDay.containsKey(day) ? perDay.get(day) : 0;
                            perDay.put(day, c + 1);
                        }

                        rescueFrequencyText.setText("Rescue uses: " + totalCount);

                        List<Entry> entries = new ArrayList<>();
                        int index = 0;
                        for (String d : perDay.keySet()) {
                            entries.add(new Entry(index++, perDay.get(d)));
                        }

                        LineDataSet dataSet = new LineDataSet(entries, "Daily rescue");
                        LineData data = new LineData(dataSet);
                        rescueChart.setData(data);
                        Description desc = new Description();
                        desc.setText("");
                        rescueChart.setDescription(desc);
                        rescueChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadSymptoms(String childId, boolean allowed, long cutoffMillis) {
        if (!allowed) {
            symptomBurdenText.setText("Symptoms: Not shared");
            return;
        }

        childrenRef.child(childId).child("logs").child("symptoms")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = 0;
                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts != null && ts >= cutoffMillis) {
                                count++;
                            }
                        }
                        symptomBurdenText.setText("Problem days: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private int zoneRank(String zone) {
        if (zone == null) return -1;
        String z = zone.toLowerCase(Locale.ROOT);
        if (z.equals("green")) return 0;
        if (z.equals("yellow")) return 1;
        if (z.equals("red")) return 2;
        return -1;
    }

    private void loadTriage(String childId, boolean allowed, long cutoffMillis) {
        if (!allowed) {
            triageIncidentsText.setText("Notable triage: Not shared");
            return;
        }

        childrenRef.child(childId).child("logs").child("triage")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int notable = 0;

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts == null || ts < cutoffMillis) continue;

                            String initialZone = s.child("initialZone").getValue(String.class);
                            String followUpZone = s.child("followUpZone").getValue(String.class);

                            int r0 = zoneRank(initialZone);
                            int r1 = zoneRank(followUpZone);

                            if (r0 >= 0 && r1 >= 0 && r1 >= r0) {
                                notable++;
                            }
                        }

                        triageIncidentsText.setText("Notable triage incidents: " + notable);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadZone(String childId, boolean allowed, long cutoffMillis) {
        if (!allowed) {
            zoneDistributionText.setText("Zone: Not shared");
            zoneChart.clear();
            return;
        }

        childrenRef.child(childId).child("logs").child("zone")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int green = 0;
                        int yellow = 0;
                        int red = 0;

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts == null || ts < cutoffMillis) continue;

                            String zone = s.child("zone").getValue(String.class);
                            if (zone == null) continue;

                            String z = zone.toLowerCase(Locale.ROOT);
                            if (z.equals("green")) green++;
                            else if (z.equals("yellow")) yellow++;
                            else if (z.equals("red")) red++;
                        }

                        zoneDistributionText.setText("Green: " + green + "  Yellow: " + yellow + "  Red: " + red);

                        List<PieEntry> entries = new ArrayList<>();
                        if (green > 0) entries.add(new PieEntry(green, "Green"));
                        if (yellow > 0) entries.add(new PieEntry(yellow, "Yellow"));
                        if (red > 0) entries.add(new PieEntry(red, "Red"));

                        PieDataSet dataSet = new PieDataSet(entries, "");
                        PieData data = new PieData(dataSet);
                        zoneChart.setData(data);
                        Description desc = new Description();
                        desc.setText("");
                        zoneChart.setDescription(desc);
                        zoneChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadAdherence(String childId, boolean allowed, long cutoffMillis) {
        if (!allowed) {
            adherenceText.setText("Adherence: Not shared");
            return;
        }

        childrenRef.child(childId).child("adherence")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer dosePerDay = snapshot.getValue(Integer.class);
                        if (dosePerDay == null || dosePerDay <= 0) {
                            adherenceText.setText("Adherence: Not set");
                            return;
                        }
                        computeAdherence(childId, dosePerDay, cutoffMillis);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void computeAdherence(String childId, int dosePerDay, long cutoffMillis) {
        childrenRef.child(childId).child("logs").child("medication").child("controller")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int usedDoses = 0;

                        for (DataSnapshot s : snapshot.getChildren()) {
                            Long ts = s.child("timestamp").getValue(Long.class);
                            if (ts == null || ts < cutoffMillis) continue;

                            Integer c = s.child("controller").getValue(Integer.class);
                            if (c != null) usedDoses += c;
                        }

                        long now = System.currentTimeMillis();
                        long days = (now - cutoffMillis) / (1000L * 60 * 60 * 24);
                        if (days < 1) days = 1;

                        long planned = (long) dosePerDay * days;
                        if (planned <= 0) {
                            adherenceText.setText("Adherence: Not set");
                            return;
                        }

                        int pct = (int) Math.round((usedDoses * 100.0) / planned);
                        adherenceText.setText("Adherence: " + pct + "%");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void exportToPdf() {
        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(1080, 1920, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(40f);

        canvas.drawText("Provider Report", 40, 80, paint);

        Bitmap rescueBitmap = rescueChart.getChartBitmap();
        Bitmap zoneBitmap = zoneChart.getChartBitmap();

        int y = 140;
        if (rescueBitmap != null) {
            canvas.drawBitmap(rescueBitmap, 40, y, null);
            y += rescueBitmap.getHeight() + 40;
        }
        if (zoneBitmap != null) {
            canvas.drawBitmap(zoneBitmap, 40, y, null);
        }

        pdf.finishPage(page);

        try {
            File file = new File(getExternalFilesDir(null), "provider_report.pdf");
            FileOutputStream out = new FileOutputStream(file);
            pdf.writeTo(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pdf.close();
        }
    }
}
