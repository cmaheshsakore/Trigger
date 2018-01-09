package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import com.mobiroo.n.sourcenextcorporation.trigger.R;


public class TextDisplayActivity extends Activity {
    public static final String EXTRA_MESSAGE = "text_tag_message";
    public static final String EXTRA_LAYOUT = "extra_layout";

        @Override
        public void onCreate(Bundle savedState) {
            super.onCreate(savedState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            int layout =  getIntent().getIntExtra(EXTRA_LAYOUT, R.layout.activity_text_display);
            setContentView(layout);

            String message = (getIntent().hasExtra(TextDisplayActivity.EXTRA_MESSAGE)) ? getIntent().getStringExtra(TextDisplayActivity.EXTRA_MESSAGE) : "";

            if (!message.isEmpty()) {
                ((TextView) findViewById(R.id.row1Text)).setText(message);
            } else { 
                this.finish();
            }
            
            
        }
        
    }
