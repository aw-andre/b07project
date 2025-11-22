package com.example.b07_group_project.b07_group_project;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.example.b07_group_project.R;

public class symptomReports extends AppCompatActivity {

    private RecyclerView symptomRecyclerView;
    private SymptomReportAdapter reportAdapter;
    private List<SymptomReport> reportList;
    private TextView emptyStateText;
    private Spinner symptomFilterSpinner, triggerFilterSpinner;
    private Button exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptomreports);

        ImageButton backButton = findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        reportList = new ArrayList<>();
        emptyStateText = findViewById(R.id.emptyStateText);
        symptomFilterSpinner = findViewById(R.id.symptomFilterSpinner);
        triggerFilterSpinner = findViewById(R.id.triggerFilterSpinner);
        exportButton = findViewById(R.id.exportButton);

        // Setup filter spinners
        ArrayAdapter<String> symptomAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All Symptoms", "Night Waking", "Activity Limits", "Cough/Wheeze"});
        symptomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomFilterSpinner.setAdapter(symptomAdapter);

        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All Triggers", "Exercise", "Cold Air", "Dust/Pets", "Smoke", "Illness", "Perfume/Cleaners"});
        triggerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        triggerFilterSpinner.setAdapter(triggerAdapter);

        symptomRecyclerView = findViewById(R.id.symptomRecyclerView);
        symptomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new SymptomReportAdapter(reportList);
        symptomRecyclerView.setAdapter(reportAdapter);

        // Add sample data
        addSampleReports();

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(symptomReports.this, "Export functionality - PDF/CSV export would be implemented here", Toast.LENGTH_LONG).show();
            }
        });

        updateEmptyState();
    }

    private void addSampleReports() {
        reportList.add(new SymptomReport("2024-01-15", "Child", "Night Waking, Activity Limits", "Exercise, Cold Air"));
        reportList.add(new SymptomReport("2024-01-14", "Parent", "Cough/Wheeze", "Dust/Pets, Illness"));
        reportList.add(new SymptomReport("2024-01-13", "Child", "Activity Limits", "Exercise"));
        reportAdapter.notifyDataSetChanged();
    }

    private void updateEmptyState() {
        if (reportList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            symptomRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            symptomRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class SymptomReport {
        String date;
        String author;
        String symptoms;
        String triggers;

        SymptomReport(String date, String author, String symptoms, String triggers) {
            this.date = date;
            this.author = author;
            this.symptoms = symptoms;
            this.triggers = triggers;
        }
    }

    private class SymptomReportAdapter extends RecyclerView.Adapter<SymptomReportAdapter.ReportViewHolder> {
        private List<SymptomReport> reports;

        SymptomReportAdapter(List<SymptomReport> reports) {
            this.reports = reports;
        }

        @Override
        public ReportViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_symptom_report, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReportViewHolder holder, int position) {
            SymptomReport report = reports.get(position);
            holder.dateText.setText("Date: " + report.date);
            holder.authorText.setText("Entered by: " + report.author);
            holder.symptomsText.setText("Symptoms: " + report.symptoms);
            holder.triggersText.setText("Triggers: " + report.triggers);
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView dateText;
            TextView authorText;
            TextView symptomsText;
            TextView triggersText;

            ReportViewHolder(View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.reportDateText);
                authorText = itemView.findViewById(R.id.reportAuthorText);
                symptomsText = itemView.findViewById(R.id.symptomsText);
                triggersText = itemView.findViewById(R.id.triggersText);
            }
        }
    }
}
