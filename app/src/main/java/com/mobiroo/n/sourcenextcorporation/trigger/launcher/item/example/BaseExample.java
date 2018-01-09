package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

/**
 * Created by krohnjw on 1/24/14.
 */
public class BaseExample implements Example {

    private int     mName;
    private int     mIcon;
    private int     mDescription;
    private String  mTag;
    private boolean mIsPro;

    public BaseExample(int name, int icon, int description, boolean isPro, String tag) {
        mName = name;
        mIcon = icon;
        mDescription = description;
        mTag = tag;
        mIsPro = isPro;
    }

    @Override
    public void showConfigurationDialog(Context context, FragmentManager manager) {

    }

    @Override
    public void createExample(Context context, Intent intent) {
        Usage.logMixpanelEvent(Usage.getAnalyticsObject(context), "example created", true, new String[] { "name", getTag()});
    }

    @Override
    public int getName() {
        return mName;
    }

    @Override
    public int getDescription() {
        return mDescription;
    }

    @Override
    public int getIcon() {
        return mIcon;
    }

    @Override
    public boolean isPro() {
        return mIsPro;
    }

    @Override
    public String getTag() {
        return mTag;
    }
}
