package com.mobiroo.n.sourcenextcorporation.trigger.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mobiroo.n.sourcenextcorporation.trigger.activity.RunTaskActivity;

/**
 * Created by krohnjw on 7/2/2014.
 */
public class MessagingService extends Service implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "Trigger";

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ID_2 = "id_2";
    public static final String EXTRA_NAME_2 = "name_2";
    public static final String EXTRA_ACTION = "pending_action";

    private static final int ACTION_NONE = 0;
    public static final int ACTION_RUN_TASK = 1;
    public static final int ACTION_SYNC = 2;

    private static final String PATH_RUN_TASK = "/run/";
    private static final String PATH_SYNC_TASKS = "/sync/";

    private GoogleApiClient mGoogleApiClient;
    private String mId;
    private String mName;
    private String mSecondaryId;
    private String mSecondaryName;
    private byte[] mPayload;
    private int mPendingAction;

    @Override
    public int onStartCommand(Intent intent, int flags, int args) {

        mId = intent.hasExtra(EXTRA_ID) ? intent.getStringExtra(EXTRA_ID) : "";
        mName = intent.hasExtra(EXTRA_NAME) ? intent.getStringExtra(EXTRA_NAME) : "";
        mPendingAction = intent.getIntExtra(EXTRA_ACTION, ACTION_NONE);
        mSecondaryId = intent.hasExtra(EXTRA_ID_2) ? intent.getStringExtra(EXTRA_ID_2) : "";
        mSecondaryName = intent.hasExtra(EXTRA_NAME_2) ? intent.getStringExtra(EXTRA_NAME_2) : "";

        if (mPendingAction == ACTION_SYNC) {
            mName = "##DEFAULT##";
            mId = "##DEFAULT##";
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (mId.isEmpty() || mName.isEmpty() || (mPendingAction == ACTION_NONE)) {
            stopSelf();
            return START_NOT_STICKY;
        }


        if ((!mSecondaryId.isEmpty())) {
            mPayload = (mId + "##" + mName + "##" + mSecondaryId + "##" + mSecondaryName).getBytes();
            Log.d("Trigger", "Setting payload to " + mId + "##" + mName + "##" + mSecondaryId + "##" + mSecondaryName);
        } else {
            mPayload = (mId + "##" + mName).getBytes();
            Log.d("Trigger", "Setting payload to " + mId + "##" + mName);
        }


        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            switch (mPendingAction) {
                                case ACTION_RUN_TASK:
                                    Log.d("Trigger", "Sending message to RUN_TASK");
                                    Wearable.MessageApi.sendMessage(
                                            mGoogleApiClient, node.getId(), PATH_RUN_TASK, mPayload)
                                            .setResultCallback(getSendMessageResultCallback());
                                    break;
                                case ACTION_SYNC:
                                    Log.d("Trigger", "Sending message to SYNC");
                                    Wearable.MessageApi.sendMessage(
                                            mGoogleApiClient, node.getId(), PATH_SYNC_TASKS, null)
                                            .setResultCallback(getSendMessageResultCallback());
                                    break;
                            }

                        }
                    }
                });
    }

    private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.d("Trigger", "Failed to connect to Google Api Client with status "
                            + sendMessageResult.getStatus());
                } else {
                    Log.d("Trigger", "Got result " + sendMessageResult.getStatus());
                }

                if (mPendingAction == ACTION_RUN_TASK) {
                    startActivity(new Intent(MessagingService.this, RunTaskActivity.class)
                            .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                    sendMessageResult.getStatus().isSuccess()
                                            ? ConfirmationActivity.SUCCESS_ANIMATION
                                            : ConfirmationActivity.FAILURE_ANIMATION
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
