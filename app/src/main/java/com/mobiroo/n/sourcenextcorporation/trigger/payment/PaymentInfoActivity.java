package com.mobiroo.n.sourcenextcorporation.trigger.payment;

import java.util.List;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet.RequestConstants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentInfoActivity extends Activity implements OnClickListener {

    private Address mShipping;
    private Address mBilling;
    private CreditCard mCard;
    List<Address.Country> mCountries;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_billing_info);
        
        Intent intent = getIntent();
        
        if (intent.hasExtra(RequestConstants.EXTRA_CREDIT_CARD)) {
            mCard = (CreditCard) intent.getParcelableExtra(RequestConstants.EXTRA_CREDIT_CARD);
        }
        
        if (intent.hasExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS)) {
            mShipping = (Address) intent.getParcelableExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS);
        }
        
        if (intent.hasExtra(RequestConstants.EXTRA_BILLING_ADDRESS)) {
            mBilling = (Address) intent.getParcelableExtra(RequestConstants.EXTRA_BILLING_ADDRESS);
        }
        
        mCountries = Address.getCountryList();
        
        Spinner shipping_country = (Spinner) findViewById(R.id.shipping_country);
        shipping_country.setAdapter(new ArrayAdapter<Address.Country>(this, android.R.layout.simple_list_item_1, mCountries));
        
        Spinner billing_country = (Spinner) findViewById(R.id.billing_country);
        billing_country.setAdapter(new ArrayAdapter<Address.Country>(this, android.R.layout.simple_list_item_1, mCountries));
        
        ((Button) findViewById(R.id.cancel_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.confirm_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.button_use_shipping)).setOnClickListener(this);
        
        updateUi();
    }
    
    private void updateUi() {
        if (mShipping != null) {
            setItemText(R.id.shipping_name, mShipping.getName());
            setItemText(R.id.shipping_company_name, mShipping.getCompanyName());
            setItemText(R.id.shipping_email_address, mShipping.getEmail());
            setItemText(R.id.shipping_address, mShipping.getStreetAddress());
            setItemText(R.id.shipping_address_secondary, mShipping.getStreetSecondary());
            setItemText(R.id.shipping_apt, mShipping.getApt());
            setItemText(R.id.shipping_region, mShipping.getRegion());
            setItemText(R.id.shipping_postcode, mShipping.getPostcode());
            setItemText(R.id.shipping_phone, mShipping.getPhone());
            setItemText(R.id.shipping_city, mShipping.getCity());
            if (mCountries.indexOf(mShipping.getCountry()) > 0) {
                ((Spinner) findViewById(R.id.shipping_country)).setSelection(mCountries.indexOf(mShipping.getCountry()));
            }
        }
        
        if (mBilling != null) {
            setItemText(R.id.billing_name, mBilling.getName());
            setItemText(R.id.billing_company_name, mBilling.getCompanyName());
            setItemText(R.id.billing_email_address, mBilling.getEmail());
            setItemText(R.id.billing_address, mBilling.getStreetAddress());
            setItemText(R.id.billing_address_secondary, mBilling.getStreetSecondary());
            setItemText(R.id.billing_apt, mBilling.getApt());
            setItemText(R.id.billing_region, mBilling.getRegion());
            setItemText(R.id.billing_postcode, mBilling.getPostcode());
            setItemText(R.id.billing_phone, mBilling.getPhone());
            setItemText(R.id.billing_city, mBilling.getCity());
            if (mCountries.indexOf(mBilling.getCountry()) > 0) {
                ((Spinner) findViewById(R.id.billing_country)).setSelection(mCountries.indexOf(mBilling.getCountry()));
            }
        }
        
        if (mCard != null) {
            setItemText(R.id.card_number, mCard.getNumber());
            setItemText(R.id.card_expiry_month, mCard.getExpiryMonth());
            setItemText(R.id.card_expiry_year, mCard.getExpiryYear());
            setItemText(R.id.card_cvc, mCard.getCvc());
        }
    }

    private void fillShipping() {
        if (mShipping == null) {
            mShipping = new Address();
        }
        
        mShipping.setName(getItemText(R.id.shipping_name));
        mShipping.setCompanyName(getItemText(R.id.shipping_company_name));
        mShipping.setEmail(getItemText(R.id.shipping_email_address));
        mShipping.setStreetAddress(getItemText(R.id.shipping_address));
        mShipping.setStreetSecondary(getItemText(R.id.shipping_address_secondary));
        mShipping.setApt(getItemText(R.id.shipping_apt));
        mShipping.setCity(getItemText(R.id.shipping_city));
        mShipping.setRegion(getItemText(R.id.shipping_region));
        Spinner country = (Spinner) findViewById(R.id.shipping_country);
        Address.Country item =  (Address.Country) country.getSelectedItem();
        mShipping.setCountry(item);
        mShipping.setPostcode(getItemText(R.id.shipping_postcode));
        mShipping.setPhone(getItemText(R.id.shipping_phone));
    }
    
    private void fillBilling() {
        
        if (mBilling == null) {
            mBilling = new Address();
        } 
        
        mBilling.setName(getItemText(R.id.billing_name));
        mBilling.setCompanyName(getItemText(R.id.billing_company_name));
        mBilling.setEmail(getItemText(R.id.billing_email_address));
        mBilling.setStreetAddress(getItemText(R.id.billing_address));
        mBilling.setStreetSecondary(getItemText(R.id.billing_address_secondary));
        mBilling.setApt(getItemText(R.id.billing_apt));
        mBilling.setCity(getItemText(R.id.billing_city));
        mBilling.setRegion(getItemText(R.id.billing_region));
        Spinner country = (Spinner) findViewById(R.id.billing_country);
        Address.Country item =  (Address.Country) country.getSelectedItem();
        mBilling.setCountry(item);
        mBilling.setPostcode(getItemText(R.id.billing_postcode));
        mBilling.setPhone(getItemText(R.id.billing_phone));
    }
    
    private void fillCard() {
        if (mCard == null) {
            mCard = new CreditCard();
        }
        
        mCard.setNumber(getItemText(R.id.card_number));
        mCard.setExpiryMonth(getItemText(R.id.card_expiry_month));
        mCard.setExpiryYear(getItemText(R.id.card_expiry_year));
        mCard.setCvc(getItemText(R.id.card_cvc));
    }
    
    private void copyShipping() {
        
        fillShipping();
        
        setItemText(R.id.billing_name, mShipping.getName());
        setItemText(R.id.billing_company_name, mShipping.getCompanyName());
        setItemText(R.id.billing_email_address, mShipping.getEmail());
        setItemText(R.id.billing_address, mShipping.getStreetAddress());
        setItemText(R.id.billing_address_secondary, mShipping.getStreetSecondary());
        setItemText(R.id.billing_apt, mShipping.getApt());
        setItemText(R.id.billing_region, mShipping.getRegion());
        setItemText(R.id.billing_postcode, mShipping.getPostcode());
        setItemText(R.id.billing_phone, mShipping.getPhone());
        setItemText(R.id.billing_city, mShipping.getCity());
        
        Spinner shipping = (Spinner) findViewById(R.id.shipping_country);
        Spinner billing = (Spinner) findViewById(R.id.billing_country);
        billing.setSelection(shipping.getSelectedItemPosition());

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.confirm_button:
                finishWithData();
                break;
            case R.id.button_use_shipping:
                copyShipping();
                break;
        }
    }
    
    private String getItemText(int id) {
        TextView t = (TextView) findViewById(id);
        return (t != null) ? t.getText().toString() : "";
    }
    
    private void setItemText(int id, String value) {
        TextView t = (TextView) findViewById(id);
        if (t != null) {
            t.setText(value);
        }
    }
    private void finishWithData() {
        
        fillCard();
        fillShipping();
        fillBilling();

        
        if (mCard.isValid() && mShipping.isValid() && mBilling.isValid()) {
            Intent intent = new Intent();
            intent.putExtra(RequestConstants.EXTRA_BILLING_ADDRESS, mBilling);
            intent.putExtra(RequestConstants.EXTRA_SHIPPING_ADDRESS, mShipping);
            intent.putExtra(RequestConstants.EXTRA_CREDIT_CARD, mCard);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            String message = "";
            if (!mCard.isValid()) {
                message = getString(mCard.getErrorMessage());
            } else if (!mShipping.isValid()) { 
                message = getString(mShipping.getErrorMessage());
            } else {
                message = getString(mBilling.getErrorMessage());
            }
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        
    }
}
