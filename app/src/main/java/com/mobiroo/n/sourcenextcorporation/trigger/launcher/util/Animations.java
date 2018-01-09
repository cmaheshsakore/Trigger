package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class Animations {

    public static final int DURATION_DEFAULT = 1000;
    
    public static TranslateAnimation slideViewInFromTop(int duration) {
        TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0);
        slide.setDuration(duration);
        slide.setFillAfter(true);
        return slide;
    }

    public static TranslateAnimation slideViewOutTop(int duration) {
        TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT,0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_SELF, -1.0f);
        slide.setDuration(duration);
        slide.setFillAfter(true);
        return slide;
    }
    
    public static TranslateAnimation slideViewInFromRight(int duration) {
        TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_PARENT,0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_PARENT, 0);
        slide.setDuration(duration);
        slide.setFillAfter(true);
        return slide;
    }

    public static TranslateAnimation slideViewOutRight(int duration) {
        TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF,1.0f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_PARENT, 0);

        slide.setDuration(duration);
        slide.setFillAfter(true);
        return slide;
    }
    
    public static TranslateAnimation slideViewOutLeft(int duration) {
        TranslateAnimation slide = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_SELF,-1.0f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);

        slide.setDuration(duration);
        slide.setFillAfter(true);
        return slide;
    }
    
    public static AlphaAnimation fadeIn(int duration) {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(duration);
        return animation;
    }
    
    public static AlphaAnimation fadeOut(int duration) {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(duration);
        return animation;
    }
    
}
