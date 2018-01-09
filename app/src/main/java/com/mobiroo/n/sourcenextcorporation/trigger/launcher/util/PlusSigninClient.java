package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiroo.n.sourcenextcorporation.trigger.BuildConfig;

/**
 * Created by krohnjw on 12/23/2014.
 */
public class PlusSigninClient {

    protected GoogleApiClient mGoogleApiClient;
    protected Context               mContext;

    protected String                mWalletScope;
    protected final String          mProfileScope = "https://www.googleapis.com/auth/userinfo.profile";
    protected final String          mEmailScope = "https://www.googleapis.com/auth/userinfo.email";
    protected final String          mPlusScope = "https://www.googleapis.com/auth/plus.login";
    protected final String          mWalletScopeSandbox = "https://www.googleapis.com/auth/paymentssandbox.make_payments";
    protected final String          mWalletScopeProduction = "https://www.googleapis.com/auth/payments.make_payments";


    protected GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    protected GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener;

    public GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                            .setEnvironment(
                                    BuildConfig.DEBUG || BuildConfig.FLAVOR.equals("sandbox")
                                            ? WalletConstants.ENVIRONMENT_SANDBOX
                                            : WalletConstants.ENVIRONMENT_PRODUCTION
                            )
                            .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                            .build())
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
        }

        return mGoogleApiClient;
    }


    public PlusSigninClient(Context context, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        mContext = context;
        mConnectionCallbacks = connectionCallbacks;
        mConnectionFailedListener = connectionFailedListener;
        getGoogleApiClient();
    }

    public void connect() {

        if (!getGoogleApiClient().isConnected() && !getGoogleApiClient().isConnecting()) {
            Logger.d("PlusClient: Connecting");
            getGoogleApiClient().connect();
        }
    }

    public void disconnect() {
        if (getGoogleApiClient().isConnected()) {
            Logger.d("PlusClient: Disconnecting");
            getGoogleApiClient().disconnect();
        }
    }

    // Send this token to the server as the challenge
    public String getTokenForLogin() {
        String account = Plus.AccountApi.getAccountName(getGoogleApiClient());
        try {
            return GoogleAuthUtil.getToken(mContext, account, Plus.SCOPE_PLUS_LOGIN.toString()); // TODO: May need to use string here
        } catch (Exception e) {
            Logger.d("Exception generating token");
            return "";
        }

    }
    public void signOut() {
        Plus.AccountApi.revokeAccessAndDisconnect(getGoogleApiClient());
    }

    public static class ProfileInfo {
        public String name;
        public String email;

        public ProfileInfo(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    public boolean isConnected() {
        return getGoogleApiClient().isConnected();
    }

    public boolean isConnecting() {
        return getGoogleApiClient().isConnecting();
    }

    public Person getPerson() {
        return Plus.PeopleApi.getCurrentPerson(getGoogleApiClient());
    }

    public ProfileInfo getProfileInfo(PendingResult results) {
        String account = Plus.AccountApi.getAccountName(getGoogleApiClient());
        Person current = Plus.PeopleApi.getCurrentPerson(getGoogleApiClient());
        String name = account;
        try { name = current.getDisplayName(); }
        catch (Exception e) { }

        return new ProfileInfo(name, account);


    }
}
