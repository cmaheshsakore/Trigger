package com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher;

import java.io.File;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class MyBackupAgent extends BackupAgentHelper {
    static final String PREFS_BACKUP_KEY = "prefs";
    static final String PREFS_NAME = "NFCTaskLauncherPrefs";

    static final String DB_BACKUP_KEY = "db";
    private static final String DB_NAME = "TagStorage";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper prefs = new SharedPreferencesBackupHelper(this, PREFS_NAME);
        addHelper(PREFS_BACKUP_KEY, prefs);

        /*
        FileBackupHelper dbs = new FileBackupHelper(this, DB_NAME);
        addHelper(DB_BACKUP_KEY, dbs);
        */
    }

    @Override
    public File getFilesDir() {
        File path = getDatabasePath(DB_NAME);
        return path.getParentFile();
    }

}
