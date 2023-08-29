package com.bhola.desiKahaniyaAdult;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ftab2 extends Fragment {

    List<String> storyURL;
    List<String> storyName;
    public static AudioStory_Details_Adapter adapter2;
    StorageReference mStorageReference;
    ProgressBar progressBar2;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    String TAG = "TAGA";
    Context context;
    boolean storyLocked = false;
    public static ArrayList<Audio_Model> collectionData;


    public ftab2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ftab2, container, false);

        context = view.getContext();
        storyURL = new ArrayList<String>();
        storyName = new ArrayList<String>();
        progressBar2 = view.findViewById(R.id.progressBar2);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("audiostories");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadAudioDatabase(view);

        if (!SplashScreen.isInternetAvailable(getContext())) {
            Toast.makeText(getContext(), "Check Internet Connection!", Toast.LENGTH_SHORT).show();
        } else {

        }
        return view;
    }


    private void loadAudioDatabase(View view) {

        collectionData = new ArrayList<Audio_Model>();
        Cursor cursor;
        if (SplashScreen.App_updating.equals("active")) {
            //fake content while upadting app

            readStoryFromJson("Fake");
            Collections.shuffle(collectionData);

        } else {

            if (SplashScreen.Login_Times < 4) {
                //Mixed content
                readStoryFromJson("Fake");
                readStoryFromJson("Censored");
                Collections.shuffle(collectionData);

            } else if (SplashScreen.Login_Times < 6) {
                // censored Content
                readStoryFromJson("Censored");
                readStoryFromJson("All");

                storyLocked = true;

            } else {
                // full Content
                readStoryFromJson("All");
                storyLocked = true;
            }
        }


        if (SplashScreen.App_updating.equals("active")) {
            collectionData.clear();
        }
        adapter2 = new AudioStory_Details_Adapter(collectionData, getActivity(), storyLocked);
        recyclerView.setAdapter(adapter2);
        progressBar2.setVisibility(View.GONE);
        adapter2.notifyDataSetChanged();


    }

    private void readStoryFromJson(String story_type) {

        String filename = "";
        if (story_type.equals("Fake")) {
            filename = "audiostories_f.json";
        }
        if (story_type.equals("Censored")) {
            filename = "audiostories_c.json";
        }
        if (story_type.equals("All")) {
            filename = "audiostories_a.json";
        }

        boolean dataAvailable = retreive_sharedPreferences(filename.replace(".json", ""), context);

        if (!dataAvailable) {
            String json = SplashScreen.loadJSONFromAsset(filename, context);
            try {

                ArrayList<Audio_Model> tempList = new ArrayList<Audio_Model>();
                JSONArray jsonArray = new JSONArray(json);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    int like = jsonObject.getInt("Like");
                    int read = jsonObject.getInt("Read");
                    String title = jsonObject.getString("Title");
                    String fileName = jsonObject.getString("filename");
                    String audiolink = jsonObject.getString("audiolink");
                    String href = jsonObject.getString("href");

                    Audio_Model myItem = new Audio_Model();
                    myItem.setLike(like);
                    myItem.setRead(read);
                    myItem.setTitle(title);
                    myItem.setAudiolink(audiolink);
                    myItem.setHref(href);
                    myItem.setStoryFileName(fileName);


                    tempList.add(myItem);
                    // Now you can use the myItem object as needed
                }

                save_sharedPrefrence(context, tempList, filename);
                collectionData.addAll(tempList);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }


    public static boolean retreive_sharedPreferences(String filename, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("audio_stories", MODE_PRIVATE);
        String json = sharedPreferences.getString(filename, null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Audio_Model>>() {
        }.getType();


        if (json == null) {
            // Handle case when no ArrayList is saved in SharedPreferences
            return false;
        } else {
            ArrayList<Audio_Model> tempList = new ArrayList<Audio_Model>();
            tempList = gson.fromJson(json, type);
            collectionData.addAll(tempList);

            return true;
        }


    }


    public static void save_sharedPrefrence(Context context, ArrayList<Audio_Model> iemList, String filename) {


        SharedPreferences sharedPreferences = context.getSharedPreferences("audio_stories", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


// Convert the ArrayList to JSON string
        Gson gson = new Gson();
        String json = gson.toJson(iemList);

// Save the JSON string to SharedPreferences
        editor.putString(filename, json);
        editor.apply();

    }
}


class AudioStory_Details_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    boolean storyLocked;
    RewardedInterstitialAd mRewardedVideoAd;
    com.facebook.ads.InterstitialAd facebook_IntertitialAds;
    ArrayList<Audio_Model> collectionData = new ArrayList<Audio_Model>();


    public AudioStory_Details_Adapter(ArrayList<Audio_Model> data, FragmentActivity activity, boolean storyLocked) {
        this.context = activity;
        this.collectionData = data;
        this.storyLocked = storyLocked;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View Story_ROW_viewHolder = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_layout, parent, false);
        return new Story_ROW_viewHolder(Story_ROW_viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int POSITION = position;

//        AudioModel model = (AudioModel) collectionData.get(position);
        Audio_Model storyItemModel = (Audio_Model) collectionData.get(position);

        String filename = SplashScreen.decryption(storyItemModel.getTitle().replace("-", " ").trim());
        ((Story_ROW_viewHolder) holder).imageview.setImageResource(R.drawable.mp3);


        ((Story_ROW_viewHolder) holder).title.setText(filename);
//        ((Story_ROW_viewHolder) holder).date.setText(storyItemModel.getDate());


        if (storyItemModel.getRead() == 1) {
            ((Story_ROW_viewHolder) holder).title.setTextColor(Color.parseColor("#9A3412"));
        } else {
            ((Story_ROW_viewHolder) holder).title.setTextColor(Color.parseColor("#374151"));
        }
        if (!SplashScreen.Vip_Member && storyLocked) {
            if (holder.getAbsoluteAdapterPosition() > 1) {
                ((Story_ROW_viewHolder) holder).lock.setVisibility(View.VISIBLE);
            } else {
                ((Story_ROW_viewHolder) holder).lock.setVisibility(View.GONE);

            }
        }


        ((Story_ROW_viewHolder) holder).recyclerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((Story_ROW_viewHolder) holder).lock.getVisibility() == View.VISIBLE) {
                    Toast.makeText(context, "Become DesiKahani Member to listen this story", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, VipMembership.class));
                } else {
                    if (SplashScreen.isInternetAvailable(v.getContext())) {
                        Intent intent = new Intent(context, AudioPlayer.class);
                        intent.putExtra("storyURL", storyItemModel.getAudiolink());
                        intent.putExtra("storyName", filename);
                        intent.putExtra("storyFileName", storyItemModel.getStoryFileName());
                        intent.putExtra("audioHref", storyItemModel.getHref());
                        intent.putExtra("title", storyItemModel.getTitle());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        v.getContext().startActivity(intent);
                    } else {
                        Toast.makeText(context, "Check Internet Connection!" + System.lineSeparator() +
                                "इंटरनेट कनेक्शन चेक करे", Toast.LENGTH_SHORT).show();
                    }
                }

            }

        });

//        if (SplashScreen.Ads_State.equals("active")) {
//            loadNativeAds(((Story_ROW_viewHolder) holder).template, ((Story_ROW_viewHolder) holder).facebook_BannerAd_layout, holder.getAbsoluteAdapterPosition());
//        }


    }


    @Override
    public int getItemCount() {
        return collectionData.size();
    }


    public class Story_ROW_viewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView date;
        TextView views;
        ImageView lock;
        ImageView imageview;
        LinearLayout recyclerview;
        TemplateView template;
        LinearLayout facebook_BannerAd_layout;

        public Story_ROW_viewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerview = itemView.findViewById(R.id.recyclerviewLayout);
            imageview = itemView.findViewById(R.id.imageview);
            title = itemView.findViewById(R.id.titlee);
            date = itemView.findViewById(R.id.date_recyclerview);
            lock = itemView.findViewById(R.id.lock);
            views = itemView.findViewById(R.id.views);
            facebook_BannerAd_layout = itemView.findViewById(R.id.banner_container);

        }
    }
}

class Audio_Model {
    private int Like;
    private int Read;
    private String Title;
    private String StoryFileName;
    private String audiolink;
    private String href;

    public Audio_Model() {
    }

    public Audio_Model(int like, int read, String title, String storyFileName, String audiolink, String href) {
        Like = like;
        Read = read;
        Title = title;
        StoryFileName = storyFileName;
        this.audiolink = audiolink;
        this.href = href;
    }

    public int getLike() {
        return Like;
    }

    public void setLike(int like) {
        Like = like;
    }

    public int getRead() {
        return Read;
    }

    public void setRead(int read) {
        Read = read;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getStoryFileName() {
        return StoryFileName;
    }

    public void setStoryFileName(String storyFileName) {
        StoryFileName = storyFileName;
    }

    public String getAudiolink() {
        return audiolink;
    }

    public void setAudiolink(String audiolink) {
        this.audiolink = audiolink;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}



