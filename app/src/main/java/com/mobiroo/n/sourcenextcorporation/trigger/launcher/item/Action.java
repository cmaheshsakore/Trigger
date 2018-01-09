package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

public class Action {
    private final String mCode;
    private final int mNameId;

    private boolean mChecked;

    public Action(String code, int nameId) {
        mCode = code;
        mNameId = nameId;
    }

    public String getCode() {
        return mCode;
    }

    public int getNameId() {
        return mNameId;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
