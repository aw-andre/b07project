package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class childUserInterfaceHome extends AppCompatActivity {

    private String CHILD_ID;

    private TextView todaysZoneText;
    private EditText streakDaysEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.childuserinterfacehome);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        CHILD_ID = getIntent().getStringExtra("childId");
        if (CHILD_ID == null || CHILD_ID.isEmpty()) {
            finish();
            return;
        }

        todaysZoneText = findViewById(R.id.textView);
        streakDaysEdit = findViewById(R.id.editTextNumber);

        loadLatestZone();
        loadTechniqueStreak();

        Button b1 = findViewById(R.id.button);
        b1.setOnClickListener(v -> {
            Intent i = new Intent(childUserInterfaceHome.this, techniqueHelper.class);
            i.putExtra("childId", CHILD_ID);
            startActivity(i);
        });

        Button b2 = findViewById(R.id.button2);
        b2.setOnClickListener(v -> {
            Intent i = new Intent(childUserInterfaceHome.this, medicationLog.class);
            i.putExtra("childId", CHILD_ID);
            startActivity(i);
        });

        Button b3 = findViewById(R.id.button3);
        b3.setOnClickListener(v -> {
            Intent i = new Intent(childUserInterfaceHome.this, checkInAndSymptoms.class);
            i.putExtra("childId", CHILD_ID);
            startActivity(i);
        });

        Button b4 = findViewById(R.id.button4);
        b4.setOnClickListener(v -> {
            Intent i = new Intent(childUserInterfaceHome.this, troubleBreathing.class);
            i.putExtra("childId", CHILD_ID);
            startActivity(i);
        });

        Button b5 = findViewById(R.id.button5);
        b5.setOnClickListener(v -> {
            Intent i = new Intent(childUserInterfaceHome.this, badges.class);
            i.putExtra("childId", CHILD_ID);
            startActivity(i);
        });
    }

    // get latest zone log
    private void loadLatestZone() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(CHILD_ID)
                .child("logs")
                .child("zone");

        ref.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot s) {
                if (s.exists()) {
                    for (DataSnapshot x : s.getChildren()) {
                        String z = x.child("zone").getValue(String.class);
                        if (z != null) todaysZoneText.setText("Today's Zone: " + z);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError e) {}
        });
    }

    // count consecutive allStepsCorrect sessions
    private void loadTechniqueStreak() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("children")
                .child(CHILD_ID)
                .child("logs")
                .child("techniqueSession");

        ref.orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot s) {
                        int streak = 0;

                        if (s.exists()) {
                            List<DataSnapshot> list = new ArrayList<>();
                            for (DataSnapshot x : s.getChildren()) {
                                list.add(x);
                            }

                            for (int i = list.size() - 1; i >= 0; i--) {
                                DataSnapshot x = list.get(i);
                                Boolean ok = x.child("allStepsCorrect").getValue(Boolean.class);
                                if (ok != null && ok) {
                                    streak++;
                                } else {
                                    break;
                                }
                            }
                        }

                        streakDaysEdit.setText(String.valueOf(streak));
                    }

                    @Override
                    public void onCancelled(DatabaseError e) {}
                });
    }


}
