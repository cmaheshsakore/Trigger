package com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.GoogleSigninActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.PlusSigninActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerTwitterAuthRequest;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.worker.workerFoursquareAuthRequest;

public class LoginSettingsFragment extends SettingsFragment {

    private static final int REQUEST_LOGIN_FOURSQUARE = 1001;
    private static final int REQUEST_LOGIN_TWITTER = 1002;
    private static final int REQUEST_LOGIN_GOOGLE = 1004;
    private static final int REQUEST_GOOGLE_WALLET = 1005;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_logins,  null);
    }

    @Override
    protected void setupClickHandlers() {
        
    }

    @Override
    protected void loadSettings() {
        setFoursquareClick();
        setSSOClick();
        setTwitterClick();
    }

    @Override
    public void onClick(View v) {
        updatePreferences();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (getFragmentRequestCode(requestCode)) {
            case REQUEST_LOGIN_FOURSQUARE:
                setFoursquareClick();
                break;
            case REQUEST_LOGIN_GOOGLE:
                setSSOClick();
                break;
            case REQUEST_GOOGLE_WALLET:
                setSSOClick();
                break;
            case REQUEST_LOGIN_TWITTER:
                setTwitterClick();
                break;
        }
    }
    
   

    private void setTwitterClick() {
        final LinearLayout layout = (LinearLayout) mView.findViewById(R.id.prefTwitterCredentialsContainer);
        int pad = layout.getPaddingTop();

        String uToken = SettingsHelper.getPrefString(getActivity(), OAuthConstants.TWITTER_TOKEN_PREF, "");
        String uSecret = SettingsHelper.getPrefString(getActivity(), OAuthConstants.TWITTER_SECRET_PREF, "");

        final TextView text = (TextView) mView.findViewById(R.id.prefClearTwitterText);
        if (((!uToken.equals("")) || (!uSecret.equals("")))) {
            text.setText(String.format(getString(R.string.service_twitter), getString(R.string.status_logged_in)));
            layout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.layoutPreferencesDialogTwitter));
                    builder.setPositiveButton(getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            SettingsHelper.setPrefString(getActivity(), OAuthConstants.TWITTER_TOKEN_PREF, "");
                            SettingsHelper.setPrefString(getActivity(), OAuthConstants.TWITTER_SECRET_PREF, "");
                            setTwitterClick();

                        }

                    });
                    builder.setNegativeButton(getString(R.string.dialogCancel), null);
                    builder.create().show();

                }
            });
            text.setTextColor(getResources().getColor(R.color.TextColor));
        } else {
            text.setText(String.format(getString(R.string.service_twitter), getString(R.string.status_logged_out)));
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(getActivity(), workerTwitterAuthRequest.class);
                    intent.putExtra(workerTwitterAuthRequest.EXTRA_SKIP_MESSAGE, true);
                    startActivityForResult(intent, REQUEST_LOGIN_TWITTER);
                }

            });
        }

        layout.setPadding(0, pad, 0, pad);
    }

    private void setSSOClick() {
        final LinearLayout layout = (LinearLayout) mView.findViewById(R.id.prefSSOCredentialsContainer);

        if (!BuildConfiguration.isPlayStoreAvailable()) {
            layout.setVisibility(View.GONE);
        }
        
        int pad = layout.getPaddingTop();

        String uToken = SettingsHelper.getPrefString(getActivity(), OAuthConstants.SSO_OAUTH_TOKEN_PREF, "");
        String uSecret = SettingsHelper.getPrefString(getActivity(), OAuthConstants.SSO_OAUTH_ACCOUNT_NAME, "");

        final TextView text = (TextView) mView.findViewById(R.id.prefClearSSOText);
        if (((!uToken.equals("")) || (!uSecret.equals("")))) {
            text.setText(String.format(getString(R.string.service_google), SettingsHelper.getPrefString(getActivity(), OAuthConstants.SSO_OAUTH_ACCOUNT_NAME, getString(R.string.status_logged_in))));
            layout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.sso_clear_dialog));
                    builder.setPositiveButton(getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            startActivityForResult(new Intent(getActivity(), GoogleSigninActivity.class).putExtra(PlusSigninActivity.EXTRA_DELETE_CREDENTIALS, true), REQUEST_LOGIN_GOOGLE);
                        }

                    });
                    builder.setNegativeButton(getString(R.string.dialogCancel), null);
                    builder.create().show();

                }
            });
            text.setTextColor(getResources().getColor(R.color.TextColor));
        } else {
            text.setText(String.format(getString(R.string.service_google), getString(R.string.status_logged_out)));
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    startActivityForResult(new Intent(getActivity(), GoogleSigninActivity.class), REQUEST_LOGIN_GOOGLE);
                }

            });
        }

        layout.setPadding(0, pad, 0, pad);
    }

    private void setFoursquareClick() {
        final LinearLayout layout = (LinearLayout) mView.findViewById(R.id.prefFoursquareCredentialsContainer);
        int pad = layout.getPaddingTop();

        String uToken = SettingsHelper.getPrefString(getActivity(), OAuthConstants.FOURSQUARE_ACCESS_TOKEN_PREF, "");
        String uSecret = SettingsHelper.getPrefString(getActivity(), OAuthConstants.FOURSQUARE_AUTH_TOKEN_PREF, "");


        final TextView text = (TextView) mView.findViewById(R.id.prefClearFoursquareText);
        if (((!uToken.equals("")) || (!uSecret.equals("")))) {
            text.setText(String.format(getString(R.string.service_foursquare), getString(R.string.status_logged_in)));
            layout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.layoutPreferencesDialogFoursquare));
                    builder.setPositiveButton(getString(R.string.dialogOK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            SettingsHelper.setPrefString(getActivity(), OAuthConstants.FOURSQUARE_ACCESS_TOKEN_PREF, "");
                            SettingsHelper.setPrefString(getActivity(), OAuthConstants.FOURSQUARE_AUTH_TOKEN_PREF, "");
                            setFoursquareClick();
                        }

                    });
                    builder.setNegativeButton(getString(R.string.dialogCancel), null);
                    builder.create().show();

                }
            });
        } else {
            text.setText(String.format(getString(R.string.service_foursquare), getString(R.string.status_logged_out)));
            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    startActivityForResult(new Intent(getActivity(), workerFoursquareAuthRequest.class), REQUEST_LOGIN_FOURSQUARE);
                }

            });
        }

        layout.setPadding(0, pad, 0, pad);
    }
    
}
