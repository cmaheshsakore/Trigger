package com.mobiroo.n.sourcenextcorporation.trigger.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.item.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.service.MessagingService;
import com.mobiroo.n.sourcenextcorporation.trigger.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.util.DataHelper;

import java.util.ArrayList;


public class MainActivity extends BaseActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private TextView mTitle;
    private TextView mBody;
    private ImageView mIcon;

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_BODY = "extra_body";
    public static final String EXTRA_ICON = "extra_icon";

    protected ArrayList<Task> mTasks;

    private final String TAG = "Trigger";

    private WearableListView.Adapter mAdapter;

    private WatchViewStub mStub;
    private WearableListView mList;

    public static final String EXTRA_SYNC_DATA = "load_data";

    @Override
    public void onConnected(Bundle bundle) {
        getSyncData();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //loadCachedData(); // use Cached data
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataHelper.cacheData(MainActivity.this, dataEvents);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // TODO: Show the an error
    }

    private void getSyncData(){
        Log.d("Trigger", "Calling getSyncData");
        PutDataMapRequest request = PutDataMapRequest.create("/tasks/");
        PendingResult<DataItemBuffer> pendingResult = Wearable.DataApi.getDataItems(mGoogleApiClient, request.getUri());
        pendingResult.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(final DataItemBuffer result) {
                Log.d("Trigger", "Got result of " + result.getStatus().isSuccess());
                if(result.getStatus().isSuccess()) {
                    if (result.getCount() > 0) {
                        try {
                            DataMapItem d = DataMapItem.fromDataItem(result.get(0));
                            String data = DataHelper.cacheDataItem(MainActivity.this, d, false);
                            Log.d("Trigger", "Building list from returned string " + data);
                            buildTaskList(data);
                        } catch (Exception e) {
                            setVisibility(VIEW_NO_TASKS_SHOWN);
                            Log.d("Trigger", "Exception build data");
                        }
                        reloadTaskView();
                    } else {
                        setVisibility(VIEW_NO_TASKS_SHOWN);
                    }
                } else {
                    setVisibility(VIEW_NO_TASKS_SHOWN);
                }
            }
        });
    }

    private class TaskAdapter extends WearableListView.Adapter {

        private ArrayList<Task> mItems;
        private Context mContext;
        private int mLayout;

        private class TaskHolder extends WearableListView.ViewHolder {

            public TextView item;
            public View container;
            public TaskHolder(View itemView) {
                super(itemView);
                item = (TextView) itemView.findViewById(android.R.id.text1);
                container = itemView.findViewById(R.id.container);
            }
        }

        public TaskAdapter(Context context, int layout, ArrayList<Task> data) {
            mContext = context;
            mLayout = layout;
            mItems = data;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            View v = LayoutInflater.from(parent.getContext()).inflate(mLayout, parent, false);
            return new TaskHolder(v);
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int position) {
            final Task current = mItems.get(position);

            TaskHolder h = (TaskHolder) viewHolder;
            h.item.setText(current.getName());
            h.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, MessagingService.class);
                    String[] names = current.getName().split(" - ");
                    i.putExtra(MessagingService.EXTRA_NAME, names[0]);
                    i.putExtra(MessagingService.EXTRA_ID, current.getId());
                    if (current.hasSecondaryData()) {
                        if (names.length >= 2) {
                            i.putExtra(MessagingService.EXTRA_NAME_2, names[1]);
                        }
                        i.putExtra(MessagingService.EXTRA_ID_2, current.getSecondaryId());
                    }
                    i.putExtra(MessagingService.EXTRA_ACTION, MessagingService.ACTION_RUN_TASK);
                    startService(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mTasks = new ArrayList<Task>(0);
        mAdapter = new TaskAdapter(MainActivity.this, R.layout.list_item_task, mTasks);
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean load = getIntent().getBooleanExtra(EXTRA_SYNC_DATA, true);

        mStub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        mStub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mList = (WearableListView) mStub.findViewById(R.id.list);
                mList.setAdapter(mAdapter);

                if (load) {
                    if (!mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    } else {
                        getSyncData();
                    }
                }

                stub.findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSharedPreferences(Constants.TASKS_PREF_NAME, 0).edit().putLong(Constants.PREF_SYNC_REQUESTED, System.currentTimeMillis()).commit();
                        Intent i = new Intent(MainActivity.this, MessagingService.class);
                        i.putExtra(MessagingService.EXTRA_ACTION, MessagingService.ACTION_SYNC);
                        startService(i);
                        setVisibility(VIEW_LOADING_SHOWN);
                    }
                });
            }
        });



        // Wait for GoogleApiClient to connect locally and either get new data from map OR use cached on failure
        //loadSyncData();

    }

    private void buildTaskList(String data) {
        String[] serializedTasks = data.split("~~");
        mTasks = new ArrayList<Task>(serializedTasks.length);
        Log.d("Trigger", "Build task list from " + data);
        for (String t: serializedTasks) {
            String[] taskData = t.split("##");
            Log.d("Trigger", "Task: " + t + " size: " + taskData.length);
            if (taskData.length == 3) {
                try {
                    mTasks.add(new Task(taskData[0], taskData[1], taskData[2], ""));
                } catch (Exception e) {
                    Log.d("Trigger", "Exception setting up task list: " + e);
                    e.printStackTrace();
                }
            } else {
                try {
                    mTasks.add(new Task(taskData[0], taskData[1]));
                } catch (Exception e) {
                    Log.d("Trigger", "Exception setting up task list: " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void reloadTaskView() {

        Log.d("Trigger", "Setting adapter to show tasks of size " + mTasks.size());

        mAdapter = new TaskAdapter(MainActivity.this, R.layout.list_item_task, mTasks);
        mAdapter.notifyDataSetChanged();
        mList.setAdapter(mAdapter);
        setVisibility(mTasks.size() > 0 ? VIEW_LIST_SHOWN : VIEW_NO_TASKS_SHOWN);
    }

    private final int VIEW_LIST_SHOWN = 1;
    private final int VIEW_LOADING_SHOWN = 2;
    private final int VIEW_NO_TASKS_SHOWN = 3;

    private void setVisibility(int view) {

        switch (view) {
            case VIEW_LIST_SHOWN:
                Log.d("Trigger", "Showing List");
                mStub.findViewById(R.id.list).setVisibility(View.VISIBLE);
                mStub.findViewById(R.id.progress).setVisibility(View.GONE);
                mStub.findViewById(R.id.card_none).setVisibility(View.GONE);
                break;
            case VIEW_LOADING_SHOWN:
                Log.d("Trigger", "Showing Loading");
                mStub.findViewById(R.id.list).setVisibility(View.GONE);
                mStub.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                mStub.findViewById(R.id.card_none).setVisibility(View.GONE);
                break;
            case VIEW_NO_TASKS_SHOWN:
                Log.d("Trigger", "Showing No Tasks");
                mStub.findViewById(R.id.list).setVisibility(View.GONE);
                mStub.findViewById(R.id.progress).setVisibility(View.GONE);
                mStub.findViewById(R.id.card_none).setVisibility(View.VISIBLE);
                break;
            default:
                Log.d("Trigger", "Missed, showing none");
                mStub.findViewById(R.id.list).setVisibility(View.GONE);
                mStub.findViewById(R.id.progress).setVisibility(View.GONE);
                mStub.findViewById(R.id.card_none).setVisibility(View.VISIBLE);
        }
    }

}