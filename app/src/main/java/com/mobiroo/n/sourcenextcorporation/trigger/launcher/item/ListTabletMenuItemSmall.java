package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

/**
 * Created by krohnjw on 5/20/2014.
 */
public class ListTabletMenuItemSmall extends ListTabletMenuItem  {

    private boolean mSelect = false;

    public ListTabletMenuItemSmall(Context context, int icon_id, int text, boolean showSpacer, String tag) {
        super(context, icon_id, text, showSpacer, tag);
    }

    public ListTabletMenuItemSmall(Context context, int icon_id, int text, boolean showSpacer, String tag, boolean select) {
        super(context, icon_id, text, showSpacer, tag);
        mSelect = select;
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if ((mEnabled) && (!mTag.isEmpty())) {
            //if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.list_item_tablet_menu_entry_small, null);
            //}

            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(mIconResId);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(mTextId);

            /*if (position == (adapter.getCount() - 1) || !mShowSpacer) {
                (convertView.findViewById(R.id.divider)).setVisibility(View.GONE);
            }*/
        } else {
            convertView = View.inflate(mContext, R.layout.list_item_tablet_menu_empty, null);
        }
        return convertView;
    }

    @Override
    public boolean selectInMenu() { return mSelect; }
}
