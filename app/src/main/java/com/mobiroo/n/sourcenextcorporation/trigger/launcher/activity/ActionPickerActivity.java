package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildTools;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActionCategory;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.Action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionPickerActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    private ActionCategory[] mActions;
    private final int REQUEST_CONFIGURE_ACTIONS = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActions = BuildTools.buildActionCategoryList(!BuildTools.shouldShowSecureSettingOperation(ActionPickerActivity.this, true, 17));

        setContentView(R.layout.activity_action_picker);

        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        ((TextView) findViewById(android.R.id.title)).setTextColor(Color.WHITE);
        ((TextView) findViewById(android.R.id.title)).setBackgroundColor(getResources().getColor(R.color.highlight_yellow));
        (findViewById(R.id.titleDivider)).setBackgroundColor(getResources().getColor(R.color.title_spacer_colored));
        
        /* Incoming actions to be configured from pre-loaded tag */
        final String preconfiguredActions = getIntent().getStringExtra(Constants.EXTRA_PRELOADED_CODES);
        if (preconfiguredActions != null) {
            List<String> actionsToSelect = Arrays.asList(preconfiguredActions.split(","));
            // Iterate over codes and mark actions as checked prior to rendering
            for (ActionCategory category : mActions) {
                for (Action action : category.getActions()) {
                    if (actionsToSelect.contains(action.getCode())) {
                        action.setChecked(true);
                        findViewById(R.id.ok_button).setEnabled(true);
                    }
                }
            }
        }

        ExpandableListView list = (ExpandableListView) findViewById(R.id.list);
        list.setAdapter(new ActionsAdapter(mActions));
        list.setDividerHeight(0);
        list.setOnChildClickListener(this);

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfigurationDialog(getPendingActionsIterable(), false);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(ActionPickerActivity.this)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Usage.canLogData(ActionPickerActivity.this)) {
            EasyTracker.getInstance(this).activityStop(this);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView listView, View view, int groupPosition, int childPosition, long id) {
        ActionsAdapter adapter = (ActionsAdapter) listView.getExpandableListAdapter();

        Action action = (Action) adapter.getChild(groupPosition, childPosition);

        action.setChecked(!action.isChecked());
        if (action.isChecked()) {
            showActionWarning(action.getCode());
        }
        adapter.notifyDataSetChanged();

        Usage.logEvent(
                Usage.getAnalyticsObject(ActionPickerActivity.this),
                "Action selected",
                false
        );

        findViewById(R.id.ok_button).setEnabled(getSelectedActions().size() > 0);

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONFIGURE_ACTIONS:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
        }
    }

    private void showActionWarning(String code) {

        if (code.equals(Codes.OPERATE_GPS) || code.equals(Codes.NOTIFICATION_MODE)) {
            /* Check if package has WRITE_SECURE_SETTINGS */
            PackageManager pm = getPackageManager();
            if (!(pm.checkPermission(permission.WRITE_SECURE_SETTINGS, getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
                showSecureSettingDialog();
            }
        } else if (code.equals(Codes.OPERATE_AIRPLANE_MODE)) {
            PackageManager pm = getPackageManager();
            if (Build.VERSION.SDK_INT >= 17) {
                if (!(pm.checkPermission(permission.WRITE_SECURE_SETTINGS, getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
                    showSecureSettingDialog();
                } else {
                    showNFCInAirplaneModeDialog();
                }
            } else {
                showNFCInAirplaneModeDialog();
            }
        } else if (code.equals(Codes.KILL_APPLICATION)) {
            showCloseAppsDialog();
        } else if (code.equals(Codes.OPERATE_MOBILE_DATA)) {
            if (Build.VERSION.SDK_INT >= 17) {
                PackageManager pm = getPackageManager();
                if (!(pm.checkPermission(permission.MODIFY_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED)) {
                    showModifyPhoneDialog();
                }
            }
        } else if (code.equals(Codes.OPERATE_HOTSPOT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                showRootMaybeDialog();
            }
        }
    }

    private void showRootMaybeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.root_access_title));
        builder.setMessage(R.string.root_access_required)
                .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).create().show();
    }

    private void showCloseAppsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.layoutAppKillAppText));
        builder.setMessage(R.string.layoutAppKillTaskDisclaimer)
                .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).create().show();
    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    private void showNFCInAirplaneModeDialog() {
        /* Check user permission about NFC in airplane mode, show a dialog if NFC is set to disable in airplane mode */
        String radios = (Build.VERSION.SDK_INT < 17) ? Settings.System.getString(getContentResolver(), Settings.System.AIRPLANE_MODE_RADIOS) : Settings.Global.getString(getContentResolver(), Settings.Global.AIRPLANE_MODE_RADIOS);
        String radio_var = (Build.VERSION.SDK_INT < 10) ? Settings.System.RADIO_NFC : Settings.Global.RADIO_NFC;
        if (radios.contains(radio_var)) {
            showDialog(this, "", getString(R.string.airplaneModeWarning), getString(R.string.dialogOK));
        }
    }

    private void showModifyPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.alertSecureSettingTitle));
        builder.setMessage(getString(R.string.alertSecureSetting));
        String buttonText = (Build.VERSION.SDK_INT >= 16) ? getString(R.string.dialogOK) : getString(R.string.alertSecureSettingMakeSystemApp);
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    Logger.d("Trying to grant permission");
                    if (Utils.isRootPresent()) {
                        String results = Utils.requestModifyPhoneState();
                        if ((results != null) && (results.startsWith("REBOOT:", 0))) {
                            // Pop Reboot dialog, this is API < 16 and we have to move the app to /system
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActionPickerActivity.this);
                            builder.setTitle(getString(R.string.rebootTitle));
                            builder.setMessage(getString(R.string.rebootText));
                            builder.setPositiveButton(getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            builder.create().show();
                        }
                    } else {
                        Logger.e("Could not get root access");
                        Toast.makeText(ActionPickerActivity.this, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Logger.e("Could not get root access");
                    Toast.makeText(ActionPickerActivity.this, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    Logger.e("Could not get root access");
                    Toast.makeText(ActionPickerActivity.this, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showSecureSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.alertSecureSettingTitle));
        builder.setMessage(getString(R.string.alertSecureSetting));
        String buttonText = (Build.VERSION.SDK_INT >= 16) ? getString(R.string.dialogOK) : getString(R.string.alertSecureSettingMakeSystemApp);
        builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    Logger.d("Trying to grant permission");
                    if (Utils.isRootPresent()) {
                        String results = Utils.requestWriteSecureSettings();
                        if ((results != null) && (results.startsWith("REBOOT:", 0))) {
                            // Pop Reboot dialog, this is API < 16 and we have to move the app to /system
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActionPickerActivity.this);
                            builder.setTitle(getString(R.string.rebootTitle));
                            builder.setMessage(getString(R.string.rebootText));
                            builder.setPositiveButton(getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            builder.create().show();
                        }
                    } else {
                        Logger.e("Could not get root access");
                        Toast.makeText(ActionPickerActivity.this, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Logger.e("Could not get root access");
                    Toast.makeText(ActionPickerActivity.this, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    Logger.e("Could not get root access");
                    Toast.makeText(ActionPickerActivity.this, getString(R.string.noRootText), Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void showDialog(Context context, String title, String message, String buttonText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != null)
            builder.setTitle(title);
        if (message != null)
            builder.setMessage(message);

        if (buttonText != null)
            builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });

        AlertDialog alert = builder.create();
        if (alert != null)
            alert.show();
        else
            Logger.d("Alert is null");
    }

    private Iterable<String> getPendingActionsIterable() {
        return Iterables.transform(getSelectedActions(), new Function<Action, String>() {
            @Override
            public String apply(Action action) {
                return action.getCode();
            }
        });
    }

    private void showConfigurationDialog(Iterable<String> pendingActions, boolean hasPreloadedActions) {
        Intent intent = new Intent(ActionPickerActivity.this, ConfigureActionsActivity.class);
        intent.putExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, getIntent().getIntExtra(TaskWizardActivity.EXTRA_FRAGMENT_NUM, 1));
        intent.putExtra(Constants.EXTRA_PENDING_ACTIONS, TextUtils.join(",", pendingActions));
        startActivityForResult(intent, REQUEST_CONFIGURE_ACTIONS);
    }

    private List<Action> getSelectedActions() {
        List<Action> selectedActions = new ArrayList<Action>();

        ExpandableListView list = (ExpandableListView) findViewById(R.id.list);
        ExpandableListAdapter adapter = list.getExpandableListAdapter();
        for (int g = 0; g < adapter.getGroupCount(); g++) {
            for (int c = 0; c < adapter.getChildrenCount(g); c++) {
                Action action = (Action) adapter.getChild(g, c);
                if (action.isChecked()) {
                    selectedActions.add(action);
                }
            }
        }

        return ImmutableList.copyOf(selectedActions);
    }

    private class ActionsAdapter extends BaseExpandableListAdapter {
        ActionCategory[] mCategories;

        public ActionsAdapter(ActionCategory[] categories) {
            mCategories = categories;
        }

        @Override
        public int getGroupCount() {
            return mCategories.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mCategories[groupPosition].getActions().length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mCategories[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mCategories[groupPosition].getActions()[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return (10000 * groupPosition) + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.expandable_list_group, parent, false);
            }

            convertView.findViewById(R.id.divider).setVisibility((groupPosition == 0) ? View.GONE : View.VISIBLE);

            ActionCategory category = (ActionCategory) getGroup(groupPosition);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(category.getNameId());
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(category.getIconId());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.expandable_list_child, parent, false);
            }

            Action action = (Action) getChild(groupPosition, childPosition);

            AppCompatCheckedTextView textView = (AppCompatCheckedTextView) ((convertView).findViewById(android.R.id.text1));
            textView.setText(action.getNameId());
            textView.setChecked(action.isChecked());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}