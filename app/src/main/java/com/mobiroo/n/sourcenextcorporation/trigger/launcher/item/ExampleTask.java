package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class ExampleTask implements Parcelable {
    
    public static final String EXTRA_EXAMPLE_TASK = "com.trigger.launcher.example_task";
    
    private final int mNameId;
    private final int mDescriptionId;
    private final int mIconId;

    private final String mPayload;
    private final int mPreferredTrigger;
    private final int[] mSecondaryTriggers;

    private Trigger mTrigger;
    
    public ExampleTask(int nameId, int descriptionId, int iconId, String payload, int preferred, int[] secondary) {
        mNameId = nameId;
        mDescriptionId = descriptionId;
        mIconId = iconId;
        mPayload = payload;
        mPreferredTrigger = preferred;
        mSecondaryTriggers = secondary;
    }

    public String getName(Context context) {
        return context.getString(mNameId);
    }

    public int getNameId() {
        return mNameId;
    }

    public String getDescription(Context context) {
        return context.getString(mDescriptionId);
    }

    public int getDescriptionId() {
        return mDescriptionId;
    }

    public int getIconId() {
        return mIconId;
    }

    public int getPreferredTrigger() {
        return mPreferredTrigger;
    }

    public int[] getSecondaryTriggers() {
        return mSecondaryTriggers;
    }

    public String getPayload() {
        return mPayload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(mNameId);
        dest.writeInt(mDescriptionId);
        dest.writeInt(mIconId);
        dest.writeString(mPayload);
        dest.writeInt(mPreferredTrigger);
        dest.writeIntArray(mSecondaryTriggers);
    }

    public static final Creator<ExampleTask> CREATOR = new Creator<ExampleTask>() {
        @Override
        public ExampleTask createFromParcel(Parcel in) {
            return new ExampleTask(in.readInt(), in.readInt(), in.readInt(), in.readString(), in.readInt(), in.createIntArray());
        }

        @Override
        public ExampleTask[] newArray(int size) {
            return new ExampleTask[size];
        }
    };
    
    public void setTrigger(Trigger trigger) {
        mTrigger = trigger;
    }
    
    public Trigger getTrigger() {
        return mTrigger;
    }

}
