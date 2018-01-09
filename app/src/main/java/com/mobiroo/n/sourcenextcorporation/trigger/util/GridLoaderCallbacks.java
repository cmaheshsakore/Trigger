package com.mobiroo.n.sourcenextcorporation.trigger.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.GridView;

public abstract class GridLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<BetterAsyncTask.TaskResult<T>> {
    private Fragment mFragment;
    private GridView mGridView;
    private View mEmptyView;
    
    public GridLoaderCallbacks(Fragment fragment, GridView grid, View empty) {
        mFragment = fragment;
        mGridView = grid;
        mEmptyView = empty;
    }

    private void setListShown(boolean shown) {
        if (shown) {
            mGridView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE); 
        } else {
            mGridView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE); 
        }
    }
    public final void onLoadFinished(Loader<BetterAsyncTask.TaskResult<T>> loader, BetterAsyncTask.TaskResult<T> data) {
        setListShown(true);

        if (data.success()) {
            onLoadFinished(data.getObject());
        } else {
            Log.e(mFragment.getClass().getName(), "Error in loader", data.getException());
            onLoadFailed(data.getException());
        }
    }

    public void onLoaderReset(Loader<BetterAsyncTask.TaskResult<T>> loader) {
        if (!mFragment.isVisible()) {
            return;
        }
        mGridView.setAdapter(null);
        setListShown(false);
        
    }

    protected void onLoadFailed(Exception ex) {
        mGridView.setAdapter(null);
        setListShown(false);
    }

    protected abstract void onLoadFinished(T result);
}
