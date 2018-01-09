package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import org.apache.http.message.BasicNameValuePair;

import android.os.Parcel;
import android.os.Parcelable;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.CommandArguments;

public class ActionViewConfiguration implements Parcelable {

    private final String mCode;
    private final CommandArguments mArguments;

    public static final Creator<ActionViewConfiguration> CREATOR = new Creator<ActionViewConfiguration>() {
        @Override
        public ActionViewConfiguration createFromParcel(Parcel source) {
            return new ActionViewConfiguration(source);
        }

        @Override
        public ActionViewConfiguration[] newArray(int size) {
            return new ActionViewConfiguration[size];
        }
    };
    
    private ActionViewConfiguration(Parcel in) {
        mCode = in.readString();
        mArguments = in.readParcelable(CommandArguments.class.getClassLoader());
    }
    
    public ActionViewConfiguration(String code, BasicNameValuePair... pairs) {
        mCode = code;
        mArguments = new CommandArguments(pairs);
    }

    public String getCode() {
        return mCode;
    }

    public CommandArguments getArguments() {
        return mArguments;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCode);
        dest.writeParcelable(mArguments, flags);
    }
    
}

