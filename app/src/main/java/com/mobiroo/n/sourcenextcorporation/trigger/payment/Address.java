package com.mobiroo.n.sourcenextcorporation.trigger.payment;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Address implements Parcelable {

    public static final int BILLING = 1;
    public static final int SHIPPING = 2;

    private String  mName;
    private String  mCompanyName;
    private String  mEmail;
    private String  mStreetAddress;
    private String  mStreetSecondary;
    private String  mApt;
    private String  mCity;
    private String  mRegion;
    private Country  mCountry;
    private String  mPostcode;
    private String  mPhone;

    private int mMessage = 0;
    
    public Address(Parcel source) {
        mName = source.readString();
        mCompanyName = source.readString();
        mEmail = source.readString();
        mStreetAddress = source.readString();
        mStreetSecondary = source.readString();
        mApt = source.readString();
        mCity = source.readString();
        mRegion = source.readString();
        mCountry = source.readParcelable(Country.class.getClassLoader());
        mPostcode = source.readString();
        mPhone = source.readString();
    }

    public Address() {
        mName = "";
        mCompanyName = "";
        mEmail = "";
        mStreetAddress = "";
        mStreetSecondary = "";
        mApt = "";
        mCity = "";
        mRegion = "";
        mCountry = new Country();
        mPostcode = "";
        mPhone = "";
    }

    public int getErrorMessage() {
        return mMessage;
    }
    public boolean isValid() {
        mMessage = 0;
        
        if (mName.isEmpty()) {
            mMessage = R.string.full_name;
        } else if (mStreetAddress.isEmpty()){
            mMessage = R.string.street_address;
        } else if (mEmail.isEmpty()) {
            mMessage = R.string.email;
        } 
        
        return (mMessage == 0) ? true : false;
    }

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        mName = value;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String value) {
        mCompanyName = value;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String value) {
        mEmail = value;
    }

    public String getStreetAddress() {
        return mStreetAddress;
    }

    public void setStreetAddress(String value) {
        mStreetAddress = value;
    }

    public String getStreetSecondary() {
        return mStreetSecondary;
    }

    public void setStreetSecondary(String value) {
        mStreetSecondary = value;
    }

    public String getApt() {
        return mApt;
    }

    public void setApt(String value) {
        mApt = value;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String value) {
        mCity = value;
    }

    public String getRegion() {
        return mRegion;
    }

    public void setRegion(String value) {
        mRegion = value;
    }

    public Country getCountry() {
        return mCountry;
    }

    public void setCountry(Country value) {
        mCountry = value;
    }

    public String getPostcode() {
        return mPostcode;
    }

    public void setPostcode(String value) {
        mPostcode = value;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String value) {
        mPhone = value;
    }

    public static class Country implements Comparable<Country>, Parcelable {
        private String mDisplay;
        private String mCode;

        public Country(String display, String code) {
            mCode = code;
            mDisplay = display;
        }

        public Country() {
            mCode = "";
            mDisplay = "";
        }

        public String getDisplay() {
            return mDisplay;
        }

        public String getCode() {
            return mCode;
        }

        public String toString() {
            return getDisplay();
        }

        @Override
        public int compareTo(Country comp) {

            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            for (int i=0; i < this.getDisplay().length(); i++) {
                char source = (this.getDisplay().length() > i) ? this.getDisplay().charAt(i) : '\0';
                char other = (comp.getDisplay().length() > i) ? comp.getDisplay().charAt(i) : '\0';
                if ((int) source < (int) other) {
                    return BEFORE;
                } else if ((int) source > (int) other) {
                    return AFTER;
                }
            }

            return EQUAL;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mDisplay);
            dest.writeString(mCode);
        }

        public static final Creator<Country> CREATOR = new Creator<Country>() {
            @Override
            public Country createFromParcel(Parcel source) {
                return new Country(source.readString(), source.readString());
            }

            @Override
            public Country[] newArray(int size) {
                return new Country[size];
            }
        };

        @Override
        public boolean equals (Object o) {

            if(o instanceof Country){
                Country in = (Country) o;
                return ((in.getCode().equals(getCode()) && (in.getDisplay().equals(getDisplay()))));
            } else {
                return false;
            }
        }
    }

    public static List<Country> getCountryList() {
        ArrayList<Country> countries = new ArrayList<Country>();
        Locale[] locales = Locale.getAvailableLocales();
        for(Locale locale: locales) {
            String country = locale.getDisplayCountry();
            String code = locale.getCountry();
            if (code.length() <= 3) {
                if (country.trim().length()>0) {
                    if (!countries.contains(new Country(country, code))) {
                        countries.add(new Country(country, code));
                    }
                }
            }
        }
        Collections.sort(countries);

        return countries;
    }

    public static String formatAddress(Context context, Address address) {
        // different locales may need different address formats, which would be handled in
        // R.string.address_format
        String address2 = address.getStreetSecondary().length() == 0 ?
                address.getStreetSecondary() : address.getStreetSecondary() + "\n";

                String addressString = "";
                try {
                    addressString = context.getString(R.string.address_format, address.getName(),
                            address.getStreetAddress(), address2, "", address.getCity(), address.getRegion(),
                            address.getPostcode());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return addressString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mCompanyName);
        dest.writeString(mEmail);
        dest.writeString(mStreetAddress);
        dest.writeString(mStreetSecondary);
        dest.writeString(mApt);
        dest.writeString(mCity);
        dest.writeString(mRegion);
        dest.writeParcelable(mCountry, 0);
        dest.writeString(mPostcode);
        dest.writeString(mPhone);

    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel source) {
            return new Address(source);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

}
