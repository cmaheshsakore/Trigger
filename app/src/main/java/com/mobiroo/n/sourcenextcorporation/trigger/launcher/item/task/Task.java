package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.Lists;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Task implements Parcelable {
    
    public static final int SECONDARY_ID_PADDING = 20000;
    
    private String mName;
    private String mId;
    private String mSecondaryId;
    private String mSecondaryName;
    private String mCommands;
    private String mLastUsedDate;
    private ArrayList<SavedAction> mActions;
    private boolean mEnabled;
    private boolean mHasLoaded;

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    
    private Task(Parcel in)
    {
        mId = in.readString();
        mName = in.readString();
        mLastUsedDate = in.readString();
        mSecondaryId = in.readString();
        mSecondaryName = in.readString();
        mEnabled = in.readByte() != 0;
        if (mActions == null) {
            mActions = new ArrayList<SavedAction>();
        }
        in.readTypedList(mActions, SavedAction.CREATOR);     
    }
    
    public Task() {
        mName = "";
        mId = "";
        mCommands = "";
        mLastUsedDate = "";
        mSecondaryId = "";
        mSecondaryName = "";
        mActions = new ArrayList<SavedAction>();
        mEnabled = true;
    }

    public Task(String id, String name, String date, ArrayList<SavedAction> actions) {
        mId = id;
        mName = name;
        mSecondaryId = "";
        mSecondaryName = "";
        mLastUsedDate = date;
        mActions = (actions != null) ? actions : new ArrayList<SavedAction>();
        mEnabled = true;
    }
    
    public Task(String id, String name, String date, String alt_id, String alt_name, ArrayList<SavedAction> actions) {
        mId = id;
        mName = Utils.decodeData(name);
        mLastUsedDate = date;
        mSecondaryId = (alt_id == null) ? "" : alt_id;
        mSecondaryName = (alt_name == null) ? "" : alt_name;
        mActions = (actions != null) ? actions : new ArrayList<SavedAction>();
        mEnabled = true;
    }
    
    public Task(String id, String name, String date, String alt_id, String alt_name, int enabled, ArrayList<SavedAction> actions) {
        mId = id;
        mName = Utils.decodeData(name);
        mLastUsedDate = date;
        mSecondaryId = (alt_id == null) ? "" : alt_id;
        mSecondaryName = (alt_name == null) ? "" : alt_name;
        mActions = (actions != null) ? actions : new ArrayList<SavedAction>();
        mEnabled = (enabled != 0);
    }
    
    public int getPayloadSize(boolean includeName, boolean buildForSwitch) {
        try {
            return buildPayloadString(includeName, buildForSwitch).getBytes("US-ASCII").length;
        } catch (UnsupportedEncodingException e) {
            Logger.e(Constants.TAG, "Exception thrown build payload", e);
            return 0;
        }
        
    }
    
    public String buildPayloadString(boolean includeName, boolean buildForSwitch) {

        /* Payload is of the form ID:Name:Args */
        StringBuilder builder = new StringBuilder();
        if (!buildForSwitch) {
            builder.append(Constants.COMMAND_TAG_ID + ":");
        }
        builder.append(mId);
        if (includeName) {
            if (mName == null) { mName = "Task"; }
            if (buildForSwitch) {
                builder.append(":" + Constants.COMMAND_TAG_NAME + ":" + Utils.encodeData(mName));
            } else {
                builder.append(":" + Utils.encodeData(mName));
            }
        }
        int numActions = 0;
        for (SavedAction action: mActions) {
            if ((numActions == 0) && (buildForSwitch)) {
                builder.append(":" + action.getMessage());  
                numActions++;
            }
            else {
                builder.append(";" + action.getMessage());
            }
        }
        Logger.d("Payload is " + builder.toString());
        return builder.toString();
    }

    public String getId() {
        return mId;
    }
    
    public void setId(String id) {
        mId = id;
    }
    
    public boolean isSecondTagNameValid() {
        if ((mSecondaryName != null) && (!mSecondaryName.isEmpty())) {
            return true;
        }
        
        return false;
    }
    
    public String getFullName() {
        String name = mName;
        if (isSecondTagNameValid()) {
            name += " - " + mSecondaryName;
        }
        return name;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public String getSecondaryId() {
        return mSecondaryId;
    }
    
    public String getSecondaryName() {
        return mSecondaryName;
    }

    public void setSecondaryId(String id) { mSecondaryId = id; }

    public void setSecondaryName(String name) { mSecondaryName = name; }

    public String getCommands() {
        return mCommands;
    }
   
    public String getLastUsedDate() {
        return mLastUsedDate;
    }
    
    public ArrayList<SavedAction> getActions() {
        return mActions;
    }
    
    public void addAction(SavedAction action) {
        if (mActions == null) {
            mActions = new ArrayList<SavedAction>();
        }
        mActions.add(action);
    }
    
    public void setActions(ArrayList<SavedAction> actions) {
        mActions = actions;
    }

    public static boolean isStringValid(String value) {
        return ((value != null) && (!value.isEmpty()));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
    
    public void setEnabledAndWriteChange(Context context, boolean enabled) {
        mEnabled = enabled;
        DatabaseHelper.updateEnabledStatus(context, this, enabled);
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mLastUsedDate);
        dest.writeString((mSecondaryId == null) ? "" : mSecondaryId);
        dest.writeString((mSecondaryName == null) ? "" : mSecondaryName);
        dest.writeByte((byte) (mEnabled ? 1: 0));
        dest.writeTypedList(mActions);
    }
    
    public static ArrayList<Task> loadFromCursor(Cursor c) {
        return loadFromCursor(c, "");
    }
    
    public static ArrayList<Task> loadFromCursor(Cursor c, String prefix) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        
        String secondary = c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_TWO_ID));
        
        tasks.add(new Task(
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_ID)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_NAME)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_LAST_ACCESSED)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_TWO_ID)),
                c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_TWO_NAME)),
                c.getInt(c.getColumnIndex(prefix + DatabaseHelper.FIELD_ENABLED)),
                null));
    
        if ((secondary != null) && (!secondary.isEmpty())) {
            tasks.add(new Task(
                    c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_TWO_ID)),
                    c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_TWO_NAME)),
                    c.getString(c.getColumnIndex(prefix + DatabaseHelper.FIELD_TASK_LAST_ACCESSED)),
                    null,
                    null,
                    null));
        }
        
        return tasks;
    }
    
    public void loadActions(Context context) {
        ArrayList<SavedAction> actions = Lists.newArrayList();

        Cursor cursor = null;
        try { cursor = context.getContentResolver().query(TaskProvider.Contract.ACTIONS, new String[] {DatabaseHelper.FIELD_ACTIVITY, DatabaseHelper.FIELD_DESCRIPTION}, DatabaseHelper.FIELD_TAG_ID + "=?", new String[] { getId() }, null); }
        catch (Exception e) { Logger.e("Exception loading actions: " + e, e); }

        if (cursor != null && cursor.moveToFirst()) {

            do {
                String actionMessage = cursor.getString(0);
                String actionPretty  = cursor.getString(1);
                actions.add(new SavedAction(actionMessage, actionPretty, "", ""));
            } while (cursor.moveToNext());
            cursor.close();
        }

        setActions(actions);
    }
    
    public static String getPayload(Context context, String id, String name) {
        Task tag = Task.loadTask(context, id, name, "");
        return tag.buildPayloadString(true, false);
    }
    
    public static Task loadTask(Context context, String id, String name, String date) {
        // For some reason we're seeing the following error here sometimes:
        // unable to open database file (code 14) 
        // android.database.sqlite.SQLiteCantOpenDatabaseException
        ArrayList<SavedAction> actions = Lists.newArrayList();
        Cursor cursor = context.getContentResolver().query(TaskProvider.Contract.ACTIONS, new String[] {DatabaseHelper.FIELD_ACTIVITY, DatabaseHelper.FIELD_DESCRIPTION}, DatabaseHelper.FIELD_TAG_ID + "=?", new String[] {id}, null);
        if (cursor.moveToFirst()) {
            do {
                String actionMessage = cursor.getString(0);
                String actionPretty  = cursor.getString(1);
                actions.add(new SavedAction(actionMessage, actionPretty, "", ""));
            } while (cursor.moveToNext());
        }
        
        return new Task(id, name, date, actions);
    }
    
    public void delete(Context context) {
        if (context != null) {
            context.getContentResolver().delete(TaskProvider.Contract.ACTIONS, DatabaseHelper.FIELD_TAG_ID + "=?", new String[]{getId()});
            context.getContentResolver().delete(TaskProvider.Contract.TASKS, DatabaseHelper.FIELD_TASK_ID  + "=?", new String[]{getId()});
        }
    }
    
    public static int generateSecondaryId(Task primary) {
        return generateSecondaryId(Integer.parseInt(primary.getId()));
    }
    
    public static int generateSecondaryId(int id) {
        return id + SECONDARY_ID_PADDING;
    }

    public boolean hasLoaded() {
        return mHasLoaded;
    }

    public void setLoaded() {
        mHasLoaded = true;
    }
}
