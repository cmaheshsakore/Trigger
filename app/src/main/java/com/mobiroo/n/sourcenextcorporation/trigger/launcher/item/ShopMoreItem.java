package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

public class ShopMoreItem implements ListItem {
    
    private View mView;
    
    public View getView(final Context context) {
        if (mView != null) {
            return mView;
        }

        mView = View.inflate(context, R.layout.shop_more, null);
        mView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Usage.storeTuple(context, Codes.SHOP_TAGSTAND, Codes.COMMAND_URL, -2);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://nfctags.trigger.com"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        
        return mView;
    }
        
    @Override
    public View getView(final ListItemsAdapter adapter, int position, View convertView) {
        if (convertView != null) {
            return convertView;
        }

        View view = adapter.getActivity().getLayoutInflater().inflate(R.layout.shop_more, null);
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Usage.storeTuple(adapter.getActivity(), Codes.SHOP_TAGSTAND, Codes.COMMAND_URL, -2);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://nfctags.trigger.com"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                adapter.getActivity().startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
