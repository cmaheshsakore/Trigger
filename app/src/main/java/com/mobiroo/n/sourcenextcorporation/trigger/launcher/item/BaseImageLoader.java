package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class BaseImageLoader  extends AsyncTask<PendingImage, Void, PendingImage[]> {

        protected Context       mContext;
        protected ImageView     mLargeView;
        protected ProgressBar   mProgress;
        
        protected float         mWidth;
        protected float         mHeight;
        
        protected int           mSampleSize = 1;
        
        public BaseImageLoader() {
            
        }
        
        public BaseImageLoader(Context context, ImageView largeView) {
            mContext = context;
            mLargeView = largeView;
        }

        public void setProgressDialog(ProgressBar progress) {
            mProgress = progress;
        }
        
        /**
         * Take in a width in DIP and store it locally as a width in pixels
         * for the final render
         * @param width
         */
        public void setWidth(int width) {
            mWidth = Utils.dipToPixels(mContext, width);
        }
        
        /**
         * Take in a height in DIP and store it locally as a height in pixels
         * for the final render
         * @param height
         */
        public void setHeight(int height) {
            mHeight = Utils.dipToPixels(mContext, height);
        }
        
        /**
         * Used to set a static sample size if height and widt are not set.  Default to 1 (full)
         * @param size
         */
        public void setSampleSize(int size) {
            mSampleSize = size;
        }
        
        @Override
        protected PendingImage[] doInBackground(PendingImage... params) {

            for (int i=0; i < params.length; i++) {
                PendingImage image = params[i];
                String name = image.getUrl();
                if (!name.isEmpty()) {
                    //Logger.d("Loading " + name);
                    Bitmap bmp = null;
                    bmp = loadImageFromCache(name);
                    if (bmp != null) {
                       // Logger.d("Got data from cache");
                        params[i].setBitmap(bmp);
                    } else {
                        if (name.contains(".png") || name.contains(".jpg")) {
                            //Logger.d("Cache miss for " + cleanUrl(name));
                            try {
                                URL url = new URL(name);
                                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                cacheImage(bmp, name);
                                params[i].setBitmap(bmp);
                            } catch (Exception e) {
                                Logger.e("Exception loading bitmap from " + params[0], e);
                            }
                        }                        
                    }
                }
            }

            return params;

        }

        @Override
        public void onPostExecute(PendingImage[] images) {
            boolean setLarge = false;
            for (int i=0; i < images.length; i++) {
                PendingImage image = images[i];

                if (!setLarge) {
                    if (image.getBitmap() != null) {
                        setLarge = true;
                        if ((mLargeView != null) && (mLargeView.getDrawable() == null)) {
                            
                            mLargeView.setImageBitmap(image.getBitmap());
                            mLargeView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                image.loadBitmapIntoView();

            }
            
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
        }

        public void cacheImage(Bitmap bm, String url) {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File root = Environment.getExternalStorageDirectory();
                if (root.canWrite()) {
                    try {
                        File container = new File(root.getPath() + "/Android/data/com.mobiroo.n.sourcenextcorporation.trigger/files/");
                        container.mkdirs();
                        File img = new File(container, cleanUrl(url));
                        OutputStream fOut = new FileOutputStream(img);
                        bm.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public Bitmap loadImageFromCache(String url) {
            String state = Environment.getExternalStorageState();
            // Check if the image exists locally
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File root = Environment.getExternalStorageDirectory();
                if (root.canRead()) {
                    try {
                        File container = new File(root.getPath() + "/Android/data/com.mobiroo.n.sourcenextcorporation.trigger/files/");
                        File img = new File(container, cleanUrl(url));
                        if (img.exists()) {
                            return decodeSampledBitmapFromFile(img, mWidth, mHeight);
                        } else {
                            //Logger.d("Loading from assets" + url);
                            return loadFromAssets(url);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        private String cleanUrl(String url) {
            return url.replace("http://", "").replace("/", ".");
        }
        
        private String getFileNameFromUrl(String url) {
            String name = url;

            if (url.contains("trigger")) {
                name = name.replace("http://trigger.com:3000/tl_store/", "");
                name = name.replace("http://trigger.com/tl_store/", "");
                name = name.replace("http://www.trigger.com/tl_store/", "");
            }

            if (url.contains("gettrigger")) {
                name = name.replace("http://gettrigger.com/tl_store/", "");
            }
            return name;
        }
        
        private Bitmap loadFromAssets(String url) {
            Bitmap b = null;
            if (mContext != null) {
                String name = getFileNameFromUrl(url);  
                if (!name.isEmpty()) {
                    try {
                        InputStream is = mContext.getAssets().open(name);
                        b = decodeSampledBitmapFromStream(is, mWidth, mHeight);
                        is.close();
                    }
                    catch (Exception e) {
                        Logger.e("Exception getting image from assets", e);
                    }
                }
            }
            
            return b;
        }
        public int calculateInSampleSize(
                BitmapFactory.Options options, float reqWidth, float reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
                if (inSampleSize == 1) {
                    inSampleSize = 2;
                }
            }

            return inSampleSize;
        }
        
        public Bitmap decodeSampledBitmapFromStream(InputStream res, float reqWidth, float reqHeight) {
            
         // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(res, null, options);
            
            // Calculate inSampleSize
            if (reqWidth > 0 && reqHeight > 0) {
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            } else {
                options.inSampleSize = mSampleSize;
            }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(res, null, options);
        }
        
        
        public Bitmap decodeSampledBitmapFromFile(File res, float reqWidth, float reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(res.getAbsolutePath(), options);
            
            // Calculate inSampleSize
            if (reqWidth > 0 && reqHeight > 0) {
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            } else {
                options.inSampleSize = mSampleSize;
            }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(res.getAbsolutePath(), options);
        }

    }
