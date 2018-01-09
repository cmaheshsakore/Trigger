package com.mobiroo.n.sourcenextcorporation.trigger.launcher.util;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.preferences.activity.SettingsHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.Date;

public class Logger {
    public static final String  DIR_NAME = "Trigger";
    public static final String  LEGACY_DIR_NAME = "NFCTL";
    public static final String  FILE_NAME = "debug.txt";

    private static final int TRUNCATE_LOG_FILE_SIZE = 1048576;
    private static final String TRUNCATED_CHECKPOINT = "LOG_FILE_WAS_TRUNCATED_AT_THIS_TIME";

    public static void i(String Message) {
        Logger.i(Constants.TAG, Message);
    }

    public static void i(String TAG, String Message) {
        Log.i(TAG, Message);
        if (SettingsHelper.getInstance().debuggingEnabled()) {
            writeFile(Message);
        }
    }

    public static void e(String Message) {
        Logger.e(Constants.TAG, Message);
    }

    public static void e(String Message, Exception e) {
        Logger.e(Constants.TAG, Message, e);
    }
    
    public static void e(String TAG, String Message, Exception e) {
        e.printStackTrace();
        
        if (SettingsHelper.getInstance().debuggingEnabled()) {
            Log.e(TAG, Message);
            writeFile(e.getMessage());
        }
    }

    public static void e(String TAG, String Message) {
        
        if (SettingsHelper.getInstance().debuggingEnabled()) {
            Log.e(TAG, Message);
            writeFile(Message);
        }
    }

    public static void d(String Message) {
        Logger.d(Constants.TAG, Message);
    }

    public static void d(String TAG, String Message) {
        // Only log debug if we have debug enabled
        if (SettingsHelper.getInstance().debuggingEnabled()) {
            Log.d(TAG, Message);
            writeFile(Message);
        }
    }

    public static void d(String s, Object...args) {
        if (SettingsHelper.getInstance().debuggingEnabled()) {
            String message = s;
            try { message = String.format(s, args); }
            catch (Exception ignored) { }
            Log.d(Constants.TAG, message);
            writeFile(message);
        }
    }
    private static class fileWriter extends AsyncTask<String, Void, Void> {
        
        private boolean mEncode = false;;
        private boolean mStamp = true;
        private boolean mAppendFile = true;
        
        public fileWriter(boolean encode, boolean timestamp, boolean append) {
            this.mEncode = encode;
            this.mStamp = timestamp;
            this.mAppendFile = append;
        }
        
        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(String... params) {
            String message = params[0];
            if (this.mEncode) {
                message = Base64.encodeToString(message.getBytes(), Base64.DEFAULT);
            }
            String title = params[1];
            
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()) {
                try {
                    File container = new File(root.getPath() + "/" + DIR_NAME + "/");
                    container.mkdirs();
                    File log = new File(container, title);

                    long truncateTime = -1;
                    if (log.length() > TRUNCATE_LOG_FILE_SIZE) {
                        truncateTime = Logger.truncateFile(log);
                    }

                    FileWriter writer = new FileWriter(log, mAppendFile);
                    BufferedWriter out = new BufferedWriter(writer);
                    Date now = new Date();
                    if (this.mStamp) {
                        out.write(now.toLocaleString() + ": " + message + "\n");
                    } else {
                        out.write(message + "\n");
                    }
                    out.flush();
                    writer.flush();
                    out.close();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    public static synchronized long truncateFile(File log) {

        long truncateBegin = System.currentTimeMillis();
        try {
            RandomAccessFile raf = new RandomAccessFile(log, "rw");

            if (raf.length() <= TRUNCATE_LOG_FILE_SIZE) {raf.close(); return -1;}

            byte[] buf = new byte[TRUNCATE_LOG_FILE_SIZE / 2];
            raf.seek(TRUNCATE_LOG_FILE_SIZE / 2);
            raf.readFully(buf);
            raf.seek(0);
            raf.write(buf);
            raf.writeBytes("\n" + TRUNCATED_CHECKPOINT + "\n");
            raf.setLength(TRUNCATE_LOG_FILE_SIZE / 2 + TRUNCATED_CHECKPOINT.length() + 2);
            raf.close();
        } catch (Exception e) {
            return -1;
        }

        return (System.currentTimeMillis() - truncateBegin);
    }

    public static void writeFile(String message, String title, boolean stamp, boolean encode, boolean append) {
        new Logger.fileWriter(encode, stamp, append).execute(message, title);
    }
    private static void writeFile(String message) {
        new Logger.fileWriter(false, true, true).execute(message, FILE_NAME);
    }
}
