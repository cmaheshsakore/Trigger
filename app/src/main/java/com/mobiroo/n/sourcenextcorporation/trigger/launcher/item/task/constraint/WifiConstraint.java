package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListStringItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krohnjw on 12/19/13.
 */
public class WifiConstraint extends Constraint {

    private List<ListItem> mWifiList;
    private ArrayList<String> mWifiNetworks;
    private TextView mWifiView;
    private String mCondition = CONNECTED;

    public WifiConstraint() {
    }

    public WifiConstraint(String id, String trigger_id, String type, String key1, String key2) {
        super(id, trigger_id, type, key1, key2);
    }

    public WifiConstraint(int type, String key1, String key2) {
        super(type, key1, key2);
    }

    @Override
    public String getType() {
        return String.valueOf(TYPE_WIFI);
    }

    final String INVALID_SSID = "COMPLETELYINVALIDVALUEDONTUSETHIS";

    @Override
    public boolean isConstraintSatisfied(Context context) {
        String condition = getExtra(2);
        logd("Checking constraint: " + condition);

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean satisfied = true;

        if (IS_ON.equals(condition)) {
            satisfied = wifiManager.isWifiEnabled();
            logd("satisfied " + satisfied);
            return satisfied;
        } else if (IS_OFF.equals(condition)) {
            satisfied = !wifiManager.isWifiEnabled();
            logd("satisfied " + satisfied);
            return satisfied;
        }

        // Condition is either CONNECTED TO or NOT CONNECTED TO

        String ssid = INVALID_SSID;

        // Get currently connected SSID
        try {
            ssid = wifiManager.getConnectionInfo().getSSID();
            if ((ssid != null) && (!ssid.isEmpty())) {
                ssid = ssid.replace("\"", "");
            }
        } catch (Exception e) {
            Logger.e("Exception getting currently connected SSID", e);
        } finally {
            if (ssid == null) {
                ssid = "";
            }
            logd("Current SSID is " + ssid);
        }

        // Test if we care about this SSID
        boolean isInList = false;
        if (!getExtra(1).isEmpty()) {
            logd("List contains " + getExtra(1));
            String[] ssids = getExtra(1).split(",");
            for (String name : ssids) {
                isInList |= ((ssid != null) && (ssid.equals(name)));
            }
        }

        if (NOT_CONNECTED.equals(condition)) {
            // If the list of networks is empty and ssid is invalid/empty or we are not connected to a network in the list return true
            satisfied = (getExtra(1).isEmpty())
                    ? ssid.isEmpty() || INVALID_SSID.equals(ssid) || ssid.equals("<unknown ssid>")
                    : !isInList;

            logd("satisfied " + satisfied);
            return satisfied;
        } else {
            // If the list of networks to check is empty OR we are connected to a network in the list return true
            satisfied = getExtra(1).isEmpty() || isInList;
            logd("satisfied " + satisfied);
            return satisfied;
        }

    }

    @Override
    public View getView(Context context) {
        View base = super.getView(context);

        View child = View.inflate(context, R.layout.constraint_wifi, null);

        mWifiList = new ArrayList<ListItem>();
        mWifiNetworks = new ArrayList<String>();
        mWifiView = (TextView) child.findViewById(R.id.wifi_picker);
        mWifiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateWifiList(mContext);
            }
        });

        int id = R.id.connected;
        if (NOT_CONNECTED.equals(mCondition)) {
            id = R.id.not_connected;
        } else if (IS_ON.equals(mCondition)) {
            id = R.id.is_on;
        } else if (IS_OFF.equals(mCondition)) {
            id = R.id.is_off;
        }
        ((AppCompatRadioButton) child.findViewById(id)).setChecked(true);

        addChildToContainer(base, child);

        return base;
    }

    @Override
    public int getText() {
        return R.string.constraint_wifi;
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_action_wifi;
    }

    @Override
    public Constraint buildConstraint(Context context) {
        int checked = ((RadioGroup) getBaseView().findViewById(R.id.connection_type)).getCheckedRadioButtonId();
        String condition = CONNECTED;
        switch (checked) {
            case R.id.is_off:
                condition = IS_OFF;
                break;
            case R.id.is_on:
                condition = IS_ON;
                break;
            case R.id.not_connected:
                condition = NOT_CONNECTED;
                break;
            case R.id.connected:
                condition = CONNECTED;
                break;
        }

        return new WifiConstraint(TYPE_WIFI, TextUtils.join(",", mWifiNetworks), condition);
    }

    @Override
    public void loadData(Context context, Constraint c) {
        super.loadData(context, c);
        String[] networks = c.getExtra(1).split(",");
        mWifiNetworks = new ArrayList<String>();
        for (int i = 0; i < networks.length; i++) {
            mWifiNetworks.add(networks[i]);
        }
        mCondition = (!c.getExtra(2).isEmpty()) ? c.getExtra(2) : CONNECTED;

    }

    @Override
    protected void getTriggerViewText(Context context, Holders.Trigger holder) {
        String network = getExtra(1);
        String condition = getExtra(2);

        Logger.d("Connected is " + condition);

        if (NOT_CONNECTED.equals(condition)) {
            if (network.isEmpty()) {
                network = context.getString(R.string.any_network);
            }
            holder.constraint_wifi_text.setVisibility(View.VISIBLE);
            holder.constraint_wifi_text.setText(String.format(context.getString(R.string.display_not_connected),
                    network.replace(",", ", ")));
        } else if (IS_ON.equals(condition)) {
            holder.constraint_wifi_text.setVisibility(View.VISIBLE);
            holder.constraint_wifi_text.setText(String.format("%s: %s", context.getString(R.string.constraint_wifi), context.getString(R.string.is_on)));
        } else if (IS_OFF.equals(condition)) {
            holder.constraint_wifi_text.setVisibility(View.VISIBLE);
            holder.constraint_wifi_text.setText(String.format("%s: %s", context.getString(R.string.constraint_wifi), context.getString(R.string.is_off)));
        } else {
            if (!network.isEmpty()) {
                holder.constraint_wifi_text.setVisibility(View.VISIBLE);
                holder.constraint_wifi_text.setText(String.format(context.getString(R.string.display_wifi_network),
                        network.replace(",", ", ")));
            } else {
                holder.constraint_wifi_text.setVisibility(View.GONE);
            }
        }

    }

    private void updateWifiUi() {
        if (mWifiNetworks.size() == 0) {
            mWifiView.setText(mContext.getString(R.string.any_network));
        } else if (mWifiNetworks.size() == 1) {
            mWifiView.setText((mWifiNetworks.get(0).isEmpty()) ? mContext.getString(R.string.any_network) : mWifiNetworks.get(0));
        } else {
            mWifiView.setText(mContext.getString(R.string.two_or_more));
        }
    }

    private void populateWifiList(Context context) {
        if ((mWifiList == null) || (mWifiList.size() == 0)) {
            mWifiList = new ArrayList<ListItem>();

            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wm.isWifiEnabled()) {
                if (wm != null) {
                    List<WifiConfiguration> nets = wm.getConfiguredNetworks();
                    if (nets != null) {
                        for (WifiConfiguration net : nets) {
                            // Iterate over all, add networkID to map
                            if (net.SSID != null) {
                                mWifiList.add(new ListStringItem(mContext, (net.SSID.replace("\"", ""))));
                            }
                        }
                    }
                    showWifiChooser();
                    try {
                        context.unregisterReceiver(receiver);
                    } catch (Exception ignored) {

                    }
                }
            } else {
                context.registerReceiver(receiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
                wm.setWifiEnabled(true);
            }
        } else {
            showWifiChooser();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (state == WifiManager.WIFI_STATE_ENABLED) {
                populateWifiList(context);
                ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(false);
            }
        }
    };

    private void showWifiChooser() {
        mWifiNetworks = new ArrayList<String>();
        final SimpleDialogFragment fragment = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        fragment.setTitle(mContext.getString(R.string.select_network));
        fragment.setMultiSelect(true);
        fragment.setPositiveButton(mContext.getString(R.string.menu_done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = fragment.getListView().getCheckedItemPositions();
                for (int i = 0; i < fragment.getListView().getCount(); i++) {
                    if (checked.get(i)) {
                        String text = ((ListStringItem) mWifiList.get(i)).getText();
                        if (!mWifiNetworks.contains(text)) {
                            mWifiNetworks.add(text);
                        }
                    }
                }

                updateWifiUi();

                fragment.dismiss();
            }
        });
        //android.support.v4.app.FragmentManager manager = (() mContext);
        fragment.setListAdapter(new ListItemsAdapter(((Activity) mContext), mWifiList));
        fragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "mDialog");
    }

}
