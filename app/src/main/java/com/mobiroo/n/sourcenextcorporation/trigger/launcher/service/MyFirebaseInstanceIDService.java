package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * InstanceID Listenerクラス（Googleのリファレンス実装通り）
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    /**
     * Token更新時のハンドラ
     */
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Log.i("abc"," up Service id");
        MyRegistrationIntentService.startIntentService(this);
        Log.i("abc","down Service id");
    }
}