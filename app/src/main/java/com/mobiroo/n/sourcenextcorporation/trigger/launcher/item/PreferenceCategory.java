package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.BackupActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.AdvancedSettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.NotificationSettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.SettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.GeneralSettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.LoginSettingsFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment.TasksSettingsFragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PreferenceCategory implements Parcelable, ListItem {

    private int     mName;
    private int     mIcon;
    
    public PreferenceCategory(int name, int icon) {
        mName = name;
        mIcon = icon;
    }

    public int getName() {
        return mName;
    }

    public int getIcon() {
        return mIcon;
    }
    
    public SettingsFragment getFragment() {
        switch(mName) {
            case R.string.general:
                return new GeneralSettingsFragment();
            case R.string.layoutPreferencesTagsTitle:
                return new TasksSettingsFragment();
            case R.string.layoutPreferencesNotificationsTitle:
                return new NotificationSettingsFragment();
            case R.string.layoutPreferencesCredentialsTitle:
                return new LoginSettingsFragment();
            case R.string.layoutPreferencesAdvancedTitle:
                return new AdvancedSettingsFragment();
            default:
                return null;
        }
    }

    public Class<?> getActivityClass() {
        switch (mName) {
            case R.string.backup_restore:
                return BackupActivity.class;
            default:
                return null;
        }
    }
    public static final Creator<PreferenceCategory> CREATOR = new Creator<PreferenceCategory>() {
        @Override
        public PreferenceCategory createFromParcel(Parcel source) {
            return new PreferenceCategory(source.readInt(), source.readInt());
        }

        @Override
        public PreferenceCategory[] newArray(int size) {
            return new PreferenceCategory[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mName);
        dest.writeInt(mIcon);
    }

    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (null == convertView) {
            convertView = View.inflate(adapter.getActivity(), R.layout.list_item_category, null);
        }

        PreferenceCategory item = (PreferenceCategory) adapter.getItem(position);
        ((TextView) convertView.findViewById(R.id.row1Text)).setText(item.getName());
        ((ImageView) convertView.findViewById(R.id.row1Icon)).setImageResource(item.getIcon());
        return convertView;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
