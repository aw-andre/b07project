package com.example.b07_group_project;

import static android.widget.VideoView.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class techniqueHelper extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.techniquehelper);

        ImageButton button = findViewById(R.id.imageButton4);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(techniqueHelper.this, childUserInterfaceHome.class);
                startActivity(intent);
            }
        });

        VideoView videoView = findViewById(R.id.videoView3);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(findViewById(R.id.scrollView2));
        mediaController.setMediaPlayer(videoView);

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.asthma_video));
        videoView.start();




    }
}
