package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.PlusOneButton.OnPlusOneClickListener;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.PlusSigninClient;

import java.util.List;

public class FeedbackFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected PlusSigninClient mPlusClient;
    protected ConnectionResult          mConnectionResult;
    protected ProgressDialog            mProgressDialog;
    protected String                    mAccountName;

    private static final int            PLUS_ONE_REQUEST_CODE = 3001;
    private PlusOneButton               mPlusOneButton;
    private LinearLayout                mRateButton;
    private Intent                      mIntent;
    private boolean                     mHandleWhenConnected;
    
    private final String                PREF_PLUS_ONE_CLICKED = "com.trigger.launcher.feedback.plus_one_clicked2";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback,  null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeProgressDialog();
        mPlusOneButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);

        mPlusOneButton.setOnPlusOneClickListener(new OnPlusOneClickListener() {

            @Override
            public void onPlusOneClick(Intent intent) {
                SettingsHelper.setPrefBool(getActivity(), PREF_PLUS_ONE_CLICKED, true);
                if (!mPlusClient.isConnected()) {
                    mHandleWhenConnected = true;
                    mIntent = intent;
                    mPlusClient.connect();
                } else {
                    if (intent != null) {
                        startActivityForResult(intent, 0);
                        mIntent = null;
                        mHandleWhenConnected = false;
                    }
                }
                
            }
            
        });
        mRateButton = (LinearLayout) view.findViewById(R.id.rate_on_play_button);
        mRateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);  
                try {
                    startActivity(goToMarket);
                  } catch (ActivityNotFoundException e) {
                    
                  }
            }
            
        });
        
        if (!SettingsHelper.getPrefBool(getActivity(), PREF_PLUS_ONE_CLICKED)) {
            
        }
        
        if (savedInstanceState == null) {
            loadCachedUserName();
            Logger.i("OnCreate: Building plus client");
        }

        view.findViewById(R.id.geofence_help).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent help = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sites.google.com/site/triggerwiki/home"));
                List<ResolveInfo> info = getActivity().getPackageManager().queryIntentActivities(help, PackageManager.MATCH_DEFAULT_ONLY);
                if ((info != null) && (info.size() > 0)) {
                    startActivity(help);
                } else {
                    // Open a new web view ?

                }

            }

        });
        view.findViewById(R.id.task_help).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent help = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sites.google.com/site/triggerwiki/home"));
                List<ResolveInfo> info = getActivity().getPackageManager().queryIntentActivities(help, PackageManager.MATCH_DEFAULT_ONLY);
                if ((info != null) && (info.size() > 0)) {
                    startActivity(help);
                } else {
                    // Open a new web view ?

                }
            }
        });

        LinearLayout container = (LinearLayout) view.findViewById(R.id.tags_list);
        container.addView(new HelpOption(getString(R.string.help_email), R.drawable.ic_action_gmail).getView(getActivity()));
        container.addView(new HelpOption(getString(R.string.help_reddit), R.drawable.ic_logos_reddit_icon).getView(getActivity()));

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlusClient.isConnected()) {
            mPlusClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlusClient = buildPlusClient();
        mPlusOneButton.initialize("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName(), PLUS_ONE_REQUEST_CODE);
        
        mRateButton.setVisibility(
                SettingsHelper.getPrefBool(getActivity(), PREF_PLUS_ONE_CLICKED, false) ? View.VISIBLE
                : View.GONE
                );
        
    }

    private void openHelpLink(String option) {
        if (option.equals(getString(R.string.help_answers))) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sites.google.com/site/triggerwiki/home"));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Logger.e("Exception opening answers", e);
            }
        } else if (option.equals(getString(R.string.help_email))) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + BuildConfiguration.FEEDBACK_EMAIL_ADDRESS));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{BuildConfiguration.FEEDBACK_EMAIL_ADDRESS});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Trigger Feedback");
            intent.putExtra(Intent.EXTRA_TEXT, String.format("Version: %s\nOS: %s\nModel: %s\nManufacturer: %s\n\n", getPackageVersion(), Build.VERSION.RELEASE, Build.MODEL, Build.MANUFACTURER));
            startActivity(Intent.createChooser(intent, getString(R.string.menu_chooser_mail)));

        } else if (option.equals(getString(R.string.help_reddit))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://reddit.com/r/triggerandroid"));
            startActivity(intent);
        }
    }

    private String getPackageVersion() {
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return String.format("%s (%s)", packageInfo.versionName, packageInfo.versionCode);
        } catch (Exception e) {
            return null;
        }
    }

    protected PlusSigninClient buildPlusClient() {
        return new PlusSigninClient(getActivity(), this, this);
    }

    protected void initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.loading));

    }

    protected void loadCachedUserName() {
        mAccountName = SettingsHelper.getPrefString(getActivity(), OAuthConstants.SSO_OAUTH_WALLET_ACCOUNT_NAME, "");
    }

    private boolean mIntentInProgress;

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        mConnectionResult = result;

        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                getActivity().startIntentSenderForResult(result.getResolution().getIntentSender(),
                        0, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mPlusClient.connect();
            }

        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
        if (mHandleWhenConnected) {
            if (mIntent != null) {
                mProgressDialog.hide();
                startActivityForResult(mIntent, 0);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    private class HelpOption implements View.OnClickListener {
        public String Label;
        public int ImageResource;

        public HelpOption(String label, int resid) {
            Label = label;
            ImageResource = resid;
        }

        public View getView(Context context) {

            View convertView = View.inflate(context,R.layout.list_item_help_option, null);
            (convertView.findViewById(R.id.container)).setOnClickListener(this);
            HelpOption option = this;
            ((TextView) convertView.findViewById(R.id.row1Text)).setText(option.Label);
            convertView.findViewById(R.id.row1Text).setOnClickListener(this);
            ((ImageView) convertView.findViewById(R.id.row1Image)).setImageResource(option.ImageResource);

            return convertView;
        }

        @Override
        public void onClick(View v) {
            openHelpLink(this.Label);
        }
    }

}
