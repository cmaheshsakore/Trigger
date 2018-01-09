package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TaskWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example.BatteryExample;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example.Example;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example.DrivingExample;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example.SilenceExample;

import java.util.ArrayList;
import java.util.List;

public class ExamplesFragment extends Fragment {
    
    private final int REQUEST_SHOW_UPGRADE = 8001;

    private GridView            mListView;
    private LinearLayout        mHelp;

    private ArrayAdapter<Example> mAdapter;

    public static interface Listener {
        public void showStore();
    }

    private List<Example> mExamples;
    
   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_popular_tags, null);
    }

    private BroadcastReceiver upgradeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        try { getActivity().unregisterReceiver(upgradeReceiver);}
        catch (Exception ignored) {}
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(upgradeReceiver, new IntentFilter(Constants.ACTION_UPGRADE_PURCHASED));

        mAdapter = new ArrayAdapter<Example>(getActivity(), R.layout.popular_tag, mExamples) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final Example example = getItem(position);
                if (convertView == null) {
                    convertView = View.inflate(getActivity(), R.layout.popular_tag, null);
                }


                ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(example.getIcon());
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(example.getName());
                ((TextView) convertView.findViewById(android.R.id.text2)).setText(example.getDescription());

                convertView.findViewById(android.R.id.icon2).setVisibility(example.isPro() ? View.VISIBLE: View.INVISIBLE);

                // set default to red and no message following
                convertView.findViewById(android.R.id.icon2).setBackgroundColor(Color.RED);
                convertView.findViewById(R.id.heading_trial).setVisibility(View.INVISIBLE);

                Log.i("karo","in default");

                if (example.isPro()) {

                    Log.i("karo","in pro");

                    // locked or not trail version...
                    if (!IabClient.checkLocalUnlockOrTrial(getActivity())) {
                        Log.i("karo","in 1");
                        if (IabClient.isTrialAvaiable(getActivity())) {

                            Log.i("karo","in 2");

                            // Show try free message with blue
                            convertView.findViewById(R.id.heading_trial).setVisibility(View.VISIBLE);
                            convertView.findViewById(android.R.id.icon2).setBackgroundColor(Color.BLUE);
                        } else {

                            Log.i("karo","in 3");
                            // User needs to upgrade - show trial expired with red
                            convertView.findViewById(R.id.heading_trial).setVisibility(View.VISIBLE);
                            ((TextView) convertView.findViewById(R.id.heading_trial)).setText(R.string.trial_expired_title);
                        }
                    }
                    //unlocked version...
                    else {

                        Log.i("karo","in 4");

                        //is trial version OR not...
                        if (IabClient.isUserOnTrial(getActivity())) {

                            Log.i("karo","in 5");

                            // Set text to trial started with blue
                            convertView.findViewById(R.id.heading_trial).setVisibility(View.VISIBLE);
                            ((TextView) convertView.findViewById(R.id.heading_trial)).setText(R.string.trial_started);
                            convertView.findViewById(android.R.id.icon2).setBackgroundColor(Color.BLUE);
                        }
                    }
                }
                // Set on click listener
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Usage.logMixpanelEvent(Usage.getAnalyticsObject(getActivity()), "Example clicked", true, new String[] { "name", example.getTag() });

                        if (example.isPro()) {
                            if ((IabClient.checkLocalUnlockOrTrial(getActivity()))) {
                                // User has unlocked or is on a trial, show dialog for configuration
                                example.showConfigurationDialog(getActivity(), getFragmentManager());
                            } else {
                                // Show upgrade dialog
                                //2_2_18
                                //((MainActivity) getActivity()).showUpgradeDialog();
                            }
                        } else {
                            example.showConfigurationDialog(getActivity(), getFragmentManager());
                        }
                    }
                });

                return convertView;
            }
        };
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mExamples = new ArrayList<Example>();
        //mExamples.add(new DataExample(R.string.example_save_data, R.drawable.ic_example_data_saver, R.string.example_save_data_description, false, "save-data"));
        mExamples.add(new DrivingExample(R.string.example_driving, R.drawable.ic_action_car_black, R.string.example_driving_description, false, "driving"));
        mExamples.add(new SilenceExample(R.string.example_silence_at_night, R.drawable.ic_example_bedtime_tag, R.string.example_silence_at_night_description, true, "silence-at-night"));
        mExamples.add(new BatteryExample(R.string.example_save_battery, R.drawable.ic_example_battery_saver, R.string.example_save_battery_description, true, "save-battery"));

        
        mListView = (GridView) view.findViewById(android.R.id.list);
        int widthDp = Utils.getWidthInDp(getActivity());
        if (widthDp >= 1280) {
            mListView.setNumColumns(2);
        } else {
            mListView.setNumColumns(1);
        }

        
        mHelp = (LinearLayout) view.findViewById(R.id.examples_help);
        
        if (!SettingsHelper.getPrefBool(getActivity(), Constants.PREF_HIDE_WELCOME, false)) {
            showHelp();
        } else {
            hideHelp();
        }
        
        (view.findViewById(R.id.dismiss)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideHelp();
                SettingsHelper.setPrefBool(getActivity(), Constants.PREF_HIDE_WELCOME, true);
            }
            
        });
        
        (view.findViewById(R.id.create)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().startActivityForResult(new Intent(getActivity(), TaskWizardActivity.class).putExtra(TaskWizardActivity.EXTRA_TASK_IS_NEW, true)
                , MainActivity.REQUEST_CREATE_TASK);
            }
            
        });
    }
    
    private void showHelp() {
        mHelp.setVisibility(View.VISIBLE);
    }
    
    private void hideHelp() {
        mHelp.setVisibility(View.GONE);
    }
}
