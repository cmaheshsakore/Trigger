package com.mobiroo.n.sourcenextcorporation.trigger.launcher.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by krohnjw on 12/26/13.
 */
public class TaskProvider extends ContentProvider {

    public static final String AUTHORITY = "com.trigger.launcher.providers";

    public static final String BASE_URI = "content://" + AUTHORITY;

    public static final class Contract {
        public static final Uri TASKS           = Uri.parse(BASE_URI + "/tasks");
        public static final Uri TASK_COUNT      = Uri.parse(BASE_URI + "/task_count");
        public static final Uri RAN_COUNT       = Uri.parse(BASE_URI + "/ran_count");
        public static final Uri TRIGGERS        = Uri.parse(BASE_URI + "/triggers");
        public static final Uri COMPOSITE_TASKS = Uri.parse(BASE_URI + "/composite_tasks");
        public static final Uri CONSTRAINT      = Uri.parse(BASE_URI + "/constraint");
        public static final Uri ACTIONS         = Uri.parse(BASE_URI + "/actions");
        public static final Uri GEOFENCE        = Uri.parse(BASE_URI + "/geofence");
        public static final Uri ACTIVITY_FEED   = Uri.parse(BASE_URI + "/activity_feed");
        public static final Uri LOCAL_MAPPING   = Uri.parse(BASE_URI + "/local_mapping");
        public static final Uri TIMEOUTS        = Uri.parse(BASE_URI + "/timeouts");
        public static final Uri USAGE           = Uri.parse(BASE_URI + "/usage");
    }

    public static final int TASKS               = 1;
    public static final int TASK                = 2;
    public static final int TASK_COUNT          = 3;
    public static final int RAN_COUNT           = 4;
    public static final int TRIGGERS            = 5;
    public static final int COMPOSITE_TASKS     = 6;
    public static final int CONSTRAINT          = 7;
    public static final int ACTIONS             = 8;
    public static final int GEOFENCE            = 9;
    public static final int ACTIVITY_FEED       = 10;
    public static final int GEOFENCE_CLEAN      = 11;
    public static final int ACTIVITY_FEED_COUNT = 12;
    public static final int ACTIVITY_FEED_AVG_W = 13;
    public static final int LOCAL_MAPPING       = 14;
    public static final int TIMEOUTS            = 15;
    public static final int USAGE               = 16;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "task/#",         TASK);
        sUriMatcher.addURI(AUTHORITY, "tasks",          TASKS);
        sUriMatcher.addURI(AUTHORITY, "task_count",     TASK_COUNT);
        sUriMatcher.addURI(AUTHORITY, "ran_count",      RAN_COUNT);
        sUriMatcher.addURI(AUTHORITY, "triggers/#",     TRIGGERS);
        sUriMatcher.addURI(AUTHORITY, "triggers",       TRIGGERS);
        sUriMatcher.addURI(AUTHORITY, "composite_tasks",COMPOSITE_TASKS);
        sUriMatcher.addURI(AUTHORITY, "constraint",     CONSTRAINT);
        sUriMatcher.addURI(AUTHORITY, "actions",        ACTIONS);
        sUriMatcher.addURI(AUTHORITY, "geofence/clean", GEOFENCE_CLEAN);
        sUriMatcher.addURI(AUTHORITY, "geofence",       GEOFENCE);
        sUriMatcher.addURI(AUTHORITY, "activity_feed/count/weekly",
                                                        ACTIVITY_FEED_AVG_W);
        sUriMatcher.addURI(AUTHORITY, "activity_feed/count",
                                                        ACTIVITY_FEED_COUNT);
        sUriMatcher.addURI(AUTHORITY, "activity_feed",  ACTIVITY_FEED);
        sUriMatcher.addURI(AUTHORITY, "local_mapping",  LOCAL_MAPPING);
        sUriMatcher.addURI(AUTHORITY, "timeouts",       TIMEOUTS);
        sUriMatcher.addURI(AUTHORITY, "usage",          USAGE);

    }

    DatabaseHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = DatabaseHelper.getInstance(getContext());
        // Assumes any failure to acquire instance will throw an exception
        return true;
    }

    private DatabaseHelper getHelper() {
        if (mHelper == null)
            mHelper = DatabaseHelper.getInstance(getContext());

        return mHelper;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                return queryTaskList(projection, selection, selectionArgs, sortOrder);
            case TASK:
                Logger.d("Returning an individual task - TO DO");
                return null;
            case TASK_COUNT:
                return queryTaskCount();
            case RAN_COUNT:
                return queryRanCount();
            case CONSTRAINT:
                return queryTriggerConstraints(projection, selection, selectionArgs, sortOrder);
            case TRIGGERS:
                return queryTriggers(projection, selection, selectionArgs, sortOrder);
            case COMPOSITE_TASKS:
                return queryCompositeTasks(projection, selection, selectionArgs, sortOrder);
            case GEOFENCE:
                return queryGeofences(projection, selection, selectionArgs, sortOrder);
            case ACTIVITY_FEED:
                return queryActivityFeed(projection, selection, selectionArgs, sortOrder);
            case ACTIVITY_FEED_COUNT:
                return queryActivityFeedCount(projection, selection, selectionArgs, sortOrder);
            case ACTIVITY_FEED_AVG_W:
                return queryActivityFeedWeeklyCount(projection, selection, selectionArgs, sortOrder);
            case ACTIONS:
                return queryActions(projection, selection, selectionArgs, sortOrder);
            case TIMEOUTS:
                return queryTimeout(projection, selection, selectionArgs, sortOrder);
            case USAGE:
                return queryUsage(projection, selection, selectionArgs, sortOrder);
            case LOCAL_MAPPING:
                return queryLocalMapping(projection, selection, selectionArgs, sortOrder);
            default:
                Logger.d("Missed all matches, returning null");
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO - add types to this
        switch (sUriMatcher.match(uri)) {
            default:
                return "vnd.android.cursor.dir/vnd.com.trigger.launcher.generic";
        }

    }

    final String TASK_DATA_CHANGED = "com.trigger.launcher.TASK_DATA_CHANGED";

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri value;
        String action = "";
        switch (sUriMatcher.match(uri)) {
            case ACTIONS:
                value = insertActions(contentValues);
                break;
            case GEOFENCE:
                value = insertGeofence(contentValues);
                break;
            case ACTIVITY_FEED:
                value = insertActivityFeed(contentValues);
                break;
            case TASKS:
                value = insertTask(contentValues);
                action = TASK_DATA_CHANGED;
                break;
            case CONSTRAINT:
                value = insertConstraint(contentValues);
                break;
            case TRIGGERS:
                value = insertTrigger(contentValues);
                break;
            case LOCAL_MAPPING:
                value = insertLocalMapping(contentValues);
                break;
            case TIMEOUTS:
                value = insertTimeout(contentValues);
                break;
            case USAGE:
                value = insertUsage(contentValues);
                break;
            default:
                throw new IllegalArgumentException(uri + " did not match any expected value");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        if (!action.isEmpty()) getContext().sendBroadcast(new Intent(action));
        return value;
    }

    @Override
    public int delete(Uri uri, String select, String[] selectionArgs) {
        int value = -1;
        String action = "";
        switch (sUriMatcher.match(uri)) {
            case TRIGGERS:
                value = deleteTrigger(uri, select, selectionArgs);
                break;
            case CONSTRAINT:
                value = deleteConstraintsForTrigger(uri, select, selectionArgs);
                break;
            case ACTIONS:
                value = deleteActions(uri, select, selectionArgs);
                break;
            case GEOFENCE:
                value = deleteGeofence(uri, select, selectionArgs);
                break;
            case GEOFENCE_CLEAN:
                return cleanGeofences();
            case TASK:
            case TASKS:
                value = deleteTask(uri, select, selectionArgs);
                action = TASK_DATA_CHANGED;
                break;
            case LOCAL_MAPPING:
                value = deleteLocalMapping(uri, select, selectionArgs);
                break;
            case ACTIVITY_FEED:
                value = deleteActivityFeed(uri, select, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(uri + " did not match any expected value");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        if (!action.isEmpty()) getContext().sendBroadcast(new Intent(action));
        return value;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String select, String[] selectionArgs) {
        int value;
        String action = "";
        switch (sUriMatcher.match(uri)) {
            case TASKS:
                value = updateTask(contentValues, select, selectionArgs);
                action = TASK_DATA_CHANGED;
                break;
            case GEOFENCE:
                value = updateGeofence(contentValues, select, selectionArgs);
                break;
            case CONSTRAINT:
                value = updateConstraint(contentValues, select, selectionArgs);
                break;
            case TRIGGERS:
                value = updateTrigger(contentValues, select, selectionArgs);
                break;
            case LOCAL_MAPPING:
                value = updateLocalMapping(contentValues, select, selectionArgs);
                break;
            case TIMEOUTS:
                value = updateTimeout(contentValues, select, selectionArgs);
                break;
            case USAGE:
                value = updateUsage(contentValues, select, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(uri + " did not match any expected value");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        if (!action.isEmpty()) getContext().sendBroadcast(new Intent(action));
        return value;

    }

    /*
    Query definitions
     */

    public static final String PREFIX_TASKS = "Tasks.";
    public static final String NAME_TASKS = "Tasks_";
    public static final String PREFIX_TRIGGERS = "Triggers.";
    public static final String NAME_TRIGGERS = "Triggers_";

    private Cursor queryActions(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_SAVED_TASK_ACTIONS, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor queryActivityFeed(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_ACTIVITY_FEED, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor queryActivityFeedCount(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .rawQuery("SELECT SUM(" + DatabaseHelper.FIELD_TOTAL_ACTIONS + ") FROM " + DatabaseHelper.TABLE_STATS, null);
    }

    private Cursor queryActivityFeedWeeklyCount(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.MONTH, -1);

        return getHelper().getReadableDatabase()
                .rawQuery("SELECT strftime('%Y-%W', " + DatabaseHelper.FIELD_DATE + ") as week_of_year, SUM(" + DatabaseHelper.FIELD_TOTAL_ACTIONS + ") FROM " +
                        DatabaseHelper.TABLE_STATS + " WHERE " + DatabaseHelper.FIELD_DATE + " IS NOT NULL " +
                        " AND " + DatabaseHelper.FIELD_DATE + " BETWEEN '" + sdf.format(limit.getTime()) + "' AND '" + sdf.format(Calendar.getInstance().getTime()) + "'" +
                        " GROUP BY week_of_year ORDER BY " + DatabaseHelper.FIELD_DATE + " DESC", null);
    }

    private Cursor queryCompositeTasks(String[] prokection, String selection, String[] selectionArgs, String sortOrder) {

        String query = "SELECT " +
                PREFIX_TASKS + DatabaseHelper.FIELD_TASK_ID + " AS " + NAME_TASKS + DatabaseHelper.FIELD_TASK_ID + ", " +
                PREFIX_TASKS + DatabaseHelper.FIELD_TASK_NAME + " AS " + NAME_TASKS + DatabaseHelper.FIELD_TASK_NAME + ", " +
                PREFIX_TASKS + DatabaseHelper.FIELD_TASK_LAST_ACCESSED + " AS " + NAME_TASKS + DatabaseHelper.FIELD_TASK_LAST_ACCESSED + ", " +
                PREFIX_TASKS + DatabaseHelper.FIELD_TASK_TWO_ID + " AS " + NAME_TASKS + DatabaseHelper.FIELD_TASK_TWO_ID + ", " +
                PREFIX_TASKS + DatabaseHelper.FIELD_TASK_TWO_NAME + " AS " + NAME_TASKS + DatabaseHelper.FIELD_TASK_TWO_NAME + ", " +
                PREFIX_TASKS + DatabaseHelper.FIELD_ENABLED + " AS " + NAME_TASKS + DatabaseHelper.FIELD_ENABLED + ", " +
                PREFIX_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_TYPE + " AS " + NAME_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_TYPE + ", " +
                PREFIX_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_CONDITION + " AS " + NAME_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_CONDITION + ", " +
                PREFIX_TRIGGERS + DatabaseHelper.FIELD_KEY_1 + " AS " + NAME_TRIGGERS + DatabaseHelper.FIELD_KEY_1 + ", " +
                PREFIX_TRIGGERS + DatabaseHelper.FIELD_KEY_2 + " AS " + NAME_TRIGGERS + DatabaseHelper.FIELD_KEY_2 + ", " +
                PREFIX_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_TASK + " AS " + NAME_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_TASK + ", " +
                PREFIX_TRIGGERS + DatabaseHelper.FIELD_ID + " AS " + NAME_TRIGGERS + DatabaseHelper.FIELD_ID + " " +
                " FROM " + DatabaseHelper.TABLE_SAVED_TASKS + " AS Tasks " +
                " JOIN " + DatabaseHelper.TABLE_TRIGGERS + " AS Triggers " +
                " ON " + PREFIX_TASKS + DatabaseHelper.FIELD_ID + "=" + PREFIX_TRIGGERS + DatabaseHelper.FIELD_TRIGGER_TASK +
                " ORDER by " + DatabaseHelper.FIELD_TASK_NAME + " COLLATE NOCASE ASC";

        Cursor c = null;

        try { c = getHelper().getReadableDatabase().rawQuery(query, null); }
        catch (Exception e) { Logger.e("Exception running composite query: " + e); }

        return c;

    }

    private Cursor queryGeofences(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_GEOFENCES, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor queryLocalMapping(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_LOCAL_MAPPING, projection, selection, selectionArgs, null, null, sortOrder);
    }

    public Cursor queryRanCount() {
        // Don't use - use Usage helper method
        return null;
    }

    public Cursor queryTaskCount() {
        return getHelper().getReadableDatabase()
                .rawQuery("Select COUNT(" + DatabaseHelper.FIELD_TASK_ID + ") FROM " + DatabaseHelper.TABLE_SAVED_TASKS, null);
    }

    public Cursor queryTaskList(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_SAVED_TASKS,
                        (projection != null) ? projection : DatabaseHelper.FIELDS_TASK,
                        (selection != null) ? selection : null,
                        (selectionArgs != null) ? selectionArgs : null,
                        null, null,
                        (sortOrder != null) ? sortOrder : DatabaseHelper.FIELD_TASK_NAME + " COLLATE NOCASE ASC"
                );
    }

    private Cursor queryTimeout(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_TAG_TIMEOUTS, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor queryTriggerConstraints(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_CONSTRAINTS, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor queryTriggers(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_TRIGGERS, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor queryUsage(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getHelper().getReadableDatabase()
                .query(DatabaseHelper.TABLE_STATS, projection, selection, selectionArgs, null, null, sortOrder);
    }

    /*
    Update definitions
     */
    private int updateConstraint(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_CONSTRAINTS, contentValues, select, selectionArgs);
    }

    private int updateGeofence(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_GEOFENCES, contentValues, select, selectionArgs);
    }

    private int updateLocalMapping(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_LOCAL_MAPPING, contentValues, select, selectionArgs);
    }

    private int updateTask(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_SAVED_TASKS, contentValues, select, selectionArgs);
    }

    private int updateTimeout(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_TAG_TIMEOUTS, contentValues, select, selectionArgs);
    }

    private int updateTrigger(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_TRIGGERS, contentValues, select, selectionArgs);
    }

    private int updateUsage(ContentValues contentValues, String select, String[] selectionArgs) {
        return getHelper().getWritableDatabase().update(DatabaseHelper.TABLE_STATS, contentValues, select, selectionArgs);
    }

    /*
    Insert definitions
     */
    private Uri insertActions(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_SAVED_TASK_ACTIONS, null, values);
        return Uri.withAppendedPath(Contract.ACTIONS, String.valueOf(id));
    }

    private Uri insertActivityFeed(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_ACTIVITY_FEED, null, values);
        return Uri.withAppendedPath(Contract.ACTIVITY_FEED, String.valueOf(id));
    }

    private Uri insertConstraint(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_CONSTRAINTS, null, values);
        return Uri.withAppendedPath(Contract.CONSTRAINT, String.valueOf(id));
    }
    private Uri insertGeofence(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_GEOFENCES, null, values);
        return Uri.withAppendedPath(Contract.GEOFENCE, String.valueOf(id));
    }

    private Uri insertLocalMapping(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_LOCAL_MAPPING, null, values);
        return Uri.withAppendedPath(Contract.LOCAL_MAPPING, String.valueOf(id));
    }

    private Uri insertTask(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_SAVED_TASKS, null, values);
        return Uri.withAppendedPath(Contract.TASKS, String.valueOf(id));
    }

    private Uri insertTimeout(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_TAG_TIMEOUTS, null, values);
        return Uri.withAppendedPath(Contract.TIMEOUTS, String.valueOf(id));
    }

    private Uri insertTrigger(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_TRIGGERS, null, values);
        return Uri.withAppendedPath(Contract.TRIGGERS, String.valueOf(id));
    }

    private Uri insertUsage(ContentValues values) {
        long id = getHelper().getWritableDatabase().insert(DatabaseHelper.TABLE_STATS, null, values);
        return Uri.withAppendedPath(Contract.USAGE, String.valueOf(id));
    }

    /*
    Delete definitions
     */
    private int deleteActions(Uri uri, String selection, String[] selectionArgs) {
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_SAVED_TASK_ACTIONS, selection, selectionArgs);
    }

    private int deleteConstraintsForTrigger(Uri uri, String select, String[] selectionArgs) {
        // Delete any constraints belonging to this trigger ID
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_CONSTRAINTS, select, selectionArgs);
    }

    private int deleteGeofence(Uri uri, String selection, String[] selectionArgs) {
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_GEOFENCES, selection, selectionArgs);
    }

    private int deleteLocalMapping(Uri uri, String selection, String[] selectionArgs) {
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_LOCAL_MAPPING, selection, selectionArgs);
    }

    private int deleteTask(Uri uri, String selection, String[] selectionArgs) {
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_SAVED_TASKS, selection, selectionArgs);
    }

    private int deleteTrigger(Uri uri, String select, String[] selectionArgs) {
        // Remove this trigger from trigger table
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_TRIGGERS, select, selectionArgs);
    }

    private int deleteActivityFeed(Uri uri, String select, String[] selectionArgs) {
        getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_STATS, select, selectionArgs);
        return getHelper().getWritableDatabase().delete(DatabaseHelper.TABLE_ACTIVITY_FEED, select, selectionArgs);
    }
    /*
    Misc definitions
     */

    private int cleanGeofences() {
        getHelper().getWritableDatabase().execSQL("delete from " + DatabaseHelper.TABLE_GEOFENCES + " WHERE id NOT IN (select t.id from TagInfo as i join Triggers as T on i.id=t.taskId WHERE t.trigger=5);");
        return -1;
    }












}
