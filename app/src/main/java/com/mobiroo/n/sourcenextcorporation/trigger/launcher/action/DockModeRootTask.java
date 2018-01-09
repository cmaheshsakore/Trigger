package com.mobiroo.n.sourcenextcorporation.trigger.launcher.action;

import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Utils;

import android.os.AsyncTask;

public class DockModeRootTask extends AsyncTask<Integer, Void, Void> {

    @Override
    protected Void doInBackground(Integer... args) {
        int mode = args[0];
        try {
            Utils.runCommandAsRoot(new String[] {"LD_LIBRARY_PATH='/vendor/lib:/system/lib' "
                    + "/system/bin/am broadcast -a android.intent.action.DOCK_EVENT --ei android.intent.extra.DOCK_STATE " + mode});
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception setting hw dock mode " + mode, e);
        } 
        return null;
    }
    
}