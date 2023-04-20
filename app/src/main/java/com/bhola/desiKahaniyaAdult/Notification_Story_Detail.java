package com.bhola.desiKahaniyaAdult;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Notification_Story_Detail extends AppCompatActivity {

    List<Object> collectonData;
    StoryDetails_Adapter adapter2;
    DatabaseReference mref;

    ImageView back, share_ap;
    LinearLayout progressBar;
    String activityComingFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);


        actionBar();
        progressBar = findViewById(R.id.progressBar);
        mref = FirebaseDatabase.getInstance().getReference().child("Notification");
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectonData = new ArrayList<Object>();


        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FirebaseData firebaseData = ds.getValue(FirebaseData.class);
                    collectonData.add(firebaseData);
                }
                Collections.reverse(collectonData);

                if (SplashScreen.App_updating.equals("active")) {
                    collectonData.clear();
                }
                adapter2 = new StoryDetails_Adapter(collectonData, Notification_Story_Detail.this);
                recyclerView.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void actionBar() {
        TextView title = findViewById(R.id.title_collection);
        title.setText("Notifications");

        activityComingFrom = getIntent().getStringExtra("activityComingFrom");

        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(v -> onBackPressed());
        share_ap = findViewById(R.id.share_app);
        share_ap.setOnClickListener(v -> {
            share_ap.setImageResource(R.drawable.favourite_active);
            startActivity(new Intent(getApplicationContext(), Download_Detail.class));
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),Collection_GridView.class));
        super.onBackPressed();
    }
}




