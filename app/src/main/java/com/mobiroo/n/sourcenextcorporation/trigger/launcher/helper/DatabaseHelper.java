package com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.google.android.gms.location.Geofence;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ActivityFeedItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SavedAction;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.TaskTypeItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Trigger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers.TaskProvider;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TaskSet;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.TimeTask;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.constraint.Constraint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }
    
    private static final int DATABASE_VERSION = 38;
    private static final String DATABASE_NAME = "TagStorage";
    
    /* Trigger Task ields for setting and retrieving data */
    public static final String TYPE_WIFI        = String.valueOf(TaskTypeItem.TASK_TYPE_WIFI);
    public static final String TYPE_BLUETOOTH   = String.valueOf(TaskTypeItem.TASK_TYPE_BLUETOOTH);
    public static final String TYPE_NFC_TAG     = String.valueOf(TaskTypeItem.TASK_TYPE_NFC);
    public static final String TYPE_GEOFENCE    = String.valueOf(TaskTypeItem.TASK_TYPE_GEOFENCE);
    public static final String TYPE_BATTERY     = String.valueOf(TaskTypeItem.TASK_TYPE_BATTERY);
    public static final String TYPE_TIME        = String.valueOf(TaskTypeItem.TASK_TYPE_TIME);
    
    public static final String DAY_MONDAY       = "m";
    public static final String DAY_TUESDAY      = "t";
    public static final String DAY_WEDNESDAY    = "w";
    public static final String DAY_THURSDAY     = "h";
    public static final String DAY_FRIDAY       = "f";
    public static final String DAY_SATURDAY     = "s";
    public static final String DAY_SUNDAY       = "u";
    
    public static final String TRIGGER_ON_CONNECT       = "c";
    public static final String TRIGGER_ON_DISCONNECT    = "d";
    public static final String TRIGGER_ENTER            = "e";
    public static final String TRIGGER_EXIT             = "x";
    public static final String TRIGGER_NO_CONDITION     = "n";
    public static final String TRIGGER_BATTERY_GOES_ABOVE = "f";
    public static final String TRIGGER_BATTERY_GOES_BELOW = "g";
    public static final String TRIGGER_TIME             = "h";
    public static final String TRIGGER_CHARGER_ON       = "i";
    public static final String TRIGGER_WIRELESS_CHARGER = "j";
    public static final String TRIGGER_CHARGER_OFF      = "k";
    public static final String TRIGGER_CHARGER_ON_AC    = "l";
    public static final String TRIGGER_CHARGER_ON_USB   = "m";
    public static final String TRIGGER_CALENDAR_START   = "p";
    public static final String TRIGGER_CALENDAR_END     = "o";
    public static final String TRIGGER_AGENT_STARTS = "q";
    public static final String TRIGGER_AGENT_ENDS = "r";

    public static final String CONDITION_ENABLED    = "e";
    public static final String CONDITION_DISABLED   = "d";
        
    public static final String FIELD_TRIGGER_CONDITION  = "condition";
    public static final String FIELD_TRIGGER_TYPE       = "trigger";
    public static final String FIELD_TIMER_TIME         = "time";
    public static final String FIELD_TIMER_DAY          = "day";
    public static final String FIELD_PAYLOAD            = "payload";
    public static final String FIELD_NAME               = "name";
    public static final String FIELD_ENABLED            = "enabled";
    public static final String FIELD_ID                 = "Id";
    public static final String FIELD_KEY_1              = "key1";
    public static final String FIELD_KEY_2              = "key2";
    public static final String FIELD_SSID               = FIELD_KEY_1;
    public static final String FIELD_BLUETOOTH_NAME     = FIELD_KEY_1;
    public static final String FIELD_BLUETOOTH_MAC      = FIELD_KEY_2;
    public static final String FIELD_BATTERY_LEVEL      = FIELD_KEY_1;
    public static final String FIELD_TAG_ID             = "TagId";
    
    public static final String FIELD_TASK_ID            = "ID";
    public static final String FIELD_TASK_TWO_ID        = "secondary_id";
    public static final String FIELD_TASK_NAME          = "Name";
    public static final String FIELD_TASK_TWO_NAME      = "secondary_name";
    public static final String FIELD_TASK_LAST_USED     = "LastUsed";
    public static final String FIELD_TASK_LAST_ACCESSED = "LastAccessed";
    public static final String FIELD_TASK_SHARE_ID      = "ShareId";
    public static final String FIELD_ACTIVITY           = "Activity";
    public static final String FIELD_DESCRIPTION        = "Description";
    public static final String FIELD_UUID               = "UUID";
    public static final String FIELD_PAYLOAD_CAP        = "Payload";
    public static final String FIELD_TOTAL_ACTIONS      = "TotalActions";
    public static final String FIELD_DATE               = "date";
    public static final String FIELD_TAG                = "Tag";
    public static final String FIELD_LAST_EXECUTED      = "LastExecuted";
    public static final String FIELD_TIME_EXECUTED      = "TimeExecuted";
    public static final String FIELD_LATITUDE           = "latitude";
    public static final String FIELD_LONGITUDE          = "longitude";
    public static final String FIELD_RADIUS             = "radius";
    public static final String FIELD_TRANSITION         = "transition";
    public static final String FIELD_TRIGGER_TASK       = "taskId";
    public static final String FIELD_TRIGGER_ID         = "triggerId";
    public static final String FIELD_TYPE               = "type";
        
    /* Table names */ 
    public static final String TABLE_SAVED_TASKS        = "TagInfo";
    public static final String TABLE_SAVED_TASK_ACTIONS = "TagActivities";
    public static final String TABLE_LOCAL_MAPPING      = "TagMappings";
    public static final String TABLE_TAG_TIMEOUTS       = "TagLimiting";
    public static final String TABLE_STATS              = "TagStats";
    public static final String TABLE_GEOFENCES          = "Geofences";
    public static final String TABLE_ACTIVITY_FEED      = "ActivityFeed";
    public static final String TABLE_TRIGGERS           = "Triggers";
    public static final String TABLE_CONSTRAINTS        = "Constraints";
    
    public static final String TAG_LIMITING_SINGLE      = "SINGLE";

    private static final String CREATE_CONSTRAINTS_TABLE = 
            "CREATE TABLE " + TABLE_CONSTRAINTS + " ( " +
             FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
             FIELD_TRIGGER_ID + " INTEGER, " +
             FIELD_KEY_1 + " TEXT, " +
             FIELD_KEY_2 + " TEXT, " +
             FIELD_TYPE + " TEXT " +
             ");";
    
    private static final String CREATE_TRIGGERS_TABLE = 
            "CREATE TABLE " + TABLE_TRIGGERS + " (" +
             FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
             FIELD_TRIGGER_TYPE + " INTEGER," +
             FIELD_TRIGGER_CONDITION + " TEXT," +
             FIELD_KEY_1 + " TEXT," +
             FIELD_KEY_2 + " TEXT," + 
             FIELD_TRIGGER_TASK + " INTEGER" + 
             ");";
    
    private static final String CREATE_SAVED_TASKS_TABLE = 
            "CREATE TABLE " + TABLE_SAVED_TASKS + " (" + 
            FIELD_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            FIELD_TASK_TWO_ID + " INTEGER, " +
            FIELD_TASK_NAME + " TEXT, " +
            FIELD_TASK_TWO_NAME + " TEXT, " + 
            FIELD_TASK_LAST_USED + " TEXT, " +
            FIELD_TASK_LAST_ACCESSED + " TEXT,  " +
            FIELD_TASK_SHARE_ID + " TEXT, " +
            FIELD_KEY_1 + " TEXT," + // Unused but will be used for restoring old backups
            FIELD_KEY_2 + " TEXT," + // Unused but will be used for restoring old backups
            FIELD_TRIGGER_TYPE + " INTEGER," + // Unused but will be used for restoring old backups
            FIELD_TRIGGER_CONDITION + " TEXT, " + // Unused but will be used for restoring old backups
            FIELD_ENABLED + " INTEGER" +
            ");";

    private static final String CREATE_SAVED_TAGS_ACTIONS_TABLE = 
            "CREATE TABLE " + TABLE_SAVED_TASK_ACTIONS + " (" + 
            FIELD_TAG_ID + " INTEGER, " +
            FIELD_ACTIVITY + " TEXT, " +
            FIELD_DESCRIPTION + " Text" +
            ");";

    private static final String CREATE_TAG_LIMITING_TABLE = 
            "CREATE TABLE " + TABLE_TAG_TIMEOUTS + " (" + 
            FIELD_TAG + " TEXT, " + 
            FIELD_LAST_EXECUTED + " TEXT, " + 
            FIELD_TIME_EXECUTED + " TEXT" + 
            ")";

    private static final String CREATE_TAG_STATS_TABLE = 
            "CREATE TABLE " + TABLE_STATS + " (" + 
            FIELD_TOTAL_ACTIONS + " INTEGER, " +
            FIELD_DATE + " TEXT" +
            ")";
    
    private static final String CREATE_LOCAL_TAG_MAPPING_TABLE = 
            "CREATE TABLE " + TABLE_LOCAL_MAPPING + " (" + 
            FIELD_UUID + " TEXT, " + 
            FIELD_PAYLOAD_CAP + " TEXT" +
            ")";
    
    private static final String CREATE_GEOFENCES_TABLE = 
            "CREATE TABLE " + TABLE_GEOFENCES + " (" +
                    FIELD_ID + " TEXT," +
                    FIELD_LATITUDE + " TEXT, " +
                    FIELD_LONGITUDE + " TEXT, " +
                    FIELD_RADIUS + " TEXT, " +
                    FIELD_TRANSITION + " INTEGER"
                    + ")";
    
    private static final String CREATE_ACTIVITY_FEED_TABLE = 
            "CREATE TABLE " + TABLE_ACTIVITY_FEED + " (" +
                    FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                    FIELD_NAME + " TEXT, " +
                    FIELD_TRIGGER_TYPE + " INTEGER, " + 
                    FIELD_LAST_EXECUTED + " TEXT" +
                    ")";
                    
    /* Table Creation */
    
    public static final String[] tables = new String[] { 
        TABLE_SAVED_TASKS, 
        TABLE_SAVED_TASK_ACTIONS,  
        TABLE_TAG_TIMEOUTS, 
        TABLE_STATS, 
        TABLE_LOCAL_MAPPING,
        TABLE_GEOFENCES,
        TABLE_TRIGGERS,
        TABLE_CONSTRAINTS
    };
    
    public static final String[] FIELDS_CONSTRAINTS = new String[] { 
        FIELD_ID,
        FIELD_TRIGGER_ID,
        FIELD_KEY_1,
        FIELD_KEY_2,
        FIELD_TYPE
    };
    
    public static final String[] FIELDS_TASK = new String[] { 
        FIELD_TASK_ID, 
        FIELD_TASK_TWO_ID,
        FIELD_TASK_NAME, 
        FIELD_TASK_TWO_NAME,
        FIELD_TASK_LAST_USED,
        FIELD_TASK_LAST_ACCESSED,
        FIELD_TASK_SHARE_ID,
        FIELD_ENABLED
    };
    
    public static final String[] FIELDS_TASK_OLD = new String[] { 
        FIELD_TASK_ID, 
        FIELD_TASK_NAME, 
        FIELD_TASK_LAST_USED,
        FIELD_TASK_LAST_ACCESSED,
        FIELD_TASK_SHARE_ID,
        FIELD_KEY_1,
        FIELD_KEY_2,
        FIELD_TRIGGER_TYPE,
        FIELD_TRIGGER_CONDITION
    };
    
    public static final String[] FIELGS_TRIGGERS = new String[] {
        FIELD_ID,
        FIELD_TRIGGER_TYPE,
        FIELD_TRIGGER_CONDITION,
        FIELD_KEY_1,
        FIELD_KEY_2, 
        FIELD_TRIGGER_TASK, 
    };
    
    public static final String[] FIELDS_GEOFENCES = new String[] {
        FIELD_ID,
        FIELD_LATITUDE,
        FIELD_LONGITUDE,
        FIELD_RADIUS,
        FIELD_TRANSITION
    };
    
    public static final String[] FIELDS_ACTIVITY_FEED = new String[] {
        FIELD_ID,
        FIELD_NAME,
        FIELD_TRIGGER_TYPE,
        FIELD_LAST_EXECUTED
    };
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SAVED_TASKS_TABLE);
        db.execSQL(CREATE_SAVED_TAGS_ACTIONS_TABLE);
        db.execSQL(CREATE_TAG_LIMITING_TABLE);
        db.execSQL(CREATE_TAG_STATS_TABLE);
        db.execSQL(CREATE_LOCAL_TAG_MAPPING_TABLE);
        db.execSQL(CREATE_GEOFENCES_TABLE);
        db.execSQL(CREATE_ACTIVITY_FEED_TABLE);
        db.execSQL(CREATE_TRIGGERS_TABLE);
        db.execSQL(CREATE_CONSTRAINTS_TABLE);
    }

    private void execSqlCatchException(SQLiteDatabase db, String sql) {
        execSqlCatchException(db, sql, null);
    }

    private void execSqlCatchException(SQLiteDatabase db, String sql, String customMessage) {
        String message = (customMessage == null || customMessage.isEmpty()) ? "Exception running query" : customMessage;
        
        try {
            db.execSQL(sql); 
        } catch (Exception e) {
            Logger.e(message, e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        Logger.d("Upgrading from " + oldVersion);
        
        if (oldVersion < 23) {
            execSqlCatchException(db, "ALTER TABLE " + TABLE_STATS + " ADD COLUMN " + FIELD_DATE + " TEXT");
        }
        
        if (oldVersion < 27) {
            execSqlCatchException(db, CREATE_GEOFENCES_TABLE);
        }
        
        if (oldVersion < 29) {
            execSqlCatchException(db, CREATE_ACTIVITY_FEED_TABLE);
        }
        
        if (oldVersion < 30) {
            execSqlCatchException(db, CREATE_TRIGGERS_TABLE);            
        }
        
        if (oldVersion < 34) {
            execSqlCatchException(db, CREATE_CONSTRAINTS_TABLE);
        }
        
        if (oldVersion < 37) {
            execSqlCatchException(db, "ALTER TABLE " + TABLE_SAVED_TASKS + " ADD COLUMN " + FIELD_ENABLED + " INTEGER");
        }
        
        if (oldVersion < 38) {
            execSqlCatchException(db, "UPDATE " + TABLE_SAVED_TASKS + " SET " + FIELD_ENABLED + "=1");
        }
    }
    
    public static JSONObject generateDatabaseBackup(SQLiteDatabase db) {

        JSONObject backup = new JSONObject();        
        
        for (int i=0; i< tables.length; i++) {
            JSONArray current = new JSONArray();
            
            Cursor c = db.query(tables[i], null, null, null, null, null, null);
            if (c.moveToFirst()) {
                do {
                    JSONObject row = new JSONObject();
                    // Read all column data and dump it into a JSON Object
                    for (int j=0; j<c.getColumnCount(); j++) {
                        try {
                            row.put(c.getColumnName(j), c.getString(j));
                        } catch (JSONException e) {
                            Logger.e(Constants.TAG, "Exception adding data to backup " + e, e);
                        }
                    }
                    current.put(row);
                } while (c.moveToNext());
            }
            c.close();
            try {
                backup.put(tables[i], current);
            } catch (JSONException e) {
                Logger.e(Constants.TAG, "Exception adding table to backup " + e, e);
            }
        }
        return backup;
    }
    
    public static void insertBackupItem(SQLiteDatabase db, String table, ContentValues values) {
        db.insert(table, null, values);   
    }

    public static void deleteBackedUpTables(SQLiteDatabase db) {
        for (int i=0; i< tables.length; i++) {
            db.delete(tables[i], null, null);
        }
    }
    
    public static Cursor getAllTasks(Context context, String[] fields) {
        Cursor c = null;
        try { 
            c = context.getContentResolver().query(TaskProvider.Contract.TASKS, fields, null, null, null);
        } catch (Exception e) {
            Logger.e("Exception querying all tasks", e);
        }
        return c;
    }
    
    public static Cursor getAllTasks(Context context) {
        Cursor c = null;
        try { 
            c = context.getContentResolver().query(TaskProvider.Contract.TASKS, FIELDS_TASK, null, null, null);
        } catch (Exception e) {
            Logger.e("Exception querying all tasks", e);
        }
        return c;
    }

    public static TaskSet getNfcTasksForId(Context context, String id) {
        return getTaskSetByTaskId(context, TaskTypeItem.TASK_TYPE_NFC, id);
    }

    public static ArrayList<TaskSet> getWifiTasksForSSID(Context context, String ssid) {
        return getFilteredTasksForExtraValue(context, TaskTypeItem.TASK_TYPE_WIFI, 1, ssid);
    }

    public static ArrayList<TaskSet> getWifiTasksForSSID(Context context, String...ssids) {
        return getFilteredTasksForExtraValue(context, TaskTypeItem.TASK_TYPE_WIFI, 1, ssids);
    }
    
    public static ArrayList<TaskSet> getAllWifiTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_WIFI);
    }
    
    public static ArrayList<TaskSet> getBluetoothTasksForDeviceName(Context context, String name) {
        return getFilteredTasksForExtraValue(context, TaskTypeItem.TASK_TYPE_BLUETOOTH, 1, name);
    }

    public static ArrayList<TaskSet> getBluetoothTasksForDeviceName(Context context, String...names) {
        return getFilteredTasksForExtraValue(context, TaskTypeItem.TASK_TYPE_BLUETOOTH, 1, names);
    }
    
    public static ArrayList<TaskSet> getBluetoothTasksForDeviceMac(Context context, String mac) {
        return getFilteredTasksForExtraValue(context, TaskTypeItem.TASK_TYPE_BLUETOOTH, 2, mac);
    }
    
    public static ArrayList<TaskSet> getAllBluetoothTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_BLUETOOTH);
    }
    
    public static ArrayList<TaskSet> getBatteryTasksForLevel(Context context, int level) {
        return getFilteredTasksForExtraValue(context, TaskTypeItem.TASK_TYPE_BATTERY, 1, String.valueOf(level));
    }
    
    public static ArrayList<TaskSet> getAllBatteryTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_BATTERY);
    }

    public static ArrayList<TaskSet> getAllChargingTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_CHARGER);
    }

    public static ArrayList<TaskSet> getTimeTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_TIME);
    }
    
    public static ArrayList<TaskSet> getChargerTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_CHARGER);
    }
    
    public static ArrayList<TaskSet> getHeadsetTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_HEADSET);
    }

    public static ArrayList<TaskSet> getAgentTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_AGENT);
    }

    public static ArrayList<TaskSet> getAllCalendarTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_CALENDAR);
    }

    public static ArrayList<TaskSet> getAllNotificationTasks(Context context) {
        return getTasksForType(context, TaskTypeItem.TASK_TYPE_NOTIFICATION);
    }

    public static TaskSet getTaskSetByTaskId(Context context, int type, String id) {
        ArrayList<TaskSet> sets = getTasksForType(context, type);
        for (TaskSet set: sets) {
            set.getTasks();
            if ((set.getTask(0) != null) && (set.getTask(0).getId().equals(String.valueOf(id)))) {
                Logger.d("SEARCH: Found matching task " + set.getTask(0).getId());
                return set;
            }
        }
        Logger.d("SEARCH: Missed on comparison, returning empty task set");
        return new TaskSet();
    }

    public static TaskSet getTaskSetByTriggerId(Context context, int type, String id) {
        ArrayList<TaskSet> sets = getTasksForType(context, type);
        for (TaskSet set: sets) {
            if ((set.getTrigger(0) != null) && (id.equals(set.getTrigger(0).getId()))) {
                Logger.d("SEARCH: Found matching task " + set.getTask(0).getId());
                return set;
            }
        }
        Logger.d("SEARCH: Missed on comparison, returning empty task set");
        return new TaskSet();
    }
    
    public static ArrayList<TimeTask> getTimeTasksList(Context context) {
        ArrayList<TimeTask> list = new ArrayList<TimeTask>();
        ArrayList<TaskSet> sets = getTimeTasks(context);
        if ((sets != null) && (sets.size() > 0)) {
            for (TaskSet set: sets) {
                Trigger trigger = set.getTrigger(0);
                Task task = set.getTask(0);
                if ((trigger != null) && (task != null)) {
                    list.add(new TimeTask(
                            trigger.getId(),
                            trigger.getExtra(1),
                            trigger.getExtra(2),
                            task.getId())
                            );
                }
            }
        }
        return list;
    }
    
    public static TimeTask getTimeTaskFromId(Context context, String id) {
        TimeTask time = null;
        ArrayList<TaskSet> sets = getTimeTasks(context);
        if ((sets != null) && (sets.size() > 0)) {
            for (TaskSet set: sets) {
                Trigger trigger = set.getTrigger(0);
                Task task = set.getTask(0);

                if (id.equals(trigger.getId())) {
                    time = new TimeTask(
                            trigger.getId(),
                            trigger.getExtra(1),
                            trigger.getExtra(2),
                            task.getId()
                            );
                }
            }
        }

        return time;
    }

    public static ArrayList<TaskSet> getFilteredTasksForExtraValue(Context context, int taskType, int extra, String...values) {
        ArrayList<TaskSet> output = new ArrayList<TaskSet>();
        for (TaskSet task: getTasksForType(context, taskType)) {
            if (task.getTrigger(0) != null) {
                for (String value: values) {
                    if (value.equals(task.getTrigger(0).getExtra(extra))) {
                        output.add(task);
                        break;
                    }
                }
            }
        }
        return output;
    }

    public static ArrayList<TaskSet> getFilteredTasksForExtraValue(Context context, int taskType, int extra, String value) {
        ArrayList<TaskSet> output = new ArrayList<TaskSet>();
        for (TaskSet task: getTasksForType(context, taskType)) {
            if ((task.getTrigger(0) != null) && (value.equals(task.getTrigger(0).getExtra(extra)))) {
                output.add(task);
            }
        }
        return output;
    }
    
    public static ArrayList<TaskSet> getTasksForType(Context context, int type) {
        ArrayList<TaskSet> tasks = getCompositeTasks(context);
        ArrayList<TaskSet> output = new ArrayList<TaskSet>();
        for (TaskSet task: tasks) {
            if ((task.getTrigger(0) != null) && (task.getTrigger(0).getType() == type)){
                output.add(task);
            }
        }
        
        return output;
    }

    public static void deleteTrigger(Context context, Trigger trigger) {
        context.getContentResolver().delete(TaskProvider.Contract.CONSTRAINT, DatabaseHelper.FIELD_TRIGGER_ID + "=?", new String[]{trigger.getId()});
        context.getContentResolver().delete(TaskProvider.Contract.TRIGGERS, DatabaseHelper.FIELD_TRIGGER_ID + "=?", new String[]{trigger.getId()});
    }
    
    public static void removeConstraintsForTrigger(Context context, Trigger trigger) {
        removeConstraintsForTriggerId(context, trigger.getId());
    }
    
    public static void removeConstraintsForTriggerId(Context context, String id) {
        context.getContentResolver().delete(TaskProvider.Contract.CONSTRAINT, DatabaseHelper.FIELD_TRIGGER_ID + "=?", new String[]{id});
    }
    
    public static void deleteTriggersForTask(Context context, String id) {
        // Get all trigger ids associated with this task
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.TRIGGERS, new String[] { FIELD_ID }, FIELD_TRIGGER_TASK + "=?", new String[] { id }, null);

        if (c != null) {
            while (c.moveToNext()) {
                String triggerId = c.getString(0);
                Logger.d("Deleting constraints for " + triggerId);
                removeConstraintsForTriggerId(context, triggerId);
            }
            c.close();
        }
        context.getContentResolver().delete(TaskProvider.Contract.TRIGGERS, FIELD_TRIGGER_TASK + "=?", new String[] { id });
    }
    
    public static String saveTask(Context context, Task savedTag, int type, String condition, String key1, String key2, boolean saveDefaultDate) {
        return saveTask(context, savedTag, null, saveDefaultDate);
    }
    
    @SuppressWarnings("deprecation")
    public static String saveTask(Context context, Task task, Task secondaryTask,  boolean saveDefaultDate) {
        String id = task.getId();
        Logger.d("Saving ID " + task.getId());
        String name = task.getName();
        Logger.d("Saving name " + task.getName());

        // Find out if this tag already exists in the database
        boolean exists = false;
        if (id != null) {
            Cursor c  = null;
            try {
                c = context.getContentResolver().query(TaskProvider.Contract.TASKS, new String[] { DatabaseHelper.FIELD_TASK_ID, DatabaseHelper.FIELD_TASK_NAME }, DatabaseHelper.FIELD_TASK_ID + "=?", new String[] { id }, null);
                exists = (c != null && c.moveToFirst());
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        int secondaryId = 0;
        
        
        if (exists) {
            Logger.d("Task exists");
            
            secondaryId = Task.generateSecondaryId(Integer.parseInt(id));
 
            // Update name
            ContentValues values = new ContentValues();
            values.put(FIELD_TASK_NAME, name);
            values.put(FIELD_ENABLED, task.isEnabled() ? 1 : 0);
            values.put(FIELD_TASK_TWO_ID, (secondaryTask != null) ? secondaryId : null);
            values.put(FIELD_TASK_TWO_NAME, (secondaryTask != null) ? secondaryTask.getName() : null);

            context.getContentResolver().update(TaskProvider.Contract.TASKS, values, DatabaseHelper.FIELD_TASK_ID + "=?", new String[] { id });

            saveActions(context, task, id);
            
            if (secondaryTask != null) {
                saveActions(context, secondaryTask, String.valueOf(secondaryId));
            }
           
        } else {
            Logger.d("Saving new task");
            
            ContentValues values = new ContentValues();
            values.put(FIELD_TASK_NAME, name);
            values.put(FIELD_ENABLED, 1);
            if (saveDefaultDate) {
                values.put(FIELD_TASK_LAST_USED, new Date().toLocaleString());
            }

            Uri result = context.getContentResolver().insert(TaskProvider.Contract.TASKS, values);
            id = String.valueOf(ContentUris.parseId(result));
            
            task.setId(id);
            
            saveActions(context, task, id);

            if (secondaryTask != null) {

                // Update with secondary task
                secondaryId = Task.generateSecondaryId(Integer.parseInt(id));
                secondaryTask.setId(String.valueOf(secondaryId));
                ContentValues second_values = new ContentValues();
                second_values.put(FIELD_TASK_TWO_ID, secondaryId);
                second_values.put(FIELD_TASK_TWO_NAME, secondaryTask.getName());
                second_values.put(FIELD_ENABLED, 1);
                context.getContentResolver().update(TaskProvider.Contract.TASKS, second_values, DatabaseHelper.FIELD_TASK_ID + "=?", new String[] { id });

                // Save actions for secondary task
                saveActions(context, secondaryTask, String.valueOf(secondaryId));
            }
            
        }
        task.setId(id);
        return id;
    }
    
    public static void updateEnabledStatus(Context context, Task task, boolean enabled) {
        ContentValues values = new ContentValues();
        values.put(FIELD_ENABLED, (enabled) ? 1 : 0);
        context.getContentResolver().update(TaskProvider.Contract.TASKS, values, FIELD_TASK_ID + "=?", new String[]{task.getId()});
    }

     private static void saveActions(Context context, Task task, String id) {
        // Clear existing actions
         context.getContentResolver().delete(TaskProvider.Contract.ACTIONS, DatabaseHelper.FIELD_TAG_ID + "=?", new String[] { id });
        
        // Save new actions
        Logger.d("Called save actions for " + task.getActions().size());
        for (SavedAction action : task.getActions()) {
            Logger.d("Saving action " + action.getMessage() + " for " + id);
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.FIELD_ACTIVITY,    action.getMessage());
            values.put(DatabaseHelper.FIELD_DESCRIPTION, action.getDescription());
            values.put(DatabaseHelper.FIELD_TAG_ID,       id);
            context.getContentResolver().insert(TaskProvider.Contract.ACTIONS, values);
        }
    }

    public static void saveGeofence(Context context, String id, String latitude, String longitude, String radius, int transition) {
        Logger.d("Saving Geofence locally");
        
        ContentValues values = new ContentValues();
        values.put(FIELD_LATITUDE, latitude);
        values.put(FIELD_LONGITUDE, longitude);
        values.put(FIELD_RADIUS, radius);
        values.put(FIELD_TRANSITION, transition);
        
        int rows = context.getContentResolver().update(TaskProvider.Contract.GEOFENCE, values, FIELD_ID + "=?", new String[]{id});
        if (rows < 1) {
            values.put(FIELD_ID, id);
            context.getContentResolver().insert(TaskProvider.Contract.GEOFENCE, values);
        }
        
    }
    
    public static void deleteGeofence(Context context, String id) {
        int rows = context.getContentResolver().delete(TaskProvider.Contract.GEOFENCE, FIELD_ID + "=?", new String[] { id });
        Logger.d("GEO: Removed local geofence with ID " + id + ", Result = " + rows);
    }
    
    public static void deleteAllGeofences(Context context) {
        context.getContentResolver().delete(TaskProvider.Contract.GEOFENCE, null, null);
    }
    
    public static boolean doesAlertExistForTransition(Context context, String id, int transition) {
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.GEOFENCE, new String[] { FIELD_ID }, FIELD_ID + "=? AND " + FIELD_TRANSITION + "=?", new String[] { id, String.valueOf(transition) }, null);
        boolean exists = ((c != null) && (c.moveToFirst()));
        if (c != null) c.close();
        return exists;
    }
    
    public static List<Geofence> getGeofences(Context context) {
        List<Geofence> fences = new ArrayList<Geofence>();
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.GEOFENCE, FIELDS_GEOFENCES, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int transition = c.getInt(c.getColumnIndex(FIELD_TRANSITION));
                    // Force dwell instead of enter.
                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        transition = Geofence.GEOFENCE_TRANSITION_DWELL;
                    }

                    Geofence fence = new Geofence.Builder()
                        .setRequestId(c.getString(c.getColumnIndex(FIELD_ID)))
                        .setCircularRegion(
                                c.getDouble(c.getColumnIndex(FIELD_LATITUDE)), 
                                c.getDouble(c.getColumnIndex(FIELD_LONGITUDE)), 
                                c.getFloat(c.getColumnIndex(FIELD_RADIUS)))
                        .setTransitionTypes(transition)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setNotificationResponsiveness(60 * 1000)
                        .setLoiteringDelay(GeofenceClient.DWELL_DURATION)
                        .build();
                    Logger.d("Adding fence at " + fence.getRequestId());
                    
                    fences.add(fence);
                } while (c.moveToNext());
            }
            c.close();
        }
        return fences;
    }
    
    public static void storeActivityFeedEntry(Context context, ActivityFeedItem item) {
        ContentValues values = new ContentValues();
        values.put(FIELD_NAME, item.getName());
        values.put(FIELD_TRIGGER_TYPE, item.getType());
        values.put(FIELD_LAST_EXECUTED, item.getTime());
        context.getContentResolver().insert(TaskProvider.Contract.ACTIVITY_FEED, values);
    }

    public static List<ActivityFeedItem> getActivityFeed(Context context) {
        return getActivityFeed(context, 20);
    }

    public static List<ActivityFeedItem> getActivityFeed(Context context, int limit) {
        List<ActivityFeedItem> list = new ArrayList<ActivityFeedItem>();
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.ACTIVITY_FEED, FIELDS_ACTIVITY_FEED, null, null, FIELD_ID + " DESC");
        
        if (c != null) {
            int numItems = 0;
            if (c.moveToFirst()) {
                do {
                    list.add(new ActivityFeedItem(
                            c.getInt(c.getColumnIndex(FIELD_TRIGGER_TYPE)),
                            c.getString(c.getColumnIndex(FIELD_NAME)),
                            c.getString(c.getColumnIndex(FIELD_LAST_EXECUTED))));
                    numItems++;
                } while (c.moveToNext() &&
                        ((limit < 0) || (numItems < limit)));
            }
            c.close();
        }
        return list;
        
    }
    public static String getTaskNameFromId(Context context, String id) {
        String name = "";
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.TASKS, new String[] { FIELD_TASK_NAME }, FIELD_TASK_ID + "=?", new String[] { id }, null);
        if (c != null) {
            if (c.moveToFirst()) {
                name = c.getString(0);
            }
            c.close();
        }
        return name;
    }
   
    public static String saveConstraint(Context context, Constraint constraint, String triggerId) {
        String id = "";
        
        ContentValues values = new ContentValues();
        values.put(FIELD_TRIGGER_ID, triggerId);
        values.put(FIELD_KEY_1, constraint.getExtra(1));
        values.put(FIELD_KEY_2, constraint.getExtra(2));
        values.put(FIELD_TYPE, constraint.getType());
        
        long result = 0;
        if (constraint.getId() != null) {
            Logger.d("Updating constraint with id " + constraint.getId());
            result = context.getContentResolver().update(TaskProvider.Contract.CONSTRAINT, values, FIELD_ID + "=?", new String[] {  constraint.getId() });
            id = constraint.getId();
        }
        if (result < 1) {
            Uri uri = context.getContentResolver().insert(TaskProvider.Contract.CONSTRAINT,  values);
            id = String.valueOf(ContentUris.parseId(uri));
            Logger.d("Added constraint with id " + id);
        }
        
        return id;
    }
    
    public static String saveTrigger(Context context, Trigger trigger, String taskId) {
        
        ContentValues values = new ContentValues();
        values.put(FIELD_TRIGGER_CONDITION, trigger.getCondition());
        values.put(FIELD_TRIGGER_TYPE, trigger.getType());
        values.put(FIELD_KEY_1, trigger.getExtra(1));
        values.put(FIELD_KEY_2, trigger.getExtra(2));
        values.put(FIELD_TRIGGER_TASK, taskId);
        
        long result = 0;
        String id = "";
        if (trigger.getId() != null) {
            // We have a valid id, try updating
            Logger.d("Updating trigger with ID " + trigger.getId());
            result = context.getContentResolver().update(TaskProvider.Contract.TRIGGERS, values, FIELD_ID + "=?", new String[] { trigger.getId() });
            id = trigger.getId();
        }
        
        if (result < 1) {
            // Either the update missed or we 
            Logger.d("Inserting trigger");
            if (trigger.getId() != null) {
                Logger.d("Re-using id " + trigger.getId());
                values.put(FIELD_ID, trigger.getId());
            }
            Uri uri = context.getContentResolver().insert(TaskProvider.Contract.TRIGGERS, values);
            id = String.valueOf(ContentUris.parseId(uri));
            Logger.d("New trigger id " + id);
        }
        
        // Save all constraings associated with this trigger
        if (trigger.getConstraints().size() > 0) {
            Logger.d("Saving " + trigger.getConstraints().size() + " constraints for trigger");
            for (Constraint c: trigger.getConstraints()) {
                saveConstraint(context, c, id);
            }
        }
        
        return id;
        
    }
    
    public static ArrayList<TaskSet> getCompositeTasks(Context context) {

        ArrayList<TaskSet> tasks = new ArrayList<TaskSet>();
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.COMPOSITE_TASKS, null, null, null, null);

        if (c == null) {
            return tasks;
        }

        try {
            tasks = new ArrayList<TaskSet>(c.getCount());
            
            if (c.moveToFirst()) {
                do {
                    TaskSet set = new TaskSet();
                    set.addTrigger(Trigger.loadFromCursor(c, TaskProvider.NAME_TRIGGERS));
                    set.addTask(Task.loadFromCursor(c,TaskProvider. NAME_TASKS));
                    tasks.add(set);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Logger.e("Exception querying composite tasks " + e, e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
       
        return tasks;
        
    }
    
    public static ArrayList<TaskSet> getTasks(Context context) {
        
        ArrayList<TaskSet> tasks = new ArrayList<TaskSet>();
        
        try {
            
            Cursor c = context.getContentResolver().query(TaskProvider.Contract.TASKS, FIELDS_TASK, null, null, DatabaseHelper.FIELD_TASK_NAME + " COLLATE NOCASE ASC");
            
            if (c == null) {
                return tasks;
            }
            
            tasks = new ArrayList<TaskSet>(c.getCount());
            
            if (c.moveToFirst()) {
                do {
                    TaskSet set = new TaskSet();
                    set.addTask(Task.loadFromCursor(c));
                    // Load triggers for this task and build a TaskSet object
                    Cursor cTriggers = context.getContentResolver().query(TaskProvider.Contract.TRIGGERS, FIELGS_TRIGGERS, DatabaseHelper.FIELD_TRIGGER_TASK + "=?", new String[] { c.getString(0) }, null);
                    if (cTriggers != null) {
                        if (cTriggers.moveToFirst()) {
                            do {
                                set.addTrigger(Trigger.loadFromCursor(cTriggers));
                            } while (cTriggers.moveToNext());
                        }
                    }
                        
                    if (cTriggers != null) {
                        cTriggers.close();
                    }
                    
                    tasks.add(set);
                    
                } while (c.moveToNext());
            }

            c.close();
        } catch (Exception e) {
            Logger.e("Exception processing task list " + e, e);
            
        }

        return tasks;
    }
    
    public static Cursor loadTriggerConstraints(Context context, String id) {
        Cursor c = null;
        try { c = context.getContentResolver().query(TaskProvider.Contract.CONSTRAINT, FIELDS_CONSTRAINTS, FIELD_TRIGGER_ID + "=?", new String[] { id }, null); }
        catch (Exception e) { Logger.e("Exception loading constraints for " + id, e); }
        return c;
    }

    public static int getTaskCount(Context context) {
        Cursor c = context.getContentResolver().query(TaskProvider.Contract.TASK_COUNT, null, null, null, null);
        int count = ((c != null) && ( c.moveToFirst())) ? c.getInt(0) : 0;
        if (c != null) {
            c.close();
        }
        return count;
    }
}

