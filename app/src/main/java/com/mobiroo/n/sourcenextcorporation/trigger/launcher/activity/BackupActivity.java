package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.geofence.GeofenceClient;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListItemsAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.ListStringItem;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.SimpleDialogFragment;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;
import com.mobiroo.n.sourcenextcorporation.trigger.util.NetworkUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BackupActivity extends PlusSigninActivity implements OnClickListener {

    private final String mPostUrl = "https://gettrigger.com/google/backup";
    private final String mKeyEmail = "email";
    private final String mKeyId = "gid";
    private final String mKeyData = "data";

    private final String BACKUP_NAME = "Backup.json";
    private final String LEGACY_BACKUP_NAME = "Tags.json";

    protected final int REQUEST_CODE_RESOLVE_ERR = 1001;

    private ProgressDialog mDialog;

    private List<Geofence>          mFences;
    private boolean                 mIsConnected;
    private boolean                 mRunWhenConnected;

    private enum                    Operation { BACKUP, RESTORE, CLOUD_BACKUP, CLOUD_RESTORE, UNDEFINED };
    private Operation               mPendingOperation;

    private int                     mVersion = 2;

    private Toolbar mBar;
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_backup);

        mBar = (Toolbar) findViewById(R.id.toolbar);
        mBar.setNavigationIcon(R.drawable.ic_navigation_arrow_back_black);
        mBar.setTitle(getString(R.string.backup_restore));
        mBar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((LinearLayout) findViewById(R.id.backup)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.restore)).setOnClickListener(this);

        mRunWhenConnected = false;
        mShowDialogOnLoad = false;
        mResolveWhenReady = false;

        reloadUI();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRunWhenConnected = false;
    }

    @Override
    protected void runAfterConnected() {
        mIsConnected = true;
        if (mRunWhenConnected) {
            switch(mPendingOperation) {
                case BACKUP:
                    break;
                case RESTORE:
                    break;
                case CLOUD_BACKUP:
                    performCloudBackup(mId, mAccountName);
                    break;
                case CLOUD_RESTORE:
                    performCloudRestore(mId, mAccountName);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(BackupActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
        }
        return true;
    }

    private void reloadUI() {
        ((TextView) findViewById(R.id.status_text)).setText(SettingsHelper.getPrefString(BackupActivity.this, Constants.PREF_BACKUP_LAST_ACTION, getString(R.string.neverText)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.backup:
                showBackupOptions();
                break;
            case R.id.restore:
                showRestoreOptions();
                break;
        }

    }

    private void performLocalBackup() {
        new BackupWorker(BackupActivity.this).execute(false);
    }

    private void performCloudBackup(String id, String email) {

        if (mIsConnected) {
            Logger.d("Backup: Performing cloud backup");
            if ((mDialog == null) || (!mDialog.isShowing())) {
                Logger.d("Backup: Showing dialog in performCloudBackup");
                mDialog = ProgressDialog.show(BackupActivity.this, "", getString(R.string.perform_cloud_backup));
            }

            new BackupWorker(BackupActivity.this,mId, mAccountName).execute(true);
        } else {
            mRunWhenConnected = true;
        }

    }


    private class BackupWorker extends AsyncTask<Boolean, Void, Void> {

        private Context mContext;
        private String mId;
        private String mEmail;

        private final int BACKUP_LOCAL = 1;
        private final int BACKUP_CLOUD = 2;
        private int mBackupType;

        public BackupWorker(Context context, String id, String email) {
            this.mContext = context;
            this.mId = id;
            this.mEmail = email;
        }
        public BackupWorker(Context context) {
            this.mContext = context;
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            JSONObject backup = DatabaseHelper.generateDatabaseBackup(DatabaseHelper.getInstance(BackupActivity.this).getReadableDatabase());
            try { backup.put("version", mVersion); }
            catch (Exception e) { }
            
            if (params[0]) {
                mBackupType = BACKUP_CLOUD;
                // Cloud backup
                if ((mId != null) && (mEmail != null)) {
                    // Have credentials, create data to post
                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                    Logger.i("Backup Email " + mEmail);
                    Logger.i("Backup ID " + mId);
                    pairs.add(new BasicNameValuePair(mKeyEmail, mEmail));
                    pairs.add(new BasicNameValuePair(mKeyId, mId));
                    pairs.add(new BasicNameValuePair(mKeyData, Base64.encodeToString(backup.toString().getBytes(), Base64.DEFAULT)));

                    Logger.i("Backup URL is " + mPostUrl);
                    try {
                        NetworkUtil.Response r = NetworkUtil.getHttpsResponse(mPostUrl, NetworkUtil.METHOD_POST, pairs);
                        String responseString = r.getBody();
                        Logger.d("Put response = " + responseString);

                    } catch (Exception e) { Logger.e(Constants.TAG, "Exception posting backup", e); }

                }
            } else {
                // Local backup
                mBackupType = BACKUP_LOCAL;
                Logger.writeFile(backup.toString(), BACKUP_NAME, false, true, false);
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            /* Update last action status */
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a");
            SettingsHelper.setPrefString(mContext, Constants.PREF_BACKUP_LAST_ACTION, String.format(mContext.getString(R.string.backed_up), df.format(new Date())));
            /* Update last operation display */
            reloadUI();
            if (mBackupType == BACKUP_LOCAL) {
                Toast.makeText(mContext, "Backup saved to " + Environment.getExternalStorageDirectory().getPath() + "/" + Logger.DIR_NAME + "/" + BACKUP_NAME, Toast.LENGTH_LONG).show();
            }
            try { mDialog.dismiss(); }
            catch ( Exception e) { }
        }

    }
    private class RestoreWorker extends AsyncTask<Void, Void, Void> {

        private Context mContext;
        private String mString;
        private JSONObject mObject;
        private String mId;
        private String mEmail;
        private OperationStatus mStatus;


        public RestoreWorker(Context context, String id, String email) {
            this.mContext = context;
            this.mId = id;
            this.mEmail = email;
        }

        public RestoreWorker(Context context, String contents) {
            this.mContext = context;
            this.mString = contents;
        }

        @SuppressWarnings("unused")
        public RestoreWorker(Context context, JSONObject object) {
            this.mContext = context;
            this.mObject = object;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mObject != null) {
                Logger.d("Backup: Object is not null, calling restore from JSON");
                mStatus = restoreFromJson(mObject);
            } else if (mString != null) {
                Logger.d("Backup: String is not null, restoring from string");
                mStatus = restoreFromString(mString);
            } else if (!(mId == null) && !(mEmail == null)) {
                Logger.d("Backup: Received no data, pulling from server");
                // Have credentials, create data to post
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair(mKeyEmail, mEmail));
                pairs.add(new BasicNameValuePair(mKeyId, mId));


                try {
                    String uri = mPostUrl + "?" + mKeyEmail + "=" + URLEncoder.encode(mEmail, "UTF-8") + "&" + mKeyId + "=" + mId;

                    NetworkUtil.Response r = NetworkUtil.getHttpsResponse(uri, NetworkUtil.METHOD_GET);
                    if (r.getCode() == 200) {
                        Logger.d("Backup: Get response = " + r.getBody());
                        Logger.d("Backup: Calling restore with retrieved json doc");
                        mStatus = restoreFromJson(r.getJsonFromBase64());

                    }
                } catch (Exception e) {
                    Logger.e(Constants.TAG, "Exception retrieving backup", e);
                }

            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            /* Update last action status */
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss");
            SettingsHelper.setPrefString(mContext, Constants.PREF_BACKUP_LAST_ACTION, String.format(mContext.getString(R.string.restored), df.format(new Date())));

            restoreGeofencesIfNecessary();

            try { mDialog.dismiss(); }
            catch ( Exception e) { }

            /* Update last operation display */
            reloadUI();

            Utils.signalTasksChanged(mContext);
            /* Check receivers and enable if needed */
            Utils.checkReceivers(mContext);
        }

    }
    private void performLocalRestore() {

        if ((mDialog == null) || (!mDialog.isShowing())) {
            Logger.d("Backup: Showing dialog in performLocalRestore");
            mDialog = ProgressDialog.show(BackupActivity.this, "", getString(R.string.restoring));
        }

        File root = Environment.getExternalStorageDirectory();
        File container = new File(root.getPath() + "/" + Logger.DIR_NAME + "/");

        final File backup;

        /* Test for new file name */
        if (new File(container, BACKUP_NAME).exists()) {
            /* Use new backup location */
            backup = new File(container, BACKUP_NAME);
        } else {
            /* Use legacy backup */
            container = new File(root.getPath() + "/" + Logger.LEGACY_DIR_NAME + "/");
            backup = new File(container, LEGACY_BACKUP_NAME);
        }


        String contents = "";
        if (backup.exists()) {
            try {
                InputStream is = new FileInputStream(backup);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int b = is.read();
                do {
                    os.write(b);
                } while ((b = is.read()) != -1);

                contents = new String(os.toByteArray());
                is.close();

            } catch (FileNotFoundException e) {
                Logger.e("File not found for local restore");
            } catch (IOException e) {
                Logger.e("IO Exception restoring local data " + e);
            }
        }

        Logger.d("Backup: Starting restore worker for local backup");
        new RestoreWorker(BackupActivity.this, new String(Base64.decode(contents, Base64.DEFAULT))).execute();
    }

    private void performCloudRestore(String id, String email) {

        if (mIsConnected) {

            if ((mDialog == null) || (!mDialog.isShowing())) { 
                Logger.d("Backup: Showing dialog in performCloudRestore");
                try {
                    mDialog = ProgressDialog.show(BackupActivity.this, "", getString(R.string.restoring));
                } catch (Exception e) { }
            }

            Logger.d("Backup: Running cloud restore");
            new RestoreWorker(BackupActivity.this,mId, mAccountName).execute();
        } else {
            mRunWhenConnected = true;
        }

    }

    private class OperationStatus {
        public static final int ERROR_EXCEPTION_ADDING_DATA = 1;
        
        @SuppressWarnings("unused")
        public boolean hasError;
        @SuppressWarnings("unused")
        public int errorCode;
        public int version;
        
        public OperationStatus() {
            hasError = false;
            errorCode = 0;
            version = 0;
        }
        
    }
    
    private OperationStatus restoreFromJson(JSONObject contents) {
        Logger.d("Starting restore from Json");
        OperationStatus status = new OperationStatus();
        
        Logger.d("Content: " + contents.toString().length() + " bytes");
        int version = 1;
        // Get version
        if (contents.has("version")) {
            try { version = contents.getInt("version"); }
            catch (Exception e) { Logger.e("Exception parsing backup version"); }
        } else {
            version = 1;
        }
        
        Logger.d("Restoring from backup v" + version);
        status.version = version;
        
        SQLiteDatabase db = DatabaseHelper.getInstance(BackupActivity.this).getReadableDatabase();
        DatabaseHelper.deleteBackedUpTables(db);
        for (int i =0; i< DatabaseHelper.tables.length;  i++)  {
            String table = DatabaseHelper.tables[i];
            Logger.d("Backup: Restoring " + table);
            try {
                JSONArray current = contents.getJSONArray(table);
                ContentValues values = null;
                if (current != null) {
                    for (int j=0; j< current.length(); j++) {
                        values = new ContentValues();
                        JSONObject item = current.getJSONObject(j);
                        @SuppressWarnings("unchecked")
                        Iterator<String> keys = item.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            values.put(key, item.getString(key));
                        }
                        DatabaseHelper.insertBackupItem(db, table, values);
                    }
                }
            } catch (JSONException e) {
                Logger.e(Constants.TAG, "Exception restoring from JSON", e);
                status.hasError = true;
                status.errorCode = OperationStatus.ERROR_EXCEPTION_ADDING_DATA;
            }

        }
        Logger.d("Json restore completed");
        return status;
    }

    private OperationStatus restoreFromString(String contents) {
        JSONTokener tokener = new JSONTokener(contents);
        JSONObject obj = null;
        OperationStatus status = new OperationStatus();
        
        try {
            obj = new JSONObject(tokener); 
        } catch (JSONException e) {
            Logger.e(Constants.TAG, "Exception setting up restore from JSON", e);
        }

        if (obj != null) {
            status = restoreFromJson(obj);
            
        }
        return status;
    }

    private void showBackupOptions() {
        final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        dialog.setTitle(getString(R.string.backup_to));
        ListStringItem[] options =
                (BuildConfiguration.isPlayStoreAvailable()) ? new ListStringItem[] { new ListStringItem(BackupActivity.this, getString(R.string.backup_tagstand)), new ListStringItem(BackupActivity.this, getString(R.string.backup_local)) }
        : new ListStringItem[] { new ListStringItem(BackupActivity.this, getString(R.string.backup_local)) };

                dialog.setListAdapter(new ListItemsAdapter(BackupActivity.this, options));
                dialog.setListOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                        dialog.dismiss();
                        switch(position) {
                            case 0:
                                if (BuildConfiguration.isPlayStoreAvailable()) {
                                    mPendingOperation = Operation.CLOUD_BACKUP;
                                    mRunWhenConnected = true;
                                    logIn();
                                } else {
                                    mPendingOperation = Operation.BACKUP;
                                    performLocalBackup();
                                }
                                break;
                            case 1:
                                mPendingOperation = Operation.BACKUP;
                                performLocalBackup();
                                break;
                        }
                    }

                });
                dialog.show(getSupportFragmentManager(), "");
    }

    private void showRestoreOptions() {
        final SimpleDialogFragment dialog = new SimpleDialogFragment(SimpleDialogFragment.layoutListView);
        dialog.setTitle(getString(R.string.restore_from));

        ListStringItem[] options = 
                (BuildConfiguration.isPlayStoreAvailable()) ? new ListStringItem[] { new ListStringItem(BackupActivity.this, getString(R.string.backup_tagstand)), new ListStringItem(BackupActivity.this, getString(R.string.backup_local)) } 
        : new ListStringItem[] { new ListStringItem(BackupActivity.this, getString(R.string.backup_local)) };

                dialog.setListAdapter(new ListItemsAdapter(BackupActivity.this, options));
                dialog.setListOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                        dialog.dismiss();
                        switch(position) {
                            case 0:
                                if (BuildConfiguration.isPlayStoreAvailable()) {
                                    mPendingOperation = Operation.CLOUD_RESTORE;
                                    mRunWhenConnected = true;
                                    logIn();
                                } else {
                                    mPendingOperation = Operation.RESTORE;
                                    performLocalRestore();
                                }
                                break;
                            case 1:
                                mPendingOperation = Operation.RESTORE;
                                performLocalRestore();
                                break;
                        }
                    }

                });
                dialog.show(getSupportFragmentManager(), "");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Backup: OnActivityResult: " + requestCode);
        if (mPendingOperation != null) {
            Logger.d("Backup: Pending operation is " + mPendingOperation.toString());
        } else {
            mPendingOperation = Operation.UNDEFINED;
        }
        
        switch(requestCode) {
            case REQUEST_CODE_RESOLVE_ERR:
                mIsConnected = getPlusClient().isConnected();
                mRunWhenConnected = true;
                if (!mIsConnected) {
                    connect();
                } else {
                    switch(mPendingOperation) {
                        case CLOUD_RESTORE:
                            performCloudRestore(mId, mAccountName);
                            break;
                        case CLOUD_BACKUP:
                            performCloudBackup(mId, mAccountName);
                            break;
                        default:
                            break;
                    }
                }
                break;
        }
    }

    private void restoreGeofencesIfNecessary() {
        mFences = DatabaseHelper.getGeofences(this);
        if (mFences.size() > 0) {
            Context context = BackupActivity.this;
            GeofenceClient geoClient = new GeofenceClient(context);
            geoClient.setGeofences(mFences);
            geoClient.connectAndSave();
        }
    }
}
