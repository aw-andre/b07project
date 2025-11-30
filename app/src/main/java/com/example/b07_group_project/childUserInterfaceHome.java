package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class childUserInterfaceHome extends AppCompatActivity {

    private String CHILD_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.childuserinterfacehome);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CHILD_ID = getIntent().getStringExtra("childId");

        if (CHILD_ID == null || CHILD_ID.isEmpty()) {
            finish();
            return;
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(childUserInterfaceHome.this, techniqueHelper.class);
            intent.putExtra("childId", CHILD_ID);
            startActivity(intent);
        });

        Button buttonThree = findViewById(R.id.button2);
        buttonThree.setOnClickListener(v -> {
            Intent intent = new Intent(childUserInterfaceHome.this, medicationLog.class);
            intent.putExtra("childId", CHILD_ID);
            startActivity(intent);
        });

        Button buttonFour = findViewById(R.id.button3);
        buttonFour.setOnClickListener(v -> {
            Intent intent = new Intent(childUserInterfaceHome.this, checkInAndSymptoms.class);
            intent.putExtra("childId", CHILD_ID);
            startActivity(intent);
        });

        Button buttonFive = findViewById(R.id.button4);
        buttonFive.setOnClickListener(v -> {
            Intent intent = new Intent(childUserInterfaceHome.this, troubleBreathing.class);
            intent.putExtra("childId", CHILD_ID);
            startActivity(intent);
        });

        Button buttonSix = findViewById(R.id.button5);
        buttonSix.setOnClickListener(v -> {
            Intent intent = new Intent(childUserInterfaceHome.this, badges.class);
            intent.putExtra("childId", CHILD_ID);
            startActivity(intent);
        });
    }
}
