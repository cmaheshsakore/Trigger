package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class ListTabletMenuItem implements ListItem {
    protected Context mContext;
    protected int     mIconResId;
    protected int     mTextId;
    protected String  mTag;
    protected boolean mEnabled;
    protected boolean mShowSpacer;

    public static final int LAYOUT_EMPTY = -1;
    
    public ListTabletMenuItem(Context context, int icon_id, int text, boolean showSpacer, String tag) {
        mContext = context;
        mIconResId = icon_id;
        mTextId = text;
        mTag = tag;
        mShowSpacer = showSpacer;
        mEnabled = true;
    }
    
    public ListTabletMenuItem(Context context) {
        mContext = context;
        mIconResId = LAYOUT_EMPTY;
        mTextId = LAYOUT_EMPTY;
        mTag = "";
        mEnabled = true;
    }

    public ListTabletMenuItem() {
        mContext = null;
        mIconResId = LAYOUT_EMPTY;
        mTextId = LAYOUT_EMPTY;
        mTag = "";
        mEnabled = true;
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if ((mEnabled) && (!mTag.isEmpty())) {
            //if (null == convertView) {
                convertView = View.inflate(mContext, R.layout.list_item_tablet_menu_entry, null);
            //} 
            
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(mIconResId);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(mTextId);
            (convertView.findViewById(R.id.divider)).setVisibility(!mShowSpacer ? View.GONE : View.VISIBLE);

        } else {
            convertView = View.inflate(mContext, R.layout.list_item_tablet_menu_empty, null);
        }
        return convertView;
    }

    public int getTextId() {
        return mTextId;
    }
    
    public int getIconId() {
        return mIconResId;
    }

    public String getTag() {
        return mTag;
    }
    
    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean selectInMenu() { return true; }

}
