package com.example.b07_group_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.b07_group_project.R;

public class medicationLog extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationlog);
        ImageButton button = findViewById(R.id.imageButton1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(medicationLog.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });
    }
}
