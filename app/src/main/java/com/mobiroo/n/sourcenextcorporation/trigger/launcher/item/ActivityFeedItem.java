package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class ActivityFeedItem implements ListItem {

    private int         mType;
    private String      mName;
    private String      mTime;

    public ActivityFeedItem(int type, String name, String time) {

        mType = type;
        mName = name;
        mTime = time;
    }

    public String getName() {
        return mName;
    }

    public int getType() {
        return mType;
    }

    public int getTypeIcon() {
        return TaskTypeItem.getIconFromType(mType);
    }

    public String getTime() {
        return mTime;
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (convertView == null) {
            if (position == 0) {
                convertView = View.inflate(adapter.getActivity(), R.layout.list_item_feed_first, null);
            } else if (position == (adapter.getCount() - 1)) {
                convertView = View.inflate(adapter.getActivity(), R.layout.list_item_feed_last, null);
            } else {
                convertView = View.inflate(adapter.getActivity(), R.layout.list_item_feed, null);
            }
        }

        String time = Utils.getTimeStringAsLocal(getTime(), "");
        String output = Utils.formatLastUsed(getTime(), "");
        if (!time.isEmpty()) {
            output += " (" +  time + ")";
        }

        ((ImageView) convertView.findViewById(R.id.icon)).setImageResource(getTypeIcon());
        ((TextView) convertView.findViewById(R.id.message)).setText(Utils.decodeData(getName()));
        ((TextView) convertView.findViewById(R.id.date)).setText(output);

        return convertView;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
