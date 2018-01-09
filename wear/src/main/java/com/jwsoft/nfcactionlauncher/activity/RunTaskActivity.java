package com.mobiroo.n.sourcenextcorporation.trigger.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.mobiroo.n.sourcenextcorporation.trigger.R;

/**
 * Created by krohnjw on 7/7/2014.
 */
public class RunTaskActivity extends ConfirmationActivity {

    public static final String EXTRA_RESULT = "extra_result";

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ACTION = "pending_action";

    private String mId;
    private String mName;
    private byte[] mPayload;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        /*setContentView(R.layout.activity_run_task);
        mId = getIntent().hasExtra(EXTRA_ID) ? getIntent().getStringExtra(EXTRA_ID) : "";
        mName = getIntent().hasExtra(EXTRA_NAME) ? getIntent().getStringExtra(EXTRA_NAME) : "";

        mPayload = (mId + "##" + mName).getBytes();

        if (mId.isEmpty() || mName.isEmpty()) {
            setResult(RESULT_CANCELED);
            finish();
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
*/
    }
/*
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
                                    Log.d("Trigger", "Sending message to RUN_TASK");
                                    Wearable.MessageApi.sendMessage(
                                            mGoogleApiClient, node.getId(), "/run/", mPayload)
                                            .setResultCallback(getSendMessageResultCallback());

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

                setResult(sendMessageResult.getStatus().isSuccess() ? RESULT_OK : RESULT_CANCELED);
                finish();
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

    }*/
}
