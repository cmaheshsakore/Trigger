package com.trigger.launcher.item;

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.trigger.launcher.fragment.ShopFragment;

public class TextItem implements ListItem {
    private String mText;
    private ShopFragment mShopFragment;
    private View mConvertView;
    
    public TextItem(ShopFragment shopFragment, String text) {
        mShopFragment = shopFragment;
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public View getView() {
        if (mConvertView == null) {
            mConvertView = mShopFragment.getActivity().getLayoutInflater().inflate(R.layout.list_text, null);
        }
        TextView headerTextView = (TextView) mConvertView.findViewById(android.R.id.text1);
        headerTextView.setText(Html.fromHtml(mText));
        headerTextView.setPadding(0, headerTextView.getPaddingTop(), headerTextView.getPaddingRight(), headerTextView.getPaddingBottom());

        return mConvertView;
    }
    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (convertView == null) {
            convertView = mShopFragment.getActivity().getLayoutInflater().inflate(R.layout.list_text, null);
        }

        TextView headerTextView = (TextView) convertView.findViewById(android.R.id.text1);
        headerTextView.setText(Html.fromHtml(mText));
        headerTextView.setPadding(0, headerTextView.getPaddingTop(), headerTextView.getPaddingRight(), headerTextView.getPaddingBottom());

        return convertView;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
