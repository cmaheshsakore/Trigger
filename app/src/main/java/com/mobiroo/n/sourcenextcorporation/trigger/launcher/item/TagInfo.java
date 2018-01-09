package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

public class TagInfo {
    private final int mMaxSize;
    private final String mTagType;
    
    public TagInfo(int size, String type){
        mMaxSize = size;
        mTagType = type;
    }
    
    public int getSize() {
        return mMaxSize;
    }
    
    public String getType() {
        return mTagType;
    }
    
    public boolean isMessageTooLarge(int messageSize) {
        return messageSize > mMaxSize ? true: false;
    }
    
}
