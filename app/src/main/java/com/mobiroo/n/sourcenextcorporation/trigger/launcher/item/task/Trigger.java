package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task;

import android.app.AlarmManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.EventConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.trigger.CalendarTrigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.ui.Holders;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.AgentAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;

import java.util.ArrayList;
import java.util.Date;


public class Trigger implements Parcelable {

    public static final String EXTRA_TRIGGER = "com.trigger.trigger_item";
    public static final String EXTRA_PAGE    = "com.trigger.trigger_page";

    private int mTriggerType;
    private String mCondition;
    private String mExtra1;
    private String mExtra2;
    private String mId;
    private String mTaskId;

    private ArrayList<Constraint> mConstraints;

    private ImageView mIconView;
    private Holders.Trigger mHolder;

    public Trigger() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot instantiate trigger through default constructor");
    }

    public Trigger(int type, String condition, String extra1, String extra2) {
        mId = null;
        mTaskId = null;
        mTriggerType = type;
        mCondition = condition;
        mExtra1 = extra1;
        mExtra2 = extra2;
        mConstraints = new ArrayList<Constraint>();
    }

    public Trigger(String id, int type, String condition, String extra1, String extra2, String taskId) {
        mId = id;
        mTaskId = taskId;
        mTriggerType = type;
        mCondition = condition;
        mExtra1 = extra1;
        mExtra2 = extra2;
        mConstraints = new ArrayList<Constraint>();
    }

    public Trigger(Parcel source) {
        mId = source.readString();
        mTriggerType = source.readInt();
        mCondition = source.readString();
        mExtra1 = source.readString();
        mExtra2 = source.readString();
        mTaskId = source.readString();
        if (mConstraints == null) {
            mConstraints = new ArrayList<Constraint>();
        }
        source.readTypedList(mConstraints, Constraint.CREATOR);
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTaskId() {
        return mTaskId;
    }

    public void setTaskId(String id) {
        mTaskId = id;
    }

    public int getType() {
        return mTriggerType;
    }

    public void setType(int type) {
        mTriggerType = type;
    }

    public String getCondition() {
        return mCondition;
    }

    public void setCondition(String condition) {
        mCondition = condition;
    }

    public ArrayList<Constraint> getConstraints() {
        return (mConstraints != null) ? mConstraints : new ArrayList<Constraint>();
    }

    public void addConstraint(Constraint constraint) {
        if (mConstraints == null) {
            mConstraints = new ArrayList<Constraint>();
        }

        if (!mConstraints.contains(constraint)) {
            mConstraints.add(constraint);
        }
    }

    public void removeConstraint(int position) {
        if (mConstraints != null) {
            mConstraints.remove(position);
        }
    }

    public void removeConstraint(Constraint constraint) {
        if (mConstraints != null) {
            mConstraints.remove(constraint);
        }
    }

    public void setConstraints(ArrayList<Constraint> constraints) {
        mConstraints = constraints;
    }

    public String getExtra(int which) {
        switch (which) {
            case 1:
                return mExtra1;
            case 2:
                return mExtra2;
            default:
                return "";
        }
    }

    public void setExtra(int which, String value) {
        switch (which) {
            case 1:
                mExtra1 = value;
                break;
            case 2:
                mExtra2 = value;
                break;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeInt(mTriggerType);
        dest.writeString(mCondition);
        dest.writeString(mExtra1);
        dest.writeString(mExtra2);
        dest.writeString(mTaskId);
        dest.writeTypedList(mConstraints);
    }

    public static final Creator<Trigger> CREATOR = new Creator<Trigger>() {
        @Override
        public Trigger createFromParcel(Parcel source) {
            return new Trigger(source);
        }

        @Override
        public Trigger[] newArray(int size) {
            return new Trigger[size];
        }
    };

    public int getIcon(Trigger item) {
        return TaskTypeItem.getIconFromType(item.getType());
    }

    public int getIcon() {
        return TaskTypeItem.getIconFromType(getType());
    }

    public View getView(Trigger item, Context context, View convertView) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_trigger, null);
            mHolder = new Holders.Trigger(convertView);
            convertView.setTag(mHolder);
        } else {
            mHolder = (Holders.Trigger) convertView.getTag();
        }


        String label = "";
        String key1 = "";
        String key2 = "";

        if ((item.getExtra(1) != null) && (!item.getExtra(1).isEmpty())) {
            key1 = item.getExtra(1);
        }

        if ((item.getExtra(2) != null) && (!item.getExtra(2).isEmpty())) {
            key2 = item.getExtra(2);
        }

        if ((item.getCondition() != null) && (getNameIdFromCondition() != 0)) {
            label = context.getString(getNameIdFromCondition()) + " ";
            switch (getType()) {
                case TaskTypeItem.TASK_TYPE_GEOFENCE:
                case TaskTypeItem.TASK_TYPE_CALENDAR:
                    // Do nothing
                    break;
                case TaskTypeItem.TASK_TYPE_NOTIFICATION:
                    try {
                        EventConfiguration configuration = EventConfiguration.deserializeExtra(item.getExtra(1));
                        if (!TextUtils.isEmpty(configuration.name)) {
                            label += configuration.name_match_type == EventConfiguration.MATCH_TYPE_CONTAINS ? context.getString(R.string.contains) : context.getString(R.string.matches) + " " + configuration.name;
                        }
                        if (!TextUtils.isEmpty(configuration.description)) {
                            label += configuration.description_match_type == EventConfiguration.MATCH_TYPE_CONTAINS ?
                                    context.getString(R.string.contains) : context.getString(R.string.matches) + " " + configuration.description;
                        }
                    } catch (Exception e) {  }
                    break;
                case TaskTypeItem.TASK_TYPE_AGENT:
                    // Key 1 is a GUID
                    label = String.format(label, AgentAction.getNameFromGuid(context, key1));
                    break;
                default:
                    label += (!key1.isEmpty()) ? key1 : key2;
                    break;
            }
        } else if (!key1.isEmpty()) {

            switch(getType()) {
                case TaskTypeItem.TASK_TYPE_TIME:
                    // Key 1 is HH:MM (24 hr)
                    // Key 2 is days 1-7
                    try {
                        Date t = TimeTask.time_24.parse(key1);
                        label = TimeTask.time_12.format(t);
                    } catch (Exception e) {
                        label = key1;
                    }

                    // Iterate over day string and convert ints to days if applicable

                    if (!key2.isEmpty()) {
                        label += " " + TimeTask.getDayStringFromStoredValue(context, key2);
                    }
                    break;
                default:
                    label = key1 + ": " + key2;
            }
        } else {
            label = TaskTypeItem.getTaskName(context, item.getType());
        }

        mHolder.setIcon(TaskTypeItem.getIconFromType(item.getType()));
        mHolder.setLabel(label);

        if (getConstraints().size() == 0) {
            mHolder.hideConstraintsContainer();
        } else {
            mHolder.showConstraintsContainer();
            mHolder.hideAll();
            mHolder = Constraint.populateTriggerHolderViews(context, mHolder, getConstraints());
        }

        return convertView;
    }

    public void loadConstraints(Context context) {
        Cursor c = DatabaseHelper.loadTriggerConstraints(context, getId());
        mConstraints = new ArrayList<Constraint>();
        if (c != null) {
            while (c.moveToNext()) {
                Logger.d("Calling load constraints");
                mConstraints.add(Constraint.loadFromCursor(c));
            }
            c.close();
        }
    }

    public static Trigger loadFromCursor(Cursor c) {
        return loadFromCursor(c, "");
    }

    public static Trigger loadFromCursor(Cursor c, String prefix) {
        // Trigger(String id, int type, String condition, String extra1, String extra2, String taskId)    
        return new Trigger(
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_ID)),
                c.getInt(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TRIGGER_TYPE)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TRIGGER_CONDITION)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_KEY_1)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_KEY_2)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TRIGGER_TASK))
        );
    }

    public static Trigger getNfcTrigger() {
        return new Trigger(TaskTypeItem.TASK_TYPE_NFC, DatabaseHelper.TRIGGER_NO_CONDITION, null, null);
    }

    public int getNameIdFromCondition() {
        if (DatabaseHelper.TRIGGER_BATTERY_GOES_ABOVE.equals(getCondition())) {
            return R.string.goes_above;
        } else if (DatabaseHelper.TRIGGER_BATTERY_GOES_BELOW.equals(getCondition())) {
            return R.string.goes_below;
        } else if (DatabaseHelper.TRIGGER_ENTER.equals(getCondition())) {
            return R.string.when_entering;
        } else if (DatabaseHelper.TRIGGER_EXIT.equals(getCondition())) {
            return R.string.when_exiting;
        } else if (DatabaseHelper.TRIGGER_ON_CONNECT.equals(getCondition())) {
            return R.string.when_connected_to;
        } else if (DatabaseHelper.TRIGGER_ON_DISCONNECT.equals(getCondition())) {
            return R.string.when_disconnected_from;
        } else if (DatabaseHelper.TRIGGER_CHARGER_ON.equals(getCondition())) {
            return R.string.when_charger_connected;
        } else if (DatabaseHelper.TRIGGER_CHARGER_OFF.equals(getCondition())) {
            return R.string.when_charger_disconnected;
        } else if (DatabaseHelper.TRIGGER_WIRELESS_CHARGER.equals(getCondition())) {
            return R.string.when_wireless_charger_connected;
        } else if (DatabaseHelper.TRIGGER_CALENDAR_START.equals(getCondition())) {
            return R.string.when_event_starts;
        } else if (DatabaseHelper.TRIGGER_CALENDAR_END.equals(getCondition())) {
            return R.string.when_event_ends;
        } else if (DatabaseHelper.TRIGGER_AGENT_STARTS.equals(getCondition())) {
            return R.string.agent_condition_starts;
        }else if (DatabaseHelper.TRIGGER_AGENT_ENDS.equals(getCondition())) {
            return R.string.agent_condition_ends;
        }

        return 0;
    }

    public boolean constraintsSatisfied(Context context) {
        Logger.d("Checking constraints for " + getId());
        if (getConstraints().size() == 0) {
            loadConstraints(context);
        }

        Logger.d("Task has " + getConstraints().size() + " constraints");

        boolean satisfied = true;
        for (Constraint c : getConstraints()) {
            satisfied &= c.isConstraintSatisfied(context);
        }
        Logger.d("Constraints satisfied : " + satisfied);
        return satisfied;
    }

    public void delete(Context context) {
        if (context == null) return;
        context.getContentResolver().delete(TaskProvider.Contract.TRIGGERS, DatabaseHelper.FIELD_ID + "=?", new String[]{getId()});
        switch (getType()) {
            case TaskTypeItem.TASK_TYPE_GEOFENCE:
                // Remove from Geofences table as well
                context.getContentResolver().delete(TaskProvider.Contract.GEOFENCE, DatabaseHelper.FIELD_ID + "=?", new String[]{getId()});
                break;
            case TaskTypeItem.TASK_TYPE_TIME:
                // Cancel any outstanding alarm for this task
                TimeTask time = DatabaseHelper.getTimeTaskFromId(context, getId());
                if (time != null) {
                    Logger.i("Cancelling task " + time.getId());
                    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    manager.cancel(time.getIntent(context));
                }
                break;
            case TaskTypeItem.TASK_TYPE_CALENDAR:
                // TODO: test this.  does this fire and appropriately cancel?
                CalendarTrigger.scheduleNextEvent(context);

        }
    }
}
