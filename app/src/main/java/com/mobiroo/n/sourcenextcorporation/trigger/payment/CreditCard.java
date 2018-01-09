package com.mobiroo.n.sourcenextcorporation.trigger.payment;

import android.os.Parcel;
import android.os.Parcelable;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class CreditCard implements Parcelable {

    private String  mNumber;
    private String  mExpiryMonth;
    private String  mExpiryYear;
    private String  mCvc;

    private int mMessage = 0;;
    
    public CreditCard(Parcel source) {
        mNumber = source.readString();
        mExpiryMonth = source.readString();
        mExpiryYear = source.readString();
        mCvc = source.readString();
    }
    public CreditCard() {
        mNumber = "";
        mExpiryMonth = "";
        mExpiryYear = "";
        mCvc = "";
    }
    
    public int getErrorMessage() {
        return mMessage;
    }
    
    public boolean isValid() {
        mMessage = 0;
        
        if (!(mNumber.length() > 12)) {
            mMessage = R.string.card_number;
        } else if (mExpiryMonth.length() == 0) {
            mMessage = R.string.expiration_date;
        } else if (mExpiryYear.length() == 0) {
            mMessage = R.string.expiration_date;
        } else if (mCvc.length() < 2) {
            mMessage = R.string.cvc;
        }

        return (mMessage == 0) ? true : false;
                

    }
    
    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String value) {
        mNumber = value;
    }

    public String getExpiryMonth() {
        return mExpiryMonth;
    }

    public void setExpiryMonth(String value) {
        mExpiryMonth = value;
    }

    public String getExpiryYear() {
        return mExpiryYear;
    }

    public void setExpiryYear(String value) {
        mExpiryYear = value;
    }

    public String getCvc() {
        return mCvc;
    }

    public void setCvc(String value) {
        mCvc = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mNumber);
        dest.writeString(mExpiryMonth);
        dest.writeString(mExpiryYear);
        dest.writeString(mCvc);
    }


    public static final Creator<CreditCard> CREATOR = new Creator<CreditCard>() {
        @Override
        public CreditCard createFromParcel(Parcel source) {
            return new CreditCard(source);
        }

        @Override
        public CreditCard[] newArray(int size) {
            return new CreditCard[size];
        }
    };

}
