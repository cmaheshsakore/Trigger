package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

public class AppData {

    private String mName;
    private String mPackage;
    private String mActivity;

    public AppData(String n, String p, String a) {
        mName = n;
        mPackage = p;
        mActivity = a;
    }
    
    public String getPackage() {
        return mPackage;
    }
    
    public String getActivity() {
        return (mActivity.contains(mPackage)) ? mActivity.replace(mPackage + ".", mPackage + "/.") : mPackage + "/" + mActivity;
    }
    
    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
