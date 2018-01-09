package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import static com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncTask.TaskResult;

public abstract class ListLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<TaskResult<T>> {
    private ListFragment mFragment;

    public ListLoaderCallbacks(ListFragment fragment) {
        mFragment = fragment;
    }

    public final void onLoadFinished(Loader<TaskResult<T>> loader, TaskResult<T> data) {
        mFragment.setListShown(true);

        if (data.success()) {
            onLoadFinished(data.getObject());
        } else {
            Log.e(mFragment.getClass().getName(), "Error in loader", data.getException());
            onLoadFailed(data.getException());
        }
    }

    public void onLoaderReset(Loader<TaskResult<T>> loader) {
        if (!mFragment.isVisible()) {
            return;
        }
        mFragment.setEmptyText(null);
        mFragment.setListAdapter(null);
        mFragment.setListShown(false);
    }

    protected void onLoadFailed(Exception ex) {
        mFragment.setListAdapter(null);
        mFragment.setEmptyText((ex.toString()));
    }

    protected abstract void onLoadFinished(T result);
}
