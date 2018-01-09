package com.trigger.launcher.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobiroo.n.sourcenextcorporation.trigger.R;


public class ShopFragment extends Fragment {

    public static final int REQUEST_WALLET_PURCHASE = 1007;
    public static final int RESULT_BUYER_NEEDS_CHECKOUT = 5001;

    @SuppressWarnings("unused")
    private boolean mUserHasPreAuthorized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.buy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.prestigioplaza.com/catalogue/Accessories/Gadget_Accessories/NFC_tags"));
                startActivity(intent);
            }
        });
    }


}
