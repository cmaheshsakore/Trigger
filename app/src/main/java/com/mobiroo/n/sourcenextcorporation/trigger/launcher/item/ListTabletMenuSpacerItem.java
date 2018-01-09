package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class ListTabletMenuSpacerItem extends ListTabletMenuItem {
    private Context mContext;
    private int     mTextId;
    private String  mTag;
    private boolean mEnabled;

    public static final int LAYOUT_EMPTY = -1;

    public ListTabletMenuSpacerItem(Context context, int text, String tag) {
        super(context, -1, text, true, tag);
        mContext = context;
        mTextId = text;
        mTag = tag;
        mEnabled = false;
    }

    public ListTabletMenuSpacerItem(Context context) {
        super(context, -11, LAYOUT_EMPTY, true, "");
        mContext = context;
        mTextId = LAYOUT_EMPTY;
        mTag = "";
        mEnabled = false;
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        convertView = View.inflate(mContext, R.layout.list_item_tablet_menu_spacer, null);
        if (mTextId != -1) {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(mTextId);
        }
        return convertView;
    }

    public int getTextId() {
        return mTextId;
    }


    public String getTag() {
        return mTag;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

}
