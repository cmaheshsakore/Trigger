package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.service.ParserService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WidgetSingleTaskRunActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        
        if (intent.hasExtra(ParserService.EXTRA_TAG_NAME) && intent.hasExtra(ParserService.EXTRA_TAG_ID)) {
            Intent service = new Intent(this, ParserService.class);
            service.putExtra(TaskTypeItem.EXTRA_TASK_TYPE, TaskTypeItem.TASK_TYPE_MANUAL);
            service.putExtra(ParserService.EXTRA_TAG_NAME, intent.getStringExtra(ParserService.EXTRA_TAG_NAME));
            service.putExtra(ParserService.EXTRA_TAG_ID, intent.getStringExtra(ParserService.EXTRA_TAG_ID));
            if (intent.hasExtra(ParserService.EXTRA_ALT_TAG_NAME)) {
                service.putExtra(ParserService.EXTRA_ALT_TAG_NAME, intent.getStringExtra(ParserService.EXTRA_ALT_TAG_NAME));
            }
            if (intent.hasExtra(ParserService.EXTRA_ALT_TAG_ID)) {
                service.putExtra(ParserService.EXTRA_ALT_TAG_ID, intent.getStringExtra(ParserService.EXTRA_ALT_TAG_ID));
            }
            startService(service);
        }
        finish();
    }
    
    

}

