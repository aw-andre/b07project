package com.example.b07_group_project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonOne: Button = findViewById(R.id.button)
        buttonOne.setOnClickListener {
            val intent = Intent(this@MainActivity, SecondActivity::class.java)
            startActivity(intent)

        }
        val buttonTwo: Button = findViewById(R.id.button2)
        buttonTwo.setOnClickListener {
            val intent = Intent(this@MainActivity, ThirdActivity::class.java)
            startActivity(intent)
        }
        val buttonThree: Button = findViewById(R.id.button3)
        buttonThree.setOnClickListener {
            val intent = Intent(this@MainActivity, FourthActivity::class.java)
            startActivity(intent)
        }
        val buttonFour: Button = findViewById(R.id.button4)
        buttonFour.setOnClickListener {
            val intent = Intent(this@MainActivity, FifthActivity::class.java)
            startActivity(intent)
        }
        val buttonFive: Button = findViewById(R.id.button5)
        buttonFive.setOnClickListener {
            val intent = Intent(this@MainActivity, SixthActivity::class.java)
            startActivity(intent)
        }

    }
}


