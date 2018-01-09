/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobiroo.n.sourcenextcorporation.trigger.payment;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays the credentials received in the {@code FullWallet}.
 */
public class OrderCompleteActivity extends Activity implements OnClickListener {

    private Address mAddress;
    public static final String EXTRA_ORDER_NUMBER = "extra_order_number";
    public static final String EXTRA_ERROR_MESSAGE = "extra_error_message";
    public static final String EXTRA_ADDRESS        = "extra_address";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_order_complete);

        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        
        Intent intent = getIntent();
        if (intent != null) {
            mAddress = intent.getParcelableExtra(EXTRA_ADDRESS);
        }

        (findViewById(R.id.wallet_logo)).setVisibility(View.GONE);
        
        // Get incoming order data from intent
        String orderNumber = (intent.hasExtra(EXTRA_ORDER_NUMBER)) ? intent.getStringExtra(EXTRA_ORDER_NUMBER) : "Error";
        String errorMessage = (intent.hasExtra(EXTRA_ERROR_MESSAGE)) ? intent.getStringExtra(EXTRA_ERROR_MESSAGE) : "";
        
        String email = (mAddress == null) ? "Error":  mAddress.getEmail();
        
        if (!orderNumber.equals("Error")) {
            ((TextView) findViewById(R.id.confirmation)).setText(String.format(getString(R.string.confirmation_text), orderNumber, email));
        } else {
            Logger.i("Error message is " + errorMessage);
            ((TextView) findViewById(R.id.confirmation)).setText(String.format(getString(R.string.confirmation_error_text), errorMessage));
        }
        Button continueButton = (Button) findViewById(R.id.ok_button);
        continueButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        setResult(RESULT_OK);
        finish();
    }
}
