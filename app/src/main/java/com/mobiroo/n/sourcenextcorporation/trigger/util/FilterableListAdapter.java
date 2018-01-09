package com.mobiroo.n.sourcenextcorporation.trigger.util;


import android.widget.BaseAdapter;

import java.util.*;

public abstract class FilterableListAdapter<T> extends BaseAdapter {
    private final Object mLock = new Object();

    private List<T> mOriginalItems;
    private List<T> mItems = new ArrayList<T>();

    protected FilterableListAdapter() {
    }

    protected FilterableListAdapter(T[] items) {
        updateItems(items);
    }

    @SuppressWarnings("unused")
    private FilterableListAdapter(List<T> items) {
        updateItems(items);
    }

    public int getCount() {
        return mItems.size();
    }

    public boolean contains(T item) {
        synchronized (mLock) {
            return mOriginalItems.contains(item);
        }
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void addItem(T item) {
        synchronized (mLock) {
            mOriginalItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void removeItem(T item) {
        synchronized (mLock) {
            mOriginalItems.remove(item);
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        updateItems((List<T>) null);
    }

    public void updateItems(T[] items) {
        if (items != null) {
            updateItems(Arrays.asList(items));
        } else {
            clearItems();
        }
    }

    public void updateItems(List<T> items) {
        synchronized (mLock) {
            mOriginalItems = items;
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        synchronized (mLock) {
            List<T> items = new ArrayList<T>();
            if (mOriginalItems != null) {
                for (T item : mOriginalItems) {
                    if (isVisible(item)) {
                        items.add(item);
                    }
                }
            }
            Comparator<? super T> comparator = getComparator();
            if (comparator != null) {
                Collections.sort(items, comparator);
            }
            mItems = items;
        }
        super.notifyDataSetChanged();
    }

    protected Comparator<? super T> getComparator() {
        return null;
    }

    public boolean isVisible(T item) {
        return true;
    }
}
