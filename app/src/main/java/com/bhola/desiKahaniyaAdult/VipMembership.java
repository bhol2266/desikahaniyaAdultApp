package com.bhola.desiKahaniyaAdult;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VipMembership extends AppCompatActivity {


    AlertDialog dialog;
    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_membership);
        actionBar();


        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(
                        (billingResult, list) -> {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                for (Purchase purchase : list) {
                                    verifyPurchase(purchase);
                                }
                            }
                        }
                ).build();


        //start the connection after initializing the billing client
        connectGooglePlayBilling();

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
        ListView listView = findViewById(R.id.listView);
        Vip_CustomAdapter vipMembershipAdapter = new Vip_CustomAdapter(VipMembership.this, productDetailsList,billingClient);
        listView.setAdapter(vipMembershipAdapter);

    }


    private void verifyPurchase(Purchase purchase) {

        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
        billingClient.consumeAsync(
                consumeParams,
                new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.d("TAGAA", "onConsumeResponse: ");
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
                Log.d("TAGAA", "FirebaseFirestoreException: " + exception.getMessage());
            }
        });


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

    }
}