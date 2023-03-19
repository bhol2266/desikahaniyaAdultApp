package com.bhola.desiKahaniyaAdult;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class
Collection_GridView extends AppCompatActivity {
    String Ads_State;
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    AlertDialog dialog;

    Context context;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    com.facebook.ads.AdView facebook_adView;
    String TAG = "TAGA";
    AdView mAdView;

    ViewPager viewPager;
    TabLayout tabLayout;
    TabItem tabItem1, tabItem2;
    PageAdapter pageAdapter;
    private ReviewManager reviewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection__grid_view);



        navigationDrawer();
        tabview();
//        insertDataIN_Database();



    }




    private void tabview() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout1);
        tabItem1 = (TabItem) findViewById(R.id.tab1);
        tabItem2 = (TabItem) findViewById(R.id.tab2);
        viewPager = (ViewPager) findViewById(R.id.vpager);

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0 || tab.getPosition() == 1)
                    pageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //listen for scroll or page change

    }





    @Override
    public void onBackPressed() {

        exit_dialog();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (facebook_adView != null) {
            facebook_adView.destroy();
        }

        if (facebook_IntertitialAds != null) {
            facebook_IntertitialAds.destroy();

        }
    }

    private void exit_dialog() {


        Button exit, exit2;
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(nav.getContext());
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.exit_dialog, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        if (!(SplashScreen.Login_Times < 4)) {
            TextView exitMSG;
            exitMSG = promptView.findViewById(R.id.exitMSG);
            exitMSG.setVisibility(View.VISIBLE);
            init(); // Show PLay store Review option
        }


        exit = promptView.findViewById(R.id.exit_button2);
        exit2 = promptView.findViewById(R.id.exit_button1);


        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SplashScreen.exit_Refer_appNavigation.equals("active") && SplashScreen.Login_Times < 2 && SplashScreen.Refer_App_url2.length() > 10) {

                    Intent j = new Intent(Intent.ACTION_VIEW);
                    j.setData(Uri.parse(SplashScreen.Refer_App_url2));
                    try {
                        startActivity(j);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finishAffinity();
                    System.exit(0);
                    finish();
                    dialog.dismiss();

                } else {

                    finishAffinity();
                    finish();
                    System.exit(0);
                    finish();
                    dialog.dismiss();

                }
            }
        });

        exit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });


        dialog = builder.create();
        dialog.show();

    }


    private void navigationDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav = (NavigationView) findViewById(R.id.navmenu);
        nav.setItemIconTintList(null);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.menu_downloads:
                        Intent intent = new Intent(getApplicationContext(), Download_Detail.class);
                        intent.putExtra("Ads_Status", Ads_State);
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_audio:
                        startActivity(new Intent(getApplicationContext(), OfflineAudioStory.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_contacts:
                        TextView whatsapp, email;
                        AlertDialog.Builder builder = new AlertDialog.Builder(Collection_GridView.this);
                        LayoutInflater inflater = LayoutInflater.from(Collection_GridView.this);
                        View promptView = inflater.inflate(R.layout.navigation_menu_contacts, null);
                        builder.setView(promptView);
                        builder.setCancelable(true);
                        whatsapp = promptView.findViewById(R.id.whatsappnumber);
                        whatsapp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", "+919108825914");
                                clipboard.setPrimaryClip(clip);
                                navigationDrawer();
                                Toast.makeText(v.getContext(), "COPIED NUMBER", Toast.LENGTH_SHORT).show();
                            }
                        });
                        email = promptView.findViewById(R.id.email);
                        email.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("label", "bhola2266@gmail.com");
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(v.getContext(), "COPIED EMAIL", Toast.LENGTH_SHORT).show();
                            }
                        });


                        dialog = builder.create();
                        dialog.show();
                        drawerLayout.closeDrawer(GravityCompat.START);

                        break;

                    case R.id.menu_rating:


                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(SplashScreen.Main_App_url1));
                        startActivity(i);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.menu_notificaton:
                        Intent intent2 = new Intent(getApplicationContext(), Notification_Story_Detail.class);
                        intent2.putExtra("Ads_Status", Ads_State);
                        startActivity(intent2);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_share_app:
                        String share_msg = "Hi I have downloaded Hindi Desi Kahani App.\n" +
                                "It is a best app for Real Desi Bed Stories.\n" +
                                "You should also try\n" +
                                SplashScreen.Main_App_url1;
                        Intent intent1 = new Intent();
                        intent1.setAction(Intent.ACTION_SEND);
                        intent1.putExtra(Intent.EXTRA_TEXT, share_msg);
                        intent1.setType("text/plain");
                        intent = Intent.createChooser(intent1, "Share By");
                        startActivity(intent);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_second_app:

                        if (SplashScreen.Refer_App_url2.length() > 10 && SplashScreen.exit_Refer_appNavigation.equals("active")) {

                            Intent j = new Intent(Intent.ACTION_VIEW);
                            j.setData(Uri.parse(SplashScreen.Refer_App_url2));
                            Log.d("dghsdfghs", "Refer_App_url2: " + SplashScreen.Refer_App_url2);
                            startActivity(j);
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        break;

                    case R.id.Privacy_Policy:

                        Intent i5 = new Intent(Intent.ACTION_VIEW);
                        i5.setData(Uri.parse("https://sites.google.com/view/desikhaniya"));
                        startActivity(i5);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.About_Us:

                        final androidx.appcompat.app.AlertDialog.Builder builder2 = new androidx.appcompat.app.AlertDialog.Builder(nav.getContext());
                        LayoutInflater inflater2 = LayoutInflater.from(getApplicationContext());
                        View promptView2 = inflater2.inflate(R.layout.about_us, null);
                        builder2.setView(promptView2);
                        builder2.setCancelable(true);


                        dialog = builder2.create();
                        dialog.show();

                        break;


                    case R.id.Terms_and_Condition:
                        Intent intent27 = new Intent(getApplicationContext(), TermsAndConditions.class);
                        intent27.putExtra("Ads_Status", Ads_State);
                        startActivity(intent27);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }

                return true;
            }
        });
    }


    private void init() {
        reviewManager = ReviewManagerFactory.create(this);
        // Referencing the button
        showRateApp();
    }


    // Shows the app rate dialog box using In-App review API
    // The app rate dialog box might or might not shown depending
    // on the Quotas and limitations
    public void showRateApp() {
        com.google.android.play.core.tasks.Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Getting the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();

                Task<Void> flow = reviewManager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                });
            }
        });
    }


    private void insertDataIN_Database() {
        ArrayList<HashMap<String, String>> Category_List = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> m_li;

        try {

            JSONArray m_jArry = new JSONArray(loadJSONFromAsset());
            for (int i = 0; i < m_jArry.length(); i++) {


                JSONObject json_obj = m_jArry.getJSONObject(i);

                String Title = json_obj.getString("Title");
                String href = json_obj.getString("href");
                String date = json_obj.getString("date");
                int completeDate = json_obj.getInt("completeDate");
                String views = json_obj.getString("views");
                String description = json_obj.getString("description");
                String audiolink = json_obj.getString("audiolink");

                JSONObject categoryObject = json_obj.getJSONObject("category");
                String category = categoryObject.getString("title");

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
                m_li.put("description", description);
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


        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Datebase Error: " + e.getMessage());

        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = Collection_GridView.this.getAssets().open("storymodels.json");
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

}

