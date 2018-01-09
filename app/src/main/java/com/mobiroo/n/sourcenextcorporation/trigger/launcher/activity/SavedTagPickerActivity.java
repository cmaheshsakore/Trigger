package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ActionService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.util.FilterableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class SavedTagPickerActivity extends Activity {

    private ListView mListView;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.saved_tag_picker_dialog);
        
        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        
        mListView = (ListView) findViewById(R.id.tagsList);
        mListView.setVisibility(View.GONE);
        mListView.setDivider(null);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
                TaskSet tag = ((MyTagsAdapter) mListView.getAdapter()).getItem(position);
                runTag(tag);
            }
            
        });
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        new SavedTagLoader().execute(SavedTagPickerActivity.this);
    }
    
    private class MyTagsAdapter extends FilterableListAdapter<TaskSet> {
        public MyTagsAdapter(Context context, TaskSet[] tags) {
            super(tags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_saved_tag_no_date, null);
            }

            TaskSet set = getItem(position);
            Task tag = set.getTask(0);
            
            String name = tag.getName();
            
            ((TextView) convertView.findViewById(R.id.name)).setText(name);
            
            
            if (tag.isSecondTagNameValid()) {
                (convertView.findViewById(R.id.separator)).setVisibility(View.VISIBLE);
                ((TextView) convertView.findViewById(R.id.name_alt)).setVisibility(View.VISIBLE);
                ((TextView) convertView.findViewById(R.id.name_alt)).setText(tag.getSecondaryName());
            } else {
                (convertView.findViewById(R.id.separator)).setVisibility(View.GONE);
                ((TextView) convertView.findViewById(R.id.name_alt)).setVisibility(View.GONE);
            }

            boolean isLast = (position == (getCount() - 1));
            convertView.findViewById(R.id.divider).setVisibility(isLast ? View.GONE : View.VISIBLE);
            
            return convertView;
        }
    }
    
    
    public void runTag(TaskSet task) {
        Task tag = task.getTask(0);
        Intent intent = new Intent(SavedTagPickerActivity.this, ParserService.class);
        intent.putExtra(ActionService.EXTRA_TAG_NAME, tag.getName());
        intent.putExtra(ActionService.EXTRA_TAG_ID, tag.getId());
        if (Task.isStringValid(tag.getSecondaryId())) {
            intent.putExtra(ActionService.EXTRA_ALT_TAG_NAME, tag.getSecondaryName());
            intent.putExtra(ActionService.EXTRA_ALT_TAG_ID, tag.getSecondaryId());
        }
        
        startService(intent);
        
        this.finish();
    }

    private class SavedTagLoader extends AsyncTask<Context,Void,List<TaskSet>> {
        
        @Override
        protected List<TaskSet> doInBackground(Context... args) {
            ArrayList<TaskSet> set = DatabaseHelper.getTasks(args[0]);
            return (set != null) ? set : null;
            
        }
        
        protected void onPostExecute(List<TaskSet> tags) {
            
            if (tags != null) {
                // Hide progress bar spinner
                ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.GONE);
                // Show list view
                mListView.setVisibility(View.VISIBLE);
                // Load in ListView
                if (tags.size() > 0) {
                    Logger.i("Setting adapter for " + tags.size());
                    mListView.setAdapter(new MyTagsAdapter(getBaseContext(), tags.toArray(new TaskSet[tags.size()])));
                } else {
                    Logger.i("Setting adapter");
                    mListView.setAdapter(null);
                    //setEmptyText(getString(R.string.no_tags));
                }
            }
        }
    }   
    
}
