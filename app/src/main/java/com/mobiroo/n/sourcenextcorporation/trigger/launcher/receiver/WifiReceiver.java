package com.mobiroo.n.sourcenextcorporation.trigger.launcher.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.receivers.WifiReceiverIntentService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;

public class WifiReceiver extends WakefulBroadcastReceiver {

    public static final int STATE_CONNECTED = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_NONE = 3;

    private void logd(String message) {
        Logger.d(getClass().getSimpleName() + ": " + message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        SupplicantState state = SupplicantState.UNINITIALIZED;
        String action = intent.getAction();
        String ssid = "";
        logd("Signal received " + intent.getAction());

        int pendingAction = STATE_NONE;

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            // Network state changed
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State net_state = networkInfo.getState();

            logd("Net state is " + net_state);

            switch (net_state) {
                case CONNECTED:
                    pendingAction = STATE_CONNECTED;
                    state = SupplicantState.COMPLETED;
                    break;
                case DISCONNECTED:
                    pendingAction = STATE_DISCONNECTED;
                    state = SupplicantState.DISCONNECTED;
                    break;
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            // This should grab manual power off in Kit Kat

            if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                    == WifiManager.WIFI_STATE_DISABLED) {
                state = SupplicantState.DISCONNECTED;
                pendingAction = STATE_DISCONNECTED;
            }
        } else {
            state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            logd("Supplicant state is " + state);
            // This is a supplicant state change
            switch (state) {

                case COMPLETED:
                    pendingAction = STATE_CONNECTED;
                    state = SupplicantState.COMPLETED;
                    break;
                case SCANNING:
                case DISCONNECTED:
                    pendingAction = STATE_DISCONNECTED;
                    state = SupplicantState.DISCONNECTED;
                    break;
                default:
                    state = SupplicantState.INVALID;
                    break;
            }
        }

        Intent service = new Intent(context, WifiReceiverIntentService.class);
        service.putExtras(intent.getExtras());
        service.putExtra(WifiReceiverIntentService.EXTRA_PENDING_ACTION, pendingAction);
        service.putExtra(WifiReceiverIntentService.EXTRA_STATE, (android.os.Parcelable) state);

        if (pendingAction == STATE_CONNECTED) {
            ssid = WifiReceiverIntentService.getCurrentSsid(context);
            if (!ssid.isEmpty()) {
                service.putExtra(WifiReceiverIntentService.EXTRA_SSID, ssid);
            }
            logd("Starting service to check connect to " + ssid);
            startWakefulService(context, service);
        } else if (pendingAction == STATE_DISCONNECTED) {
            ssid = WifiReceiverIntentService.getLastKnownSsid(context);
            logd("Scheduling wake up to check disconnect from " + ssid);
            if (!ssid.isEmpty() && !TextUtils.equals(ssid, "<unknown ssid>")) {
                service.putExtra(WifiReceiverIntentService.EXTRA_SSID, ssid);
            }
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, PendingIntent.getService(context, 901, service, PendingIntent.FLAG_ONE_SHOT));
        } else {
            logd("No state set - returning");
        }
    }


}
