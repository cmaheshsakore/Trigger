package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.view.View;

public interface ListItem {
    public View getView(ListItemsAdapter adapter, int position, View convertView);
    public boolean isEnabled();
}
