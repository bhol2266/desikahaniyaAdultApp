package com.bhola.desiKahaniyaAdult;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;

import soup.neumorphism.NeumorphCardView;

public class ftab1 extends Fragment {
    Context context = getActivity();
    soup.neumorphism.NeumorphCardView collection1, collection2, collection3, collection4, collection5, collection6;
    private String TAG = "TAGA";

    public ftab1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_ftab1, container, false);


        gridItems(view);
        return view;
    }


    private void gridItems(View view) {

        String[] Category_List = {"Latest Stories", "Aunty Sex Story", "Bhabhi Sex", "Desi Kahani", "Family Sex Stories", "First Time Sex", "Gay Sex Stories", "Group Sex Stories", "Indian Sex Stories", "Sali Sex", "Teacher Sex", "Teenage Girl", "XXX Kahani", "अन्तर्वासना", "हिंदी सेक्स स्टोरीज"};
        createGridItems(Category_List, view);
    }

    private void createGridItems(String[] Category_List, View view) {


        GridLayout gridLayout = view.findViewById(R.id.gridlayout);
        for (int i = 0; i < Category_List.length; i++) {


            String category = Category_List[i];

            View vieww = getLayoutInflater().inflate(R.layout.homepage_griditem, null);
            TextView categoryTextView = vieww.findViewById(R.id.Textview1);
            NeumorphCardView cardView = vieww.findViewById(R.id.cardview);

            categoryTextView.setText(category);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float requiredWidth = (float) (displayMetrics.widthPixels / 2.2);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) requiredWidth, 250);
            cardView.setLayoutParams(params);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), Collection_detail.class);
                    intent.putExtra("category", category);
                    intent.putExtra("href", category);
                    startActivity(intent);
                }
            });

            gridLayout.addView(vieww);
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("GridItems.json");
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
