package com.bhola.desiKahaniyaAdult;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SplashScreen extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    TextView textView;
    LottieAnimationView lottie;

    public static String TAG = "TAGA";
    public static String Notification_Intent_Firebase = "inactive";
    public static String Main_App_url1 = "https://play.google.com/store/apps/details?id=com.bhola.desiKahaniyaAdult";
    public static String Refer_App_url2 = "https://play.google.com/store/apps/developer?id=UK+DEVELOPERS";
    public static String Ads_State = "inactive";
    public static String DB_NAME = "desikahaniya.db";
    public static String exit_Refer_appNavigation = "inactive";
    public static String App_updating = "inactive";
    public static String Notification_ImageURL = "https://hotdesipics.co/wp-content/uploads/2022/06/Hot-Bangla-Boudi-Ki-Big-Boobs-Nangi-Selfies-_002.jpg";
    DatabaseReference url_mref;
    public static int Login_Times = 0;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    RewardedInterstitialAd mRewardedVideoAd;

    public static int DB_VERSION = 1;
    public static int DB_VERSION_INSIDE_TABLE = 1;
    Handler handlerr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        textView = findViewById(R.id.textView_splashscreen);
        lottie = findViewById(R.id.lottie);

        copyDatabase();
        allUrl();
        sharedPrefrences();
        updateStoriesInDB();


        textView.setAnimation(bottomAnim);
        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LinearLayout progressbar = findViewById(R.id.progressbar);
                progressbar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        generateNotification();
        generateFCMToken();

    }


    private void copyDatabase() {


//      Check For Database is Available in Device or not
        DatabaseHelper databaseHelper = new DatabaseHelper(this, DB_NAME, DB_VERSION, "StoryItems");
        try {
            databaseHelper.CheckDatabases();
        } catch (Exception e) {
            e.printStackTrace();

        }

//        for (int i = 7; i <=10 ; i++) {
//
//            ArrayList<String> id = new ArrayList<>();
//            ArrayList<String> Datelist = new ArrayList<>();
//            ArrayList<String> Headinglist = new ArrayList<>();
//            ArrayList<String> Titlelist = new ArrayList<>();
//
////            Read Data
//            Cursor cursor = new DatabaseHelper(this, "MCB_Story", SplashScreen.DB_VERSION, "Collection" + i).readalldata();
//            while (cursor.moveToNext()) {
//                String encrypted_paragrapg = encryption(cursor.getString(2));
//                DatabaseHelper update_Encryption = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "Collection" + i);
//                String ress = update_Encryption.updateEncryptStory(cursor.getInt(0), encrypted_paragrapg);
//                Log.d(TAG, "UPDATED DATA: " + ress + cursor.getInt(0));
//
//            }
//        }


    }


    private void allUrl() {
        if (!isInternetAvailable(SplashScreen.this)) {

            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler_forIntent();
                }
            }, 2000);

            return;
        } else {
            handlerr = new Handler();
            handlerr.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler_forIntent();
                }
            }, 9000);

        }


        url_mref = FirebaseDatabase.getInstance().getReference().child("Hindi_desi_Kahani_Adult");
        url_mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Refer_App_url2 = (String) snapshot.child("Refer_App_url2").getValue();
                exit_Refer_appNavigation = (String) snapshot.child("switch_Exit_Nav").getValue();
                Ads_State = (String) snapshot.child("Ads").getValue();
                App_updating = (String) snapshot.child("updatingApp_on_PLatStore").getValue();
                Notification_ImageURL = (String) snapshot.child("Notification_ImageURL").getValue();



                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handlerr.removeCallbacksAndMessages(null);
                        handler_forIntent();
                    }
                }, 2500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });


    }


    private void generateNotification() {


        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    String msg;
                    if (!task.isSuccessful()) {
                        msg = "Failed";
                        Toast.makeText(SplashScreen.this,
                                msg,
                                Toast.LENGTH_SHORT).show();
                    }


                });
    }


    private void handler_forIntent() {
        lottie.cancelAnimation();
        if (Notification_Intent_Firebase.equals("active")) {
            Intent intent = new Intent(getApplicationContext(), Notification_Story_Detail.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), Collection_GridView.class);
            startActivity(intent);
        }
        finish();
    }


    private void generateFCMToken() {

        if (getIntent() != null && getIntent().hasExtra("KEY1")) {
            if (getIntent().getExtras().getString("KEY1").equals("Notification_Story")) {
                Notification_Intent_Firebase = "active";

            }

        }
    }



    boolean isInternetAvailable(Context context) {
        if (context == null) return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut", "Network is available : FALSE ");
        return false;
    }


    private void sharedPrefrences() {

        //Reading Login Times
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        int a = sh.getInt("loginTimes", 0);
        Login_Times = a + 1;

        // Updating Login Times data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putInt("loginTimes", a + 1);
        myEdit.commit();

    }

    private void updateStoriesInDB() {

        int completeDate = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readLatestStoryDate();

        String API_URL = "https://clownfish-app-jn7w9.ondigitalocean.app/updateStories_inDB";
        RequestQueue requestQueue = Volley.newRequestQueue(SplashScreen.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray m_jArry = jsonObject.getJSONArray("data");

                    ArrayList<HashMap<String, String>> Category_List = new ArrayList<HashMap<String, String>>();
                    HashMap<String, String> m_li;
                    for (int i = 0; i < m_jArry.length(); i++) {

                        JSONObject json_obj = m_jArry.getJSONObject(i);
                        String Title = json_obj.getString("Title");
                        String href = json_obj.getString("href");
                        String date = json_obj.getString("date");
                        String views = json_obj.getString("views");
                        int completeDate = json_obj.getInt("completeDate");
                        String audiolink = json_obj.getString("audiolink");


                        JSONObject categoryObject = json_obj.getJSONObject("category");
                        String category = categoryObject.getString("title");

                        JSONArray storyArray = json_obj.getJSONArray("description");
                        ArrayList<String> storyArrayList = new ArrayList();
                        for (int j = 0; j < storyArray.length(); j++) {
                            storyArrayList.add(storyArray.getString(j));
                        }
                        String description = String.join("\n\n", storyArrayList);


                        JSONArray tagsArray = json_obj.getJSONArray("tagsArray");
                        ArrayList<String> tagsList = new ArrayList();
                        for (int j = 0; j < tagsArray.length(); j++) {
                            tagsList.add(tagsArray.getString(j));
                        }
                        String tags = String.join(", ", tagsList);


                        JSONArray relatedStoriesLinks_Array = json_obj.getJSONArray("relatedStoriesLinks");
                        ArrayList<String> relatedStoriesList = new ArrayList();
                        for (int j = 0; j < relatedStoriesLinks_Array.length(); j++) {
                            JSONObject relatedStoriesLinksObject = (JSONObject) relatedStoriesLinks_Array.get(j);
                            relatedStoriesList.add(relatedStoriesLinksObject.getString("title"));
                        }
                        String relatedStories = String.join(", ", relatedStoriesList);

                        JSONArray storiesInsideParagraph_Array = json_obj.getJSONArray("storiesLink_insideParagrapgh");
                        ArrayList<String> storiesInsideParagraphList = new ArrayList();
                        for (int j = 0; j < storiesInsideParagraph_Array.length(); j++) {
                            JSONObject obj = (JSONObject) storiesInsideParagraph_Array.get(j);
                            storiesInsideParagraphList.add(obj.getString("title"));
                        }
                        String storiesInsideParagraph = String.join(", ", storiesInsideParagraphList);



                        //Add your values in your `ArrayList` as below:
                        m_li = new HashMap<String, String>();
                        m_li.put("Title", Title);
                        m_li.put("href", href);
                        m_li.put("date", date);
                        m_li.put("views", views);
                        m_li.put("description", description.substring(0,100));
                        m_li.put("story", description);
                        m_li.put("audiolink", audiolink);
                        m_li.put("category", category);
                        m_li.put("tags", tags);
                        m_li.put("relatedStories", relatedStories);
                        m_li.put("completeDate", String.valueOf(completeDate));
                        m_li.put("storiesInsideParagraph", storiesInsideParagraph);
                        Category_List.add(m_li);


                        DatabaseHelper insertRecord = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems");
                        String res = insertRecord.addstories(m_li);
                        Log.d(TAG, "INSERT DATA: " + res);
                    }


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
                params.put("completeDate", String.valueOf(completeDate));
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


    private String encryption(String text) {

        int key = 5;
        char[] chars = text.toCharArray();
        String encryptedText = "";
        String decryptedText = "";

        //Encryption
        for (char c : chars) {
            c += key;
            encryptedText = encryptedText + c;
        }

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return encryptedText;
    }

//    private void readJSON(String Filename, String collectionName) {
//        try {
//            JSONArray array = new JSONArray(loadJSONFromAsset(Filename));
//            ArrayList<String> titlelist = new ArrayList<String>();
//            ArrayList<String> storylist = new ArrayList<String>();
//            ArrayList<String> authorList = new ArrayList<String>();
//            ArrayList<String> dateList = new ArrayList<String>();
//
//            ArrayList<String> data = new ArrayList<String>();
//
//
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject obj = (JSONObject) array.get(i);
//                titlelist.add(obj.getString("title"));
//                authorList.add(obj.getString("author"));
//                dateList.add(obj.getString("date"));
//
//                //Story is a array
//                JSONArray story_array = obj.getJSONArray("story");
//                String paragrapg = "";
//                for (int g = 0; g < story_array.length(); g++) {
//                    paragrapg = paragrapg + "\n" + story_array.get(g).toString() + "\n\r";
//                }
//                storylist.add(paragrapg);
//            }
//
//
//            for (int i = 0; i < titlelist.size(); i++) {
//                if (titlelist.get(i).trim().length() >= 1) {
//                    DatabaseHelper insertRecord = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, collectionName);
//                    String res = insertRecord.addstories(dateList.get(i) + " by " + authorList.get(i), encryption(storylist.get(i)), titlelist.get(i));
//                    Log.d(TAG, "INSERT DATA: " + res);
//                }
//            }
//        } catch (JSONException e) {
//            Log.d(TAG, "getMessage: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open(filename + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (facebook_IntertitialAds != null) {
            facebook_IntertitialAds.destroy();

        }
    }

    public static String decryption(String encryptedText) {

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