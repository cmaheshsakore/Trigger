package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

public class WidgetIcon {

    private int mResourceId;
    private String mName;
    
    public WidgetIcon(int resId, String name) {
        mResourceId = resId;
        mName = name;
    }
    
    public int getResourceId() {
        return mResourceId;
    }
    
    public String getName() {
        return mName;
    }
}
