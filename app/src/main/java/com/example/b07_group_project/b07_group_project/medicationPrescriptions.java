package com.example.b07_group_project.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.example.b07_group_project.R;

import androidx.appcompat.app.AppCompatActivity;

public class medicationPrescriptions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationprescriptions);
        ImageButton button = findViewById(R.id.imageButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(medicationPrescriptions.this, providerUserInterfaceHome.class);
                startActivity(intent);
            }
        });
    }
}

