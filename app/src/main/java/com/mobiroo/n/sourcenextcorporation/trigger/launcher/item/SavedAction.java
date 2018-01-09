package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.BaseAction;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class SavedAction implements Parcelable {
    private final String mMessage;
    private final String mPrettyAction;
    private final String mPrettyName;
    private final String mCode;
    
    public static final Creator<SavedAction> CREATOR = new Creator<SavedAction>() {
        @Override
        public SavedAction createFromParcel(Parcel source) {
            return new SavedAction(source.readString(), source.readString(), source.readString(), source.readString());
        }

        @Override
        public SavedAction[] newArray(int size) {
            return new SavedAction[size];
        }
    };

    public SavedAction(String message, String prettyAction, String prettyName, String code) {
        mMessage = message;
        mPrettyAction = prettyAction;
        mPrettyName = prettyName;
        mCode = code;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getPrettyAction() {
        return mPrettyAction;
    }

    public String getPrettyName() {
        return mPrettyName;
    }

    public String getCode() {
        if ((mCode != null) && (!mCode.isEmpty())) {
            return mCode;
        } else {
            /* 
             * Code was not passed in with constructor,
             * try pulling the code from message
             * 
             */
            if (mMessage != null) {
                String[] args = mMessage.split(":");
                if (args[0].equals(Constants.COMMAND_ENABLE) || args[0].equals(Constants.COMMAND_DISABLE) || args[0].equals(Constants.COMMAND_TOGGLE)) {
                    return BaseAction.getCodeFromCommand(args[1]);
                } else {
                    return BaseAction.getCodeFromCommand(args[0]);
                }
            }
        }
        
        return "";
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMessage);
        dest.writeString(mPrettyAction);
        dest.writeString(mPrettyName);
        dest.writeString(mCode);
    }

    public String getDescription() {
        if (!TextUtils.isEmpty(getPrettyName())) {
            return String.format("%s %s", getPrettyAction(), getPrettyName());
        } else {
            return getPrettyAction();
        }
    }
    
    
}
