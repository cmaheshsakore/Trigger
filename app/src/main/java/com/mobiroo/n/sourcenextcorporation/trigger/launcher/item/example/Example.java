package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;

/**
 * Created by krohnjw on 1/24/14.
 */
public interface Example {
    public void showConfigurationDialog(Context context, FragmentManager manager);
    public void createExample(Context context, Intent intent);
    public int getName();
    public int getDescription();
    public int getIcon();
    public boolean isPro();
    public String getTag();
}
