package com.bhola.desiKahaniyaAdult;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoryPage extends AppCompatActivity {
    ImageView back, play_audio;
    TextView storyText, textsize, title_textview;
    DatabaseReference mref;
    String date, heading, title, href, title_category, relatedStories, storiesInsideParagraph;
    AlertDialog dialog;
    private TextToSpeech mTTS;
    Animation rotate_openAnim, rotate_closeAnim, fromBottom, toBottom;
    boolean clicked = false;
    FloatingActionButton add, share, copy, textsixe, favourite_button;
    String collection;
    String DB_TABLENUMBER;
    int _id;
    String android_id;
    SeekBar seekBar;
    Button button;
    private AdView mAdView;
    RewardedInterstitialAd mRewardedVideoAd;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;

    String TAG = "TAGA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_page);




        Intents_and_InitViews();
        actionBar();
        fetchStory();

        checkfavourite(DB_TABLENUMBER);


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Vibrator vibe = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, storyText.getText().toString());
                intent.setType("text/plain");
                intent = Intent.createChooser(intent, "Share By");

                v.getContext().startActivity(intent);
            }
        });
        favourite_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final MediaPlayer mp = MediaPlayer.create(v.getContext(), R.raw.sound);
                mp.start();

                Cursor cursor = new DatabaseHelper(StoryPage.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, DB_TABLENUMBER).readsingleRow(title);
                try {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        int liked = cursor.getInt(11);

                        if (liked == 0) {
                            favourite_button.setImageResource(R.drawable.favourite_active);
                            String res = new DatabaseHelper(StoryPage.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, DB_TABLENUMBER).updaterecord(title, 1);
                            Toast.makeText(StoryPage.this, "Downloaded to Offline Stories", Toast.LENGTH_SHORT).show();

                        } else {

                            favourite_button.setImageResource(R.drawable.favourite_inactive);
                            String res = new DatabaseHelper(StoryPage.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, DB_TABLENUMBER).updaterecord(title, 0);
                            Toast.makeText(StoryPage.this, "Removed from Offline Stories", Toast.LENGTH_SHORT).show();
                        }
                    }


                } finally {
                    cursor.close();
                }

            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Vibrator vibe = (Vibrator) v.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(80);//80 represents the milliseconds (the duration of the vibration)
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", storyText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(v.getContext(), "COPIED", Toast.LENGTH_SHORT).show();

            }
        });
        textsixe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                loadAlertdialog();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        storyText.setTextSize(progress);
                        textsize.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOnbuttonClicked();

            }
        });


    }

    private void fetchStory() {

        Cursor cursor = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readsingleRow(title);
        try {
            cursor.moveToFirst();
            if (cursor.getCount() != 0) {
                String story = cursor.getString(10);
                if (story.length() == 0) {
                    fetchStoryAPI();
                    return;
                }
                storyText.setText(story.toString().trim().replaceAll("\\/", ""));
            }

        } finally {
            cursor.close();
        }


    }

    private void fetchStoryAPI() {

        String API_URL = "https://clownfish-app-jn7w9.ondigitalocean.app/storiesDetails";
        RequestQueue requestQueue = Volley.newRequestQueue(StoryPage.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jSONArray = jsonObject.getJSONObject("data").getJSONArray("description");
                    ArrayList<String> arrayList = new ArrayList();
                    for (int i = 0; i <jSONArray.length() ; i++) {
                        arrayList.add((String) jSONArray.get(i));
                    }

                    String str = String.join("\n\n", arrayList);
                storyText.setText(str.toString().trim().replaceAll("\\/", ""));

//                   storiesInsideparagraphLayout.setVisibility(View);
//               relatedStoriesLayout.setVisibility(0);

                    new DatabaseHelper(StoryPage.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").updateStoryParagraph(title, str);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("href", href);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }




    private void Intents_and_InitViews() {

//Intents
        title = getIntent().getStringExtra("title");
        relatedStories = getIntent().getStringExtra("relatedStories");
        storiesInsideParagraph = getIntent().getStringExtra("storiesInsideParagraph");
        href = getIntent().getStringExtra("href");
        heading = getIntent().getStringExtra("Story");
        date = getIntent().getStringExtra("date");
        DB_TABLENUMBER = getIntent().getStringExtra("DB_TABLENUMBER");
        _id = getIntent().getIntExtra("_id", 0);

//InitViews
        storyText = findViewById(R.id.story_text);
        title_textview = findViewById(R.id.storyPage_Title_textview);
        textsixe = findViewById(R.id.floatingActionButtonText);
        copy = findViewById(R.id.floatingActionButtonCopy);
        share = findViewById(R.id.floatingActionButtonShare);
        favourite_button = findViewById(R.id.floatingActionFavouriteButton);
        add = findViewById(R.id.floatingActionButtonMain);


        rotate_openAnim = AnimationUtils.loadAnimation(this, R.anim.floatingbar_open_anim);
        rotate_closeAnim = AnimationUtils.loadAnimation(this, R.anim.floatingbar_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_floatingbar);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_floatingbar);


//Set Story and Tile
        title_textview.setText(title);
        setStoriesLinksInLayout();
    }


    private void checkfavourite(String DB_TABLENUMBER) {

        Cursor cursor = new DatabaseHelper(StoryPage.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, DB_TABLENUMBER).readsingleRow(title);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int liked = cursor.getInt(11);
            if (liked == 0) {
                favourite_button.setImageResource(R.drawable.favourite_inactive);
            } else {
                favourite_button.setImageResource(R.drawable.favourite_active);
            }
        }

        if (cursor != null) {
            cursor.close();
        }


    }

    private void addOnbuttonClicked() {

//setVisivility
        if (!clicked) {
            share.setVisibility(View.VISIBLE);
            copy.setVisibility(View.VISIBLE);
            textsixe.setVisibility(View.VISIBLE);
            favourite_button.setVisibility(View.VISIBLE);
        } else {
            share.setVisibility(View.INVISIBLE);
            copy.setVisibility(View.INVISIBLE);
            textsixe.setVisibility(View.INVISIBLE);
            favourite_button.setVisibility(View.VISIBLE);
        }

//setAnimation
        if (!clicked) {
            favourite_button.setAnimation(fromBottom);
            share.setAnimation(fromBottom);
            copy.setAnimation(fromBottom);
            textsixe.setAnimation(fromBottom);
            add.setAnimation(rotate_openAnim);
        } else {
            favourite_button.setAnimation(toBottom);
            share.setAnimation(toBottom);
            copy.setAnimation(toBottom);
            textsixe.setAnimation(toBottom);
            add.setAnimation(rotate_closeAnim);

        }

        clicked = !clicked;
    }


    private void actionBar() {

        text2Speech();
        title_category = getIntent().getStringExtra("bhola2");
        back = findViewById(R.id.back_arrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

    private void text2Speech() {
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            mTTS.setLanguage(new Locale("hin")); // Setting Hindi language
                        }
                    }
                });
            }
        });

    }

    private void speak(SeekBar seek_bar_pitch, SeekBar seek_bar_speed) {

        String text = heading;

        float pitch = (float) seek_bar_pitch.getProgress() / 50;
        if (pitch < 0.1) pitch = 0.1f;
        float speed = (float) seek_bar_speed.getProgress() / 50;
        if (speed < 0.1) speed = 0.1f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);


        int dividerLimit = 3900;
        if (text.length() >= dividerLimit) {
            int textLength = text.length();
            ArrayList<String> texts = new ArrayList<String>();
            int count = textLength / dividerLimit + ((textLength % dividerLimit == 0) ? 0 : 1);
            int start = 0;
            int end = text.indexOf(" ", dividerLimit);
            for (int i = 1; i <= count; i++) {
                texts.add(text.substring(start, end));
                start = end;
                if ((start + dividerLimit) < textLength) {
                    end = text.indexOf(" ", start + dividerLimit);
                } else {
                    end = textLength;
                }
            }
            for (int i = 0; i < texts.size(); i++) {
                mTTS.speak(texts.get(i), TextToSpeech.QUEUE_ADD, null);
            }
        } else {
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
//        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }


    void loadAlertdialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View promptView = inflater.inflate(R.layout.seekbar, null);
        builder.setView(promptView);
        builder.setCancelable(false);
        textsize = promptView.findViewById(R.id.textSize);

        dialog = builder.create();
        dialog.show();
        seekBar = promptView.findViewById(R.id.your_dialog_seekbar);
        button = promptView.findViewById(R.id.your_dialog_button);
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

    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();

        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mTTS != null) {
            mTTS.stop();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mTTS != null) {
            mTTS.stop();
        }
        super.onStop();
    }


    private void setStoriesLinksInLayout() {


        List<String> storiesInsideParagraphList = new ArrayList<String>(Arrays.asList(storiesInsideParagraph.split(",")));
        LinearLayout storiesInsideparagraphLayout = findViewById(R.id.storiesInsideparagraph);
        for (int i = 0; i < storiesInsideParagraphList.size(); i++) {
            String tagKey = storiesInsideParagraphList.get(i).trim();
            View view = getLayoutInflater().inflate(R.layout.tag, null);
            TextView tag = view.findViewById(R.id.tag);
            tag.setText(i + 1 + ". " + tagKey);
            tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    StoryItemModel storyItemModel = getDataFROM_DB(tagKey);
                    if (storyItemModel == null) {
                        Toast.makeText(StoryPage.this, "Story not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(v.getContext(), StoryPage.class);
                    intent.putExtra("category", title_category);
                    intent.putExtra("title", SplashScreen.decryption(storyItemModel.getTitle()));
                    intent.putExtra("date", storyItemModel.getDate());
                    intent.putExtra("href", SplashScreen.decryption(storyItemModel.getHref()));
                    intent.putExtra("relatedStories", storyItemModel.getRelatedStories());
                    intent.putExtra("storiesInsideParagraph", storyItemModel.getStoriesInsideParagraph());

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                }
            });
            storiesInsideparagraphLayout.addView(view);
        }


        List<String> myList = new ArrayList<String>(Arrays.asList(relatedStories.split(",")));
        LinearLayout relatedStoriesLayout = findViewById(R.id.relatedStoriesLayout);
        for (int i = 0; i < myList.size(); i++) {

            String tagKey = myList.get(i).trim();

            View view = getLayoutInflater().inflate(R.layout.tag, null);
            TextView relatedStoryText = view.findViewById(R.id.tag);
            relatedStoryText.setText(i + 1 + ". " + tagKey);

            relatedStoryText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoryItemModel storyItemModel = getDataFROM_DB(tagKey);
                    if (storyItemModel == null) {
                        Toast.makeText(StoryPage.this, "Story not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(v.getContext(), StoryPage.class);
                    intent.putExtra("category", title_category);
                    intent.putExtra("title", SplashScreen.decryption(storyItemModel.getTitle()));
                    intent.putExtra("date", storyItemModel.getDate());
                    intent.putExtra("href", SplashScreen.decryption(storyItemModel.getHref()));
                    intent.putExtra("relatedStories", storyItemModel.getRelatedStories());
                    intent.putExtra("storiesInsideParagraph", storyItemModel.getStoriesInsideParagraph());

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                }
            });
            relatedStoriesLayout.addView(view);
        }
    }

    private StoryItemModel getDataFROM_DB(String Title) {
        Cursor cursor = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readsingleRow(Title);

        if (cursor.getCount() == 0) {
            return null;
        }
        try {
            cursor.moveToFirst();
            StoryItemModel storyItemModel = new StoryItemModel(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getInt(9), cursor.getString(10), cursor.getInt(11), cursor.getInt(12), cursor.getString(13),cursor.getInt(14));

            return storyItemModel;
        } finally {
            cursor.close();
        }

    }


}