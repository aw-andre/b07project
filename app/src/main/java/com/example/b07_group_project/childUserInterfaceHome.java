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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.childuserinterfacehome);
    ViewCompat.setOnApplyWindowInsetsListener(
        findViewById(R.id.main),
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
          return insets;
        });

    Button button = findViewById(R.id.button);
    button.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(childUserInterfaceHome.this, techniqueHelper.class);
            startActivity(intent);
          }
        });

    Button buttonThree = findViewById(R.id.button2);
    buttonThree.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(childUserInterfaceHome.this, medicationLog.class);
            startActivity(intent);
          }
        });

    Button buttonFour = findViewById(R.id.button3);
    buttonFour.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(childUserInterfaceHome.this, checkInAndSymptoms.class);
            startActivity(intent);
          }
        });

    Button buttonFive = findViewById(R.id.button4);
    buttonFive.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(childUserInterfaceHome.this, troubleBreathing.class);
            startActivity(intent);
          }
        });

    Button buttonSix = findViewById(R.id.button5);
    buttonSix.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(childUserInterfaceHome.this, badges.class);
            startActivity(intent);
          }
        });
  }
}
