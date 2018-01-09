package com.mobiroo.n.sourcenextcorporation.trigger.launcher.wallet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.OAuthConstants;

public class ConfirmationActivity extends FragmentActivity implements OnClickListener {

    private static final int REQUEST_CODE_CHANGE_MASKED_WALLET = 1002;
    private SupportWalletFragment mWalletFragment;
    private ConfirmationFragment  mConfirmationFragment;

    private MaskedWallet mMaskedWallet;

    // Store a user selected account
    protected String mAccount;

    private Button mConfirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wallet_confirmation);
        ((TextView) findViewById(android.R.id.title)).setText(getTitle());

        mMaskedWallet = getIntent().getParcelableExtra(RequestConstants.EXTRA_MASKED_WALLET);

        // Add wallet fragment to display
        createAndAddWalletFragment();
        // Add confirmation fragment to display
        addConfirmationFragment();

        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(this);
    }

    protected String getUser() {
        mAccount = SettingsHelper.getPrefString(this, OAuthConstants.SSO_OAUTH_WALLET_ACCOUNT_NAME, null);
        return mAccount;
    }



    @SuppressWarnings("unused")
    private Account getAccountFromName(String name) {
        AccountManager am = AccountManager.get(this);
        final Account[] accounts = am.getAccountsByType("com.google");
        final int size = accounts.length;

        for (int i = 0; i < size; i++) {
            if (accounts[i].name.equals(name)) {
                return accounts[i];
            }
        }

        return null;
    }

    private void createAndAddWalletFragment() {
        WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                .setMaskedWalletDetailsLogoTextColor(getResources().getColor(R.color.TextColor))
                .setMaskedWalletDetailsBackgroundColor(Color.TRANSPARENT)
                .setMaskedWalletDetailsTextAppearance(R.style.WalletText)
                .setMaskedWalletDetailsHeaderTextAppearance(R.style.WalletText)
                ;

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                .setFragmentStyle(walletFragmentStyle)
                .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                .setMode(WalletFragmentMode.SELECTION_DETAILS)
                .build();
        mWalletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        // Now initialize the Wallet Fragment
        String accountName = getUser();
        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWallet(mMaskedWallet)
                .setMaskedWalletRequestCode(REQUEST_CODE_CHANGE_MASKED_WALLET)
                .setAccountName(accountName);
        mWalletFragment.initialize(startParamsBuilder.build());

        // add Wallet fragment to the UI
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wallet_fragment, mWalletFragment)
                .commit();
    }

    private void addConfirmationFragment() {
        mConfirmationFragment = new ConfirmationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.confirmation_fragment, mConfirmationFragment)
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        ConfirmationFragment fragment =
                (ConfirmationFragment) getSupportFragmentManager().findFragmentById(R.id.frag);
        fragment.onNewIntent(intent);
    }

    @Override
    public void onClick(View v) {
        ((ConfirmationFragment) getConfirmationFragment()).onClick(v);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Got result for " + requestCode);

        switch (requestCode) {
            case REQUEST_CODE_CHANGE_MASKED_WALLET:
                if (resultCode == Activity.RESULT_OK &&
                        data.hasExtra(WalletConstants.EXTRA_MASKED_WALLET)) {
                    mMaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                    ((ConfirmationFragment) getConfirmationFragment())
                            .changeMaskedWallet(mMaskedWallet);
                }
                // you may also want to use the new masked wallet data here, say to recalculate
                // shipping or taxes if shipping address changed
                break;
            case WalletConstants.RESULT_ERROR:
                int errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, 0);
                handleError(errorCode);
                break;
            case ConfirmationFragment.REQUEST_CODE_RESOLVE_LOAD_FULL_WALLET:
                getConfirmationFragment().onActivityResult(requestCode, resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void handleError(int errorCode) {
        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
                Toast.makeText(this, getString(R.string.spending_limit_exceeded, errorCode),
                        Toast.LENGTH_LONG).show();
                break;
            case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
            case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
            case WalletConstants.ERROR_CODE_UNKNOWN:
            default:
                // unrecoverable error
                String errorMessage = getString(R.string.google_wallet_unavailable) + "\n" +
                        getString(R.string.error_code, errorCode);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public Fragment getConfirmationFragment() {
        return getSupportFragmentManager().findFragmentById(
                R.id.confirmation_fragment);
    }

    public Fragment getWalletFragment() {
        return getSupportFragmentManager().findFragmentById(
                R.id.wallet_fragment);
    }

}
