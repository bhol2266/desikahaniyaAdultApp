package com.sgs.desiKahaniyaAdult;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.io.IOException;

public class AudioPlayer extends AppCompatActivity {
    private ImageView playBtn, downloadBtn;
    private LinearLayout progressbar, playBtnSeekbarLayout;
    private TextView loadingMessage, currentTime, storyTitle, description, progressIndicator, downloadSize;
    private MediaPlayer mediaPlayer;
    private SeekBar seekbar;
    private Handler handler;
    private Runnable runnable;
    private LottieAnimationView lottie;
    private ProgressBar progressbarUnit, progressbarDownload;
    private boolean isURLBroken = false;
    private boolean isPlaying = true;
    private String storyURL, storyName, title, audioHref;
    private int pausePosition = -1;
    private AlertDialog dialog;
    private DownloadAudioHelper.DownloadFileFromURL downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        createNotificationChannel();
        initViews();
        loadAds();
        setupDownloadAudio();
        startPlayingAudio();
        startAudioService();

        setupPlayButton();
        setupSeekBar();
        setupBufferingListener();
        setupCompletionListener();
        updateStoryRead();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "audio_channel", "Audio Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startAudioService() {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        serviceIntent.putExtra("audio_url", storyURL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void initViews() {
        playBtn = findViewById(R.id.playBtn);
        downloadBtn = findViewById(R.id.downloadBtn);
        progressbar = findViewById(R.id.progressbar);
        playBtnSeekbarLayout = findViewById(R.id.playBtn_and_SeekbarLayout);
        loadingMessage = findViewById(R.id.message);
        currentTime = findViewById(R.id.currentTime);
        seekbar = findViewById(R.id.seekbar);
        progressbarUnit = findViewById(R.id.progressbarUnit);
        lottie = findViewById(R.id.lottie);
        storyTitle = findViewById(R.id.storyTitle);

        storyURL = SplashScreen.decryption(getIntent().getStringExtra("storyURL"));
        audioHref = SplashScreen.decryption(getIntent().getStringExtra("audioHref"));
        title = SplashScreen.decryption(getIntent().getStringExtra("title"));
        storyName = getIntent().getStringExtra("storyName");
        storyTitle.setText(storyName.replace("-", " ").trim());

        playBtn.setBackgroundResource(R.drawable.play);
    }

    private void setupPlayButton() {
        playBtn.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                resumePlayback();
            } else {
                pausePlayback();
            }
        });
    }

    private void resumePlayback() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mediaPlayer.seekTo(Math.max(0, pausePosition - 500));
        mediaPlayer.start();
        playBtn.setBackgroundResource(R.drawable.pause);
        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
        isPlaying = true;
    }

    private void pausePlayback() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mediaPlayer.pause();
        pausePosition = mediaPlayer.getCurrentPosition();
        playBtn.setBackgroundResource(R.drawable.play);
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        isPlaying = false;
        lottie.setVisibility(View.INVISIBLE);
    }

    private void setupSeekBar() {
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    progressbar.setVisibility(View.VISIBLE);
                    progressbarUnit.setVisibility(View.VISIBLE);
                    lottie.setVisibility(View.INVISIBLE);
                    mediaPlayer.seekTo(progress);
                    updateCurrentTime();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupBufferingListener() {
        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            loadingMessage.setText(percent + "% buffered");
            if (percent >= 5 && isPlaying) {
                mp.start();
                lottie.setVisibility(View.VISIBLE);
                progressbarUnit.setVisibility(View.GONE);
                playBtnSeekbarLayout.setVisibility(View.VISIBLE);
            }
            progressbar.setVisibility(percent == 100 ? View.INVISIBLE : View.VISIBLE);
        });
    }

    private void setupCompletionListener() {
        mediaPlayer.setOnCompletionListener(mp -> {
            playBtn.setBackgroundResource(R.drawable.play);
            if (!isURLBroken) {
                Toast.makeText(this, "Finished", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    private void startPlayingAudio() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            mediaPlayer = new MediaPlayer();
            handler = new Handler();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(storyURL);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                try {
                    mp.reset();
                    mp.setDataSource(SplashScreen.databaseURL + "Sexstory_Audiofiles/" + audioHref + ".mp3");
                    mp.prepareAsync();
                    mp.setOnPreparedListener(mediaPlayer -> onMediaPrepared());
                } catch (IOException e) {
                    isURLBroken = true;
                    loadingMessage.setText("Audio link not working, trying another URL...");
                    loadingMessage.setTextSize(20);
                    progressbarUnit.setVisibility(View.GONE);
                }
                return false;
            });

            mediaPlayer.setOnPreparedListener(mp -> onMediaPrepared());
            playBtn.setBackgroundResource(R.drawable.pause);
        } catch (Exception e) {
            Toast.makeText(this, "LINK BROKEN", Toast.LENGTH_SHORT).show();
        }
    }

    private void onMediaPrepared() {
        seekbar.setMax(mediaPlayer.getDuration());
        updateSeekbar();
        updateCurrentTime();
        Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
    }

    private void updateSeekbar() {
        pausePosition = mediaPlayer.getCurrentPosition();
        seekbar.setProgress(pausePosition);
        runnable = () -> {
            updateSeekbar();
            updateCurrentTime();
        };
        handler.postDelayed(runnable, 1000);
    }

    private void updateCurrentTime() {
        int remainingTime = (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()) / 1000;
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        currentTime.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void loadAds() {
        if (SplashScreen.Ads_State.equals("active")) showAds();
    }

    private void showAds() {
        AdView mAdView = findViewById(R.id.adView);
        LinearLayout fbBannerLayout = findViewById(R.id.banner_container);
        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            ADS_ADMOB.BannerAd(this, mAdView);
            ADS_ADMOB.Interstitial_Ad(this);
        } else {
            ADS_FACEBOOK.interstitialAd(this, null, getString(R.string.Facebook_InterstitialAdUnit));
            ADS_FACEBOOK.bannerAds(this, null, fbBannerLayout, getString(R.string.Facebook_BannerAdUnit));
        }
    }

    private void updateStoryRead() {
        int position = getIntent().getIntExtra("position", 0);
        ftab2.adapter2.notifyItemChanged(position);
        new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").updateStoryRead(title, 1);
    }

    private void setupDownloadAudio() {
        downloadBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = LayoutInflater.from(this);
            View promptView = inflater.inflate(R.layout.download_dialog, null);
            builder.setView(promptView);
            builder.setCancelable(false);

            description = promptView.findViewById(R.id.description);
            description.setText(storyName + ".mp3 downloading...");
            progressIndicator = promptView.findViewById(R.id.progress_indicator);
            downloadSize = promptView.findViewById(R.id.downloadSize);
            progressbarDownload = promptView.findViewById(R.id.seekbar);

            Button cancelBtn = promptView.findViewById(R.id.cancelbtn);
            cancelBtn.setOnClickListener(cancel -> {
                Toast.makeText(this, "Download Cancelled", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                downloadTask.cancel(true);
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("Download", Context.MODE_PRIVATE);
                File file = new File(directory, storyName.replaceAll(" ", "_") + ".mp3");
                if (file.exists()) file.delete();
            });

            dialog = builder.create();
            dialog.show();

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("Download", Context.MODE_PRIVATE);
            File file = new File(directory, storyName.replaceAll(" ", "_") + ".mp3");
            if (!file.exists()) {
                downloadTask = new DownloadAudioHelper.DownloadFileFromURL(this, storyName, storyURL, dialog, progressbarDownload, progressIndicator, downloadSize);
                downloadTask.execute();
            } else {
                Snackbar snackbar = Snackbar.make(v, "File already exists", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, AudioPlayerService.class));
    }
}