package com.bhola.desiKahaniyaAdult;

import android.app.Activity;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    String userFcmToken;
    String title;
    String body;
    String image_url;
    String toGoActivity;
    Activity mActivity;


    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAAZi_XrcY:APA91bGSJ9M2kGrB1KrK2Wrs6bt_3oVPLawQcRB2M4yfWqTIBFpH9E8Mhnavfpx3VKcoWhWX-_3daFuRNeqfT_avtKWc0OyoTmpOxlsqStkMSDZJVI8s8u-nUt50iKUyZi6WsoUiF1EY";

    public FcmNotificationsSender(String userFcmToken, String title, String body, String image, String toGoActivity, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.image_url = image;
        this.toGoActivity = toGoActivity;
        this.mActivity = mActivity;


    }

    public void SendNotifications() {

        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("image", image_url);
            notiObject.put("icon", "app_icon"); // enter icon that exists in drawable only


            JSONObject extradata = new JSONObject();
            extradata.put("KEY1", toGoActivity);

            mainObj.put("notification", notiObject);
            mainObj.put("data", extradata);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error
                    Toast.makeText(mActivity, error.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
}
