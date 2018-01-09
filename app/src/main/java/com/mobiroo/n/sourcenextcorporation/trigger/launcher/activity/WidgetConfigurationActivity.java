package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.util.FilterableListAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.widget.WidgetLarge;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;

public class WidgetConfigurationActivity extends ListActivity {
    
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    private class Background {
        private int mIcon;
        private int mBackground;
        private String mText;
        private int mTextColor;
        
        public Background(String text, int background, int icon, int textColor) {
            mText = text;
            mIcon = icon;
            mBackground = background;
            mTextColor = textColor;
        }
        
        
        public String getText() {
            return mText;
        }
        
        public int getIcon() {
            return mIcon;
        }
        
        public int getBackground() {
            return mBackground;
        }
        
        public int getTextColor() {
            return mTextColor;
        }
        @SuppressWarnings("deprecation")
        public View getView(BackgroundAdapter adapter, int position, View view, Background item) {
            
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.widget_config_example, null);
            }
            
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            	(((LinearLayout) view.findViewById(R.id.widget_container))).setBackground(getResources().getDrawable(item.getBackground()));
            } else {
            	(((LinearLayout) view.findViewById(R.id.widget_container))).setBackgroundDrawable(getResources().getDrawable(item.getBackground()));
            }
            ((ImageView) view.findViewById(R.id.widgetButton)).setImageDrawable(getResources().getDrawable(item.getIcon()));
            ((TextView) view.findViewById(R.id.widgetText)).setText(item.getText());
            ((TextView) view.findViewById(R.id.widgetText)).setTextColor(item.getTextColor());

            return view;
        }
    }
    
    private class BackgroundAdapter extends FilterableListAdapter<Background> {

        public BackgroundAdapter(Background[] backgrounds) {
            super(backgrounds);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Background item = getItem(position);
            return item.getView(this, position, convertView, item);
        }
        
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_widget_configuration);
        
        ((TextView) findViewById(android.R.id.title)).setText(getString(R.string.select_background_color));
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
        final Background[] colors = { 
                new Background(getString(R.string.color_black), R.drawable.appwidget_dark_bg_clickable, R.drawable.icon_light, Color.WHITE), 
                new Background(getString(R.string.color_white), R.drawable.appwidget_light_bg_clickable, R.drawable.icon, Color.BLACK), 
        };
        
        setListAdapter(new BackgroundAdapter(colors));
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        SettingsHelper.setPrefInt(WidgetConfigurationActivity.this, Constants.PREF_WIDGET_BACKGROUND, position);
        updateWidget();
    }
    
    public void updateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WidgetConfigurationActivity.this);
        RemoteViews views = WidgetLarge.buildRemoteViews(WidgetConfigurationActivity.this, SettingsHelper.getPrefString(WidgetConfigurationActivity.this, Constants.PREF_WIDGET_LAST_TEXT, getString(R.string.app_name)));
        appWidgetManager.updateAppWidget(mAppWidgetId, views);
        
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}
