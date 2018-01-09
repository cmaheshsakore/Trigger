package com.mobiroo.n.sourcenextcorporation.trigger.launcher.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.ShareCompat;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.mobiroo.n.sourcenextcorporation.jwsoft.nfcactionlauncher.BuildConfiguration;
import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.item.task.Task;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Codes;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Logger;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.TagstandManager;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Usage;
import com.mobiroo.n.sourcenextcorporation.trigger.util.FilterableListAdapter;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.helper.DatabaseHelper;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.nfc.NFCUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ShareTagActivity extends Activity {
    
    private ListView mListView;
    private ShareOption[] mShareOptions;
    private String mUUID;
    private String mShareUrl;
    private String mTagName;
    private Task mTag;
    
    private class ShareOption {
        public String Label;
        public int ImageResource;
        
        public ShareOption(String label, int resid) {
            Label = label;
            ImageResource = resid;
        }
    }
    
    private class ShareOptionsAdapter extends FilterableListAdapter<ShareOption> {

        public ShareOptionsAdapter(Context context, ShareOption[] options) {
            super(options);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_share_option, null);
                }

                ShareOption option = getItem(position);
                ((TextView) convertView.findViewById(R.id.row1Text)).setText(option.Label);
                ((ImageView) convertView.findViewById(R.id.row1Image)).setImageResource(option.ImageResource);

                return convertView;

        }
        
    }
    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.saved_tag_picker_dialog);
        
        ((ListView) findViewById(R.id.tagsList)).setVisibility(View.GONE);
        
        ArrayList<ShareOption> options = new ArrayList<ShareOption>();
        if (BuildConfiguration.isPlayStoreAvailable()) {
            options.add(new ShareOption(getString(R.string.shareOptionSMS), R.drawable.ic_action_monolog));
        }
        options.add(new ShareOption(getString(R.string.shareOptionEmail), R.drawable.bar_content_mail));
        options.add(new ShareOption(getString(R.string.shareOptionTwitter), R.drawable.ic_action_twitter));
        options.add(new ShareOption(getString(R.string.shareOptionGooglePlus), R.drawable.ic_action_gplus));
        
        mShareOptions = options.toArray(new ShareOption[options.size()]);
        
        ((TextView) findViewById(android.R.id.title)).setText(getTitle());
        mListView = (ListView) findViewById(R.id.tagsList);
        
        mListView.setDivider(null);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long arg3) {
                Logger.d("Clicked on " +  mShareOptions[position].Label);
                openShareLink(mShareOptions[position].Label);
            }
            
        });
        mListView.setAdapter(new ShareOptionsAdapter(ShareTagActivity.this, mShareOptions));

        // Need to push this payload up to the cloud if we have't already
        // Check for unique Share ID
        
        mTag = (Task) getIntent().getParcelableExtra(Constants.EXTRA_SAVED_TAG);
        String id = mTag.getId();
        mTagName = mTag.getName();

        SQLiteDatabase db = DatabaseHelper.getInstance(ShareTagActivity.this).getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_SAVED_TASKS, new String[]{ "ShareId" }, "ID=?", new String[]{ id }, null, null, null);
        if ((c != null) && (c.moveToFirst())) {
            mUUID = c.getString(0);
            if (mUUID != null)
                Logger.d("Using cached share ID " + mUUID);
        }
        c.close();
        
        
        if ((mUUID == null) || (mUUID.isEmpty())) {
           
            mUUID = Secure.getString(getContentResolver(), Secure.ANDROID_ID) + "" + Calendar.getInstance(Locale.ENGLISH).getTimeInMillis();
            ContentValues values = new ContentValues();
            values.put("ShareId", mUUID);
            db.update(DatabaseHelper.TABLE_SAVED_TASKS, values, "ID=?", new String[] {id});
        } 

        Logger.d("Share ID is " + mUUID);

        mShareUrl = TagstandManager.generateShareURL(mUUID);
        // Try to generate a shortened version in the background.

        if (mUUID != null) {
            /* Push current payload to Tagstand  and generate shortened URL */
            /*if (Task.isStringValid(mTag.Key1)) {
                SharePutRequest put = new SharePutRequest(TagstandManager.generatePostURL(mUUID), NFCUtil.getTagPayload(db, id, mTagName), mTag.TaskType, mTag.Key1, mTag.Key2, mTag.TaskCondition);
                put.execute(mUUID);
            } else*/ {
                String payload = "";
                if (mTag.isSecondTagNameValid()) {
                    mTagName = mTag.getName() + " - " + mTag.getSecondaryName();
                    payload = NFCUtil.getTagPayload(this, new String[] { mTag.getId(), mTag.getSecondaryId() } , new String[] { mTag.getName(), mTag.getSecondaryName() });
                }  else {
                    payload = NFCUtil.getTagPayload(this, id, mTagName);
                }
                SharePutRequest put = new SharePutRequest(TagstandManager.generatePostURL(mUUID), payload);
                put.execute(mUUID);
            }
            
        }
    }
  
    @Override
    public void onStart() {
        super.onStart();
        if (Usage.canLogData(ShareTagActivity.this)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Usage.canLogData(ShareTagActivity.this)) {
            EasyTracker.getInstance(this).activityStop(this);
        }
    }
    
    /* Pushes payload up to cloud.  Generates a share URL.  Attempts to grab a shortened version of that URL */
    private class SharePutRequest extends TagstandManager.TagPutRequest {

        public SharePutRequest(String postURL, String payload) {
            super(postURL, payload);
        }
        public SharePutRequest(String postURL, String payload, int type, String key1, String key2, String condition) {
            super(postURL, payload, type, key1, key2, condition);
        }

        @Override
        protected String doInBackground(String... params) {
            // Posts and returns shortened url
            String shortUrl = super.doInBackground(params);
            // generates static URL
            mShareUrl = TagstandManager.generateShareURL(params[0]);
            if (!shortUrl.isEmpty()) {
                Logger.d("Shortened URL is " + shortUrl);
                mShareUrl = shortUrl;
            } else {
                // This failed to push the data.  Alert the user
                mShareUrl = "";
            }
            
            return null;
        }
        protected void onPostExecute(final String url) {
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.GONE);
            ((ListView) findViewById(R.id.tagsList)).setVisibility(View.VISIBLE);
            if (mShareUrl.isEmpty()) {
                Toast.makeText(ShareTagActivity.this, getString(R.string.share_failed), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void openShareLink(String option) {
        if (option.equals(getString(R.string.shareOptionEmail))) {
            Usage.storeTuple(ShareTagActivity.this, Codes.SHARE_EMAIL, Codes.COMMAND_SHARE, 10);
            
            String body =  String.format(getString(R.string.shareEmailPrefix), mTagName) + " <a href='" + mShareUrl + "'>" + mShareUrl + "</a> " + getString(R.string.shareEmailSuffix);
            body = body.replace("\n", "<br>");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareEmailSubject));
            intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
            try { startActivity(Intent.createChooser(intent, "Send Email")); }
            catch (Exception e) { Toast.makeText(ShareTagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); }
        } else if (option.equals(getString(R.string.shareOptionSMS))) {
            Usage.storeTuple(ShareTagActivity.this, Codes.SHARE_SMS, Codes.COMMAND_SHARE, 10);
            Intent intent = new Intent(Intent.ACTION_VIEW);         
            intent.setData(Uri.parse("sms:"));
            intent.putExtra("sms_body", getString(R.string.shareSMSPrefix) + " " + mShareUrl); 
            intent.setType("vnd.android-dir/mms-sms");
            try { startActivity(intent); }
            catch (Exception e) { Toast.makeText(ShareTagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); }
        } else if (option.equals(getString(R.string.shareOptionTwitter))) {
            Usage.storeTuple(ShareTagActivity.this, Codes.SHARE_TWITTER, Codes.COMMAND_SHARE, 10);
            String body = "";
            try { body = "http://twitter.com/intent/tweet?text=" + URLEncoder.encode(getString(R.string.shareTwitterPrefix), "UTF-8") + " " + URLEncoder.encode(mShareUrl, "UTF-8") + " " + URLEncoder.encode(getString(R.string.shareTwitterSuffix), "UTF-8"); }
            catch (Exception e) { Logger.e(Constants.TAG, "Exception encoding twitter string for sharing", e); }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(body));
            try { startActivity(intent); }
            catch (Exception e) { Toast.makeText(ShareTagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); }
            
        } else if (option.equals(getString(R.string.shareOptionGooglePlus))) {
            Usage.storeTuple(ShareTagActivity.this, Codes.SHARE_GOOGLE_PLUS, Codes.COMMAND_SHARE, 10);
            try {
                Intent shareIntent = ShareCompat.IntentBuilder.from(ShareTagActivity.this)
                        .setText(getString(R.string.shareGooglePlusPrefix) + " " + mShareUrl)
                        .setType("text/plain")
                        .getIntent()
                        .setPackage("com.google.android.apps.plus");

                startActivity(shareIntent);
            } catch (Exception e) { Toast.makeText(ShareTagActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); }


        }
         
        
        this.finish();
    }

}
