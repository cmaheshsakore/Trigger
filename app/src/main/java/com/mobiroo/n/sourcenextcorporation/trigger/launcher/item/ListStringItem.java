package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class ListStringItem implements ListItem {

    private Context mContext;
    private String mText;
    
    public ListStringItem(Context context, String text) {
        mContext = context;
        mText = text;
    }
    
    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.list_item_single, null);
        } 

        TextView tv = (TextView) convertView.findViewById(R.id.row1Text);
        ListStringItem item = (ListStringItem) adapter.getItem(position);
        tv.setText(item.mText);
        return convertView;
    }

    public String getText() {
        return mText;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }

}
