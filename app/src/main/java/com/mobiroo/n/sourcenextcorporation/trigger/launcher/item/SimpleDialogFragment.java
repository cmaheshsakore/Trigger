package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mobiroo.n.sourcenextcorporation.trigger.R;

public class SimpleDialogFragment extends DialogFragment {

    public static int layoutBasic = R.layout.simple_dialog_basic_layout;
    public static int layoutListView = R.layout.simple_dialog_listview;
    
    private int mLayoutId = 0;
    private View mView;
    private View mChildView;
    private String mMessage;
    private Spanned mSpannedMessage;
    private String mTitle;
    private boolean mShowTitle = false;
    private boolean mHideTitle = false;
    private boolean mSetChildLayout = false;
    private boolean mListViewMultiSelect = false;
    
    private String mPositiveButtonTitle;
    private OnClickListener mPositiveListener;
    
    private String mNegativeButtonTitle;
    private OnClickListener mNegativeListener;
    
    private ListItemsAdapter mAdapter;
    private OnItemClickListener mOnItemClickListener;
    
    private ListView        mListView;
    
    
    public SimpleDialogFragment() {
        mLayoutId = R.layout.simple_dialog_basic_layout;
    }
    
    public SimpleDialogFragment(int layoutId) {
        mLayoutId = layoutId;
    }
    
    public SimpleDialogFragment(View v) {
        mView = v;
    }
    
    public void setLayout(int layoutId) {
        mLayoutId = layoutId;
    }
    
    
    
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!mShowTitle) {
            setStyle(STYLE_NO_TITLE, 0);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(mLayoutId, container);
        }
        
        if (mSetChildLayout) {
            (mView.findViewById(R.id.text)).setVisibility(View.GONE);
            try {
                ((LinearLayout) mView.findViewById(R.id.body)).addView(mChildView);
            } catch (Exception ignored) { }
        } else {
            if ((mMessage != null) && (!mMessage.isEmpty())) {
                ((TextView) mView.findViewById(R.id.text)).setText(mMessage);
            }
            if ((mSpannedMessage != null) && (mSpannedMessage.length() > 0)) {
                ((TextView) mView.findViewById(R.id.text)).setText(mSpannedMessage);
            }
        }
        if (!mHideTitle) {
            if ((mTitle != null) && (!mTitle.isEmpty())) {
                ((TextView) mView.findViewById(android.R.id.title)).setText(mTitle);
            }
        } else {
            (mView.findViewById(R.id.titleDivider)).setVisibility(View.GONE);
            (mView.findViewById(android.R.id.title)).setVisibility(View.GONE);
        }
        
        if (mPositiveButtonTitle != null) {
            TextView button =(TextView) mView.findViewById(R.id.ok_button);
            button.setText(mPositiveButtonTitle);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(mPositiveListener);
        }
        
        if (mNegativeButtonTitle != null) {
            TextView button =(TextView) mView.findViewById(R.id.cancel_button);
            button.setText(mNegativeButtonTitle);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(mNegativeListener);
        }
        
        if (mAdapter != null) {
            try {
                mListView = (ListView) mView.findViewById(R.id.list);
                mListView.setAdapter(mAdapter);
                mListView.setChoiceMode((mListViewMultiSelect) ? ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_SINGLE);
            } catch (Exception e) { }
        }

        try {
            ((ListView) mView.findViewById(R.id.list)).setOnItemClickListener(mInternalItemClickListener);
        } catch (Exception e) { }
        return mView;
    }
    
    /**
     * @param title
     * Don't use if you've overridden the layout.
     */
    public void setTitle(String title) {
        mShowTitle = false;
        mTitle = title;
    }
    
    /**
     * @param message
     * Don't use if you've overridden the layout.
     */
    public void setMessage(String message) {
        mMessage = message;
        mSpannedMessage = null;
    }
    
    public void setMessage(Spanned message) {
        mSpannedMessage = message;
        mMessage = null;
    }
    
    public void setChildView(View v) {
        mSetChildLayout = true;
        mChildView = v;
    }

    public void setPositiveButton(String title, OnClickListener listener) {
        mPositiveButtonTitle = title;
        if (listener != null) {
            mPositiveListener = listener;
        } else {
            mPositiveListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
                
            };
        }
    }
    
    public void setNegativeButton(String title, OnClickListener listener) {
        mNegativeButtonTitle = title;
        if (listener != null) {
            mNegativeListener = listener;
        } else {
            mNegativeListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
                
            };
        }
    }
    
    public View getDialogView() {
        return mView;
    }
    
    public void setListAdapter(ListItemsAdapter adapter) {
        mAdapter = adapter;
    }

    public void setListOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private OnItemClickListener mInternalItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mListViewMultiSelect) {
                try {
                    TextView t = (TextView) view.findViewById(R.id.row1Text);
                    if (mListView.getCheckedItemPositions().get(position)) {
                        t.setTextColor(Color.BLACK);
                        t.setTypeface(t.getTypeface(), Typeface.BOLD);
                    } else {
                        t.setTextColor(view.getResources().getColor(R.color.TextColorList));
                        t.setTypeface(t.getTypeface(), Typeface.NORMAL);
                    }
                } catch (Exception e) { }
            }
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(parent, view, position, id);
            }
        }
    };

    public OnClickListener dismissListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            dismiss();
            
        }
        
    };
    
    public void setMultiSelect(boolean enable) {
        mListViewMultiSelect = enable;
    }

    public ListView getListView() {
        return mListView;
    }

    public void hideAllTitles() {
        mShowTitle = false;
        mHideTitle = true;
    }
}
