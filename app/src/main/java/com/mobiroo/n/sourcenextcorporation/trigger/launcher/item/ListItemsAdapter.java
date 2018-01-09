package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.trigger.launcher.item.HeaderItem;
import com.mobiroo.n.sourcenextcorporation.trigger.util.FilterableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListItemsAdapter extends FilterableListAdapter<ListItem> {
    private final List<Class<? extends ListItem>> ITEM_TYPES = new ArrayList<Class<? extends ListItem>>() {
        {
            add(HeaderItem.class);
            add(ShopItem.class);
            add(ShopMoreItem.class);
            add(TaskTypeItem.class);
            add(ListStringItem.class);
            add(ListTabletMenuItem.class);
        }
    };

    private Activity mActivity;

    public ListItemsAdapter(Activity activity, ListItem[] items) {
        mActivity = activity;
        updateItems(items);
    }
    
    public ListItemsAdapter(Activity activity, List<ListItem> items) {
        mActivity = activity;
        updateItems(items);
    }

    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = getItem(position);
        return item.getView(this, position, convertView);
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_TYPES.size();
    }

    @Override
    public int getItemViewType(int position) {
        ListItem item = getItem(position);
        return ITEM_TYPES.indexOf(item.getClass());
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }
}
