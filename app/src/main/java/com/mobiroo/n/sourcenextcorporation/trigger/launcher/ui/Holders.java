package com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

/**
 * Created by krohnjw on 5/15/2014.
 */
public class Holders {

    public static class Trigger {

        public RelativeLayout content;
        public TextView label;
        public ImageView icon;
        public TextView spacer;
        public LinearLayout constraints_container;
        public View no_constraints_container;
        public TextView constraint_time_text;
        public TextView constraint_date_text;
        public TextView constraint_wifi_text;
        public TextView constraint_charging_text;
        public TextView constraint_bluetooth_text;
        public TextView constraint_airplane_mode_text;

        public Trigger(View v) {
            content = (RelativeLayout) v.findViewById(android.R.id.content);
            label = (TextView) v.findViewById(android.R.id.text1);
            icon = (ImageView) v.findViewById(android.R.id.icon);
            spacer = (TextView) v.findViewById(R.id.spacer);
            constraints_container = (LinearLayout) v.findViewById(R.id.constraints);
            no_constraints_container = v.findViewById(R.id.no_constraints);
            constraint_time_text = (TextView) v.findViewById(R.id.time_text);
            constraint_date_text = (TextView) v.findViewById(R.id.date_text);
            constraint_wifi_text = (TextView) v.findViewById(R.id.wifi_text);
            constraint_charging_text = (TextView) v.findViewById(R.id.charging_text);
            constraint_bluetooth_text = (TextView) v.findViewById(R.id.bluetooth_text);
            constraint_airplane_mode_text = (TextView) v.findViewById(R.id.airplane_mode_text);
        }

        public void hideAll() {
            constraint_time_text.setVisibility(View.GONE);
            constraint_date_text.setVisibility(View.GONE);
            constraint_wifi_text.setVisibility(View.GONE);
            constraint_bluetooth_text.setVisibility(View.GONE);
            constraint_charging_text.setVisibility(View.GONE);
            constraint_airplane_mode_text.setVisibility(View.GONE);
        }

        public void showConstraintsContainer() {
            no_constraints_container.setVisibility(View.GONE);
            constraints_container.setVisibility(View.VISIBLE);
        }

        public void hideConstraintsContainer() {
            constraints_container.setVisibility(View.GONE);
            no_constraints_container.setVisibility(View.VISIBLE);
        }

        public void setIcon(int resId) {
            icon.setImageResource(resId);
        }

        public void setLabel(String text) {
            label.setText(text);
        }
    }
}
