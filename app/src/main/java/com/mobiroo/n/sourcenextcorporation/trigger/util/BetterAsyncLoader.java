package com.mobiroo.n.sourcenextcorporation.trigger.util;


import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;

public abstract class BetterAsyncLoader<T> extends AsyncTaskLoader<BetterAsyncTask.TaskResult<T>> {
    private BetterAsyncTask.TaskResult<T> mData;
    private long    mPollInterval;
    private Handler mRepeatHandler;

    public static final long INTERVAL_NONE  = -1;
    public static final long INTERVAL_SHORT = DateUtils.SECOND_IN_MILLIS * 15;
    public static final long INTERVAL_LONG  = DateUtils.MINUTE_IN_MILLIS;

    public BetterAsyncLoader(Context context, long pollingInterval) {
        super(context);
        if (pollingInterval > 0) {
            mPollInterval  = pollingInterval;
            mRepeatHandler = new Handler();
        }
    }

    @Override
    public void deliverResult(BetterAsyncTask.TaskResult<T> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        this.mData = data;

        super.deliverResult(data);

        if (mRepeatHandler != null) {
            startTimer();
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        mData = null;
    }

    private void startTimer() {
        mRepeatHandler.postDelayed(new Runnable() {
            public void run() {
                onContentChanged();
            }
        }, mPollInterval);
    }

    @Override
    public final BetterAsyncTask.TaskResult<T> loadInBackground() {
        try {
            return new BetterAsyncTask.TaskResult<T>(doLoadInBackground());
        } catch (Exception ex) {
            return new BetterAsyncTask.TaskResult<T>(ex);
        }
    }

    protected abstract T doLoadInBackground() throws Exception;
}
