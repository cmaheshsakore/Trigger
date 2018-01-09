package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.pushmaker.applibs.AbstractPushMakerRegistrationService;
//import com.pushmaker.pushmakersample.util.Const;

/**
 * FCM RegistrationおよびPushMakerとの通信を行うIntentServiceの具象クラス
 */
public class MyRegistrationIntentService extends AbstractPushMakerRegistrationService {

    /**
     * RegisterおよびPushMaker送信のIntentServiceを開始
     * @param context
     */
    public static void startIntentService(Context context) {

        Log.i("abc","My Register Intent");

        Intent intent = new Intent(context, MyRegistrationIntentService.class);
        // 必要ならば端末IDを設定
        intent.putExtra(AbstractPushMakerRegistrationService.EXTRA_TERMINAL_ID, "TERMINAL_ID");

        context.startService(intent);
    }

    /**
     * GCM登録解除のIntentServiceを開始
     * @param context
     */
    public static void startUnregisterIntentService(Context context) {
        Intent intent = new Intent(context, MyRegistrationIntentService.class);
        intent.putExtra(AbstractPushMakerRegistrationService.EXTRA_DO_UNREGISTER, true);

        context.startService(intent);
    }

    /**
     * 登録成功時のハンドラ
     */
    @Override
    public void pushMakerRegisterFinished() {
        Log.d(Constants.TAG, "pushMakerRegisterFinished");
    }

    /**
     * 登録スキップ時のハンドラ
     */
    @Override
    public void pushMakerRegisterSkipped() {
        Log.d(Constants.TAG, "pushMakerRegisterSkipped");
    }

    /**
     * 登録失敗時のハンドラ
     * @param e
     */
    @Override
    public void pushMakerRegisterFailed(Exception e) {
        Log.d(Constants.TAG, "pushMakerRegisterFailed: " + e.toString());
    }

    /**
     * 登録解除時のハンドラ
     */
    @Override
    public void pushMakerUnregisterFinished() {
        Log.d(Constants.TAG, "pushMakerUnregisterFinished");
    }

    /**
     * 登録解除失敗時のハンドラ
     * @param e
     */
    @Override
    public void pushMakerUnregisterFailed(Exception e) {
        Log.d(Constants.TAG, "pushMakerUnregisterFailed: " + e.toString());
    }
}