package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class GoogleSigninActivity extends PlusSigninActivity {

    public static final String EXTRA_SCOPE = "com.trigger.launcher.sso_scope";
    public static final String EXTRA_REFRESH_TOKEN = "com.trigger.launcher.sso_refresh_token";
    public static final String EXTRA_USER_TOKEN = "com.trigger.launcher.sso_token";
    public static final String EXTRA_USER_ACCOUNT = "com.trigger.launcher.sso_name";
    public static final String EXTRA_REQUEST_WALLET = "com.trigger.launcher.request_wallet";
    

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState); 
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sso_signin);
        mResolveWhenReady = true;
        mHandleConnectWhenReady = true;
    }


    @Override
    protected void runAfterConnected() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        
        if (mPlusClient.isConnected()) {
            mPlusClient.disconnect();
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_USER_TOKEN, mId);
        data.putExtra(EXTRA_USER_ACCOUNT, mAccountName);
        setResult(RESULT_OK, data);
        finish();
    }
}
