package com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.wear;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;

import java.util.ArrayList;

/**
 * Created by krohnjw on 6/27/2014.
 */
public class WearMessagingService extends Service implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static final String TASK_PATH = "/tasks/";

    private GoogleApiClient mGoogleApiClient;

    private String mPayload;
    private String mPath;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int args) {
        Logger.d("Starting service to send message to wear with action " + intent.getAction());

        ArrayList<TaskSet> tasks = DatabaseHelper.getTasks(this);

        StringBuilder p = new StringBuilder();
        for (TaskSet s : tasks) {
            Task f = s.getTask(0);
            if (f.isSecondTagNameValid()) {
                p.append(f.getId() + "##" + s.getFullName() +  "##" + f.getSecondaryId() + "##" + f.getSecondaryName() + "~~");
            } else {
                p.append(f.getId() + "##" + s.getFullName() + "~~");
            }

        }
        mPayload = p.toString();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

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

    private boolean foundDevice;

    @Override
    public void onConnected(Bundle bundle) {
        Logger.d("Client connected - sending payload " + mPayload);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        boolean foundDevice = false;

        PutDataMapRequest request = PutDataMapRequest.create(TASK_PATH);
        DataMap dataMap = request.getDataMap();
        dataMap.putString("tasks", mPayload);
        dataMap.putLong("timestamp", System.currentTimeMillis());
        PutDataRequest dataRequest = request.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d("Trigger", "Data item set: " + result.getDataItem().getUri());
                }
                stopSelf();
            }
        });
    }

    private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Logger.d("Failed to connect to Google Api Client with status "
                            + sendMessageResult.getStatus());
                } else {
                    Logger.d("Trigger", "Got result " + sendMessageResult.getStatus());
                }
                stopSelf();
            }
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        stopSelf();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.d("Failed to connect to Google client API");
        stopSelf();
    }
}
