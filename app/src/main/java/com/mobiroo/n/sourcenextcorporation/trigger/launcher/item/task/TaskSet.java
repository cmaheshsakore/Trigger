package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task;

import java.util.ArrayList;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TaskSet implements Parcelable {

    public static final String  EXTRA_TASK = "com.trigger.taskset.task";
    public static final String  EXTRA_TRIGGERS = "com.trigger.taskset.triggers";

    private ArrayList<Trigger>  mTriggers;
    private ArrayList<Task>     mTasks;
    private TaskSetHolder       mHolder;

    private String              mLastUsedText;

    private boolean             mDelete;

    public TaskSet() {
        mTriggers = new ArrayList<Trigger>();
        mTasks = new ArrayList<Task>();
    }

    public TaskSet(Parcel in) {

        if (mTriggers == null) {
            mTriggers = new ArrayList<Trigger>();
        }
        in.readTypedList(mTriggers, Trigger.CREATOR);

        if (mTasks == null) {
            mTasks = new ArrayList<Task>();
        }
        in.readTypedList(mTasks, Task.CREATOR);
    }

    public TaskSet(ArrayList<Trigger> triggers, ArrayList<Task> tasks) {
        mTriggers = triggers;
        mTasks = tasks;
    }

    public ArrayList<Trigger> getTriggers() {
        return (mTriggers != null) ? mTriggers : new ArrayList<Trigger>();
    }

    public ArrayList<Task> getTasks() {
        return mTasks;
    }

    /**
     * @param index is the position in the array list.  0 based index
     * @return
     */
    public Trigger getTrigger(int index) {
        return ((mTriggers != null) && (mTriggers.size() > index)) ? mTriggers.get(index) : null;
    }

    /**
     * @param which is the position in the array list.  0 based index
     * @return
     */
    public Task getTask(int which) {
        return ((mTasks != null) && (mTasks.size() > which)) ? mTasks.get(which) : null;
    }

    public static final int TRIGGER_NFC         = 1;
    public static final int TRIGGER_WIFI        = 2;
    public static final int TRIGGER_BLUETOOTH   = 4;
    public static final int TRIGGER_GEOFENCE    = 8;
    public static final int TRIGGER_BATTERY     = 16;
    public static final int TRIGGER_TIME        = 32;
    public static final int TRIGGER_CHARGER     = 64;
    public static final int TRIGGER_HEADSET     = 128;
    public static final int TRIGGER_CALENDAR    = 256;
    public static final int TRIGGER_AGENT       = 512;
    public static final int TRIGGER_MANUAL      = 1024;

    public int getIntFromId(int id) {
        switch (id) {
            case R.id.icon_battery:
                return TRIGGER_BATTERY;
            case R.id.icon_bluetooth:
                return TRIGGER_BLUETOOTH;
            case R.id.icon_wifi:
                return TRIGGER_WIFI;
            case R.id.icon_time:
                return TRIGGER_TIME;
            case R.id.icon_geofence:
                return TRIGGER_GEOFENCE;
            case R.id.icon_charger:
                return TRIGGER_CHARGER;
            case R.id.icon_headset:
                return TRIGGER_HEADSET;
            case R.id.icon_calendar:
                return TRIGGER_CALENDAR;
            case R.id.icon_agent:
                return TRIGGER_AGENT;
            case R.id.icon_manual:
                return TRIGGER_MANUAL;
            case R.id.icon_nfc:
            default:
                return TRIGGER_NFC;
        }
    }
    public int getTriggersInt() {
        int triggers = 0;
        if (mTriggers != null) {
            for (int i=0; i< mTriggers.size(); i++) {
                switch(mTriggers.get(i).getType()) {
                    case TaskTypeItem.TASK_TYPE_BATTERY:
                        triggers |= TRIGGER_BATTERY;
                        break;
                    case TaskTypeItem.TASK_TYPE_BLUETOOTH:
                        triggers |= TRIGGER_BLUETOOTH;
                        break;
                    case TaskTypeItem.TASK_TYPE_GEOFENCE:
                        triggers |= TRIGGER_GEOFENCE;
                        break;
                    case TaskTypeItem.TASK_TYPE_NFC:
                    case TaskTypeItem.TASK_TYPE_SWITCH:
                        triggers |= TRIGGER_NFC;
                        break;
                    case TaskTypeItem.TASK_TYPE_TIME:
                        triggers |= TRIGGER_TIME;
                        break;
                    case TaskTypeItem.TASK_TYPE_CHARGER:
                        triggers |= TRIGGER_CHARGER;
                        break;
                    case TaskTypeItem.TASK_TYPE_WIFI:
                        triggers |= TRIGGER_WIFI;
                        break;
                    case TaskTypeItem.TASK_TYPE_HEADSET:
                        triggers |= TRIGGER_HEADSET;
                        break;
                    case TaskTypeItem.TASK_TYPE_CALENDAR:
                        triggers |= TRIGGER_CALENDAR;
                        break;
                    case TaskTypeItem.TASK_TYPE_AGENT:
                        triggers |= TRIGGER_AGENT;
                        break;
                    case TaskTypeItem.TASK_TYPE_MANUAL:
                        triggers |= TRIGGER_MANUAL;
                        break;
                }
            }
        }
        return triggers;
    }

    public void addTrigger(Trigger trigger) {
        if (trigger != null) {
            mTriggers.add(trigger);
        }
    }

    public void addTask(Task task) {
        if (task != null) {
            mTasks.add(task);
        }
    }

    public void addTask(ArrayList<Task> tasks) {
        for (Task task: tasks) {
            if (task != null) {
                mTasks.add(task);
            }
        }
    }

    public static final Creator<TaskSet> CREATOR = new Creator<TaskSet>() {
        @Override
        public TaskSet createFromParcel(Parcel source) {
            return new TaskSet(source);
        }

        @Override
        public TaskSet[] newArray(int size) {
            return new TaskSet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mTriggers);
        dest.writeTypedList(mTasks);
    }

    public static class TaskSetHolder {
        public LinearLayout icon;
        public RelativeLayout taskContainer;
        public TextView lastUsed;
        public TextView name;
        public TextView nameAlt;
        public ImageView separator;
        public View divider;
        public ImageButton more;
        public TextView description;
        public int position;
        public boolean showDescription;
        public TextView actionsTitle;
        public TextView actionsList;
        public View descriptionContainer;
        public View deleteContainer;
        public TextView undo;
        public boolean hasLoaded;
    }

    public View getView(Context context, View v, ViewGroup group, int position, int count) {
        mHolder = null;

        if (v == null) {
            if (group != null) {
                v = LayoutInflater.from(context).inflate(R.layout.list_item_saved_tag, group, false);
            } else {
                v = View.inflate(context, R.layout.list_item_saved_tag, null);
            }
            mHolder = new TaskSetHolder();
            mHolder.icon = ((LinearLayout) v.findViewById(R.id.icon));
            mHolder.name = ((TextView) v.findViewById(R.id.name));
            mHolder.separator = ((ImageView) v.findViewById(R.id.separator));
            mHolder.nameAlt = ((TextView) v.findViewById(R.id.name_alt));
            mHolder.lastUsed = ((TextView) v.findViewById(R.id.last_used));
            mHolder.divider = (View) v.findViewById(R.id.divider);
            mHolder.taskContainer = ((RelativeLayout) v.findViewById(R.id.container));
            mHolder.more = ((ImageButton) v.findViewById(R.id.more));
            mHolder.description = (TextView) v.findViewById(R.id.description);
            mHolder.actionsTitle = (TextView) v.findViewById(R.id.actions_title);
            mHolder.descriptionContainer = v.findViewById(R.id.description_container);
            mHolder.showDescription = SettingsHelper.getPrefBool(context, Constants.PREF_SHOW_TASK_DESCRIPTION, true);
            mHolder.deleteContainer = v.findViewById(R.id.deleted);
            mHolder.undo = (TextView) v.findViewById(R.id.undo);
            v.setTag(mHolder);

        } else {
            mHolder = (TaskSetHolder) v.getTag();
        }

        Task task = getTask(0);
        mHolder.name.setText(task.getName());

        if (!shouldDelete()) {
            mHolder.deleteContainer.setVisibility(View.GONE);
            mHolder.taskContainer.setVisibility(View.VISIBLE);

            /*if (!task.hasLoaded()) {
                task.setLoaded();
                Animation a = AnimationUtils.loadAnimation(context, R.anim.left_slide_in);
                a.setDuration(500);
                mHolder.taskContainer.startAnimation(a);
            }*/

            // Show divider and secondary name if this is a switch
            if (task.isSecondTagNameValid()) {
                mHolder.separator.setVisibility(View.VISIBLE);
                mHolder.nameAlt.setVisibility(View.VISIBLE);
                mHolder.nameAlt.setText(task.getSecondaryName());
            } else {
                mHolder.separator.setVisibility(View.GONE);
                mHolder.nameAlt.setVisibility(View.GONE);
            }

            if (mLastUsedText == null) {
                mLastUsedText = String.format(context.getString(R.string.used_text), Utils.formatLastUsed(task.getLastUsedDate(), context.getString(R.string.neverText)));
            }
            mHolder.lastUsed.setText(mLastUsedText);


            if (mHolder.showDescription) {

                // Add icon(s)
                int childImages = mHolder.icon.getChildCount();
                int triggers = getTriggersInt();
                if (triggers >   0) {
                    for (int i=0; i < childImages; i++) {
                        View child = mHolder.icon.getChildAt(i);
                        int value = getIntFromId(child.getId());
                        if ((triggers & value) == value) {
                            child.setVisibility(View.VISIBLE);
                        } else {
                            child.setVisibility(View.GONE);
                        }
                    }
                }

                // Show  a summary of actions
                String description = "";
                mHolder.actionsTitle.setVisibility(View.VISIBLE);
                if (this.getTask(0).getActions().size() == 0) {
                    this.getTask(0).loadActions(context);
                }
                for (SavedAction action: this.getTask(0).getActions()) {
                    if (!description.isEmpty()) {
                        description += ", ";
                    }
                    description += action.getDescription();
                }

                if (this.getTasks().size() > 1) {
                    description += " - ";

                    if (this.getTask(1).getActions().size() == 0) {
                        this.getTask(1).loadActions(context);
                    }

                    String secondary = "";
                    for (SavedAction action: this.getTask(1).getActions()) {
                        if (!secondary.isEmpty()) {
                            secondary += ", ";
                        }
                        secondary += action.getDescription();
                    }

                    description += secondary;
                }
                
                mHolder.description.setVisibility(View.VISIBLE);
                mHolder.description.setText(description);
            } else {
                mHolder.actionsTitle.setVisibility(View.GONE);
                mHolder.descriptionContainer.setVisibility(View.GONE);
                mHolder.icon.setVisibility(View.GONE);
            }
        } else {
            mHolder.deleteContainer.setVisibility(View.VISIBLE);
            mHolder.taskContainer.setVisibility(View.GONE);
        }

        if (task.isEnabled()) {
            mHolder.name.setTextColor(Color.BLACK);
            mHolder.nameAlt.setTextColor(Color.BLACK);
            mHolder.name.setTypeface(mHolder.name.getTypeface(), Typeface.BOLD);
            mHolder.nameAlt.setTypeface(mHolder.nameAlt.getTypeface(), Typeface.BOLD);
            mHolder.description.setTypeface(mHolder.description.getTypeface(), Typeface.NORMAL);
        } else {
            mHolder.name.setTextColor(Color.GRAY);
            mHolder.nameAlt.setTextColor(Color.GRAY);
            mHolder.name.setTypeface(mHolder.name.getTypeface(), Typeface.ITALIC);
            mHolder.nameAlt.setTypeface(mHolder.nameAlt.getTypeface(), Typeface.ITALIC);
            mHolder.description.setTypeface(mHolder.description.getTypeface(), Typeface.ITALIC);
            mHolder.description.setText(R.string.layoutPreferencesEnableLimitingSub);
        }
        
        return v;
    }

    public View getWidgetView(Context context, View v, int position, int count) {
        mHolder = null;

        if (v == null) {
            v = View.inflate(context, R.layout.list_item_saved_tag_no_date, null);
            mHolder = new TaskSetHolder();
            mHolder.icon = ((LinearLayout) v.findViewById(R.id.icon));
            mHolder.name = ((TextView) v.findViewById(R.id.name));
            mHolder.separator = ((ImageView) v.findViewById(R.id.separator));
            mHolder.nameAlt = ((TextView) v.findViewById(R.id.name_alt));
            mHolder.lastUsed = ((TextView) v.findViewById(R.id.last_used));
            mHolder.divider = (View) v.findViewById(R.id.divider);
            mHolder.taskContainer = ((RelativeLayout) v.findViewById(R.id.container));
            mHolder.more = ((ImageButton) v.findViewById(R.id.more));
            v.setTag(mHolder);

        } else {
            mHolder = (TaskSetHolder) v.getTag();
        }

        Task task = getTask(0);

        // Set Task name
        mHolder.name.setText(task.getName());


        // Show divider and secondary name if this is a switch
        if (task.isSecondTagNameValid()) {

            mHolder.separator.setVisibility(View.VISIBLE);
            mHolder.nameAlt.setVisibility(View.VISIBLE);
            mHolder.nameAlt.setText(task.getSecondaryName());
        } else {
            mHolder.nameAlt.setVisibility(View.GONE);
            mHolder.separator.setVisibility(View.GONE);
        }


        try {  mHolder.divider.setVisibility((position == (count - 1)) ? View.GONE : View.VISIBLE);} 
        catch (Exception e) {    }

        return v;

    }

    public TaskSetHolder getHolder() {
        return mHolder;
    }

    public void markDeleted() {
        mDelete = true;
    }

    public void markDeleted(boolean delete) {
        mDelete = delete;
    }
    
    public boolean shouldDelete() {
        return mDelete;
    }
    
    public boolean shouldUse() {
        boolean trigger = getTrigger(0) != null;
        boolean task = getTask(0) != null;
        if (!task) {
            Logger.d("CHECK: Task is invalid");
            return false;
        }

        if (!trigger) {
            Logger.d("CHECK: Trigger is invalid");
            return false;
        }

        if (!getTask(0).isEnabled()) {
            Logger.d("CHECK: Task is disabled");
            return false;
        }
        return true;
    }

    public String getFullName() {
        Task t = getTask(0);
        return (t == null) ? "" : t.getFullName();
    }
}
