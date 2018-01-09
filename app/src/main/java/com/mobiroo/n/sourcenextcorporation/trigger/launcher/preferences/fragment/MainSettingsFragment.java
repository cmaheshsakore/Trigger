package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.PreferenceCategory;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

public class MainSettingsFragment extends SettingsFragment implements OnItemClickListener {

    private ListItemsAdapter mAdapter;
    private ListView            mList;
    private View                mView;
    private boolean             mIsPhoneLayout;
    
    private final PreferenceCategory[] CATEGORIES = {
            new PreferenceCategory(R.string.general, R.drawable.ic_action_gear),
            new PreferenceCategory(R.string.backup_restore, R.drawable.ic_action_export),
            new PreferenceCategory(R.string.layoutPreferencesTagsTitle, R.drawable.ic_action_wizard),
            new PreferenceCategory(R.string.layoutPreferencesNotificationsTitle, R.drawable.ic_action_phone),
            new PreferenceCategory(R.string.layoutPreferencesCredentialsTitle, R.drawable.ic_action_globe),
            new PreferenceCategory(R.string.layoutPreferencesAdvancedTitle, R.drawable.ic_action_bug),
            new PreferenceCategory(R.string.menu_help, R.drawable.ic_action_info),
            new PreferenceCategory(R.string.menu_privacy_policy, R.drawable.ic_action_privacy_policy)
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_main,  null);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mView = view;
        mIsPhoneLayout = Utils.isPhoneLayout(getActivity(), Utils.MAX_PHONE_800);
        setupPreferencesMenu();
    }
    
    private void setupPreferencesMenu() {
        mList = (ListView) mView.findViewById(R.id.list);
        mAdapter = new ListItemsAdapter(getActivity(), CATEGORIES);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
    }
    
    @Override
    protected void loadSettings() {
        
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PreferenceCategory item = (PreferenceCategory) mAdapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable(SettingsFragment.KEY_CATEGORY, item);

        SettingsFragment fragment = item.getFragment();
        if (fragment != null) {
            if (mIsPhoneLayout) {
                /* Start a new activity with the specificed fragment */
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra(SettingsFragment.KEY_CATEGORY, item);
                getActivity().startActivityForResult(intent, SettingsActivity.REQUEST_SETTINGS_SUBSCREEN);
                getActivity().overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            } else {
                /* Load this fragment into the container */
                String name = getActivity().getString(item.getName());
                
                ((SettingsActivity)getActivity()).addFragment(
                        item.getFragment(),
                        name
                        );
            }
        } else {
            if (item.getActivityClass() != null) {
                getActivity().startActivityForResult(new Intent(getActivity(), item.getActivityClass()), SettingsActivity.REQUEST_SETTINGS_SUBSCREEN);
                getActivity().overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            } else {
                if (item.getName() == R.string.menu_help) {
                    showAboutDialog();
                } if (item.getName() == R.string.menu_privacy_policy) {
                    showPrivacyPolicy();
                }
            }
        }
    }
    
    private void showAboutDialog() {
        View view = View.inflate(getActivity(), R.layout.about_dialog, null);

        TextView heading = (TextView) view.findViewById(R.id.aboutTextHeading);
        heading.setText(String.format("Trigger\nVersion: %s", getPackageVersion()));

        TextView aboutTextView = (TextView) view.findViewById(R.id.aboutText);
        aboutTextView.setText(BuildConfiguration.ABOUT_TEXT);
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(aboutTextView, Linkify.WEB_URLS);

        new AlertDialog.Builder(getActivity())
            .setView(view)
            .show();
    }
    
    private String getPackageVersion() {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return String.format("%s (%s)", packageInfo.versionName, packageInfo.versionCode);
        } catch (Exception e) {
            return null;
        }
    }

    private void showPrivacyPolicy() {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.PRIVACY_POLICY_URL));

        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
