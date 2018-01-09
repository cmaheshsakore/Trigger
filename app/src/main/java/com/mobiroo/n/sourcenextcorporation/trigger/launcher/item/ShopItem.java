package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.util.Constants;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopItem implements ListItem, View.OnClickListener, Parcelable {
    private final String        mId;
    private final String        mName;
    private final String        mLongName;
    private final String        mDescription;
    private final double        mPrice;
    private final double        mOriginalPrice;
    private final Bitmap        mThumbnail;
    private final String        mPreviewUrl1;
    private final String        mPreviewUrl2;
    private final String        mPreviewUrl3;
    private final String        mLongDescription;
    private View                mConvertView;

    private OnShopItemClickListener mListener;

    public static final Creator<ShopItem> CREATOR = new Creator<ShopItem>() {
        @Override
        public ShopItem createFromParcel(Parcel source) {
            return new ShopItem(
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readDouble(),
                    source.readDouble(),
                    null, //((Bitmap) source.readParcelable(Bitmap.class.getClassLoader())),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    source.readString(),
                    null
                    );
        }

        @Override
        public ShopItem[] newArray(int size) {
            return new ShopItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeDouble(mOriginalPrice);
        dest.writeDouble(mPrice);
        //dest.writeParcelable(mThumbnail, 0);
        dest.writeString(mPreviewUrl1);
        dest.writeString(mPreviewUrl2);
        dest.writeString(mPreviewUrl3);
        dest.writeString(mLongDescription);
        dest.writeString(mLongName);
        // Not sending listener
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public ShopItem(String id, String name, String description, double price, double currentPrice, Bitmap thumbnail, OnShopItemClickListener listener) {
        mId = id;
        mName = name;
        mDescription = description;
        mPrice = currentPrice; /* discount price is actually the current price or the full price */
        mOriginalPrice = price;
        mThumbnail = thumbnail;
        mPreviewUrl1 = null;
        mPreviewUrl2 = null;
        mPreviewUrl3 = null;
        mLongDescription = description;
        mLongName = name;
        mListener = listener;
    }
    
    public ShopItem(String id, String name, String description, double price, double currentPrice, Bitmap thumbnail, String previewUrl1, String previewUrl2, String previewUrl3, String longDescription, String longName, OnShopItemClickListener listener) {
        mId = id;
        mName = name;
        mDescription = description;
        mPrice = currentPrice; /* discount price is actually the current price or the full price */
        mOriginalPrice = price;
        mThumbnail = thumbnail;
        mPreviewUrl1 = previewUrl1;
        mPreviewUrl2 = previewUrl2;
        mPreviewUrl3 = previewUrl3;
        mLongDescription = longDescription;
        mLongName = longName;
        mListener = listener;
    }

    public String getId() {
        return mId;
    }

    /**
     * @return Item name
     */
    public String getName() {
        return mName;
    }

    public String getLongName() {
        return mLongName;
    }
    
    /**
     * @return Item description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * @return Price in cents
     */
    public double getPrice() {
        return mPrice;
    }
    
    public double getPriceInDollars() {
        return mPrice / 100;
    }

    /**
     * @return Non discounted price in cents
     */
    public double getOriginalPrice() {
        return mOriginalPrice;
    }
    
    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    
    public List<NameValuePair> toParams(int index, int quantity) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(makeParamName("id",       index), getId()));
        params.add(new BasicNameValuePair(makeParamName("quantity", index), Integer.toString(quantity)));
        return params;
    }

    private String makeParamName(String name, int index) {
        return String.format("items[%d][%s]", index, name);
    }

    public View getView(Context context) {
        if (mConvertView == null) {
            mConvertView = View.inflate(context, R.layout.shop_item_2, null);
            mConvertView.findViewById(android.R.id.content).setOnClickListener(this);
            //(mConvertView.findViewById(android.R.id.progress)).setVisibility(View.VISIBLE);
        }

        String price = NumberFormat.getCurrencyInstance(Locale.US).format(getPrice() / 100.0); /* mBaseView price */
        
        ((TextView) mConvertView.findViewById(android.R.id.text1)).setText(getName());
        ((TextView) mConvertView.findViewById(android.R.id.text2)).setText(price);

        int height = (int) context.getResources().getDimension(R.dimen.shop_item_image_height);
        int width = (int) context.getResources().getDimension(R.dimen.shop_item_image_width);
        loadThumnail(mConvertView, context, height, width);
        
        return mConvertView;
    }
    @Override
    public View getView(ListItemsAdapter adapter, int position, View convertView) {
        if (convertView == null) {
            convertView = adapter.getActivity().getLayoutInflater().inflate(R.layout.shop_item_2, null);
            convertView.findViewById(android.R.id.content).setOnClickListener(this);
        }

        String price = NumberFormat.getCurrencyInstance(Locale.US).format(getPrice() / 100.0); /* mBaseView price */
        
        ((TextView) convertView.findViewById(android.R.id.text1)).setText(getName());
        ((TextView) convertView.findViewById(android.R.id.text2)).setText(price);

        Context context = adapter.getActivity().getBaseContext();
        int height = (int) context.getResources().getDimension(R.dimen.shop_item_image_height);
        int width = (int) context.getResources().getDimension(R.dimen.shop_item_image_width);
        
        loadThumnail(convertView, context, height, width);
        
        return convertView;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void onClick(final View view) {
        mListener.onClick(this);
    }

    public interface OnShopItemClickListener {
        public void onClick(ShopItem item);
    }

    public String getPreviewImageUrl(int which) {
        String urlBase = Constants.SHOP_URL_BASE + "/tl_store/";
        switch(which) {
            case 1:
                return (mPreviewUrl1 != null) ? urlBase + mPreviewUrl1 : "";
            case 2:
                return (mPreviewUrl2 != null) ? urlBase + mPreviewUrl2 : "";
            case 3:
                return (mPreviewUrl3 != null) ? urlBase + mPreviewUrl3 : "";
            default:
                return "";
        }
    }
    

    public String getLongDescription() {
        return mLongDescription;
    }
    
    
    public void loadThumnail(View view, Context context, int height, int width) {

        String resource = getPreviewImageUrl(1);
        
        BaseImageLoader loader = new BaseImageLoader(context, null);
        if (height > 0) {
            loader.setHeight(height);
        }
        
        if (width > 0) {
            loader.setWidth(width);
        }
        
        ProgressBar bar = (ProgressBar) view.findViewById(android.R.id.progress);
        if (loader.loadImageFromCache(resource) == null) {
            bar.setVisibility(View.VISIBLE);
        }
        loader.setProgressDialog(bar);
        loader.execute(new PendingImage((ImageView) view.findViewById(android.R.id.icon), resource));
    }
    

}
