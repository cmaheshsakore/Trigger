package com.mobiroo.n.sourcenextcorporation.trigger.launcher.fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActivityFeedItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import static com.mobiroo.n.sourcenextcorporation.trigger.util.BetterAsyncTask.TaskResult;

public class ActivityFeedFragment extends Fragment {

    private List<ActivityFeedItem>      mActivityFeed;
    
    private LoaderManager.LoaderCallbacks<TaskResult<int[]>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<TaskResult<int[]>>() {
        @Override
        public Loader<TaskResult<int[]>> onCreateLoader(int i, Bundle bundle) {
            return new BetterAsyncLoader<int[]>(getActivity(), BetterAsyncLoader.INTERVAL_NONE) {
                @Override
                protected int[] doLoadInBackground() throws Exception {
                    int[] usage = new int[3];
                    Cursor cursor = null;
                    try {
                        cursor = getActivity().getContentResolver().query(Uri.withAppendedPath(TaskProvider.Contract.ACTIVITY_FEED, "count"), null, null, null, null);
                        if (cursor == null || !cursor.moveToFirst()) {
                            usage[0] = 0;
                        }
                        usage[0] = cursor.getInt(0);

                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    mActivityFeed = DatabaseHelper.getActivityFeed(getContext());

                    return usage;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<TaskResult<int[]>> loader, TaskResult<int[]> result) {
            if (!result.success()) {
                return;
            }

            int[] usage = result.getObject();
            int total = usage[0];

            ((TextView) getView().findViewById(R.id.actions_performed)).setText(String.valueOf(total));
            
            // empty history case
            if (mActivityFeed.size() < 1) {
            	mActivityFeed.add(new ActivityFeedItem(TaskTypeItem.TASK_TYPE_TIME, getString(R.string.activity_feed_none), null));
                ((TextView) getView().findViewById(R.id.actions_performed)).setText(String.valueOf(0));
            }
            
            ListView list = (ListView) getView().findViewById(android.R.id.list);
            list.setAdapter(new ListItemsAdapter(getActivity(), mActivityFeed.toArray(new ActivityFeedItem[mActivityFeed.size()])));

        }

        @Override
        public void onLoaderReset(Loader<TaskResult<int[]>> taskResultLoader) {}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats,  null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view.findViewById(R.id.export).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportData(getActivity());
            }
        });
        view.findViewById(R.id.export).setLongClickable(true);
        view.findViewById(R.id.export).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getActivity(), R.string.export, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearStats(getActivity());
            }
        });
        view.findViewById(R.id.clear).setLongClickable(true);
        view.findViewById(R.id.clear).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getActivity(), R.string.clear, Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }

    private void clearStats(Context context) {
        context.getContentResolver().delete(TaskProvider.Contract.ACTIVITY_FEED , null, null);
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);
    }

    private void exportData(Context context) {
        Logger.d("Calling export data");
        List<ActivityFeedItem> items = mActivityFeed = DatabaseHelper.getActivityFeed(context, -1);
        StringBuilder b = new StringBuilder();
        b.append("BEGIN ACTIVITY LOG\n");

        if ((items != null) && (items.size() > 0)) {
            /* Dump all items into log */
            for (ActivityFeedItem item : items) {
                b.append(item.getName() + ", " + Utils.getTimeStringAsLocal(item.getTime(), "", Constants.STANDARD_DAY_TIME_FORMAT) + "\n");
            }
        }
        b.append("END ACTIVITY LOG");
        new AsyncFileWriter(getActivity()).execute(b.toString());
    }

    private static String mPath = "";

    private static class AsyncFileWriter extends AsyncTask<String, Void, Void> {

        private boolean mEncode = false;;
        private boolean mStamp = true;
        private boolean mAppendFile = true;
        private Context mContext;

        private final String mTitle = "activity_log_raw.txt";

        public AsyncFileWriter(Context context) {
            mContext = context;
            this.mEncode = false;
            this.mStamp = false;
            this.mAppendFile = true;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(String... params) {
            String message = params[0];
            if (this.mEncode) {
                message = Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
            }

            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()) {
                try {
                    File container = new File(root.getPath() + "/" + Constants.LOG_FILE_DIRECTORY + "/");
                    mPath = container.toString();
                    container.mkdirs();
                    File log = new File(container, mTitle);
                    if (log.exists()) {
                        log.delete();
                    }
                    log.createNewFile();

                    FileWriter writer = new FileWriter(log, mAppendFile);
                    BufferedWriter out = new BufferedWriter(writer);
                    Date now = new Date();
                    if (this.mStamp) {
                        out.write(now.toLocaleString() + ": " + message + "\n");
                    } else {
                        out.write(message + "\n");
                    }
                    out.flush();
                    writer.flush();
                    out.close();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public void onPostExecute(final Void unused) {
            Toast.makeText(mContext, "Activity log exported to " + mPath + "/" + "Activity_log.txt", Toast.LENGTH_LONG).show();
        }

    }
}
