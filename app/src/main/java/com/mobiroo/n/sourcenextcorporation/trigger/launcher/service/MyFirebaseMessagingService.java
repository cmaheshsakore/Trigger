package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.util.NotificationUtil;

import java.util.Map;

/**
 * FCM通知受信クラス
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * メッセージ受信時の処理
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        if (data == null) return;
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.KEY_PREF_IS_RECEIVE_NOTIFICATION, true)) {
            String msg = data.get("msg");
            String snd = data.get("snd");
            String url = data.get("url");

            if (msg != null) {
                NotificationUtil.showNotification(this, msg, snd, url);
            }
        }
    }
}