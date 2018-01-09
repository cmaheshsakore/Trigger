package com.mobiroo.n.sourcenextcorporation.trigger.payment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import android.widget.Button;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class ConfirmationActivity extends FragmentActivity implements OnClickListener {

    private Button mConfirmButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_payment_confirmation);
        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        ConfirmationFragment fragment =
                (ConfirmationFragment) getSupportFragmentManager().findFragmentById(R.id.frag);
        fragment.onNewIntent(intent);
    }

    @Override
    public void onClick(View v) {
        ((ConfirmationFragment) getSupportFragmentManager().findFragmentById(R.id.frag)).onClick(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Got result " + resultCode + " FROM " + requestCode);
        switch (requestCode) {
            default:
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag);
                fragment.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
