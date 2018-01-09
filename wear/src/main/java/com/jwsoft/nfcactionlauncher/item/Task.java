package com.mobiroo.n.sourcenextcorporation.trigger.item;

/**
 * Created by krohnjw on 7/2/2014.
 */
public class Task {
    private String mId;
    private String mName;
    private String mSecondaryId;
    private String mSecondaryName;

    public Task(String id, String name) {
        mId = id;
        mName = name;
        mSecondaryId = "";
        mSecondaryName = "";
    }

    public Task(String id, String name, String secondaryId, String secondaryName) {
        mId = id;
        mName = name;
        mSecondaryId = secondaryId;
        mSecondaryName = secondaryName;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public boolean hasSecondaryData() {
        return !mSecondaryId.isEmpty();
    }
    public String getSecondaryName() {
        return mSecondaryName;
    }

    public String getSecondaryId() {
        return mSecondaryId;
    }
}
