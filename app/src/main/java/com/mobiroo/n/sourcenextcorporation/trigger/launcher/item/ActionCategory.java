package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

public class ActionCategory {
    private final String   mCategory;
    private final int      mNameId;
    private final int      mIconId;
    private final Action[] mActions;

    public ActionCategory(String code, int nameId, int iconId, Action[] actions) {
        mCategory = code;
        mNameId   = nameId;
        mIconId   = iconId;
        mActions  = actions;
    }

    public String getCategory() {
        return mCategory;
    }

    public int getNameId() {
        return mNameId;
    }

    public int getIconId() {
        return mIconId;
    }

    public Action[] getActions() {
        return mActions;
    }
}
