package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;

public class BaseNfcActivity extends AppCompatActivity{

    protected NfcAdapter mAdapter = null;
    protected PendingIntent mPendingIntent;
    protected IntentFilter[] mFilters;
    protected String[][] mTechLists;
    
    protected Context mContext;

    @SuppressWarnings("unused")
    private boolean mShowMenu = true;

    protected Class<MainActivity> mUpActivityClass = MainActivity.class;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mContext = this;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(this)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Usage.canLogData(this)) {
            EasyTracker.getInstance(this).activityStop(this); // Add this method.
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getNfcAdapter();
    }
    
    private void getNfcAdapter() {
        try { mAdapter = NfcAdapter.getDefaultAdapter(mContext);
        } catch (Exception e) { Logger.e("Error getting NFC Adapter " + e); }
        
        try {
            if (mAdapter != null) {
                mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            }
        } catch (Exception e) {
            Logger.e("Exception setting up foreground dispatch " + e);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }
    }
    
    protected  void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.dialogOK), new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        builder.create().show();
    }
    public void rebuildList() {
        // Must override this method with local data
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(this, mUpActivityClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    
    public void finishMe() {
        this.setResult(RESULT_OK);
        this.finish();
    }

    protected void hideMenu() {
        mShowMenu = false;
    }
    
    protected void showMenu() {
        mShowMenu = true;
    }



}
