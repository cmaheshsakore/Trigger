package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;

/**
 * @author krohnjw
 * Member class used to store options when inflating a view for a specific command
 */
public class CommandArguments implements Parcelable {

    public static final String OPTION_INITIAL_STATE = "option_initial_state";
    public static final String OPTION_SELECTED_APPLICATION = "option_selected_application";
    public static final String OPTION_BRIGHTNESS_AUTO_ENABLED = "option_brightness_auto_enabled";
    public static final String OPTION_EXTRA_FLAG_ONE = "option_flag_one";
    public static final String OPTION_EXTRA_FLAG_TWO = "option_flag_two";
    public static final String OPTION_EXTRA_FLAG_THREE = "option_flag_three";
    public static final String OPTION_EXTRA_FLAG_FOUR = "option_flag_four";
    public static final String OPTION_EXTRA_FLAG_FIVE = "option_flag_five";
    public static final String OPTION_EXTRA_FLAG_SIX = "option_flag_six";
    
    private HashMap<String,String> mArguments;
    
    public static final Creator<CommandArguments> CREATOR = new Creator<CommandArguments>() {
        @Override
        public CommandArguments createFromParcel(Parcel source) {
            return new CommandArguments(source);
        }

        @Override
        public CommandArguments[] newArray(int size) {
            return new CommandArguments[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(mArguments);
    }
    
    @SuppressWarnings("unchecked")
    public CommandArguments(Parcel in) {
        mArguments = in.readHashMap(null);
    }
    
    public CommandArguments() {
        mArguments = new HashMap<String,String>();
    }
            
    public CommandArguments(BasicNameValuePair... pairs) {
        mArguments = new HashMap<String,String>();
        for (BasicNameValuePair pair: pairs) {
            mArguments.put(pair.getName(), pair.getValue());
        }
    }
    
    public HashMap<String,String> getArguments() {
        return mArguments;
    }
    
    public void putArgument(String name, String value) {
        mArguments.put(name, value);
    }
    
    public boolean hasArgument(String name) {
        return mArguments.containsKey(name);
    }

   public String getValue(String key) {
       return (mArguments.containsKey(key)) ? mArguments.get(key) : "";
   }
    
    
}
