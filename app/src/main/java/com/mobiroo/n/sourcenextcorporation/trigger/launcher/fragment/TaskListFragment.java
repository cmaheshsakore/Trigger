package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildTools;
import com.mobiroo.n.sourcenextcorporation.trigger.FlavorInfo;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.billing.IabClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.MainActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.SelectTriggerActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity.TaskWizardActivity;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncLoader;
import com.mobiroo.n.sourcenextcorporation.trigger.util.GridLoaderCallbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TaskListFragment extends Fragment implements OnClickListener {

    private static final int REQUEST_RUN_TAG = 1;
    private static final int REQUEST_CREATE_TAG = 2;

    private TaskSet mActionTask;
    private ProgressBar mProgress;
    private ScrollView mIntro;
    private GridView mListView;

    private GridLoaderCallbacks<ArrayList<TaskSet>> mLoaderCallbacks;

    private GeofenceClient mGeoClient;
    private boolean mRequestInProgress = false;
    private MyTagsAdapter mAdapter;

    private int mClickedPosition;

    private ArrayList<TaskSet> mPendingDeletes;

    private int mListPosition;

    private BroadcastReceiver mMigrationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
        }

    };

    private void setupLoader() {
        mLoaderCallbacks = new GridLoaderCallbacks<ArrayList<TaskSet>>(this, mListView, mIntro) {
            @Override
            public Loader<BetterAsyncTask.TaskResult<ArrayList<TaskSet>>> onCreateLoader(int i, Bundle bundle) {
                return new BetterAsyncLoader<ArrayList<TaskSet>>(getActivity(), BetterAsyncLoader.INTERVAL_NONE) {
                    @Override
                    protected ArrayList<TaskSet> doLoadInBackground() throws Exception {
                        Logger.d("Loading tasks");
                        ArrayList<TaskSet> tasks = DatabaseHelper.getTasks(getActivity());
                        return (tasks != null) ? tasks : null;
                    }
                };
            }

            @Override
            protected void onLoadFinished(ArrayList<TaskSet> tags) {
                Logger.d("Load Finished");
                if ((tags != null) && (tags.size() > 0)) {
                    mAdapter = new MyTagsAdapter(getActivity(), tags);
                    setListAdapter(mAdapter);
                } else {
                    setListAdapter(null);
                    setEmptyText();
                    if (!BuildTools.shouldShowShop()) {
                        try {
                            getView().findViewById(R.id.shop_button).setVisibility(View.GONE);
                        } catch (Exception ignored) {

                        }
                    }
                }
            }
        };

    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onResume() {
        super.onResume();

        mPendingDeletes = new ArrayList<TaskSet>();

        getActivity().registerReceiver(upgradeReceiver, new IntentFilter(Constants.ACTION_UPGRADE_PURCHASED));

        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
        getActivity().registerReceiver(mMigrationReceiver, new IntentFilter("com.trigger.launcher.migration_complete"));


        if (!FlavorInfo.SHOW_UPGRADE || IabClient.checkLocalUnlock(getActivity())) {
            getUpgradeCard().setVisibility(View.GONE);
        }
    }

    private BroadcastReceiver upgradeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getUpgradeCard().setVisibility(View.GONE);
        }
    };

    public View getUpgradeCard() {
        return getView().findViewById(R.id.upgrade_card);
    }

    @Override
    public void onPause() {
        super.onPause();

        storeListPosition();

        try { getActivity().unregisterReceiver(upgradeReceiver);}
        catch (Exception ignored) {}

        try { getActivity().unregisterReceiver(mMigrationReceiver); }
        catch (Exception ignored) { }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_my_tags_new, null);
        return v;
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
        mIntro = (ScrollView) view.findViewById(R.id.intro);
        mListView = (GridView) view.findViewById(android.R.id.list);
        mListView.setScrollingCacheEnabled(true);

        int widthDp = Utils.getWidthInDp(getActivity());
        if (widthDp >= 1280) {
            mListView.setNumColumns(3);
        } else if (widthDp >= 600) {
            mListView.setNumColumns(2);
        }

        (view.findViewById(R.id.button_try_now)).setOnClickListener(this);
        (view.findViewById(R.id.button_create)).setOnClickListener(this);
        (view.findViewById(R.id.button_upgrade)).setOnClickListener(this);

        ((TextView) view.findViewById(R.id.upgrade_unlocks_text)).setText(getString(R.string.upgrade_unlocks_triggers_count, TaskTypeItem.PAID_TRIGGER_COUNT));

        setupLoader();
    }

    private void storeListPosition() {
        mListPosition = getListView().getFirstVisiblePosition();
    }

    private int getListPosition() {
        return mListPosition;
    }

    public void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
        if (getListPosition() > 0) {
            getListView().setSelection(getListPosition());
        }
        setListShown(true);
    }


    public GridView getListView() {
        return (GridView) getView().findViewById(android.R.id.list);
    }


    public void setListShown(boolean shown) {
        mProgress.setVisibility(View.GONE);
        if (shown) {
            setDisplay(View.GONE, View.VISIBLE);
        } else {
            setDisplay(View.VISIBLE, View.GONE);
        }
    }


    public void setEmptyText() {
        setDisplay(View.VISIBLE, View.GONE);
    }

    private void setDisplay(int noTagsVisibility, int tagsPresentVisibility) {
        mIntro.setVisibility(noTagsVisibility);

        getListView().setVisibility(tagsPresentVisibility);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RUN_TAG:
                refresh();
                break;
            default:
                break;
        }
    }

    private void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }

    public void reuseTag(TaskSet task) {
        Task tag = task.getTask(0);
        Task newTag = Task.loadTask(getActivity(), tag.getId(), tag.getName(), "");
        newTag.setId(null);

        Intent intent = new Intent(getActivity(), SelectTriggerActivity.class);
        intent.putExtra(Constants.EXTRA_SAVED_TAG_REUSE, newTag);
        startActivity(intent);
    }

    public void runTag(TaskSet task) {
        Task tag = task.getTask(0);
        Intent intent = new Intent(getActivity(), ParserService.class);
        intent.putExtra(ParserService.EXTRA_TAG_NAME, tag.getName());
        intent.putExtra(ParserService.EXTRA_TAG_ID, tag.getId());
        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_MANUAL);
        if (Task.isStringValid(tag.getSecondaryId())) {
            intent.putExtra(ParserService.EXTRA_ALT_TAG_NAME, tag.getSecondaryName());
            intent.putExtra(ParserService.EXTRA_ALT_TAG_ID, tag.getSecondaryId());
        }
        intent.putExtra(ParserService.EXTRA_SKIP_CHECK, true);
        getActivity().startService(intent);

    }

    private class MyTagsAdapter extends ArrayAdapter<TaskSet> {

        protected GridView mListView;

        public MyTagsAdapter(Context context, List<TaskSet> tags) {
            super(context, R.layout.list_item_saved_tag, tags);
            mListView = getListView();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            boolean isNew = (convertView == null);
            final TaskSet item = getItem(position);
            convertView = item.getView(getActivity(), convertView, mListView, position, getCount());
            if (isNew) {
                registerForContextMenu(convertView);
                item.getHolder().taskContainer.setOnClickListener(mOnStandardClick);
                item.getHolder().taskContainer.setOnLongClickListener(mLongClick);
                item.getHolder().more.setOnClickListener(mOnMoreClick);
                item.getHolder().undo.setOnClickListener(mOnUndoClick);
            }

            return convertView;
        }

        private OnLongClickListener mLongClick = new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                final int position = mListView.getPositionForView(v);
                if (position >= 0) {
                    mClickedPosition = position;
                    mActionTask = mAdapter.getItem(position);
                    openContextMenu(mActionTask, v);
                }
                return true;
            }

        };

        private OnClickListener mOnMoreClick = new OnClickListener() {

            @Override
            public void onClick(View v) {

                final int position = mListView.getPositionForView(v);
                mClickedPosition = position;
                mActionTask = mAdapter.getItem(position);
                openContextMenu(mActionTask, v);

            }
        };

        private OnClickListener mOnStandardClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                final int position = mListView.getPositionForView(v);
                mActionTask = mAdapter.getItem(position);
                editTask(mActionTask);
            }

        };

        private OnClickListener mOnUndoClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                final int position = mListView.getPositionForView(v);
                Logger.d("Removing pending delete at position" + position);
                mAdapter.getItem(position).markDeleted(false);
                mAdapter.notifyDataSetChanged();
            }
        };

    }

    private void editTask(TaskSet task) {
        Intent intent = new Intent(getActivity(), TaskWizardActivity.class);
        intent.putExtra(TaskSet.EXTRA_TASK, task);
        intent.getBooleanExtra(TaskWizardActivity.EXTRA_TASK_IS_NEW, false);
        try {
            getActivity().startActivityForResult(intent, REQUEST_CREATE_TAG);
        } catch (Exception e) {
            Logger.e("Exception starting edit request", e);
        }
    }

    private void deleteTaskSet(TaskSet set) {
        Logger.d("Delete called for task set at position " + set.getTask(0).getName());

        if ((getActivity() == null) || (getActivity().isFinishing()))
            return;

        if (!set.shouldDelete()) {
            return;
        }

        if (mPendingDeletes.contains(set)) {
            TaskHelper.deleteTag(getActivity(), set);
            mAdapter.remove(set);
            for (Trigger trigger : set.getTriggers()) {
                switch (trigger.getType()) {
                    case TaskTypeItem.TASK_TYPE_GEOFENCE:
                        if (BuildConfiguration.USE_GEOFENCES) {
                            mRequestInProgress = true;
                            Logger.d("Removing geofence for " + trigger.getId());
                            mGeoClient = new GeofenceClient(getActivity());
                            final List<String> ids = new ArrayList<String>();
                            mGeoClient.setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    mRequestInProgress = false;
                                    if (status.isSuccess()) {
                                        Logger.d("GEO: Geofence removed " + TextUtils.join(",", ids));
                                        for (String id : ids) {
                                            DatabaseHelper.deleteGeofence(getActivity(), id);
                                        }
                                    } else {
                                        Logger.d("GEO: There was a problem removing the geofence");
                                    }

                                    mGeoClient.disconnect();
                                    refresh();
                                }
                            });

                            ids.add(trigger.getId());
                            mGeoClient.connectAndRemoveIds(ids);
                        }
                        break;
                    default:
                        mRequestInProgress = false;
                }
            }
        }
        if (mAdapter.getCount() > 0)
            mAdapter.notifyDataSetChanged();
        else
            setListShown(false);

        Utils.signalTasksChanged(getActivity());

    }

    private OnMenuItemClickListener mPopupClickedListener = new OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.pop_up_delete:
                    if (!mRequestInProgress) {
                        // Mark task to be deleted in the UI
                        if (mClickedPosition <= mAdapter.getCount()) {
                            mActionTask.markDeleted();
                            if (mClickedPosition == mAdapter.getCount()) {
                                mClickedPosition--; // There's a scenario where we have clicked the last item in the list and before delete is selected an item
                                // is removed from the list.  So we then have an out of bounds index.
                            }

                            mAdapter.getItem(mClickedPosition).markDeleted();
                            mAdapter.notifyDataSetChanged();
                            final TaskSet set = mActionTask;

                            Logger.d("Adding pending delete for " + set.getTask(0).getName());

                            mPendingDeletes.add(set);
                            // Schedule a delete
                            final Handler handler = new Handler();
                            Timer t = new Timer();
                            t.schedule(new TimerTask() {
                                public void run() {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            deleteTaskSet(set);
                                        }
                                    });
                                }
                            }, 3000);
                        }

                    }
                    break;
                case R.id.pop_up_edit:
                    editTask(mActionTask);
                    break;
                case R.id.pop_up_run:
                    runTag(mActionTask);
                    break;
                case R.id.pop_up_enable:
                    mAdapter.getItem(mClickedPosition).getTask(0).setEnabledAndWriteChange(getActivity(), true);
                    mAdapter.notifyDataSetChanged();
                    break;
                case R.id.pop_up_disable:
                    mAdapter.getItem(mClickedPosition).getTask(0).setEnabledAndWriteChange(getActivity(), false);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }

    };

    private void openContextMenu(TaskSet task, View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.inflate(task.getTask(0).isEnabled()
                ? R.menu.menu_popup_task_list
                : R.menu.menu_popup_task_list_enable);

        popupMenu.setOnMenuItemClickListener(mPopupClickedListener);
        if (!getActivity().isFinishing()) {
            popupMenu.show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_try_now:
                SimpleDialogFragment dialog = new SimpleDialogFragment();
                dialog.setPositiveButton(getString(R.string.try_it_now), new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Usage.logEvent(null, "try it now", false);
                        Intent intent = new Intent(getActivity(), ParserService.class);
                        intent.putExtra(ParserService.EXTRA_PAYLOAD, "Z:0:" + getString(R.string.my_battery_task) + ";D:I2;J:30:0");
                        intent.putExtra(ParserService.EXTRA_TAG_NAME, getString(R.string.my_battery_task));
                        intent.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_MANUAL);
                        getActivity().startService(intent);
                    }
                });
                dialog.setChildView(View.inflate(getActivity(), R.layout.include_demo_task, null));
                dialog.setTitle(getString(R.string.my_battery_task));
                dialog.show(getChildFragmentManager(), "example-dialog");

                break;
            case R.id.button_create:
                Usage.logEvent(null, "create a task", false);
                getActivity().startActivityForResult(new Intent(getActivity(), TaskWizardActivity.class).putExtra(TaskWizardActivity.EXTRA_TASK_IS_NEW, true)
                        , MainActivity.REQUEST_CREATE_TASK);
                break;
            case R.id.button_upgrade:
                break;
                //2_2_18
                //hiding the upgrade option...
                //((MainActivity) getActivity()).showUpgradeDialog();
        }

    }
}