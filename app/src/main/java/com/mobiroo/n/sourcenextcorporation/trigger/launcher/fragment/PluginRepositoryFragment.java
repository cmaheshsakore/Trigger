package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.FilterableListAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PluginRepositoryFragment extends Fragment {
   
    private class PluginItem  {

        private int mIcon;
        private int mTitle;
        private int mDescription;
        private boolean mIsInstalled;
        private String mPackage;
        
        public PluginItem(int icon, int title, int description, String packageName, boolean isInstalled) {
            mIcon = icon;
            mTitle = title;
            mDescription = description;
            mIsInstalled = isInstalled;
            mPackage = packageName;
        }
        
        public Uri getUri() {
            return Uri.parse("https://play.google.com/store/apps/details?id=" + mPackage);
        }

        public int getIcon() {
            return mIcon;
        }
        
        public int getDescription() {
            return mDescription;
        }
        
        public int getTitle() {
            return mTitle;
        }

        public boolean isInstalled() { return mIsInstalled; }

        public Intent getLauncherIntent(Context context) {
            return Utils.getLaunchIntentForPackage(context, mPackage);
        }

        public String getPackage() { return mPackage; }
    }
    
    private GridView            mListView;
    private List<PluginItem>    mItems;
    private class PluginAdapter extends FilterableListAdapter<PluginItem> {

        public PluginAdapter(FragmentActivity activity, PluginItem[] array) {
            super(array);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            final PluginItem item = getItem(position);
            
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.list_item_plugin, null);
            }

            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(item.getIcon());
            convertView.findViewById(android.R.id.summary).setVisibility(item.isInstalled() ? View.VISIBLE : View.GONE);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.getTitle());
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(item.getDescription());

            if (!item.isInstalled()) {
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, item.getUri());
                        startActivity(intent);
                    }
                });
            } else {
                final Intent launcher = item.getLauncherIntent(getActivity());
                if (launcher == null) {
                    convertView.setEnabled(false);
                    ((TextView) convertView.findViewById(android.R.id.summary)).setTextColor(getResources().getColor(R.color.write_success));
                } else {
                    convertView.setEnabled(true);

                    ((TextView) convertView.findViewById(android.R.id.summary)).setTextColor(getResources().getColor(R.color.pressed_nfctl));
                    ((TextView) convertView.findViewById(android.R.id.summary)).setText(R.string.configure);
                    convertView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            startActivity(launcher);
                        }
                    });
                }
            }
            return convertView;
        }
        
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plugins, null);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        PackageManager manager = getActivity().getPackageManager();

        mItems = ImmutableList.of( 
                new PluginItem(R.drawable.ic_plugin_tag, R.string.plugin_tag_reuse, R.string.plugin_tag_reuse_description, "com.trigger.launcher.tagreuse", Utils.isPackageInstalled(manager, "com.trigger.launcher.tagreuse")),
                new PluginItem(R.drawable.ic_plugin_call, R.string.plugin_call, R.string.plugin_call_description, "com.trigger.nfctl.plugins.call", Utils.isPackageInstalled(manager, "com.trigger.nfctl.plugins.call")),
                new PluginItem(R.drawable.ic_plugin_sms, R.string.plugin_sms, R.string.plugin_sms_description, "com.trigger.nfctl.plugins.sms", Utils.isPackageInstalled(manager, "com.trigger.nfctl.plugins.sms")),
                new PluginItem(R.drawable.ic_plugin_lock, R.string.plugin_lock, R.string.plugin_lock_description, "com.trigger.launcher.nfctl.plugins.lockscreenplugin", Utils.isPackageInstalled(manager, "com.trigger.launcher.nfctl.plugins.lockscreenplugin"))
                );
        
        mListView = (GridView) view.findViewById(android.R.id.list);
        int widthDp = Utils.getWidthInDp(getActivity());
        if (widthDp >= 1280) {
            mListView.setNumColumns(2);
        } else {
            mListView.setNumColumns(1);
        }
        mListView.setAdapter(new PluginAdapter(getActivity(), mItems.toArray(new PluginItem[mItems.size()])));
    }
}
