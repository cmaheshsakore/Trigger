package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

public class PendingImage {
    private ImageView   mView;
    private String      mUrl;
    private Bitmap      mBitmap;

    public PendingImage(ImageView view, String url) {
        mView = view;
        mUrl = url;
        mBitmap = null;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap b) {
        mBitmap = b;
    }

    public String getUrl() {
        return mUrl;
    }

    public ImageView getView() {
        return mView;
    }

    public void loadBitmapIntoView() {
        if ((mBitmap != null) && (mView.getDrawable() == null)) {
            mView.setVisibility(View.VISIBLE);
            mView.setImageBitmap(mBitmap);
        }
    }
};