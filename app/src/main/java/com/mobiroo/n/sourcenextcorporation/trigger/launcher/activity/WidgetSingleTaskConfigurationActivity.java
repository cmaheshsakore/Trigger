package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.WidgetIcon;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.WidgetIcons;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ActionService;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.util.FilterableListAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;

import java.util.ArrayList;
import java.util.List;

public class WidgetSingleTaskConfigurationActivity extends Activity {
    
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private WidgetIcon mSelectedIcon;
    private ListView    mListView;
    private TaskSet     mSelectedTaskSet;
    private Task mSelectedTag;;
    
    private class IconsAdapter extends FilterableListAdapter<WidgetIcon> {

        private Context mContext;
        
        public IconsAdapter(WidgetIcon[] icons, Context context) {
            super(icons);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WidgetIcon item = (WidgetIcon) getItem(position);
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.list_item_icon, null);
            }
            ((ImageView) convertView.findViewById(android.R.id.icon)).setImageResource(item.getResourceId());
            return convertView;
        }
        
    }
    
    private class MyTagsAdapter extends FilterableListAdapter<TaskSet> {
        public MyTagsAdapter(Context context, TaskSet[] tags) {
            super(tags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getItem(position).getWidgetView(WidgetSingleTaskConfigurationActivity.this, convertView, position, getCount());
            return convertView;
        }
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
                    mListView.setAdapter(new MyTagsAdapter(getBaseContext(), tags.toArray(new TaskSet[tags.size()])));
                } else {
                    mListView.setAdapter(null);
                }
            }
        }
    } 
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        final Context context = WidgetSingleTaskConfigurationActivity.this;
        Logger.i("Got ID " + mAppWidgetId);
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_widget_single_task_configuration);
        
        ((TextView) findViewById(android.R.id.title)).setText(getString(R.string.widget_single_task_configure_title));

        // Set up a grid view with all icons
        GridView grid = ((GridView) findViewById(R.id.grid));
        grid.setAdapter(new IconsAdapter(WidgetIcons.getIcons(), WidgetSingleTaskConfigurationActivity.this));
        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                mSelectedIcon = ((WidgetIcon) adapter.getItemAtPosition(position));
                ((ImageView) findViewById(R.id.group_two_detail)).setImageResource(mSelectedIcon.getResourceId());
                view.setSelected(true);
            }
            
        });

        ((RadioGroup) findViewById(R.id.background)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.background_black) {
                    ((TextView) findViewById(R.id.group_one_detail)).setText(getString(R.string.black));
                } else {
                    ((TextView) findViewById(R.id.group_one_detail)).setText(getString(R.string.white));
                }
                
            }
            
        });
        
        // Allow user to pick a saved task from a list
        mListView = ((ListView) findViewById(R.id.list));
        mListView.setVisibility(View.GONE);
        mListView.setDivider(null);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
                mSelectedTaskSet = ((MyTagsAdapter) mListView.getAdapter()).getItem(position);
                mSelectedTag = mSelectedTaskSet.getTask(0);
                ((TextView) findViewById(R.id.group_three_detail)).setText(mSelectedTag.getName());
                if (mSelectedTag.isSecondTagNameValid()) {
                    ((TextView) findViewById(R.id.group_three_detail)).setText(mSelectedTag.getName() + " + " + mSelectedTag.getSecondaryName());
                } else {
                    ((TextView) findViewById(R.id.group_three_detail)).setText(mSelectedTag.getName());    
                }
                v.setSelected(true);
            }
            
        });
        new SavedTagLoader().execute(WidgetSingleTaskConfigurationActivity.this);
        
        ((Button) findViewById(R.id.done_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mSelectedTag != null) && (mSelectedIcon != null)) {
                    SettingsHelper.setPrefString(WidgetSingleTaskConfigurationActivity.this, Constants.PREF_WIDGET_SINGLE_ICON_KEY + mAppWidgetId, mSelectedIcon.getName());
                    SettingsHelper.setPrefString(WidgetSingleTaskConfigurationActivity.this, Constants.PREF_WIDGET_SINGLE_TASK_KEY + mAppWidgetId, mSelectedTag.getName());
                    updateWidget(context);
                }
            }
        });
    }

    public static Bitmap loadBitmapFromView(View v) {
        
        if (v.getMeasuredHeight() <= 0) {
            int dimen = MeasureSpec.makeMeasureSpec(96, MeasureSpec.EXACTLY);
            v.measure(dimen, dimen);
            v.layout(0, 0, v.getMeasuredWidth(),v.getMeasuredHeight());
            Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.draw(c);
            return b;
        } else {
            Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_8888);                
            Canvas c = new Canvas(b);
            v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
            v.draw(c);
            return b;
        }
        
    }
    
    public void updateWidget(Context context) {
        
        /* Inflate our custom view */
        View v = View.inflate(context, R.layout.widget_single_task, null);
        v.setDrawingCacheEnabled(true);
        ((ImageView) v.findViewById(R.id.icon)).setImageResource(mSelectedIcon.getResourceId());
        /* Get currently selected color */
        if (((RadioButton) findViewById(R.id.background_black)).isChecked()) {
            ((RelativeLayout) v.findViewById(R.id.widget_container)).setBackgroundColor(Color.BLACK);
        }
        
        Intent intent = new Intent();
        Intent launchIntent = new Intent(this, WidgetSingleTaskRunActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        if (Task.isStringValid(mSelectedTag.getSecondaryId())) {

            launchIntent.putExtra(ActionService.EXTRA_TAG_ID, mSelectedTag.getId() + "," + mSelectedTag.getSecondaryId());
            launchIntent.putExtra(ActionService.EXTRA_TAG_NAME, mSelectedTag.getName() + "," + mSelectedTag.getSecondaryName());
            
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mSelectedTag.getName() + " - " + mSelectedTag.getSecondaryName());
            
        } else {
            launchIntent.putExtra(ActionService.EXTRA_TAG_ID, mSelectedTag.getId());
            launchIntent.putExtra(ActionService.EXTRA_TAG_NAME, mSelectedTag.getName());
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mSelectedTag.getName());
        }
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
        
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, loadBitmapFromView(v));
       
        setResult(RESULT_OK, intent);
        finish();
    }

    public void headingClicked(View v) {
        switch (v.getId()) {
            case R.id.group_one_heading:
                setHeadingVisibile(R.id.group_one);
                break;
            case R.id.group_two_heading:
                setHeadingVisibile(R.id.group_two);
                break;
            case R.id.group_three_heading:
                setHeadingVisibile(R.id.group_three);
                break;
        }
    }

    private void setHeadingVisibile(int id) {
        findViewById(R.id.group_one).setVisibility((id == R.id.group_one) ? View.VISIBLE : View.GONE);
        findViewById(R.id.group_two).setVisibility((id == R.id.group_two) ? View.VISIBLE : View.GONE);
        findViewById(R.id.group_three).setVisibility((id == R.id.group_three) ? View.VISIBLE : View.GONE);
    }
}
