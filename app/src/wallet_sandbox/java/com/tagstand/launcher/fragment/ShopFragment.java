package com.trigger.launcher.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.trigger.launcher.activity.ShopItemDescriptionActivity;
import com.trigger.launcher.item.HeaderItem;
import com.trigger.launcher.item.ListItem;
import com.trigger.launcher.item.ShopItem;
import com.trigger.launcher.item.ShopMoreItem;
import com.trigger.launcher.item.TextItem;
import com.trigger.launcher.util.Logger;
import com.trigger.launcher.util.ShopJsonFetcher;
import com.trigger.launcher.util.Usage;
import com.trigger.launcher.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ShopFragment extends Fragment implements ShopItem.OnShopItemClickListener {

    public static final int REQUEST_WALLET_PURCHASE = 1007;
    public static final int RESULT_BUYER_NEEDS_CHECKOUT = 5001;

    @SuppressWarnings("unused")
    private boolean mUserHasPreAuthorized = false;

    private TextView mEmpty;
    private ProgressBar mProgress;    

    private int mWidthDp;

    private List<ListItem>  mNfcItems;
    private List<ListItem>  mNfcGear;
    private LinearLayout    mContent;
    private View            mView;
    private shopLoader      mShopLoader;
    
    private String mTagsCategoryText;
    private String mGearCategoryText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        
        cancelShopLoader();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWidthDp = Utils.getWidthInDp(getActivity());
        mView = view;

    }

    private void cancelShopLoader() {
        if (mShopLoader != null) {
            mShopLoader.cancel(true);
        }
    }
    private void getElements() {
        mProgress = (ProgressBar) mView.findViewById(android.R.id.progress);
        mEmpty = (TextView) mView.findViewById(android.R.id.empty);
        mContent = (LinearLayout) mView.findViewById(android.R.id.content);
    }

    private void loadContent() {
        View v = getView();

        if (v != null) {
            mView = getView();
        }
        getElements();
        mShopLoader = new shopLoader(getActivity());
        mShopLoader.execute();
    }

    private class shopLoader extends AsyncTask<Void, Void, Void> {

        private List<ShopItem> mTags;
        private List<ShopItem> mGear;

        private Context mContext;

        public shopLoader(Context context) {
            mContext = context;
        }

        private String tryLoadString(JSONObject object, String field) {
            try {
                return object.getString(field);
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            mTags = new ArrayList<ShopItem>();
            mGear = new ArrayList<ShopItem>();
            ShopJsonFetcher fetcher = new ShopJsonFetcher(getActivity());
            try {
                String data = fetcher.loadData(ShopJsonFetcher.CACHE_RESPONSE);
                JSONArray array = fetcher.createArrayFromData(data);
                for (int i=0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    byte[] thumbData = Base64.decode(object.getString("thumbnail"), Base64.DEFAULT);

                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inScaled = true;
                    opts.inDensity = DisplayMetrics.DENSITY_XHIGH;
                    opts.inTargetDensity = mContext.getResources().getDisplayMetrics().densityDpi;

                    Bitmap thumbnail = BitmapFactory.decodeStream(new ByteArrayInputStream(thumbData, 0, thumbData.length), null, opts);

                    if (object.has("category_tags_text")) {
                        mTagsCategoryText = object.getString("category_tags_text");
                    }
                    
                    if (object.has("category_gear_text")) {
                        mGearCategoryText = object.getString("category_gear_text");
                    }
                    
                    // Check object for headings
                    
                    ShopItem item = new ShopItem(
                            object.getString("id"),
                            object.getString("name"),
                            object.getString("description"),
                            object.getDouble("price"),
                            object.getDouble("discount_price"),
                            thumbnail,
                            object.getString("image_1"),
                            tryLoadString(object,"image_2"),
                            tryLoadString(object,"image_3"),
                            tryLoadString(object,"long_description"),
                            tryLoadString(object,"long_name"),
                            ShopFragment.this);

                    if (object.getString("category").equals("gear")) {
                        mGear.add(item);
                    } else {
                        mTags.add(item);

                    }
                }
            } catch (Exception e) {
                Logger.e("Could not load shop data", e);

            }
            return null;
        }

        @Override
        public void onPostExecute(final Void unused) {
            mNfcItems = new ArrayList<ListItem>();
            mNfcGear = new ArrayList<ListItem>();

            if (mTags != null) {
                mNfcItems.addAll(mTags);
            }

            if (mGear != null) {            	
                mNfcGear.addAll(mGear);
            }

            if (mTags == null) {
                showItems(false);
            } else {
                showItems(true);
            }
        }

    }

    public void showItems(boolean shown) {
        try {
            mProgress.setVisibility(View.GONE);
        } catch (Exception ignored) {}

        if (shown) {
            buildItemList();
            mContent.setVisibility(View.VISIBLE);
            mEmpty.setVisibility(View.GONE);
        } else {
            try {
                mEmpty.setText(getString(R.string.shop_unavailable));
            } catch (Exception e) {
                /* fail silently */
            }
            
            mContent.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
        } 
    }

    private LinearLayout getRow() {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private int getPixelsFromDp(float pixels) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics());
    }

    private int getItemCountFromDp(int dp) {
        int count = 3;
        if (dp <= 320) {
            count = 2;
        } else if (dp >= 598) {
            count = 4;
        }

        return count;

    }
    private void buildItemList() {
        Activity activity = getActivity();
        if (activity != null) {
            mWidthDp = Utils.getWidthInDp(activity);
            final int itemsInRow =  getItemCountFromDp(mWidthDp);
    
            final int row_bottom_margin = 32;
            mContent.removeAllViews();
            mContent.setOrientation(LinearLayout.VERTICAL);
            mContent.addView(new HeaderItem(ShopFragment.this, getString(R.string.recommended_tags)).getView());
            if (mTagsCategoryText != null) {
                mContent.addView(new TextItem(ShopFragment.this, mTagsCategoryText).getView());
            }
    
            if ((mNfcItems != null) && (mNfcItems.size() > 0)) {
                LinearLayout row = getRow();
    
                for (int i=0; i < mNfcItems.size(); i++) {
                    ShopItem item = (ShopItem) mNfcItems.get(i);
                    if ((row == null) || ((i % itemsInRow) == 0)) {
                        mContent.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                        row = getRow();
                    }
    
                    /* Add child to row */
                    row.addView(item.getView(getActivity()));
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                /* For at least phone layouts set a bottom margin */
                params.setMargins(0, 0, 0, getPixelsFromDp(row_bottom_margin));
                mContent.addView(row, params);
            }
    
            mContent.addView(new HeaderItem(ShopFragment.this, getString(R.string.nfc_gear_title)).getView());
            if (mGearCategoryText != null) {
                mContent.addView(new TextItem(ShopFragment.this, mGearCategoryText).getView());
            }
    
            if ((mNfcGear != null) && (mNfcGear.size() > 0)) {
                LinearLayout row = getRow();
    
                for (int i=0; i < mNfcGear.size(); i++) {
                    ShopItem item = (ShopItem) mNfcGear.get(i);
                    if ((row == null) || ((i % itemsInRow) == 0)) {
                        mContent.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                        row = getRow();
                    }
    
                    /* Add child to row */
                    row.addView(item.getView(getActivity()));
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                /* For at least phone layouts set a bottom margin */
                params.setMargins(0, 0, 0, getPixelsFromDp(row_bottom_margin));
                mContent.addView(row, params);
    
            }
    
            mContent.addView(new ShopMoreItem().getView(getActivity()));
        }
    }


    @Override
    public void onClick(final ShopItem item) {
        Usage.storeShopItem(getActivity().getBaseContext(), item);
        Usage.logPurchaseEvent(null, "Item Info shown", item, false);
        /* Change to show description */
        Intent intent = new Intent(getActivity(), ShopItemDescriptionActivity.class);
        intent.putExtra(ShopItemDescriptionActivity.EXTRA_ITEM, item);
        startActivity(intent);
    }
}
