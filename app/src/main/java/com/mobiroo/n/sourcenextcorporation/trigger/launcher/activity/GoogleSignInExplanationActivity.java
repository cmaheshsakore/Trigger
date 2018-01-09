package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

public class GoogleSignInExplanationActivity extends Activity {
    
    private final int REQUEST_ACCOUNT_AUTH = 1;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_google_sso_explained);
        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        (findViewById(R.id.sign_in_button)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivityForResult(new Intent(GoogleSignInExplanationActivity.this, GoogleSigninActivity.class), REQUEST_ACCOUNT_AUTH);
            }
            
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_AUTH:
                Logger.d("Received result from auth " + RESULT_OK);
                if (resultCode == RESULT_OK) {
                    Usage.logUserProperty(this, "Receive Marketing", ((CheckBox) findViewById(R.id.receive_marketing)).isChecked());
                }

            default:
                Logger.d("Returning result from Auth");
                setResult(resultCode, data);
                finish();
        }
        
        
        
    }

}
