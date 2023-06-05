package com.bhola.desiKahaniyaAdult;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.ImmutableList;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import soup.neumorphism.NeumorphCardView;

public class VipMembership extends AppCompatActivity {


    AlertDialog dialog;
    private BillingClient billingClient;
    LinearLayout progressBar;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_membership);
        actionBar();
        progressBar = findViewById(R.id.progressBar);


        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {

                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase : list) {
                                    verifyPurchase(purchase);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Successfull", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(VipMembership.this, SplashScreen.class));

                                }
                            } else {
                                // Handle any other error codes.
                                Toast.makeText(this, "Something went wrong try again!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);

                            }

                        }
                ).build();


        //start the connection after initializing the billing client
        connectGooglePlayBilling();


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView price2 = findViewById(R.id.price2);
                Button refreshBtn=findViewById(R.id.refreshBtn);

                if (price2.getText().equals("not set")) {
                    refreshBtn.setVisibility(View.VISIBLE);
                    refreshBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            connectGooglePlayBilling();
                        }
                    });

                }
            }
        }, 2000);

    }


    void connectGooglePlayBilling() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    getProductDetails();
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectGooglePlayBilling();

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

    }

    private void getProductDetails() {

        List<String> productIds = new ArrayList<>();
        List<QueryProductDetailsParams.Product> list = new ArrayList<>();
        productIds.add("vip_1");
        productIds.add("vip_3");
        productIds.add("vip_12");
// Add more product IDs as needed

        QueryProductDetailsParams.Builder queryBuilder = QueryProductDetailsParams.newBuilder();

        for (String productId : productIds) {
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build();
            list.add(product);
        }
        queryBuilder.setProductList(list);


        QueryProductDetailsParams queryProductDetailsParams = queryBuilder.build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                // Handle the product details response for multiple products
                createListView(productDetailsList);
            }
        });


    }

    private void createListView(List<ProductDetails> productDetailsList) {
        Log.d(SplashScreen.TAG, "createListView: " + productDetailsList);
//        ListView listView = findViewById(R.id.listView);
//        Vip_CustomAdapter vipMembershipAdapter = new Vip_CustomAdapter(VipMembership.this, productDetailsList,billingClient);
//        listView.setAdapter(vipMembershipAdapter);

        TextView title = findViewById(R.id.title);
        TextView price = findViewById(R.id.price);
        TextView membershipType = findViewById(R.id.membershipType);
        ProductDetails productDetails = productDetailsList.get(0);
        title.setText(productDetails.getTitle());
        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
        membershipType.setText("limited peroid");
        NeumorphCardView lauchBilling = findViewById(R.id.launchBilling);
        lauchBilling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);
            }
        });


        TextView title2 = findViewById(R.id.title2);
        TextView price2 = findViewById(R.id.price2);
        TextView membershipType2 = findViewById(R.id.membershipType2);
        ProductDetails productDetails2 = productDetailsList.get(1);
        title2.setText(productDetails2.getTitle());
        price2.setText(productDetails2.getOneTimePurchaseOfferDetails().getFormattedPrice());
        membershipType2.setText("special offer");
        NeumorphCardView lauchBilling2 = findViewById(R.id.launchBilling2);
        lauchBilling2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails2)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);

            }
        });

        TextView title3 = findViewById(R.id.title3);
        TextView price3 = findViewById(R.id.price3);
        TextView membershipType3 = findViewById(R.id.membershipType3);
        ProductDetails productDetails3 = productDetailsList.get(2);
        title3.setText(productDetails3.getTitle());
        price3.setText(productDetails3.getOneTimePurchaseOfferDetails().getFormattedPrice());
        membershipType3.setText("popular");
        NeumorphCardView lauchBilling3 = findViewById(R.id.launchBilling3);
        lauchBilling3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = VipMembership.this;

                ImmutableList productDetailsParamsList =
                        ImmutableList.of(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(productDetails3)
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
                                        .build()
                        );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

// Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams);
                progressBar.setVisibility(View.VISIBLE);

            }
        });


    }


    private void verifyPurchase(Purchase purchase) {

        int Validity_period = 0;

        if (purchase.getProducts().get(0).equals("vip_1")) {
            Validity_period = 30;
        } else if (purchase.getProducts().get(0).equals("vip_3")) {
            Validity_period = 90;
        } else {
            Validity_period = 365;

        }

        savePurchaseDetails_inSharedPreference(purchase.getPurchaseToken(), Validity_period, purchase.getPurchaseTime());

        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.consumeAsync(
                consumeParams,
                new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.d(SplashScreen.TAG, "Consumed: ");
                        }
                    }
                }
        );


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentRef = db.collection("purchases").document(purchase.getPurchaseToken());

        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
//                    ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
//                    billingClient.consumeAsync(
//                            consumeParams,
//                            new ConsumeResponseListener() {
//                                @Override
//                                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
//                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                                        Log.d("TAGAA", "onConsumeResponse: ");
//                                    }
//                                }
//                            }
//                    );
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("purchaseToken", purchase.getPurchaseToken());
                    data.put("purchaseTime", purchase.getPurchaseTime());
                    data.put("orderId", purchase.getOrderId());
                    data.put("date", new Date());
                    data.put("isValid", true);

                    documentRef.set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Data successfully written to Firestore
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error writing data to Firestore
                                }
                            });


                    // Document doesn't exist
                }
            } else {
                // Error occurred while retrieving the document
                FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
                // Handle the exception
                Log.d(SplashScreen.TAG, "FirebaseFirestoreException: " + exception.getMessage());
            }
        });


    }

    private void savePurchaseDetails_inSharedPreference(String purchaseToken, int validity_period, long purchaseTime) {
        //Reading purchase Token
        SharedPreferences sh = getSharedPreferences("UserInfo", MODE_PRIVATE);
        String a = sh.getString("purchaseToken", purchaseToken);

        // Creating purchase Token into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("purchaseToken", purchaseToken);
        myEdit.putInt("validity_period", validity_period);

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dateFormat.format(currentDate);
        myEdit.putString("purchase_date", dateString);
        myEdit.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();

        billingClient.queryPurchasesAsync(
                BillingClient.ProductType.INAPP,
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED &&
                                        !purchase.isAcknowledged()) {
                                    verifyPurchase(purchase);
                                    progressBar.setVisibility(View.GONE);

                                }
                            }
                        }
                    }
                }
        );
    }


    private void exit_dialog() {


        Button getVip, cancel;
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(VipMembership.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.membership_exit_dialog, null);
        builder.setView(promptView);
        builder.setCancelable(false);


        getVip = promptView.findViewById(R.id.getVip);
        cancel = promptView.findViewById(R.id.cancel);


        getVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        dialog = builder.create();
        dialog.show();

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        dialog.getWindow().setBackgroundDrawable(inset);

    }


    private void actionBar() {

        ImageView back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    @Override
    public void onBackPressed() {
        exit_dialog();
        handler.removeCallbacksAndMessages(null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);

    }
}