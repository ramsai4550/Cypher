package com.example.cypher;

import static com.example.cypher.R.id.videoView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY =3000; // 5 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Set your layout XML here

        VideoView videoView = findViewById(R.id.videoView); // Replace with your VideoView ID
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.vid); // Replace with your video file

        videoView.setOnCompletionListener(mp -> {
            // Video playback is complete, start MainActivity after a delay
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the splash activity to prevent going back to it
            }, SPLASH_DELAY);
        });

        videoView.start(); // Start playing the video
    }
}

