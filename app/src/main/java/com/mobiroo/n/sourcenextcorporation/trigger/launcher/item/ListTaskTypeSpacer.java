package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

import android.view.View;
import android.widget.TextView;

public class ListTaskTypeSpacer extends TaskTypeItem {

    private String  mText;
    private int     mTextInt;
    private int     mBackground;
    private boolean mEnabled;
    private Class   mClass;

    public ListTaskTypeSpacer(String text, int color) {
        super(R.string.layoutPreferencesTagsMore, -1, -1, -1, null, -1, R.layout.list_item_spacer);
        mTextInt = R.string.layoutPreferencesTagsMore;
        mText = text;
        mBackground = color;
        mEnabled = false;
        mClass = null;
    }

    public ListTaskTypeSpacer(int text, int color, boolean enabled, Class clickClass) {
        super(R.string.layoutPreferencesTagsMore, -1, -1, -1, null, -1, R.layout.list_item_spacer);
        mTextInt = text;
        mBackground = color;
        mEnabled = enabled;
        mClass = clickClass;
    }
    
    public Class getClassOnClick() {
        return mClass;
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (null == convertView) {
            convertView = View.inflate(adapter.getActivity(), R.layout.list_item_spacer, null);
        } 

        if (mBackground > 0) {
            (convertView.findViewById(R.id.row1Text)).setBackgroundColor(getBackgroundColor());
        }

        TextView tv = (TextView) convertView.findViewById(R.id.row1Text);
        if (getTextInt() > 0) {
            tv.setText(getTextInt());
        } else if (getText() != null) {
            tv.setText(getText());
        }

        return convertView;
    }

    public int getBackgroundColor() {
        return mBackground;
    }
    
    public String getText() {
        return mText;
    }

    public int getTextInt() { return mTextInt; }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

}
