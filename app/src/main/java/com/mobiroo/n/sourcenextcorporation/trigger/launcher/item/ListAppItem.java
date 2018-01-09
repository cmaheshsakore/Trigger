package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

public class ListAppItem implements ListItem {

    private Context mContext;
    private ActivityInfo mActivityInfo;
    
    public ListAppItem(Context context, ActivityInfo info) {
        mContext = context;
        mActivityInfo = info;
    }
    
    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.list_item_single, null);
        } 

        TextView tv = (TextView) convertView.findViewById(R.id.row1Text);
        ListAppItem item = (ListAppItem) adapter.getItem(position);
        tv.setText(item.getLabel());
        return convertView;
    }

    public String getLabel() {
        return (mActivityInfo != null) ? String.valueOf(mActivityInfo.loadLabel(mContext.getPackageManager())) : mContext.getString(R.string.layoutPreferencesNotificationsSubNone);
    }
    
    public String getPackage() {
        return (mActivityInfo != null) ? mActivityInfo.packageName : mContext.getString(R.string.layoutPreferencesNotificationsSubNone);
    }
    
    public String getPackageName() {
        return (mActivityInfo != null) ? mActivityInfo.name : mContext.getString(R.string.layoutPreferencesNotificationsSubNone);
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }

}
