package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ListIconTextItem implements ListItem {

    private Context mContext;
    private String mText;
    private int mIconId;
    
    public ListIconTextItem(Context context, String text, int icon) {
        mContext = context;
        mText = text;
        mIconId = icon;
    }
    
    public ListIconTextItem(Context context, int text, int icon) {
        mContext = context;
        mText = context.getString(text);
        mIconId = icon;
    }
    
    
    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.list_item_single_with_icon, null);
        } 

        ListIconTextItem item = (ListIconTextItem) adapter.getItem(position);
        ((TextView) convertView.findViewById(R.id.row1Text)).setText(item.mText);
        ((ImageView) convertView.findViewById(R.id.row1Icon)).setImageResource(getIconId());
        return convertView;
    }

    public String getText() {
        return mText;
    }
    
    public int getIconId() {
        return mIconId;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }

}
