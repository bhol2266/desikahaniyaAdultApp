package com.bhola.desiKahaniyaAdult;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ParseException;
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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SplashScreen extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    TextView textView;
    LottieAnimationView lottie;
    public static String TAG = "TAGA";
    public static String Notification_Intent_Firebase = "inactive";
    public static String Main_App_url1 = "https://play.google.com/store/apps/details?id=com.bhola.desiKahaniyaAdult";
    public static String Refer_App_url2 = "https://play.google.com/store/apps/developer?id=Marveltech+Apps";
    public static String Ads_State = "inactive";
    public static String Ad_Network_Name = "admob";
    public static String DB_NAME = "desikahaniya";
    public static String exit_Refer_appNavigation = "inactive";
    public static String App_updating = "active";
    public static String databaseURL = "https://bucket2266.s3.ap-south-1.amazonaws.com/"; //default
    public static String Notification_ImageURL = "https://hotdesipics.co/wp-content/uploads/2022/06/Hot-Bangla-Boudi-Ki-Big-Boobs-Nangi-Selfies-_002.jpg";
    DatabaseReference url_mref;
    public static int Login_Times = 0;
    public static boolean homepageAdShown = false;
    public static int Native_Ad_Interval = 4;

    com.facebook.ads.InterstitialAd facebook_IntertitialAds;


    public static int DB_VERSION = 1;//manual set
    public static int currentApp_Version = 3;//manual set
    public static int Firebase_Version_Code = 1;//manual set
    public static int DB_VERSION_INSIDE_TABLE = 2; //manual set
    Handler handlerr;

    public static String apk_Downloadlink = "";
    public static String countryLocation = "";
    public static String countryCode = "";
    public static boolean update_Mandatory = false;
    public static String DB_TABLE_NAME = "";  //This is a table name "StoryItems or FakeStory"
    public static String API_URL = "https://clownfish-app-jn7w9.ondigitalocean.app/";
    private FirebaseAnalytics mFirebaseAnalytics;
    public static boolean Vip_Member = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullscreenMode();
        setContentView(R.layout.splash_screen);


        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        textView = findViewById(R.id.textView_splashscreen);
        lottie = findViewById(R.id.lottie);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        copyDatabase();
        allUrl();
        sharedPrefrences();


        if (SplashScreen.Login_Times > 5) {
            updateStoriesInDB();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LinearLayout progressbar = findViewById(R.id.progressbar);
                progressbar.setVisibility(View.VISIBLE);
            }
        }, 1500);


        textView.setAnimation(bottomAnim);
        lottie.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

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

    }


    private void trasferData() {

        String[] Category_List = {"Audio_Story_Fake", "Audio_Story"};

        for (int i = 0; i < Category_List.length; i++) {
            ArrayList<Map<String, String>> tempData = new ArrayList<>();

            Cursor cursor = new DatabaseLoveStory(SplashScreen.this, "MCB_Story", 5, Category_List[i]).readalldata();
            while (cursor.moveToNext()) {
                Map<String, String> mapObj = new HashMap<>();
                mapObj.put("Title", cursor.getString(1));
                mapObj.put("story", "");
                mapObj.put("href", cursor.getString(1));
                mapObj.put("date", "04-02-2023");
                mapObj.put("views", "6541");
                mapObj.put("description", "");
                mapObj.put("audiolink", decryption(cursor.getString(2)));
                mapObj.put("category", Category_List[i]);
                mapObj.put("tags", "");
                mapObj.put("completeDate", "20230204");
                mapObj.put("storiesInsideParagraph", "");
                mapObj.put("relatedStories", "");

                tempData.add(mapObj);

            }
            cursor.close();

            for (int j = 0; j <= 19; j++) {
                Map<String, String> mapOb = tempData.get(j);

                String res = new DatabaseHelper(SplashScreen.this, DB_NAME, DB_VERSION, "FakeStory").addstories((HashMap<String, String>) mapOb);
                Log.d(TAG, "onSuccess: " + res);
            }
        }

    }


    private void allUrl() {
        if (!isInternetAvailable(SplashScreen.this)) {

            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Login_Times > 5) {
                        App_updating = "inactive";
                        Ads_State = "active";
                        Ad_Network_Name = "admob";
                    }
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
                Ad_Network_Name = (String) snapshot.child("Ad_Network").getValue();

                Firebase_Version_Code = snapshot.child("version_code").getValue(Integer.class);
                apk_Downloadlink = (String) snapshot.child("apk_Downloadlink").getValue();
                update_Mandatory = (boolean) snapshot.child("update_Mandatory").getValue();
                databaseURL = (String) snapshot.child("databaseURL").getValue();
                API_URL = (String) snapshot.child("API_URL").getValue();


                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handlerr.removeCallbacksAndMessages(null);
                        handler_forIntent();
                    }
                }, 1500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (Login_Times > 5) {
                    App_updating = "inactive";
                    Ads_State = "active";
                    Ad_Network_Name = "admob";
                }
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }

        });


    }


    private void generateNotification() {


        FirebaseMessaging.getInstance().subscribeToTopic("all").addOnCompleteListener(task -> {
            String msg;
            if (!task.isSuccessful()) {
                msg = "Failed";
                Toast.makeText(SplashScreen.this, msg, Toast.LENGTH_SHORT).show();
            }


        });
    }


    private void handler_forIntent() {
        lottie.cancelAnimation();

        if (SplashScreen.Vip_Member) {
            vipMemberPrivileges();
        }

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

    static boolean isInternetAvailable(Context context) {
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

        //Reading purchase Token
        SharedPreferences sharedPreferences1 = SplashScreen.this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String purchaseToken = sharedPreferences1.getString("purchaseToken", "not set");
        int validity_period = sharedPreferences1.getInt("validity_period", 0);
        String purchase_date = sharedPreferences1.getString("purchase_date", "not set");

        if (purchaseToken.equals("not set") || validity_period == 0) {
            return;
        }


        // Convert String back to Date
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // Parse the original date
            Date originalDate = dateFormat.parse(purchase_date);

            // Create a Calendar instance and set it to the original date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(originalDate);

            // Add 30 days to the original date
            calendar.add(Calendar.DAY_OF_MONTH, validity_period);

            // Get the resulting date
            Date newDate = calendar.getTime();

            // Format the new date as a string
            String expirationDateString = dateFormat.format(newDate);

            Log.d(TAG, "Membership Expiry Date: " + expirationDateString);
            // Get the current date
            Date currentDate = new Date();
            String currentDateString = dateFormat.format(currentDate);


            // Compare the new date with the current date
            if (expirationDateString.equals(currentDateString)) {
                Vip_Member = false;
                Toast.makeText(this, "Your Membership has expried", Toast.LENGTH_SHORT).show();
            } else if (newDate.after(currentDate)) {
                Vip_Member = true;

            } else if (newDate.before(currentDate)) {
                Toast.makeText(this, "Your Membership has expried", Toast.LENGTH_SHORT).show();
                Vip_Member = false;

            }


        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }

    }

    private void vipMemberPrivileges() {
        App_updating = "inactive";
        Ads_State = "inactive";
        Login_Times = 10;
    }

    private void updateStoriesInDB() {

        int completeDate = new DatabaseHelper(this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems").readLatestStoryDate();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference storiesRef = db.collection("storymodels");

        storiesRef.whereGreaterThan("completeDate", completeDate)
                .orderBy("completeDate", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Access your document data here

                                Map<String, Object> data = document.getData();
                                HashMap<String, String> m_li = Utils.FirebaseObject_TO_HashMap(data);


                                DatabaseHelper insertRecord = new DatabaseHelper(getApplicationContext(), SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "StoryItems");
                                String res = insertRecord.addstories(m_li);
                                Log.d(TAG, "INSERT DATA: " + res);


                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

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

    public static String loadJSONFromAsset(String filename, Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
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

    private void fullscreenMode() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

}