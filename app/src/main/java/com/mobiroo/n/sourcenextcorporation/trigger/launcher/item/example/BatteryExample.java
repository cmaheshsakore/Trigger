package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.example;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.AutoSyncAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.WifiAdapterAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.BatteryTrigger;

/**
 * Created by krohnjw on 1/24/14.
 */
public class BatteryExample extends BaseExample implements Example {

    private String mPercentage;

    public BatteryExample(int name, int icon, int description, boolean isPro, String tag) {
        super(name, icon, description, isPro, tag);
    }

    @Override
    public void showConfigurationDialog(final Context context, FragmentManager manager) {
        // Ask for battery percentage to activate
        final SimpleDialogFragment dialog = new SimpleDialogFragment();
        dialog.hideAllTitles();
        final View child = View.inflate(context, R.layout.include_example_battery, null);
        dialog.setChildView(child);
        dialog.setPositiveButton(context.getString(R.string.menu_done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPercentage = ((TextView) child.findViewById(R.id.percentage)).getText().toString();
                Intent intent = new Intent();
                intent.putExtra("key1", mPercentage);
                createExample(context, intent);
                dialog.dismiss();
            }
        });
        dialog.show(manager, getTag());

    }

    @Override
    public void createExample(Context context, Intent intent) {
        super.createExample(context, intent);
        // Turn off wifi and dim display for battery task
        mPercentage = intent.getStringExtra("key1");
        if (mPercentage.isEmpty()) {
            return;
        }

        Task task = new Task("", context.getString(R.string.example_task_battery_saver), "", null);
        task.addAction(new SavedAction("D:" + Constants.MODULE_WIFI, context.getString(R.string.disableText), context.getString(R.string.adapterWifi), new WifiAdapterAction().getCode()));
        task.addAction(new SavedAction("D:" + Constants.MODULE_SYNC, context.getString(R.string.disableText), context.getString(R.string.listWifiAutoSync), new AutoSyncAction().getCode()));

        String id = DatabaseHelper.saveTask(context, task, null, false);

        Trigger trigger = new Trigger(null, TaskTypeItem.TASK_TYPE_BATTERY, DatabaseHelper.TRIGGER_BATTERY_GOES_BELOW, mPercentage, "", id);

        DatabaseHelper.saveTrigger(context, trigger, id);

        BatteryTrigger.enable(context);

        Toast.makeText(context, String.format(context.getString(R.string.example_created), context.getString(R.string.example_task_battery_saver)), Toast.LENGTH_LONG).show();
    }
}
