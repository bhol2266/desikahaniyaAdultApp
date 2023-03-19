package com.bhola.desiKahaniyaAdult;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class admin_panel extends AppCompatActivity {
    public static int counter = 0;

    DatabaseReference mref, notificationMref;
    TextView Users_Counters;
    EditText title_story, pragraphofstory, date, image_url;
    Button selectStory, insertBTN, Refer_App_url_BTN, STory_Switch_Active_BTN;
    Switch switch_Exit_Nav, switch_Activate_Ads, switch_App_Updating;
    Button Ad_Network;
    static String uncensored_title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminpanel);


        initViews();
        appControl();
        deleteNotification_Stories();
        Add_Stories_to_Notification_Buttons();


    }


    private void initViews() {

        mref = FirebaseDatabase.getInstance().getReference().child("Hindi_desi_Kahani_Adult");
        notificationMref = FirebaseDatabase.getInstance().getReference();
        selectStory = findViewById(R.id.selectStory);
        insertBTN = findViewById(R.id.insert);
        switch_Activate_Ads = findViewById(R.id.Activate_Ads);
        switch_App_Updating = findViewById(R.id.App_updating_Switch);
        switch_Exit_Nav = findViewById(R.id.switch_Exit_Nav);
        Refer_App_url_BTN = findViewById(R.id.Refer_App_url_BTN);
        title_story = findViewById(R.id.title_story);
        pragraphofstory = findViewById(R.id.pragraphofstory);
        date = findViewById(R.id.dateofstory);
        image_url = findViewById(R.id.image_url);


    }

    private void Add_Stories_to_Notification_Buttons() {


        selectStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> title = new ArrayList<>();
                List<String> paragraph = new ArrayList<>();

                try {
                    Cursor cursor = new DatabaseHelper(admin_panel.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readalldata();
                    while (cursor.moveToNext()) {
                        title.add(cursor.getString(3));
                        paragraph.add(cursor.getString(2));
                    }
                } catch (Exception ignored) {

                }

                int randomNum = (int) (Math.random() * (title.size() - 1 - 0 + 1) + 0);
                pragraphofstory.setText(decryption(paragraph.get(randomNum)));
                title_story.setText(title.get(randomNum));
                date.setText("2022-04-19");
                uncensored_title = title.get(randomNum);
            }
        });


        insertBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!image_url.getText().toString().isEmpty() && !pragraphofstory.getText().toString().isEmpty() && !title_story.getText().toString().isEmpty() && !date.getText().toString().isEmpty()) {
                    pasteAndRuncode();
                    FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender("/topics/all", uncensored_title, "पूरी कहानी पढ़ें", image_url.getText().toString(), "Notification_Story", admin_panel.this);
                    fcmNotificationsSender.SendNotifications();
                } else {
                    Toast.makeText(admin_panel.this, "Enter data", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    private void deleteNotification_Stories() {

        Button deleteNotificationStories;
        deleteNotificationStories = findViewById(R.id.deleteNotificationStories);
        deleteNotificationStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notificationMref.child("Notification").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String keys = ds.getKey();
                            notificationMref.child("Notification").child(keys).removeValue();
                            Toast.makeText(admin_panel.this, "Deleted all Stories", Toast.LENGTH_SHORT).show();

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });

            }
        });


    }

    void pasteAndRuncode() {

        String titlee = title_story.getText().toString();
        String paragrapg = pragraphofstory.getText().toString();
        String datee = date.getText().toString();

        String Push_ID = notificationMref.push().getKey();
        notificationMref.child("Notification").child(Push_ID).child("Title").setValue(titlee);
        notificationMref.child("Notification").child(Push_ID).child("Heading").setValue(paragrapg);
        notificationMref.child("Notification").child(Push_ID).child("Date").setValue(datee);
        mref.child("Notification_ImageURL").setValue(image_url.getText().toString());
        Toast.makeText(getApplicationContext(), "Data is Successfully Added", Toast.LENGTH_SHORT).show();

        title_story.getText().clear();
        date.getText().clear();
        pragraphofstory.getText().clear();

    }

    private void appControl() {
        checkButtonState();
        EditText Refer_App_url2;

        Refer_App_url2 = findViewById(R.id.Refer_App_url2);
        Refer_App_url_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Refer_App_url2.length() > 2) {
                    mref.child("Refer_App_url2").setValue(Refer_App_url2.getText().toString());
                    Toast.makeText(admin_panel.this, "Refer_App_url2 ADDED", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(admin_panel.this, "Field is Empty", Toast.LENGTH_SHORT).show();
            }

        });
        switch_Exit_Nav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    mref.child("switch_Exit_Nav").setValue("active");
                } else {
                    mref.child("switch_Exit_Nav").setValue("inactive");
                }

            }
        });

        switch_Activate_Ads.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mref.child("Ads").setValue("active");
                } else {
                    mref.child("Ads").setValue("inactive");
                }

            }
        });

        switch_App_Updating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mref.child("updatingApp_on_PLatStore").setValue("active");
                } else {
                    mref.child("updatingApp_on_PLatStore").setValue("inactive");
                }

            }
        });

    }


    private void checkButtonState() {

        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                image_url.setText((String) snapshot.child("Notification_ImageURL").getValue().toString().trim());
                String match = (String) snapshot.child("switch_Exit_Nav").getValue().toString().trim();

                if (match.equals("active")) {
                    switch_Exit_Nav.setChecked(true);

                } else {

                    switch_Exit_Nav.setChecked(false);
                }

                String Ads = (String) snapshot.child("Ads").getValue().toString().trim();

                if (Ads.equals("active")) {
                    switch_Activate_Ads.setChecked(true);

                } else {
                    switch_Activate_Ads.setChecked(false);
                }

                if (snapshot.child("updatingApp_on_PLatStore").getValue().toString().trim().equals("active")) {
                    switch_App_Updating.setChecked(true);
                } else {
                    switch_App_Updating.setChecked(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String decryption(String encryptedText) {

        int key = 5;
        String decryptedText = "";

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return decryptedText;
    }

}