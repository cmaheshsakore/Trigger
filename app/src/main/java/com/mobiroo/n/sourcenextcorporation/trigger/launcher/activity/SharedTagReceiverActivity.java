package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class SharedTagReceiverActivity extends Activity {

    private ProgressDialog mDialog;
    
    @Override
    public void onCreate(Bundle savedState) {
        setContentView(R.layout.tag_receive_share);
        super.onCreate(savedState);
        
        Uri uri = getIntent().getData();        

        String uid = null;
        if (uri.getQueryParameter("uid") != null) {
            uid = uri.getQueryParameter("uid");
        }
        
        mDialog = ProgressDialog.show(this, "", getString(R.string.sharedReceiverDialogMessage), true, false);
        TagLoader loader = new TagLoader(SharedTagReceiverActivity.this, uid, uri);
        loader.execute();
    }
    
    private class TagLoader extends AsyncTask<Void,Void,String> {

        private Context mContext;
        private String mUid;
        private Uri mUri;
        
        public TagLoader(Context context, String uuid, Uri uri) {
            mContext = context;
            mUid = uuid;
            mUri = uri;
        }
        @Override
        protected String doInBackground(Void... args) {
            // Get payload from server based on incoming short URL
            Logger.i("Calling getTag");
            return TagstandManager.getTag(mContext, mUid, mUri.toString());

        }
        
        protected void onPostExecute(final String payload) {
            mDialog.cancel();
            if ((payload != null) && (!payload.isEmpty())) {
                Intent intent = new Intent(mContext, ImportTagActivity.class);
                intent.putExtra(Constants.EXTRA_SHARED_TAG_PAYLOAD, payload);
                startActivity(intent);
                SharedTagReceiverActivity.this.finish();
            }
        }
    }
}
