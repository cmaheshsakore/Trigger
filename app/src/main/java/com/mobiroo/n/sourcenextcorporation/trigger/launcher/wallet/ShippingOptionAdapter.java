package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;

public class ShippingOptionAdapter extends ListItemsAdapter {

    private Context mContext;

    public ShippingOptionAdapter(Activity activity, ShippingOption[] options) {
        super(activity, options);
        mContext = activity.getBaseContext();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_shipping, null);
        }

        ShippingOption item = (ShippingOption) getItem(position);
        String name = item.getName();
        name = name.replace("(", "\n("); // Break (NO TRACKING) onto a new line for display purposes
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(name);
        ((TextView) convertView.findViewById(android.R.id.text2)).setText(NumberFormat.getCurrencyInstance(Locale.US).format(item.getRateInDollars()));

        return convertView;
    }
}

