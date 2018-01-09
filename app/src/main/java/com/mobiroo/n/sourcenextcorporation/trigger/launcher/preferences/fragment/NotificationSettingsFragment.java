package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListStringItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

public class NotificationSettingsFragment extends SettingsFragment {
    
    final int ACTIVITY_PICK_NOTIFICATION = 2;
    final int ACTIVITY_PICK_TIMER = 3;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_notifications,  null);
    }

    @Override
    protected void setupClickHandlers() {

    }

    @Override
    protected void loadSettings() {
        
        updateNotificationDisplay(getBoolean(mContext,Constants.PREF_SHOW_NOTIFICATION, true), getBoolean(mContext, Constants.PREF_SHOW_TOAST, false));
        updateVibrateDisplay(getBoolean(mContext, Constants.PREF_VIBRATE, false));
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.NotificationContainer:
                showNotificationOptions();
                break;
            case R.id.VibrateContainer:
                showVibrateOptions();
                break;
            case R.id.NotificationSoundButton:
                popNotificationPicker();
                break;
            case R.id.TimerSoundButton:
                popTimerPicker();
                break;
        }
        
        updatePreferences();
        
    }
    
    private void updateNotificationDisplay(boolean notificationEnabled, boolean toastEnabled) {
        TextView t = (TextView) mView.findViewById(R.id.prefNotificationSubText);
        if (notificationEnabled && !toastEnabled) {
            t.setText(getString(R.string.layoutPreferencesNotificationsSubBar));
        }
        else if (!notificationEnabled && toastEnabled) {
            t.setText(getString(R.string.layoutPreferencesNotificationsSubToast));
        }
        else if (notificationEnabled && toastEnabled) {
            t.setText(getString(R.string.layoutPreferencesNotificationsSubBoth));
        }
        else {
            t.setText(getString(R.string.layoutPreferencesNotificationsSubNone));
        }
        
        
        /* In ICS and up hide notification tone */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ((LinearLayout) mView.findViewById(R.id.notification_sound_layout)).setVisibility(View.GONE);
        } else {
            ((LinearLayout) mView.findViewById(R.id.notification_sound_layout)).setVisibility(View.VISIBLE);
        }
        
    }

    private void updateVibrateDisplay(boolean enabled) {
        TextView t = (TextView) mView.findViewById(R.id.prefVibrateSubText);
        if (enabled) {
            t.setText(getString(R.string.layoutPreferencesVibrateSubAlways));
        } else {
            t.setText(getString(R.string.layoutPreferencesVibrateSubNever));
        }
    }
    
    private void showNotificationOptions() {
        final ArrayList<ListStringItem> items = new ArrayList<ListStringItem>() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            {
                add(new ListStringItem(mContext,getString(R.string.layoutPreferencesNotificationsSubBar)));
                add(new ListStringItem(mContext,getString(R.string.layoutPreferencesNotificationsSubToast)));
                add(new ListStringItem(mContext,getString(R.string.layoutPreferencesNotificationsSubBoth)));
                add(new ListStringItem(mContext,getString(R.string.layoutPreferencesNotificationsSubNone)));
            }
        };


        final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        dialog.setTitle(getString(R.string.layoutPreferencesVibrateOptionTitle));
        dialog.setListAdapter(new ListItemsAdapter(getActivity(), items.toArray(new ListStringItem[items.size()]))); 
        dialog.setListOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

                SharedPreferences.Editor editor = getEditor(getActivity());
                String clicked = ((ListStringItem) adapter.getItemAtPosition(position)).getText();
                boolean pNotification = false, pToast = false;
                if (clicked.equals(getString(R.string.layoutPreferencesNotificationsSubBar))) {
                    pNotification = true;
                    pToast = false;
                } else if (clicked.equals(getString(R.string.layoutPreferencesNotificationsSubToast))) {
                    pNotification = false;
                    pToast = true;
                } else if (clicked.equals(getString(R.string.layoutPreferencesNotificationsSubBoth))) {
                    pNotification = true;
                    pToast = true;
                } else {
                    pNotification = false;
                    pToast = false;

                }

                editor.putBoolean(Constants.PREF_SHOW_NOTIFICATION, pNotification);
                editor.putBoolean(Constants.PREF_PERSISTENT_NOTIFICATION, pNotification);
                editor.putBoolean(Constants.PREF_SHOW_TOAST, pToast);

                editor.commit();
                updateNotificationDisplay(pNotification, pToast);
                dialog.dismiss();
            }

        });

        dialog.show(getFragmentManager(), "dialog");

    }

    private void showVibrateOptions() {

        final ArrayList<ListStringItem> items = new ArrayList<ListStringItem>() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            {
                add(new ListStringItem(mContext,getString(R.string.layoutPreferencesVibrateOptionEnable))); 
                add(new ListStringItem(mContext,getString(R.string.layoutPreferencesVibrateOptionDisable)));
            }
        };


        final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        dialog.setTitle(getString(R.string.layoutPreferencesVibrateOptionTitle));
        dialog.setListAdapter(new ListItemsAdapter(getActivity(), items.toArray(new ListStringItem[items.size()]))); 
        dialog.setListOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                SharedPreferences.Editor editor = getEditor(getActivity());
                String clicked = ((ListStringItem) adapter.getItemAtPosition(position)).getText();
                if (clicked.equals(getString(R.string.layoutPreferencesVibrateOptionEnable))) {
                    editor.putBoolean(Constants.PREF_VIBRATE, true);
                    updateVibrateDisplay(true);
                } else {
                    editor.putBoolean(Constants.PREF_VIBRATE, false);
                    updateVibrateDisplay(false);

                }
                editor.commit();
                dialog.dismiss();
            }
        });

        dialog.show(getFragmentManager(), "dialog");

    }
    
    private void popNotificationPicker() {
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        // Current URI
        try {
            SharedPreferences settings = getPrefs(getActivity());
            String savedURI = settings.getString(Constants.PREF_NOTIFICATION_URI, "");
            if (!(savedURI == null) && (!(savedURI.equals(""))))
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(savedURI));
        } catch (Exception e) {
        }
        getActivity().startActivityForResult(i, ACTIVITY_PICK_NOTIFICATION);
    }

    private void popTimerPicker() {
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        // Current URI
        try {
            SharedPreferences settings = getPrefs(getActivity());
            String savedURI = settings.getString(Constants.PREF_TIMER_URI, "");
            if (!(savedURI == null) && (!(savedURI.equals(""))))
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(savedURI));
        } catch (Exception e) {
        }
        getActivity().startActivityForResult(i, ACTIVITY_PICK_TIMER);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        switch (getFragmentRequestCode(requestCode)) {
            case ACTIVITY_PICK_NOTIFICATION:
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences.Editor editor = getEditor(getActivity());

                    // Update the position of the notification tone
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Ringtone rt = RingtoneManager.getRingtone(mContext, uri);
                    Log.i(Constants.TAG, "Got back " + rt);
                    if ((rt != null) && (uri != null)) {
                        editor.putString(Constants.PREF_NOTIFICATION_URI, uri.toString());
                        editor.putBoolean(Constants.PREF_PLAY_AUDIO, true);
                        editor.commit();
                    } else {
                        editor.putString(Constants.PREF_NOTIFICATION_URI, "");
                        editor.putBoolean(Constants.PREF_PLAY_AUDIO, false);
                        editor.commit();
                    }
                }
                break;
            case ACTIVITY_PICK_TIMER:
                if (resultCode == Activity.RESULT_OK) {
                    SharedPreferences.Editor editor = getEditor(getActivity());

                    // Update the position of the notification tone
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    Ringtone rt = RingtoneManager.getRingtone(mContext, uri);
                    Log.i(Constants.TAG, "Got back " + rt);
                    if ((rt != null) && (uri != null)) {
                        editor.putString(Constants.PREF_TIMER_URI, uri.toString());
                        editor.commit();
                    } else {
                        // Do Nothing
                        editor.putString(Constants.PREF_TIMER_URI, "");
                        editor.commit();
                    }
                }
                break;
        }
    }
}
