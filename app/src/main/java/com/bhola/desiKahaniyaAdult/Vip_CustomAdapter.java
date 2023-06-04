package com.bhola.desiKahaniyaAdult;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ProductDetails;
import com.google.common.collect.ImmutableList;

import java.util.List;

import soup.neumorphism.NeumorphCardView;

public class Vip_CustomAdapter extends BaseAdapter {

    VipMembership context;
    List<ProductDetails> productDetailsList;
    LayoutInflater inflater;
    BillingClient billingClient;

    public Vip_CustomAdapter(VipMembership ctx, List<ProductDetails> productDetailsList, BillingClient billingClient) {
        this.context = ctx;
        this.productDetailsList = productDetailsList;
        this.billingClient = billingClient;
        inflater = LayoutInflater.from(ctx);

    }

    @Override
    public int getCount() {

        return productDetailsList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.vip_grid_layout, null);
        TextView title = view.findViewById(R.id.title);
        TextView price = view.findViewById(R.id.price);
        TextView membershipType = view.findViewById(R.id.membershipType);

        ProductDetails productDetails = productDetailsList.get(i);

        title.setText(productDetails.getTitle());
        price.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
        if (i == 0) {
            membershipType.setText("limited peroid");
        } else if (i == 1) {
            membershipType.setText("popular");
        } else {
            membershipType.setText("special offer");

        }


        NeumorphCardView lauchBilling = view.findViewById(R.id.launchBilling);
        lauchBilling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // An activity reference from which the billing flow will be launched.
                Activity activity = context;

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
            }
        });

        return view;
    }
}
