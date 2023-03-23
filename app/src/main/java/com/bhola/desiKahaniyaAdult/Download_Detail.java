package com.bhola.desiKahaniyaAdult;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class Download_Detail extends AppCompatActivity {


    String Ads_State;
    List<Object> collectonData;
    public static StoryDetails_Adapter adapter;
    ImageView back;
    RecyclerView recyclerView;
    TextView emptyView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);

        actionBar();
        initViews();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectonData = new ArrayList<Object>();

        getDataFromDatabase();
        checkCollectionDataEmpty();

        adapter = new StoryDetails_Adapter(collectonData, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }



    private void getDataFromDatabase() {

        Cursor cursor = (new DatabaseHelper((Context) this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems")).readLikedStories();
        try {
            while (cursor.moveToNext()) {
                StoryItemModel storyItemModel = new StoryItemModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getInt(9), "cursor.getString(10)", cursor.getInt(11), cursor.getInt(12), cursor.getString(13), cursor.getInt(14));
                collectonData.add(storyItemModel);
            }
        } finally {
            cursor.close();
        }


    }


    private void checkCollectionDataEmpty() {
        if (collectonData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

        }
    }


    private void actionBar() {
        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Collection_GridView.class);
                intent.putExtra("Ads_Status", Ads_State);
                startActivity(intent);
                finish();
            }
        });

    }


    private void initViews() {
        emptyView = findViewById(R.id.emptyView);
        recyclerView = findViewById(R.id.recyclerView_Downloads);
    }

}

