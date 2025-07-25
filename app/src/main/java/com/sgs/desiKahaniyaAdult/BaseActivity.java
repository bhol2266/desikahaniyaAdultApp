package com.sgs.desiKahaniyaAdult;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import soup.neumorphism.NeumorphCardView;

public class BaseActivity extends AppCompatActivity {

    NeumorphCardView bottomPlayer;
    TextView audioTitle, currentTime, totalTime;
    ImageView playPauseBtn;
    SeekBar bottomSeekbar;
    boolean isPlaying = true;
    ImageView stopBtn;


    BroadcastReceiver bottomReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getIntExtra("current", 0);
            int duration = intent.getIntExtra("duration", 0);
            String title = intent.getStringExtra("title");
             isPlaying = intent.getBooleanExtra("isPlaying",false);


            playPauseBtn.setImageResource(isPlaying ? R.drawable.pause : R.drawable.play);



            audioTitle.setText(title);

            if (duration > 0) {
                bottomPlayer.setVisibility(View.VISIBLE);
                bottomSeekbar.setMax(duration);
                bottomSeekbar.setProgress(current);
                currentTime.setText(formatTime(current));
                totalTime.setText(formatTime(duration));
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        bottomPlayer = findViewById(R.id.bottomAudioPlayer);
        audioTitle = findViewById(R.id.audioTitle);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        bottomSeekbar = findViewById(R.id.bottomSeekbar);



        IntentFilter filter = new IntentFilter("PROGRESS_UPDATE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bottomReceiver, filter, RECEIVER_EXPORTED);
//            registerReceiver(bufferReceiver, bufferFilter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(bottomReceiver, filter);
//            registerReceiver(bufferReceiver, bufferFilter);
        }

        playPauseBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, AudioPlayerService.class);
            i.setAction(isPlaying ? "PAUSE" : "PLAY");
            ContextCompat.startForegroundService(this, i);

            playPauseBtn.setImageResource(isPlaying ? R.drawable.play : R.drawable.pause);

            isPlaying = !isPlaying;
        });

        stopBtn = findViewById(R.id.stopBtn);
        if (stopBtn != null) {
            stopBtn.setOnClickListener(v -> {
                Intent stopIntent = new Intent(this, AudioPlayerService.class);
                stopService(stopIntent); // 🔥 Stop the service completely
                bottomPlayer.setVisibility(View.GONE); // Hide the UI
                isPlaying = false;
            });
        }

        bottomSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent seekIntent = new Intent(BaseActivity.this, AudioPlayerService.class);
                    seekIntent.setAction("SEEK");
                    seekIntent.putExtra("seekTo", progress);
                    startService(seekIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        bottomPlayer.setOnClickListener(v -> {
//            Intent i = new Intent(this, AudioPlayer.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(i);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bottomReceiver);
    }

    private String formatTime(int millis) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}
